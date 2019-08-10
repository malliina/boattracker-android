package com.malliina.boattracker.ui.callouts

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.malliina.boattracker.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.BubbleLayout
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.*
import timber.log.Timber

data class TopSpeedInfo(val speed: Speed, val dateTime: String)

class Callouts(mapView: MapView,
               val map: MapboxMap,
               val style: Style,
               private val activity: Activity,
               private val layers: Layers) {
    companion object {
        const val CalloutImageName = "callout-image"
        const val CalloutLayerId = "callout-layer"
        const val CalloutSourceId = "callout-source"
        const val CustomDataKey = "custom_data"
        const val TrophyIconId = "trophy-14"

        val gson = Gson()
        val speedAdapter: JsonAdapter<TopSpeedInfo> = Json.moshi.adapter(TopSpeedInfo::class.java)
        val marineSymbolAdapter: JsonAdapter<MarineSymbol> = Json.moshi.adapter(MarineSymbol::class.java)
    }

    private val calloutImages: MutableMap<String, Bitmap> = mutableMapOf()
    private val calloutViews: MutableMap<String, View> = mutableMapOf()

    private val calloutProps = arrayOf(
        PropertyFactory.iconImage(CalloutImageName),
        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
        PropertyFactory.iconOffset(arrayOf(-2f, -2f))
    )

    private val symbolManager = SymbolManager(mapView, map, style).apply {
        iconAllowOverlap = true
    }

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        map.addOnMapClickListener { latLng ->
            uiScope.launch { onMapClick(latLng) }
            true
        }
        style.addSource(GeoJsonSource(CalloutSourceId, FeatureCollection.fromFeatures(emptyList())))
    }

    fun createTrophy(top: CoordBody) {
        val str = speedAdapter.toJson(
            TopSpeedInfo(
                top.speed,
                top.boatTime
            )
        )
        val opts = SymbolOptions()
            .withLatLng(top.coord.latLng())
            .withIconImage(TrophyIconId)
            .withData(gson.fromJson(str, JsonObject::class.java))
        create(opts)
    }

    private fun create(opts: SymbolOptions) {
        symbolManager.create(opts)
    }

    private suspend fun onMapClick(latLng: LatLng): Boolean {
        val maybePrevious = style.getLayerAs<SymbolLayer>(CalloutLayerId)
        if (maybePrevious == null) {
            handleTap(latLng, listOf(::handleTrophyTap, ::handleMarksTap))
        } else {
            style.removeLayer(CalloutLayerId)
        }
        return true
    }

    private suspend fun handleTap(latLng: LatLng, handlers: List<suspend (LatLng) -> Boolean>) {
        handlers.firstOrNull()?.let {
            val wasHandled = it(latLng)
            if (!wasHandled) handleTap(latLng, handlers.drop(1))
        }
    }

    private suspend fun handleTrophyTap(latLng: LatLng): Boolean {
        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(latLng))
        features.firstOrNull { it.geometry()?.type() == "Point" }?.let {
            speedAdapter.fromJson(
                gson.toJson(it.properties()?.getAsJsonObject(CustomDataKey)))?.let { info ->
                val callout: BubbleLayout = activity.layoutInflater.inflate(R.layout.trophy, null) as BubbleLayout
                callout.findViewById<TextView>(R.id.trophy_speed_text).text = info.speed.formatted()
                callout.findViewById<TextView>(R.id.trophy_datetime_text).text = info.dateTime
                showCallout(latLng, callout)
                return true
            }
        }
        return false
    }

    private suspend fun handleMarksTap(latLng: LatLng): Boolean {
        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(latLng), *layers.marks.toTypedArray())
        features.map { f ->
            UserSettings.instance.lang?.let {lang ->
                marineSymbolAdapter.fromJson(gson.toJson(f.properties()))?.let {
                    val callout: BubbleLayout = activity.layoutInflater.inflate(R.layout.marine_symbol, null) as BubbleLayout
                    val markLang = lang.mark

                    callout.findViewById<TextView>(R.id.mark_type_label).text = markLang.aidType
                    callout.findViewById<TextView>(R.id.mark_nav_label).text = markLang.navigation
                    callout.findViewById<TextView>(R.id.mark_location_label).text = markLang.location
                    callout.findViewById<TextView>(R.id.mark_owner_label).text = markLang.owner

                    callout.findViewById<TextView>(R.id.mark_name_text).text = it.name(lang.language)?.value ?: ""
                    callout.findViewById<TextView>(R.id.mark_type_text).text = it.aidType.translate(markLang.aidTypes)
                    callout.findViewById<TextView>(R.id.mark_nav_text).text = it.navMark.translate(markLang.navTypes)
                    callout.findViewById<TextView>(R.id.mark_location_text).text = it.location(lang.language)?.value ?: ""
                    callout.findViewById<TextView>(R.id.mark_owner_text).text = it.owner

                    showCallout(latLng, callout)
                    return true
                }
            }
        }
        return false
    }

    private suspend fun showCallout(latLng: LatLng, callout: BubbleLayout) {
        val bitmap = calloutBitmap(callout)
        style.addImage(CalloutImageName, bitmap)
        val calloutPointFeature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
        style.getSourceAs<GeoJsonSource>(CalloutSourceId)?.setGeoJson(calloutPointFeature)
        val maybeLayer = style.getLayerAs<SymbolLayer>(CalloutLayerId)
        if (maybeLayer != null) {
            maybeLayer.setProperties(*calloutProps)
        } else {
            val layer = SymbolLayer(CalloutLayerId, CalloutSourceId).withProperties(*calloutProps)
            style.addLayer(layer)
        }
        calloutImages[CalloutImageName] = bitmap
        calloutViews[CalloutImageName] = callout
    }

    private suspend fun calloutBitmap(callout: BubbleLayout): Bitmap {
        return withContext(Dispatchers.IO) {
            val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            callout.measure(measureSpec, measureSpec)
            val measuredWidth = callout.measuredWidth
            callout.arrowPosition = 1.0f * measuredWidth / 2 - 5
            toBitmap(callout)
        }
    }

    private fun toBitmap(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)

        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight

        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun clear() {
        style.getLayer(CalloutLayerId)?.let {
            style.removeLayer(it)
        }
        style.getSource(CalloutSourceId)?.let {
            style.removeSource(it)
        }
        symbolManager.deleteAll()
    }
}
