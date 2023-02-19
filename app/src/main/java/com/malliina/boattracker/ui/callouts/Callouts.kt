package com.malliina.boattracker.ui.callouts

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.VesselInfo
import com.malliina.boattracker.ui.map.MapFragment
import com.malliina.boattracker.ui.map.VesselsRenderer
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.QueriedFeature
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface MeasuredCoord {
    val coord: Coord
    val speed: Speed
}

@JsonClass(generateAdapter = true)
data class WrappedSpeed(val speed: Speed)

data class SimpleCoord(override val coord: Coord, override val speed: Speed) : MeasuredCoord

@JsonClass(generateAdapter = true)
data class SpeedInfo(val speed: Speed, val dateTime: String) {
    companion object {
        val dateTime = "dateTime"
    }
}

// https://docs.mapbox.com/android/maps/examples/symbol-layer-info-window/
class Callouts(
    val map: MapboxMap,
    val annotations: ViewAnnotationManager,
    val style: Style,
    private val activity: Activity,
    private val conf: ClientConf,
    private val settings: UserSettings,
    private val fragment: MapFragment
) {
    companion object {
        const val CalloutImageName = "callout-image"
        const val CalloutLayerId = "callout-layer"
        const val CalloutSourceId = "callout-source"

        val gson = Json.gson
        val speedAdapter: JsonAdapter<SpeedInfo> = Json.moshi.adapter(SpeedInfo::class.java)
        val wrappedAdapter: JsonAdapter<WrappedSpeed> = Json.moshi.adapter(WrappedSpeed::class.java)
        val marineSymbolAdapter: JsonAdapter<MarineSymbol> =
            Json.moshi.adapter(MarineSymbol::class.java)
        val fairwayAreaAdapter: JsonAdapter<FairwayArea> =
            Json.moshi.adapter(FairwayArea::class.java)
        val limitAreaAdapter: JsonAdapter<LimitArea> =
            Json.moshi.adapter(LimitArea::class.java)
        val trafficSignAdapter: JsonAdapter<TrafficSign> =
            Json.moshi.adapter(TrafficSign::class.java)
    }

    private val layers = conf.layers

    private val calloutImages: MutableMap<String, Bitmap> = mutableMapOf()
    private val calloutViews: MutableMap<String, View> = mutableMapOf()

//    private val calloutProps = arrayOf(
//        PropertyFactory.iconImage(CalloutImageName),
//        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
//        PropertyFactory.iconOffset(arrayOf(-2f, -2f))
//    )

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val symbolInfo = fragment.view?.findViewById<VesselInfo>(R.id.symbol_info)
    private val animator = BoatAnimator(map)

    init {
        map.addOnMapClickListener { latLng ->
            uiScope.launch { onMapClick(latLng) }
            true
        }
//        style.addSource(GeoJsonSource(CalloutSourceId, FeatureCollection.fromFeatures(emptyList())))
    }

    private suspend fun onMapClick(latLng: Point): Boolean {
        annotations.removeAllViewAnnotations()
        val previousSymbol = symbolInfo?.visibility == View.VISIBLE
        if (previousSymbol) {
            symbolInfo?.let { info ->
                val anim = AnimationUtils.loadAnimation(
                    fragment.context,
                    R.anim.slide_down
                )
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        info.visibility = View.GONE
                    }
                })
                info.startAnimation(anim)
            }
        } else {
//            style.styleLayerExists(CalloutLayerId)
//            val maybePrevious = style.getLayerAs<SymbolLayer>(CalloutLayerId)
//            if (maybePrevious == null) {
            settings.lang?.let { lang ->
                Timber.i("Handling symbol...")
                val handled = handleSymbol2(latLng, lang)
//                    if (!handled) {
//                        val callout =
//                            pointCallout(latLng, lang) ?: marksCallout(latLng, lang) ?: areaCallout(
//                                latLng,
//                                lang
//                            ) ?: limitCallout(latLng, lang)
//                        callout?.let {
//                            showCallout(latLng, it)
//                        }
//                    }
            }
//            } else {
//                Timber.i("Previous callout was on map, removing it...")
//                style.removeStyleLayer(CalloutLayerId)
//            }
        }
        return true
    }

    private suspend fun handleSymbol2(latLng: Point, lang: Lang): Boolean {
        val pixel = map.pixelForCoordinate(latLng)
        val mapSize = map.getSize()
        val x = pixel.x / mapSize.width
        val y = pixel.y / mapSize.height
        val anchorPosition =
            if (x < 0.33 && y < 0.33) ViewAnnotationAnchor.TOP_LEFT
            else if (x > 0.66 && y < 0.33) ViewAnnotationAnchor.TOP_RIGHT
            else if (x >= 0.33 && y < 0.33) ViewAnnotationAnchor.TOP
            else if (x < 0.33 && y > 0.66) ViewAnnotationAnchor.BOTTOM_LEFT
            else if (x >= 0.66 && y > 0.66) ViewAnnotationAnchor.BOTTOM_RIGHT
            else if (x < 0.33) ViewAnnotationAnchor.LEFT
            else if (x > 0.66) ViewAnnotationAnchor.RIGHT
            else ViewAnnotationAnchor.BOTTOM
        val parsed = parsePoint(latLng, lang) ?: parseArea(latLng, lang)
        parsed?.let { content ->
            val view = ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    PopupView(content) {
                        annotations.removeAllViewAnnotations()
                    }
                }
                val layout = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams = FrameLayout.LayoutParams(layout)
            }
            val options = viewAnnotationOptions {
                geometry(latLng)
                anchor(anchorPosition)
//                            associatedFeatureId(feature.id())
            }
            annotations.addViewAnnotation(view, options)
        }
        return false
    }

    private suspend fun parsePoint(latLng: Point, lang: Lang): PopupContent? {
        query(latLng, null).firstOrNull { it.feature.geometry()?.type() == "Point" }?.let { queried ->
            val feature = queried.feature
            val asGson = gson.toJson(feature.properties())
            speedAdapter.readOpt(asGson)?.let { info ->
                return PopupContent(null, listOf(InfoItem(lang.track.speed, info.speed.formatKn())), info.dateTime)
            }
            marineSymbolAdapter.readOpt(asGson)?.let { symbol ->
                val markLang = lang.mark
                val aidType = InfoItem(markLang.aidType, symbol.aidType.translate(markLang.aidTypes))
                val construction = symbol.construction?.let { c ->
                    InfoItem(markLang.construction, c.translate(markLang.structures))
                }
                val nav =
                    if (symbol.navMark.isKnown())
                        InfoItem(markLang.navigation, symbol.navMark.translate(markLang.navTypes))
                    else null
                val location = symbol.location(lang.language)?.let { loc ->
                    InfoItem(markLang.location, loc.value)
                }
                val owner = InfoItem(markLang.owner, symbol.owner)
                return PopupContent.nonEmpty(
                    symbol.name(lang.language)?.value ?: "",
                    listOf(aidType, construction, nav, location, owner),
                    null
                )
            }
            trafficSignAdapter.readOpt(asGson)?.let { sign ->
                val signs = lang.limits.signs
                val nameOrEmpty = sign.nameOrEmpty(lang.language)
                val info = sign.sign?.let { info -> InfoItem(lang.mark.markType, info.translate(signs.limits, signs.infos)) }
                return PopupContent.nonEmpty(nameOrEmpty, listOf(info), null)
            }
            VesselsRenderer.vesselAdapter.readOpt(asGson)?.let { vessel ->
                val destination = vessel.destination?.let { InfoItem(lang.ais.destination, it) }
                val speed = InfoItem(lang.track.speed, vessel.sog.formatKn())
                val draft = InfoItem(lang.ais.draft, vessel.draft.formatMeters())
                return PopupContent.nonEmpty(vessel.name, listOf(destination, speed, draft), vessel.time.dateTime)
            }
        }
        return null
    }

    private suspend fun parseArea(latLng: Point, lang: Lang): PopupContent? {
        val fairwayLang = lang.fairway
        query(latLng, layers.fairwayAreas).map { queried ->
            fairwayAreaAdapter.readOpt(gson.toJson(queried.feature.properties()))?.let { area ->
                val limits = limitAreaInfo(latLng)
                val areaItems = listOf(
                    InfoItem(fairwayLang.fairwayType, area.fairwayType.translate(fairwayLang.types)),
                    InfoItem(fairwayLang.fairwayDepth, area.fairwayDepth.formatMeters()),
                    InfoItem(fairwayLang.harrowDepth, area.harrowDepth.formatMeters()),
                )
                val limitItems = limits?.let { ls ->
                    val limitLang = lang.limits
                    listOf(
                        InfoItem(limitLang.limit, ls.types.joinToString { it.translate(limitLang.types) }),
                        ls.limit?.let { speed -> InfoItem(limitLang.magnitude, speed.formatKmhInt()) },
                        ls.fairwayName?.let { n -> InfoItem(limitLang.fairwayName, n.value) }
                    )
                } ?: emptyList()
                return PopupContent.nonEmpty(area.owner.value, areaItems + limitItems, null)
            }
        }
        return null
    }

    private suspend fun limitAreaInfo(latLng: Point): LimitArea? {
        query(latLng, layers.limits).map { f ->
            limitAreaAdapter.readOpt(gson.toJson(f.feature.properties()))?.let { return it }
        }
        return null
    }

    private suspend fun query(latLng: Point, layerIds: List<String>?): List<QueriedFeature> = suspendCancellableCoroutine { cont ->
        val cancellable = map.queryRenderedFeatures(
            RenderedQueryGeometry(map.pixelForCoordinate(latLng)),
            RenderedQueryOptions(layerIds, null)
        ) { e ->
            if (e.isValue) {
                cont.resume(e.value ?: emptyList())
            } else {
                cont.resumeWithException(Exception("Failed to query point $latLng with layers $layerIds"))
            }
        }
        cont.invokeOnCancellation {
            cancellable.cancel()
        }
    }

    private fun handleSymbol(latLng: Point, lang: Lang): Boolean {
        val features = map.queryRenderedFeatures(
            RenderedQueryGeometry(map.pixelForCoordinate(latLng)),
            RenderedQueryOptions(null, null)
        ) { e ->
            if (e.isValue) {
                val list = e.value ?: emptyList()
                Timber.i("Got ${list.size} features")
                list.firstOrNull { it.feature.geometry()?.type() == "Point" }?.let { feature ->
                    Timber.i("Got feature ${feature.feature}")
                }
            } else {
                Timber.w("Query failed.")
            }
        }
//        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(latLng))
//        features.firstOrNull { it.geometry()?.type() == "Point" }?.let { feature ->
//            val asGson = gson.toJson(feature.properties())
//            VesselsRenderer.vesselAdapter.readOpt(asGson)?.let { vessel ->
//                symbolInfo?.let { info ->
//                    info.fill(vessel, lang)
//                    info.visibility = View.VISIBLE
//                    info.startAnimation(
//                        AnimationUtils.loadAnimation(fragment.context, R.anim.slide_up)
//                    )
//                    animateCameraToVessel(vessel, newZoom = 12.0)
//                    return true
//                }
//            }
//        }
        return false
    }

