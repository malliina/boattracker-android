package com.malliina.boattracker.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileActivity
import com.malliina.boattracker.ui.tracks.TracksActivity
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
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
import timber.log.Timber
import androidx.core.content.ContextCompat

class MapActivity: AppCompatActivity() {
    companion object {
        const val StyleUrl = "mapbox://styles/malliina/cjgny1fjc008p2so90sbz8nbv"
        const val BoatIconId = "boat-resized-opt-30"
        const val BoatIconSize: Float = 0.7f
        const val profileCode = 101
//        const val TrophyIconId = "trophy-14"
    }
    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel
    private var map: MapboxMap? = null
    private val style: Style? get() = map?.style
    private var mapState: MapState = MapState(null, null)
    private var conf: ClientConf? = null
    private var profile: BoatUser? = null
    private val trails: MutableMap<TrackMeta, LineString> = mutableMapOf()
    private val topSpeedMarkers: MutableMap<TrackName, ActiveMarker> = mutableMapOf()
    private val lang: Lang? get() = when (profile?.language) {
        Language.Swedish -> conf?.languages?.swedish
        Language.Finnish -> conf?.languages?.finnish
        Language.English -> conf?.languages?.english
        else -> conf?.languages?.english
    }

    enum class MapMode {
        Fit, Follow, Stay
    }

    data class ActiveMarker(val marker: Marker, val topPoint: CoordBody)

    private var mapMode: MapMode = MapMode.Fit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("Hello, using token %s", BuildConfig.MapboxAccessToken)
        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken)
        setContentView(R.layout.map_activity)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            this.map = map
            map.setStyle(Style.Builder().fromUri(StyleUrl), Style.OnStyleLoaded {

            })
        }

        // Observer code happens on the main thread
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.getUser().observe(this, Observer { mapState ->
            mapState?.let { state ->
                Timber.i("Got ${state.user?.email ?: "no email"} with track ${state.track}")
                this.mapState = state
                viewModel.openSocket(state.user?.idToken, state.track)
                findViewById<Button>(R.id.profile).visibility = Button.VISIBLE
            }
        })
        viewModel.getConf().observe(this, Observer { conf ->
            Timber.i("Got conf.")
            this.conf = conf
        })
        viewModel.getCoords().observe(this, Observer { coords ->
            coords?.let { cs -> map?.let { m -> onCoords(cs, m) }  }
        })
        viewModel.getProfile().observe(this, Observer { profile ->
            Timber.i("Using language ${profile.language}")
            this.profile = profile
        })
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        viewModel.disconnect()
        clearMap()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.reconnect()
    }

    /**
     * Called when the user has selected a track.
     *
     * @see TracksActivity
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { it ->
            it.getParcelableExtra<TrackName>(TrackName.key)?.let { name ->
                mapMode = MapMode.Fit
                viewModel.update(MapState(mapState.user, name))
            }
        }
    }

    private fun onCoords(coords: CoordsData, map: MapboxMap) {
        val from = coords.from
        val newPoints = coords.coords.map { c -> Point.fromLngLat(c.coord.lng, c.coord.lat) }
        val meta = TrackMeta(from.trackName)

        // Updates track
        val lineString = updateOrCreateTrail(meta, coords.from.topPoint, newPoints, map)
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
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), durationMs)
                        mapMode = MapMode.Follow
                    }
                }
                MapMode.Follow ->
                    map.cameraPosition = CameraPosition.Builder().target(asLatLng(newPoints.last())).build()
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
                        ?.setProperties(PropertyFactory.iconRotate(Geo.instance.bearing(lastTwo[0], lastTwo[1]).toFloat()))
                }
            }
            // Updates trophy
            topSpeedMarkers[meta.trackName]?.let { active ->
                val top = from.topPoint
                if (active.topPoint.speed.knots < top.speed.knots) {
                    fill(active.marker, top)
                }
            }
        }
    }

    private fun fill(marker: Marker, with: CoordBody) {
        marker.title = with.speed.toString()
        marker.snippet = with.boatTime
        marker.position = with.coord.latLng()
    }

    private fun asLatLng(p: Point): LatLng = LatLng(p.latitude(), p.longitude())

    private fun updateOrCreateTrail(meta: TrackMeta, topSpeed: CoordBody, coords: List<Point>, map: MapboxMap): LineString {
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
                val symbol = SymbolLayer(layerId, iconSourceId)
                    .withProperties(PropertyFactory.iconImage(BoatIconId), PropertyFactory.iconSize(BoatIconSize))
                style?.addLayer(symbol)
            }
            // Adds trophy
            val icons = IconFactory.getInstance(this)
            val options = MarkerOptions()
                .position(topSpeed.coord.latLng())
                .title(topSpeed.speed.formatted())
                .snippet(topSpeed.boatTime)
            val bitmap = svgToBitmap(this, R.drawable.ic_trophy)
            val trophyOptions = if (bitmap != null) options.icon(icons.fromBitmap(bitmap)) else options
            val marker = map.addMarker(trophyOptions)
            topSpeedMarkers[meta.trackName] = ActiveMarker(marker, topSpeed)
            // Adds trophy 2
//            val trophySrcId = trophySourceId(trackName)
//            val trophySource = GeoJsonSource(trophySrcId, topSpeed.coord.point())
//            map.addSource(trophySource)
//            val trophyId = trophyLayerId(trackName)
//            val trophySymbol = SymbolLayer(trophyId, trophySrcId)
//                .withProperties(PropertyFactory.iconImage(TrophyIconId), PropertyFactory.iconSize(1.0f))
//            map.addLayer(trophySymbol)
            lineString
        } else {
            old.coordinates().addAll(coords)
            old
        }
    }

    // TODO move elsewhere
    private fun svgToBitmap(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        return if (drawable != null) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            null
        }
    }


    fun profileClicked(button: View) {
        val u = mapState.user
        val c = conf
        if (u != null && c != null && lang != null) {
            lang?.let { l ->
                Timber.i("Opening profile for ${u.email}...")
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra(ProfileInfo.key, ProfileInfo(u.email, u.idToken, l, mapState.track))
                }
                startActivityForResult(intent, profileCode)
            }
        } else {
            Timber.i("Opening login screen...")
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, profileCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.i("Returning with code $requestCode")
        when (requestCode) {
            profileCode -> data?.let {
                if (it.getBooleanExtra(ProfileActivity.refreshSignIn, false)) {
                    viewModel.signInSilently(this)
                }
            }
            else -> {}
        }
    }

    fun locationClicked(button: View) {
        Timber.i("Location")
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
