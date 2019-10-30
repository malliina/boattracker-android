package com.malliina.boattracker.ui.callouts

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.google.gson.Gson
import com.malliina.boattracker.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.BubbleLayout
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.min

@JsonClass(generateAdapter = true)
data class TopSpeedInfo(val speed: Speed, val dateTime: String)

// https://docs.mapbox.com/android/maps/examples/symbol-layer-info-window/
class Callouts(
    val map: MapboxMap,
    val style: Style,
    private val activity: Activity,
    private val layers: Layers,
    private val lang: Lang
) {
    companion object {
        const val CalloutImageName = "callout-image"
        const val CalloutLayerId = "callout-layer"
        const val CalloutSourceId = "callout-source"

        val gson = Gson()
        val speedAdapter: JsonAdapter<TopSpeedInfo> = Json.moshi.adapter(TopSpeedInfo::class.java)
        val marineSymbolAdapter: JsonAdapter<MarineSymbol> =
            Json.moshi.adapter(MarineSymbol::class.java)
        val fairwayAreaAdapter: JsonAdapter<FairwayArea> =
            Json.moshi.adapter(FairwayArea::class.java)
        val limitAreaAdapter: JsonAdapter<LimitArea> =
            Json.moshi.adapter(LimitArea::class.java)
        val trafficSignAdapter: JsonAdapter<TrafficSign> =
            Json.moshi.adapter(TrafficSign::class.java)
    }

    private val calloutImages: MutableMap<String, Bitmap> = mutableMapOf()
    private val calloutViews: MutableMap<String, View> = mutableMapOf()

    private val calloutProps = arrayOf(
        PropertyFactory.iconImage(CalloutImageName),
        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
        PropertyFactory.iconOffset(arrayOf(-2f, -2f))
    )

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        map.addOnMapClickListener { latLng ->
            uiScope.launch { onMapClick(latLng) }
            true
        }
        style.addSource(GeoJsonSource(CalloutSourceId, FeatureCollection.fromFeatures(emptyList())))
    }

    private suspend fun onMapClick(latLng: LatLng): Boolean {
        val maybePrevious = style.getLayerAs<SymbolLayer>(CalloutLayerId)
        if (maybePrevious == null) {
            val callout = pointCallout(latLng) ?: marksCallout(latLng) ?: areaCallout(latLng) ?: limitCallout(latLng)
            callout?.let {
                showCallout(latLng, it)
            }
        } else {
            style.removeLayer(CalloutLayerId)
        }
        return true
    }

    private fun pointCallout(latLng: LatLng): BoatCallout? {
        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(latLng))
        features.firstOrNull { it.geometry()?.type() == "Point" }?.let {
            val speedInfo = speedAdapter.readOpt(gson.toJson(it.properties()))
            speedInfo?.let { info ->
                val callout: TrophyCallout =
                    activity.layoutInflater.inflate(R.layout.trophy, null) as TrophyCallout
                callout.fill(info)
                return callout
            }
            val trafficInfo = trafficSignAdapter.readOpt(gson.toJson(it.properties()))
            trafficInfo?.let { sign ->
                val callout: TrafficSignCallout =
                    activity.layoutInflater.inflate(R.layout.traffic_sign, null) as TrafficSignCallout
                callout.fill(sign, lang)
                return callout
            }
        }
        return null
    }

    private fun marksCallout(latLng: LatLng): MarineSymbolCallout? {
        val features = map.queryRenderedFeatures(
            map.projection.toScreenLocation(latLng),
            *layers.marks.toTypedArray()
        )
        features.map { f ->
            val jsonString = gson.toJson(f.properties())
            marineSymbolAdapter.readOpt(jsonString)?.let {
                val callout: MarineSymbolCallout = activity.layoutInflater.inflate(
                    R.layout.marine_symbol,
                    null
                ) as MarineSymbolCallout
                callout.fill(it, lang)
                return callout
            }
        }
        return null
    }

    private fun areaCallout(latLng: LatLng): FairwayAreaCallout? {
        val features = map.queryRenderedFeatures(
            map.projection.toScreenLocation(latLng),
            *layers.fairwayAreas.toTypedArray()
        )
        features.map { f ->
            val json = gson.toJson(f.properties())
            fairwayAreaAdapter.readOpt(json)?.let { area ->
                val callout: FairwayAreaCallout =
                    activity.layoutInflater.inflate(
                        R.layout.fairway_area_symbol,
                        null
                    ) as FairwayAreaCallout
                callout.fill(area, limitAreaInfo(latLng), lang)
                return callout
            }
        }
        return null
    }

    private fun limitCallout(latLng: LatLng): FairwayLimitCallout? {
        limitAreaInfo(latLng)?.let { limit ->
            val callout: FairwayLimitCallout =
                activity.layoutInflater.inflate(R.layout.fairway_limit_symbol, null) as FairwayLimitCallout
            callout.fill(limit, lang.limits)
            return callout
        }
        return null
    }

    private fun limitAreaInfo(latLng: LatLng): LimitArea? {
        val features = map.queryRenderedFeatures(
            map.projection.toScreenLocation(latLng),
            *layers.limits.toTypedArray()
        )
        features.map { f -> limitAreaAdapter.readOpt(gson.toJson(f.properties()))?.let { return it } }
        return null
    }

    private fun <T> JsonAdapter<T>.readOpt(jsonString: String): T? {
        return try {
            this.read(jsonString)
        } catch (e: JsonDataException) {
            Timber.i("Failed to parse '$jsonString': ${e.message ?: "JSON failure."}")
            null
        }
    }

    private fun <T> JsonAdapter<T>.read(jsonString: String): T {
        return this.fromJson(jsonString)
            ?: throw JsonDataException("Moshi returned null for '$jsonString'.")
    }

    private suspend fun showCallout(latLng: LatLng, callout: BubbleLayout) {
        val bitmap = calloutBitmap(callout)
        style.addImage(CalloutImageName, bitmap)
        val calloutPointFeature =
            Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
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
            toBitmap(callout)
        }
    }

    private fun toBitmap(callout: BubbleLayout): Bitmap {
        val display = activity.resources.displayMetrics
        val displayWidth = display.widthPixels
        val widthSpec = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.AT_MOST)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(display.heightPixels, View.MeasureSpec.AT_MOST)
        callout.measure(widthSpec, heightSpec)

        val measuredWidth = callout.measuredWidth
        val measuredHeight = callout.measuredHeight

        val w = min(measuredWidth, displayWidth)
        // In the middle, I guess.
        callout.arrowPosition = 1.0f * w / 2 - 5
        val h = measuredHeight
        callout.layout(0, 0, w, h)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        callout.draw(canvas)
        return bitmap
    }

    fun clear() {
        style.getLayer(CalloutLayerId)?.let {
            style.removeLayer(it)
        }
        style.getSource(CalloutSourceId)?.let {
            style.removeSource(it)
        }
    }
}