//    private fun animateCameraToVessel(
//        vessel: Vessel,
//        newZoom: Double
//    ) {
//        val cameraPosition: CameraPosition = map.cameraPosition
//        val animatorSet = AnimatorSet()
//        Timber.i("Animating to bearing ${vessel.heading ?: vessel.cog}...")
//        animatorSet.playTogether(
//            animator.createLatLngAnimator(cameraPosition.target, vessel.coord.latLng()),
//            animator.createZoomAnimator(cameraPosition.zoom, newZoom),
//            animator.createBearingAnimator(cameraPosition.bearing, vessel.heading ?: vessel.cog)
// //            createTiltAnimator(cameraPosition.tilt, feature.getNumberProperty("tilt").doubleValue())
//        )
//        animatorSet.start()
//    }

//    private fun pointCallout(latLng: Point, lang: Lang): BoatCallout? {
//        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(latLng))
//        val inflater = activity.layoutInflater
//        features.firstOrNull { it.geometry()?.type() == "Point" }?.let {
//            val asGson = gson.toJson(it.properties())
//            speedAdapter.readOpt(asGson)?.let { info ->
//                val callout: TrophyCallout =
//                    inflater.inflate(R.layout.trophy, null) as TrophyCallout
//                callout.fill(info)
//                return callout
//            }
//            trafficSignAdapter.readOpt(asGson)?.let { sign ->
//                val callout: TrafficSignCallout =
//                    inflater.inflate(R.layout.traffic_sign, null) as TrafficSignCallout
//                callout.fill(sign, lang)
//                return callout
//            }
//            VesselsRenderer.vesselAdapter.readOpt(asGson)?.let { vessel ->
//                val callout = inflater.inflate(R.layout.vessel_symbol, null) as VesselCallout
//                callout.fill(vessel, lang)
//                return callout
//            }
//        }
//        return null
//    }

    private fun marksCallout(latLng: Point, lang: Lang) {
        val q = RenderedQueryGeometry(map.pixelForCoordinate(latLng))
        val opts = RenderedQueryOptions(layers.marks, null)
        val features = map.queryRenderedFeatures(
            RenderedQueryGeometry(map.pixelForCoordinate(latLng)),
            RenderedQueryOptions(layers.marks, null)
        ) { e ->
            if (e.isValue) {
                val list = e.value ?: emptyList()
                list.firstOrNull()?.let { f ->
                    val jsonString = gson.toJson(f.feature.properties())
                }
            }
        }
//        features.map { f ->
//            val jsonString = gson.toJson(f.properties())
//            marineSymbolAdapter.readOpt(jsonString)?.let {
//                val callout: MarineSymbolCallout = activity.layoutInflater.inflate(
//                    R.layout.marine_symbol,
//                    null
//                ) as MarineSymbolCallout
//                callout.fill(it, lang)
//                return callout
//            }
//        }
//        return null
    }

