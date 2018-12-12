package com.malliina.boattracker.ui.map

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.login.LoginActivity
import com.malliina.boattracker.ui.profile.ProfileActivity
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
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import timber.log.Timber

class MapActivity: AppCompatActivity() {
    companion object {
        const val BoatIconId = "boat-resized-opt-30"
        const val BoatIconSize: Float = 0.7f
//        const val TrophyIconId = "trophy-14"
    }
    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel
    private var map: MapboxMap? = null
    private var user: UserInfo? = null
    private val trails: MutableMap<TrackMeta, LineString> = mutableMapOf()
    private val topSpeedMarkers: MutableMap<TrackName, ActiveMarker> = mutableMapOf()

    enum class MapMode {
        Fit, Follow, Stay
    }

    data class ActiveMarker(val marker: Marker, val topPoint: CoordBody)

    private var mapMode: MapMode = MapMode.Fit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(localClassName)

        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken)
        setContentView(R.layout.map_activity)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            this.map = map
        }

        AppCenter.start(application, "768ec01e-fe9c-46b2-a05a-5389fa9d148f",
            Analytics::class.java, Crashes::class.java)

        // Observer code happens on the main thread
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.getUser().observe(this, Observer { userInfo ->
//            Timber.i("Got user $userInfo")
            user = userInfo
            viewModel.openSocket(userInfo?.idToken)
            findViewById<Button>(R.id.profile).visibility = Button.VISIBLE
        })
        viewModel.coords().observe(this, Observer { coords ->
            coords?.let { cs -> map?.let { m -> onCoords(cs, m) }  }
        })
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        // https://developers.google.com/identity/sign-in/android/backend-auth
        viewModel.signInSilently(this)
    }

    private fun onCoords(coords: CoordsData, map: MapboxMap) {
        val from = coords.from
        val newPoints = coords.coords.map { c -> Point.fromLngLat(c.coord.lng, c.coord.lat) }
        val meta = TrackMeta(from.trackName)

        // Updates track
        val lineString = updateOrCreateTrail(meta, coords.from.topPoint, newPoints, map)
        val trailSource = map.getSourceAs<GeoJsonSource>(meta.trailSource)
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
            map.getSourceAs<GeoJsonSource>(meta.iconSource)?.let { source ->
                newPoints.lastOrNull()?.let { last ->
                    source.setGeoJson(last)
                }
                // Updates boat icon bearing
                val lastTwo = latLngs.takeLast(2)
                if (lastTwo.size == 2) {
                    map.getLayerAs<SymbolLayer>(meta.iconLayer)
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
            map.addSource(source)
            val lineLayer = LineLayer(meta.trailLayer, trailSource).withProperties(
                PropertyFactory.lineWidth(1f),
                PropertyFactory.lineColor(Color.BLACK)
            )
            map.addLayer(lineLayer)
            trails[meta] = lineString
            // Adds boat icon
            coords.lastOrNull()?.let { latLng ->
                val iconSourceId = meta.iconSource
                val iconSource = GeoJsonSource(iconSourceId, latLng)
                map.addSource(iconSource)
                val layerId = meta.iconLayer
                val symbol = SymbolLayer(layerId, iconSourceId)
                    .withProperties(PropertyFactory.iconImage(BoatIconId), PropertyFactory.iconSize(BoatIconSize))
                map.addLayer(symbol)
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
        val u = user
        if (u != null) {
            Timber.i( "Opening profile for ${u.email}...")
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.userEmail, u.email.email)
                putExtra(ProfileActivity.userToken, u.idToken.token)
            }
            startActivity(intent)
        } else {
            Timber.i("Opening login screen...")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun locationClicked(button: View) {
        Timber.i("Location")
    }

    private fun clearMap() {
        map?.let { map ->
            trails.keys.forEach { meta ->
                map.removeLayer(meta.trailLayer)
                map.removeSource(meta.trailSource)
                map.removeLayer(meta.iconLayer)
                map.removeSource(meta.iconSource)
                map.removeLayer(meta.trophyLayer)
                map.removeSource(meta.trophySource)
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

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        viewModel.disconnect()
        clearMap()
        Timber.i("Stopped map.")
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
