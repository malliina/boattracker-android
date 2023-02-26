package com.malliina.boattracker.ui.map

import android.graphics.Color
import com.malliina.boattracker.*
import com.mapbox.geojson.*
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconRotationAlignment
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
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
    private val vesselTrailsId = "trails-vessels"
    private val vesselPointsId = "points-vessels"

    override fun onVessels(vessels: List<Vessel>, map: MapboxMap) {
        Timber.i("Rendering ${vessels.size} vessels...")
        vessels.forEach { v ->
            val history = vesselHistory[v.mmsi] ?: emptyList()
            val tail = (listOf(v) + history).take(maxTrailLength)
            vesselHistory[v.mmsi] = tail
        }
        map.getStyle()?.let { style ->
            uiScope.launch {
                updateVessels(vessels, style)
                val trails = vesselHistory.values.map { history ->
                    LineString.fromLngLats(history.map { toPoint(it.coord) })
                }
                val geos = MultiLineString.fromLineStrings(trails)
                val trailsSrc = style.getSourceAs<GeoJsonSource>(vesselTrailsId)
                if (trailsSrc == null) {
                    val src = geoJsonSource(vesselTrailsId) {
                        geometry(geos)
                    }
                    style.addSource(src)
                    val trailsLayer = lineLayer(vesselTrailsId, src.sourceId) {
                        lineWidth(1.0)
                        lineColor(Color.BLACK)
                    }
                    style.addLayer(trailsLayer)
                } else {
                    trailsSrc.geometry(geos)
                }
            }
        }
    }

    private fun updateVessels(vessels: List<Vessel>, style: Style) {
        val features = vessels.map { vessel ->
            val point = toPoint(vessel.coord)
            val json = Json.toGson(vessel, vesselAdapter)
            Feature.fromGeometry(point, json)
        }
        val coll = FeatureCollection.fromFeatures(features)
        val src = style.getSourceAs<GeoJsonSource>(vesselPointsId)
        if (src == null) {
            val source = geoJsonSource(vesselPointsId) {
                featureCollection(coll)
            }
            style.addSource(source)
            val layer = symbolLayer(vesselPointsId, vesselPointsId) {
                iconImage(icons.boat)
                iconSize(MapFragment.BoatIconSize)
                iconRotate(Expression.get(Vessel.headingKey))
                iconRotationAlignment(IconRotationAlignment.MAP)
            }
            style.addLayer(layer)
        } else {
            src.featureCollection(coll)
        }
    }

    fun clear(style: Style) {
        removeSourceAndLayerIfExists(vesselTrailsId, style)
        removeSourceAndLayerIfExists(vesselPointsId, style)
    }

    private fun removeSourceAndLayerIfExists(id: String, style: Style) {
        style.getSourceAs<GeoJsonSource>(id)?.let {
            style.removeStyleLayer(id)
            style.removeStyleSource(id)
        }
    }

    private fun toPoint(c: Coord) = Point.fromLngLat(c.lng, c.lat)
}
