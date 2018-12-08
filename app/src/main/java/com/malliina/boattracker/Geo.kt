package com.malliina.boattracker

import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Geo {
    companion object {
        val instance = Geo()
    }

    // // https://www.movable-type.co.uk/scripts/latlong.html
    fun bearing(from: LatLng, to: LatLng): Double {
        val dLon = to.longitude - from.longitude
        val y = sin(dLon) * cos(to.latitude)
        val x = cos(from.latitude) * sin(to.latitude) - sin(from.latitude) * cos(to.latitude) * cos(dLon)
        val brng = toDeg(atan2(y, x))
        return 360 - ((brng + 360) % 360)
    }

    private fun toDeg(rad: Double): Double = rad * 180 / kotlin.math.PI
}
