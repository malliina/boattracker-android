package com.malliina.boattracker.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.JsonObject
import com.malliina.boattracker.BuildConfig
import com.malliina.boattracker.Coord
import com.malliina.boattracker.CoordBody
import com.malliina.boattracker.CoordsData
import com.malliina.boattracker.Geo
import com.malliina.boattracker.IconsConf
import com.malliina.boattracker.Json
import com.malliina.boattracker.R
import com.malliina.boattracker.Speed
import com.malliina.boattracker.TrackName
import com.malliina.boattracker.UserState
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.callouts.Callouts
import com.malliina.boattracker.ui.callouts.MeasuredCoord
import com.malliina.boattracker.ui.callouts.SimpleCoord
import com.malliina.boattracker.ui.callouts.SpeedInfo
import com.malliina.boattracker.ui.callouts.WrappedSpeed
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileFragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.layers.properties.generated.IconRotationAlignment
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import kotlinx.android.synthetic.main.buttons_view.view.*
import timber.log.Timber

class MapFragment : ComposeFragment() {
    companion object {
        const val BoatIconSize: Double = 0.7
        const val profileCode = 901
    }

    private val args: MapFragmentArgs by navArgs()
    private var isRestart = false

    private lateinit var mapView: MapView
    private val viewModel: MapViewModel by viewModels()
    private val map: MapboxMap get() = mapView.getMapboxMap()
    private val style: Style? get() = map.getStyle()

    private val userState: UserState get() = UserState.instance
    private val icons: IconsConf? get() = app.settings.conf?.map?.icons
    private val trails: MutableMap<TrackMeta, FeatureCollection> = mutableMapOf()
    private var ais: VesselsRenderer? = null
    private var callouts: Callouts? = null

    enum class MapMode {
        Fit, Follow, Stay
    }