//    private fun areaCallout(latLng: LatLng, lang: Lang): FairwayAreaCallout? {
//        val features = map.queryRenderedFeatures(
//            map.projection.toScreenLocation(latLng),
//            *layers.fairwayAreas.toTypedArray()
//        )
//        features.map { f ->
//            val json = gson.toJson(f.properties())
//            fairwayAreaAdapter.readOpt(json)?.let { area ->
//                val callout: FairwayAreaCallout =
//                    activity.layoutInflater.inflate(
//                        R.layout.fairway_area_symbol,
//                        null
//                    ) as FairwayAreaCallout
//                callout.fill(area, limitAreaInfo(latLng), lang)
//                return callout
//            }
//        }
//        return null
//    }

//    private fun limitCallout(latLng: Point, lang: Lang): FairwayLimitCallout? {
//        limitAreaInfo(latLng)?.let { limit ->
//            val callout: FairwayLimitCallout =
//                activity.layoutInflater.inflate(
//                    R.layout.fairway_limit_symbol,
//                    null
//                ) as FairwayLimitCallout
//            callout.fill(limit, lang.limits)
//            return callout
//        }
//        return null
//    }

//    private fun limitAreaInfo(latLng: LatLng): LimitArea? {
//        val features = map.queryRenderedFeatures(
//            map.projection.toScreenLocation(latLng),
//            *layers.limits.toTypedArray()
//        )
//        features.map { f ->
//            limitAreaAdapter.readOpt(gson.toJson(f.properties()))?.let { return it }
//        }
//        return null
//    }

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

    private fun show(latLng: Point, popup: PopupContent) {
//        map.map
    }

