package com.malliina.boattracker.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.JsonObject
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.callouts.*
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileFragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.map_fragment.view.*
import timber.log.Timber

class MapFragment : Fragment() {
    val app: BoatApp get() = requireActivity().application as BoatApp

    companion object {
        const val BoatIconSize: Float = 0.7f
        const val profileCode = 901
    }

    private val args: MapFragmentArgs by navArgs()

    private lateinit var mapView: MapView
    private val viewModel: MapViewModel by viewModels()
    private var map: MapboxMap? = null
    private val style: Style? get() = map?.style

    private var mapState: UserTrack = UserTrack(null, null)
    private val userState: UserState get() = UserState.instance
    private val icons: IconsConf? get() = app.settings.conf?.map?.icons
    private val trails: MutableMap<TrackMeta, FeatureCollection> = mutableMapOf()
    private val topSpeedMarkers: MutableMap<TrackName, ActiveMarker> = mutableMapOf()
    private var ais: VesselsRenderer? = null
    private var callouts: Callouts? = null

    enum class MapMode {
        Fit, Follow, Stay
    }

    data class ActiveMarker(val marker: Marker, val topPoint: CoordBody)

    private var mapMode: MapMode = MapMode.Fit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.actionBar?.hide()
        if (args.fit) {
            mapMode = MapMode.Fit
        }
        mapView = view.mapView
        mapView.onCreate(savedInstanceState)
        // Observer code happens on the main thread
        viewModel.user.observe(viewLifecycleOwner) { state ->
            if (args.fit) {
                mapMode = MapMode.Fit
            }
            val trackName = args.track ?: state.track
            val email = state.user?.email ?: "no email"
            Timber.i("Got $email with track ${trackName ?: "no track"}")
            this.mapState = state
            viewModel.openSocket(state.user?.idToken, trackName)
        }
        viewModel.conf.observe(viewLifecycleOwner) { conf ->
            // Sets profile visible when both conf and user have been loaded
            viewModel.user.observe(viewLifecycleOwner) {
                view.profile.visibility = Button.VISIBLE
            }
            app.settings.lang?.let {
                ais = VesselsRenderer(conf.layers.ais, conf.map.icons, it)
            }
            mapView.getMapAsync { map ->
                this.map = map
                map.setStyle(Style.Builder().fromUri(conf.map.styleUrl)) { style ->
                    callouts = Callouts(map, style, requireActivity(), conf, app.settings, this)
                }
            }
        }
        viewModel.coords.observe(viewLifecycleOwner) { coords ->
            map?.let { m ->
                onCoords(coords, m)
            }
        }
        viewModel.vessels.observe(viewLifecycleOwner) { vessels ->
            map?.let { m ->
                ais?.onVessels(vessels, m)
            }
        }
        view.profile.setOnClickListener {
            val user = userState.user
            if (user == null) {
                launchLogin()
            } else {
                val action = MapFragmentDirections.mapToProfile(app.settings.lang!!.appName)
                findNavController().navigate(action)
            }
        }
        view.center.setOnClickListener {

        }
        if (args.refresh) {
            viewModel.signInSilently(requireContext())
        }
    }

    private fun launchLogin() {
        Timber.i("Opening login screen...")
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivityForResult(intent, profileCode)
        // After success, onActivityResult is called?
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.i("Returning with code $requestCode")
        when (requestCode) {
            profileCode -> data?.let {
                if (it.getBooleanExtra(ProfileFragment.refreshSignIn, false)) {
                    viewModel.signInSilently(requireContext())
                }
            }
            else -> {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.hide()
        mapView.onStart()
    }

    override fun onStop() {
        viewModel.disconnect()
        clearMap()
        super.onStop()
        mapView.onStop()
    }

    private fun onCoords(coords: CoordsData, map: MapboxMap) {
        val from = coords.from
        val newPoints = coords.coords//.map { c -> Point.fromLngLat(c.coord.lng, c.coord.lat) }
        val meta = TrackMeta(from.trackName)
        // Updates track
        val featureColl = updateOrCreateTrail(meta, coords.from.topPoint, newPoints)
        val trailSource = style?.getSourceAs<GeoJsonSource>(meta.trailSource)
        trailSource?.setGeoJson(featureColl)
        val latLngs = extractCoords(featureColl).map { p -> asLatLng(p) }
        if (newPoints.isNotEmpty()) {
            // Updates map position
            when (mapMode) {
                MapMode.Fit -> {
                    if (latLngs.size > 1) {
                        val bounds = LatLngBounds.Builder().includes(latLngs).build()
                        val durationMs = 2000
                        val padding = 20
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, padding),
                            durationMs
                        )
                        mapMode = MapMode.Follow
                    }
                }
                MapMode.Follow ->
                    map.cameraPosition =
                        CameraPosition.Builder().target(asLatLng(newPoints.last().coord.point()))
                            .build()
                MapMode.Stay ->
                    Unit
            }
            // Updates boat icon
            style?.getSourceAs<GeoJsonSource>(meta.iconSource)?.let { source ->
                newPoints.lastOrNull()?.let { last ->
                    source.setGeoJson(last.coord.point())
                }
                // Updates boat icon bearing
                val lastTwo = latLngs.takeLast(2)
                if (lastTwo.size == 2) {
                    style?.getLayerAs<SymbolLayer>(meta.iconLayer)?.setProperties(
                        PropertyFactory.iconRotate(
                            Geo.instance.bearing(lastTwo[0], lastTwo[1]).toFloat()
                        ),
                        PropertyFactory.iconRotationAlignment("map")
                    )
                }
            }
            // Updates trophy
            style?.getSourceAs<GeoJsonSource>(meta.trophySource)
                ?.setGeoJson(topFeature(from.topPoint))
        }
    }

    private fun asLatLng(p: Point): LatLng = LatLng(p.latitude(), p.longitude())

    private fun updateOrCreateTrail(
        meta: TrackMeta,
        topSpeed: CoordBody,
        coords: List<CoordBody>
    ): FeatureCollection {
        val old = trails[meta]
        return if (old == null) {
            // Adds trail
            val trailSource = meta.trailSource
            val trackFeature = FeatureCollection.fromFeatures(speedFeatures(coords))
            val source = GeoJsonSource(trailSource, LineString.fromLngLats(emptyList()))
            style?.addSource(source)
            val lineLayer = LineLayer(meta.trailLayer, trailSource).withProperties(
                PropertyFactory.lineWidth(1f),
                PropertyFactory.lineColor(Styles.instance.trackColor)
            )
            style?.addLayer(lineLayer)
            trails[meta] = trackFeature
            // Adds boat icon
            coords.lastOrNull()?.let { latLng ->
                icons?.boat?.let { icon ->
                    val iconSourceId = meta.iconSource
                    val iconSource = GeoJsonSource(iconSourceId, latLng.coord.point())
                    style?.addSource(iconSource)
                    val layerId = meta.iconLayer
                    val symbol = SymbolLayer(layerId, iconSource.id).withProperties(
                        PropertyFactory.iconImage(icon),
                        PropertyFactory.iconSize(BoatIconSize)
                    )
                    style?.addLayer(symbol)
                }
            }
            // Adds trophy using GeoJSON manually instead of using SymbolManager in order to make
            // z-index work as desired (i.e. trophy is shown on top of trails)
            val feature = topFeature(topSpeed)
            val trophySource = GeoJsonSource(meta.trophySource, feature)
            style?.addSource(trophySource)
            icons?.trophy?.let { trophyIcon ->
                val symbol = SymbolLayer(meta.trophyLayer, meta.trophySource).withProperties(
                    PropertyFactory.iconImage(trophyIcon)
                )
                style?.addLayer(symbol)
            }
            trackFeature
        } else {
            val oldFeatures = old.features() ?: emptyList()
            val latest = latestMeasurement(oldFeatures)
            val latestList = if (latest == null) emptyList() else listOf(latest)
            FeatureCollection.fromFeatures(oldFeatures + speedFeatures(latestList + coords))
        }
    }

    private fun extractCoords(coll: FeatureCollection): List<Point> {
        val features = coll.features() ?: emptyList()
        return features.flatMap {
            it.geometry()?.let { geo ->
                when (geo) {
                    is LineString -> geo.coordinates()
                    else -> emptyList()
                }
            } ?: emptyList()
        }
    }

    private fun latestMeasurement(features: List<Feature>): SimpleCoord? {
        features.lastOrNull()?.let { feature ->
            val geometry = feature.geometry()
            val lastCoord = when (geometry) {
                is LineString -> geometry.coordinates().lastOrNull()
                else -> null
            }
            val speed = feature.getNumberProperty(Speed.key)?.toDouble()
            lastCoord?.let { p ->
                speed?.let { s ->
                    return SimpleCoord(Coord.fromPoint(p), Speed(s))
                }
            }
        }
        return null
    }

    private fun speedFeatures(coords: List<MeasuredCoord>): List<Feature> = when (coords.size) {
        0 -> {
            emptyList()
        }
        1 -> {
            val single = coords.first()
            val info = WrappedSpeed(single.speed)
            val feature = Feature.fromGeometry(
                LineString.fromLngLats(listOf(single.coord.point())),
                Json.toGson(info, Callouts.wrappedAdapter)
            )
            listOf(feature)
        }
        else -> {
            coords.zip(coords.drop(1)).map { pair ->
                val avgSpeed = (pair.first.speed.knots + pair.second.speed.knots) / 2
                val edge = LineString.fromLngLats(
                    listOf(
                        pair.first.coord.point(),
                        pair.second.coord.point()
                    )
                )
                val info = WrappedSpeed(Speed(avgSpeed))
                Feature.fromGeometry(edge, Json.toGson(info, Callouts.wrappedAdapter))
            }
        }
    }

    private fun topFeature(top: CoordBody): Feature =
        Feature.fromGeometry(top.coord.point(), trophyJson(top))

    private fun trophyJson(top: CoordBody): JsonObject =
        Json.toGson(SpeedInfo(top.speed, top.boatTime), Callouts.speedAdapter)

    private fun clearMap() {
        callouts?.clear()
        callouts = null
        map?.let { map ->
            trails.keys.forEach { meta ->
                style?.let { s ->
                    s.removeLayer(meta.trailLayer)
                    s.removeSource(meta.trailSource)
                    s.removeLayer(meta.iconLayer)
                    s.removeSource(meta.iconSource)
                    s.removeLayer(meta.trophyLayer)
                    s.removeSource(meta.trophySource)
                }
            }
            trails.clear()
            topSpeedMarkers.values.forEach { m -> map.removeMarker(m.marker) }
            topSpeedMarkers.clear()
            map.clear()
        }
    }

    // https://www.mapbox.com/android-docs/maps/overview/
    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized)
            mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized)
            mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mapView.isInitialized)
            mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::mapView.isInitialized)
            mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.map = null
        if (::mapView.isInitialized)
            mapView.onDestroy()
    }
}

data class TrackMeta(val trackName: TrackName) {
    val trailSource = "$trackName-trail-source"
    val trailLayer = "$trackName-trail-layer"
    val iconSource = "$trackName-boat-source"
    val iconLayer = "$trackName-boat-layer"
    val trophySource = "$trackName-top-source"
    val trophyLayer = "$trackName-top-layer"
}