    private var mapMode: MapMode = MapMode.Fit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val helsinki = Point.fromLngLat(24.9, 60.14)
        val view = MapView(
            context,
            MapInitOptions(
                context,
                ResourceOptions.Builder().accessToken(BuildConfig.MapboxAccessToken).build(),
                cameraOptions = CameraOptions.Builder().center(helsinki).zoom(10.0).build()
            )
        )
        view.id = R.id.map
        mapView = view
        val cl = ConstraintLayout(context)
        cl.addView(view)
        val buttons = inflater.inflate(R.layout.buttons_view, null)
        cl.addView(buttons)
        return cl
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.actionBar?.hide()
        if (args.fit) {
            mapMode = MapMode.Fit
        }
        // Observer code happens on the main thread
        viewModel.user.observe(viewLifecycleOwner) { state ->
            if (args.fit) {
                mapMode = MapMode.Fit
            }
            val trackName = args.track ?: state.track
            val email = state.user?.email ?: "no email"
            Timber.i("Got $email with track ${trackName ?: "no track"}")
            viewModel.reconnect(trackName)
        }
        viewModel.conf.observe(viewLifecycleOwner) { conf ->
            Timber.i("Conf loaded.")
            view.profile.visibility = Button.VISIBLE
            app.settings.lang?.let {
                ais = VesselsRenderer(conf.layers.ais, conf.map.icons, it)
            }
            val mapboxMap = mapView.getMapboxMap()
            mapboxMap.loadStyleUri(conf.map.styleUrl) { loaded ->
                Timber.i("Style loaded.")
                callouts = Callouts(mapboxMap, mapView.viewAnnotationManager, loaded, requireActivity(), conf, app.settings, this)
            }
        }
        viewModel.coords.observe(viewLifecycleOwner) { coords ->
            onCoords(coords, map)
        }
        viewModel.vessels.observe(viewLifecycleOwner) { vessels ->
            ais?.onVessels(vessels, map)
        }
        view.profile.setOnClickListener {
            val user = userState.user
            if (user == null) {
                launchLogin()
            } else {
                val action = MapFragmentDirections.mapToProfile(lang.appName)
                findNavController().navigate(action)
            }
        }
//        view.center.setOnClickListener {
//        }
        if (args.refresh) {
            Timber.i("Signing in silently from Fragment...")
            viewModel.signInSilently(requireContext())
        }
        Timber.i("MapFragment created")
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
        if (isRestart) {
            viewModel.restart()
        }
        isRestart = true
    }

    override fun onStop() {
        viewModel.disconnect()
        clearMap()
        super.onStop()
    }

    private fun onCoords(coords: CoordsData, map: MapboxMap) {
        if (style == null) return
        val from = coords.from
        val newPoints = coords.coords // .map { c -> Point.fromLngLat(c.coord.lng, c.coord.lat) }
        val meta = TrackMeta(from.trackName)
        // Updates track
        val featureColl = updateOrCreateTrail(meta, coords.from.topPoint, newPoints)
        val trailSource = style?.getSourceAs<GeoJsonSource>(meta.trailSource)
        val exists = style?.styleSourceExists(meta.trailSource) ?: false
        trailSource?.featureCollection(featureColl)
        val latLngs = extractCoords(featureColl).map { p -> asLatLng(p) }
        if (newPoints.isNotEmpty()) {
            // Updates map position
            when (mapMode) {
                MapMode.Fit -> {
                    if (latLngs.size > 1) {
//                        val bounds = LatLngBounds.Builder().includes(latLngs).build()
//                        val durationMs = 2000
//                        val padding = 20
//                        map.animateCamera(
//                            CameraUpdateFactory.newLatLngBounds(bounds, padding),
//                            durationMs
//                        )
                        mapMode = MapMode.Follow
                    }
                }
                MapMode.Follow -> {}
//                    map.cameraPosition =
//                        CameraPosition.Builder().target(asLatLng(newPoints.last().coord.point()))
//                            .build()
                MapMode.Stay -> {}
            }
            // Updates boat icon
            style?.getSourceAs<GeoJsonSource>(meta.iconSource)?.let { source ->
                newPoints.lastOrNull()?.let { last ->
                    source.geometry(last.coord.point())
                }
                // Updates boat icon bearing
                val lastTwo = latLngs.takeLast(2)
                if (lastTwo.size == 2) {
                    style?.getLayerAs<SymbolLayer>(meta.iconLayer)?.apply {
                        iconRotate(Geo.instance.bearing(lastTwo[0], lastTwo[1]))
                        iconRotationAlignment(IconRotationAlignment.MAP)
                    }
                }
            } ?: run {
                Timber.w("Unable to find source ${meta.iconSource}")
            }
            // Updates trophy
            style?.getSourceAs<GeoJsonSource>(meta.trophySource)?.feature(topFeature(from.topPoint))
        }
    }

    private fun asLatLng(p: Point): Point = Point.fromLngLat(p.longitude(), p.latitude())

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
            val source = geoJsonSource(trailSource) {
                geometry(LineString.fromLngLats(emptyList()))
            }
            style?.addSource(source)
            val lineLayer = lineLayer(meta.trailLayer, trailSource) {
                lineWidth(1.0)
//                lineColor(Styles.instance.trackColor)
            }
            style?.addLayer(lineLayer)
            trails[meta] = trackFeature
            // Adds boat icon
            coords.lastOrNull()?.let { latLng ->
                icons?.boat?.let { icon ->
                    val iconSourceId = meta.iconSource
                    val iconSource = GeoJsonSource.Builder(iconSourceId).geometry(latLng.coord.point()).build()
                    style?.addSource(iconSource)
                    val layerId = meta.iconLayer
                    val symbol = symbolLayer(layerId, iconSourceId) {
                        iconImage(icon)
                        iconSize(BoatIconSize)
                    }
                    style?.addLayer(symbol)
                }
            }
            // Adds trophy using GeoJSON manually instead of using SymbolManager in order to make
            // z-index work as desired (i.e. trophy is shown on top of trails)
            val trophySource = geoJsonSource(meta.trophySource) {
                feature(topFeature(topSpeed))
            }
            style?.addSource(trophySource)
            icons?.trophy?.let { trophyIcon ->
                val symbol = symbolLayer(meta.trophyLayer, meta.trophySource) {
                    iconImage(trophyIcon)
                }
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
        style?.let { s ->
            trails.keys.forEach { meta ->
                s.removeStyleLayer(meta.trailLayer)
                s.removeStyleSource(meta.trailSource)
                s.removeStyleLayer(meta.iconLayer)
                s.removeStyleSource(meta.iconSource)
                s.removeStyleLayer(meta.trophyLayer)
                s.removeStyleSource(meta.trophySource)
                ais?.clear(s)
            }
        }
        trails.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRestart = false
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