//    private suspend fun showCallout(latLng: Point, callout: BubbleLayout) {
//        val bitmap = calloutBitmap(callout)
//        style.addImage(CalloutImageName, bitmap)
//        val calloutPointFeature =
//            Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
//        style.getSourceAs<GeoJsonSource>(CalloutSourceId)?.setGeoJson(calloutPointFeature)
//        val maybeLayer = style.getLayerAs<SymbolLayer>(CalloutLayerId)
//        if (maybeLayer != null) {
//            maybeLayer.setProperties(*calloutProps)
//        } else {
//            val layer = SymbolLayer(CalloutLayerId, CalloutSourceId).withProperties(*calloutProps)
//            style.addLayer(layer)
//        }
//        calloutImages[CalloutImageName] = bitmap
//        calloutViews[CalloutImageName] = callout
//    }

//    private suspend fun calloutBitmap(callout: BubbleLayout): Bitmap {
//        return withContext(Dispatchers.IO) {
//            toBitmap(callout)
//        }
//    }

//    private fun toBitmap(callout: BubbleLayout): Bitmap {
//        val display = activity.resources.displayMetrics
//        val displayWidth = display.widthPixels
//        val widthSpec = View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.AT_MOST)
//        val heightSpec =
//            View.MeasureSpec.makeMeasureSpec(display.heightPixels, View.MeasureSpec.AT_MOST)
//        callout.measure(widthSpec, heightSpec)
//
//        val measuredWidth = callout.measuredWidth
//        val measuredHeight = callout.measuredHeight
//
//        val w = min(measuredWidth, displayWidth)
//        // In the middle, I guess.
//        callout.arrowPosition = 1.0f * w / 2 - 5
//        val h = measuredHeight
//        callout.layout(0, 0, w, h)
//        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
//        bitmap.eraseColor(Color.TRANSPARENT)
//        val canvas = Canvas(bitmap)
//        callout.draw(canvas)
//        return bitmap
//    }

    fun clear() {
        style.getLayer(CalloutLayerId)?.let {
            style.removeStyleLayer(it.layerId)
        }
        style.getSource(CalloutSourceId)?.let {
            style.removeStyleSource(it.sourceId)
        }
    }
}
