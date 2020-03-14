package com.malliina.boattracker.ui.map

import android.content.Intent
import android.graphics.Color
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
import com.malliina.boattracker.ui.callouts.Callouts
import com.malliina.boattracker.ui.callouts.TopSpeedInfo
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileFragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
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
    companion object {
        const val StyleUrl = "mapbox://styles/malliina/cjgny1fjc008p2so90sbz8nbv"
        const val BoatIconId = "boat-resized-opt-30"
        const val TrophyIconId = "trophy-gold-path"
        const val BoatIconSize: Float = 0.7f
        const val profileCode = 901
    }

    private val args: MapFragmentArgs by navArgs()

    private lateinit var mapView: MapView
    private val viewModel: MapViewModel by viewModels()
    private var map: MapboxMap? = null
    private val style: Style? get() = map?.style

    private var mapState: MapState = MapState(null, null)
    private val settings: UserSettings get() = UserSettings.instance
    private val trails: MutableMap<TrackMeta, LineString> = mutableMapOf()
    private val topSpeedMarkers: MutableMap<TrackName, ActiveMarker> = mutableMapOf()

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
        val token = BuildConfig.MapboxAccessToken
        Timber.i("Using token %s", token)
        Mapbox.getInstance(requireContext(), token)
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
        mapView.getMapAsync { map ->
            this.map = map
            map.setStyle(Style.Builder().fromUri(StyleUrl)) { style ->
                viewModel.conf.observe(viewLifecycleOwner) { conf ->
                    callouts?.clear()
                    callouts = Callouts(map, style, requireActivity(), conf.layers)
                }
            }
        }

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
            UserSettings.instance.conf = conf
            viewModel.user.observe(viewLifecycleOwner) { mapState ->
                view.profile.visibility = Button.VISIBLE
            }
        }
        viewModel.coords.observe(viewLifecycleOwner) { coords ->
            coords?.let {
                map?.let { m ->
                    onCoords(it, m)
                }
            }
        }
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            Timber.i("Using language ${profile.language}")
            settings.profile = profile
        }
        view.profile.setOnClickListener { profileButton ->
            val user = settings.user
            if (user == null) {
                launchLogin()
            } else {
                val action = MapFragmentDirections.mapToProfile(settings.lang!!.appName)
                findNavController().navigate(action)
            }
        }
        view.center.setOnClickListener { centerButton ->

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
        viewModel.reconnect()
    }

    override fun onStop() {
        viewModel.disconnect()
        clearMap()
        super.onStop()
        mapView.onStop()
    }

    private fun onCoords(coords: CoordsData, map: MapboxMap) {
        val from = coords.from
        val newPoints = coords.coords.map { c -> Point.fromLngLat(c.coord.lng, c.coord.lat) }
        val meta = TrackMeta(from.trackName)
        // Updates track
        val lineString = updateOrCreateTrail(meta, coords.from.topPoint, newPoints)
        val trailSource = style?.getSourceAs<GeoJsonSource>(meta.trailSource)
        trailSource?.setGeoJson(lineString)
        val latLngs = lineString.coordinates().map { asLatLng(it) }
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
                        CameraPosition.Builder().target(asLatLng(newPoints.last())).build()
                MapMode.Stay ->
                    Unit
            }
            // Updates boat icon
            style?.getSourceAs<GeoJsonSource>(meta.iconSource)?.let { source ->
                newPoints.lastOrNull()?.let { last ->
                    source.setGeoJson(last)
                }
                // Updates boat icon bearing
                val lastTwo = latLngs.takeLast(2)
                if (lastTwo.size == 2) {
                    style?.getLayerAs<SymbolLayer>(meta.iconLayer)
                        ?.setProperties(
                            PropertyFactory.iconRotate(
                                Geo.instance.bearing(lastTwo[0], lastTwo[1]).toFloat()
                            )
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
        coords: List<Point>
    ): LineString {
        val old = trails[meta]
        return if (old == null) {
            // Adds trail
            val trailSource = meta.trailSource
            val lineString = LineString.fromLngLats(coords)
            val source = GeoJsonSource(trailSource, LineString.fromLngLats(emptyList()))
            style?.addSource(source)
            val lineLayer = LineLayer(meta.trailLayer, trailSource).withProperties(
                PropertyFactory.lineWidth(1f),
                PropertyFactory.lineColor(Color.BLACK)
            )
            style?.addLayer(lineLayer)
            trails[meta] = lineString
            // Adds boat icon
            coords.lastOrNull()?.let { latLng ->
                val iconSourceId = meta.iconSource
                val iconSource = GeoJsonSource(iconSourceId, latLng)
                style?.addSource(iconSource)
                val layerId = meta.iconLayer
                val symbol = SymbolLayer(layerId, iconSourceId).withProperties(
                    PropertyFactory.iconImage(BoatIconId),
                    PropertyFactory.iconSize(BoatIconSize)
                )
                style?.addLayer(symbol)
            }
            // Adds trophy using GeoJSON manually instead of using SymbolManager in order to make
            // z-index work as desired (i.e. trophy is shown on top of trails)
            val feature = topFeature(topSpeed)
            val trophySource = GeoJsonSource(meta.trophySource, feature)
            style?.addSource(trophySource)
            val symbol = SymbolLayer(meta.trophyLayer, meta.trophySource).withProperties(
                PropertyFactory.iconImage(TrophyIconId)
            )
            style?.addLayer(symbol)
            lineString
        } else {
            old.coordinates().addAll(coords)
            old
        }
    }

    private fun topFeature(top: CoordBody): Feature {
        return Feature.fromGeometry(top.coord.point(), trophyJson(top))
    }

    private fun trophyJson(top: CoordBody): JsonObject {
        val str = Callouts.speedAdapter.toJson(TopSpeedInfo(top.speed, top.boatTime))
        return Callouts.gson.fromJson(str, JsonObject::class.java)
    }

    private fun clearMap() {
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
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
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
