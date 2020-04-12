package com.malliina.boattracker.ui.map

import android.graphics.Color
import com.malliina.boattracker.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface AISMessages {
    fun onVessels(vessels: List<Vessel>, map: MapboxMap)
}

class VesselsRenderer(val conf: AisLayers, val icons: IconsConf, val lang: Lang) : AISMessages {
    companion object {
        val vesselAdapter: JsonAdapter<Vessel> = Json.moshi.adapter(Vessel::class.java)
    }

    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val maxTrailLength = 100
    private val vesselHistory: MutableMap<Mmsi, List<Vessel>> = mutableMapOf()

    override fun onVessels(vessels: List<Vessel>, map: MapboxMap) {
        Timber.i("Rendering ${vessels.size} vessels...")
        vessels.forEach { v ->
            val history = vesselHistory[v.mmsi] ?: emptyList()
            val tail = (listOf(v) + history).take(maxTrailLength)
            vesselHistory[v.mmsi] = tail
        }
        map.style?.let { style ->
            uiScope.launch {
                vessels.forEach { vessel -> updateVessel(vessel, style) }
                val trails = vesselHistory.values.map { history ->
                    LineString.fromLngLats(history.map { toPoint(it.coord) })
                }
                val geos = MultiLineString.fromLineStrings(trails)
                val vesselTrailsId = "trails-vessels"
                val trailsSrc = style.getSourceAs<GeoJsonSource>(vesselTrailsId)
                if (trailsSrc == null) {
                    val src = GeoJsonSource(vesselTrailsId, geos)
                    style.addSource(src)
                    val trailsLayer = LineLayer(vesselTrailsId, src.id).withProperties(
                        PropertyFactory.lineWidth(1f),
                        PropertyFactory.lineColor(Color.BLACK)
                    )
                    style.addLayer(trailsLayer)
                } else {
                    trailsSrc.setGeoJson(geos)
                }
            }
        }
    }

    private fun updateVessel(vessel: Vessel, style: Style) {
        val id = "vessel-${vessel.mmsi}"
        val point = toPoint(vessel.coord)
        val src = style.getSourceAs<GeoJsonSource>(id)
        val json = Json.toGson(vessel, vesselAdapter)
        val feature = Feature.fromGeometry(point, json)
        if (src == null) {
            style.addSource(GeoJsonSource(id, feature))
            val layer = SymbolLayer(id, id).withProperties(
                PropertyFactory.iconImage(icons.boat),
                PropertyFactory.iconSize(MapFragment.BoatIconSize),
                PropertyFactory.iconRotate(Expression.get(Vessel.headingKey)),
                PropertyFactory.iconRotationAlignment("map")
            )
            style.addLayer(layer)
        } else {
            src.setGeoJson(feature)
        }
    }

    private fun toPoint(c: Coord) = Point.fromLngLat(c.lng, c.lat)
}
