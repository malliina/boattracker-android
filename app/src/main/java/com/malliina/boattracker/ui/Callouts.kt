package com.malliina.boattracker.ui

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
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.squareup.moshi.JsonAdapter
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
    }

    private val calloutImages: MutableMap<String, Bitmap> = mutableMapOf()
    private val calloutViews: MutableMap<String, View> = mutableMapOf()

    private val symbolManager = SymbolManager(mapView, map, style).apply {
        iconAllowOverlap = true
    }

    init {
        map.addOnMapClickListener { latLng ->
            onMapClick(latLng)
        }
        style.addSource(GeoJsonSource(CalloutSourceId, FeatureCollection.fromFeatures(emptyList())))
    }

    fun createTrophy(top: CoordBody) {
        val str = speedAdapter.toJson(TopSpeedInfo(top.speed, top.boatTime))
        val opts = SymbolOptions()
            .withLatLng(top.coord.latLng())
            .withIconImage(TrophyIconId)
            .withData(gson.fromJson(str, JsonObject::class.java))
        Timber.i("Data $str as ${gson.fromJson(str, JsonObject::class.java)}")
        create(opts)
    }

    private fun create(opts: SymbolOptions) {
        symbolManager.create(opts)
    }

    private fun onMapClick(latLng: LatLng): Boolean {
        val maybePrevious = style.getLayerAs<SymbolLayer>(CalloutLayerId)
        if (maybePrevious == null) {
            map.queryRenderedFeatures(map.projection.toScreenLocation(latLng)).firstOrNull { it.geometry()?.type() == "Point" }?.let {
                speedAdapter.fromJson(gson.toJson(it.properties()?.getAsJsonObject(CustomDataKey)))?.let { info ->
                    showTrophyCallout(latLng, info)
                }
            }
        } else {
            style.removeLayer(CalloutLayerId)
        }
        return true
    }

    /**
     * TODO should probably generate the bitmap on a background thread
     */
    private fun showTrophyCallout(latLng: LatLng, info: TopSpeedInfo) {
        val startCallout = System.currentTimeMillis()
        val callout: BubbleLayout = activity.layoutInflater.inflate(R.layout.trophy, null) as BubbleLayout
        callout.findViewById<TextView>(R.id.trophy_speed_text).text = info.speed.formatted()
        callout.findViewById<TextView>(R.id.trophy_datetime_text).text = info.dateTime
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        callout.measure(measureSpec, measureSpec)
        val measuredWidth = callout.measuredWidth
        callout.arrowPosition = 1.0f * measuredWidth / 2 - 5
        val start = System.currentTimeMillis()
        val bitmap = toBitmap(callout)
        val end = System.currentTimeMillis()
        style.addImage(CalloutImageName, bitmap)
        val calloutPointFeature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
        style.getSourceAs<GeoJsonSource>(CalloutSourceId)?.setGeoJson(calloutPointFeature)
        val props = arrayOf(
            PropertyFactory.iconImage(CalloutImageName),
            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
            PropertyFactory.iconOffset(arrayOf(-2f, -2f))
        )
        val maybeLayer = style.getLayerAs<SymbolLayer>(CalloutLayerId)
        if (maybeLayer != null) {
            maybeLayer.setProperties(*props)
        } else {
            style.addLayer(
                SymbolLayer(CalloutLayerId, CalloutSourceId)
                    .withProperties(*props)
            )
        }
        val added = System.currentTimeMillis()
        Timber.i("Produced callout in ${added-startCallout} ms. Steps $startCallout, $start, $end, $added")
        calloutImages[CalloutImageName] = bitmap
        calloutViews[CalloutImageName] = callout
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
