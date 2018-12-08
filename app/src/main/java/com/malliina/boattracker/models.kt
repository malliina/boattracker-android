package com.malliina.boattracker

import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.regex.Pattern

data class Email(val email: String) {
    override fun toString(): String = email
}

data class IdToken(val token: String) {
    override fun toString(): String = token
}

data class UserInfo(val email: Email, val idToken: IdToken)

data class TrackName(val name: String) {
    override fun toString(): String = name
}

data class Speed(val knots: Double) {
    companion object {
        fun format(s: Speed): String = "%.2f kn".format(s.knots)
    }

    fun formatted(): String = format(this)

    override fun toString() = formatted()
}

data class Distance(val mm: Double) {
    override fun toString() = "$mm"
}

data class Temperature(val celsius: Double) {
    override fun toString() = "$celsius"
}

data class TrackRef(val trackName: TrackName,
                    val boatName: String,
                    val start: String,
                    val distance: Distance,
                    val topSpeed: Speed,
                    val durationSeconds: Long,
                    val topPoint: CoordBody) {
    fun formatDistance() = "%.2f km".format(distance.mm / 1000000)

    fun formatDuration() = formatSeconds(durationSeconds)

    fun formatStart() = start.take(10)

    private fun formatSeconds(seconds: Long): String {
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / (60 * 60)) % 24
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    companion object {
        fun parse(json: JSONObject): TrackRef = TrackRef(
            TrackName(json.getString("trackName")),
            json.getString("boatName"),
            json.getString("start"),
            Distance(json.getDouble("distance")),
            Speed(json.getDouble("topSpeed")),
            json.getLong("duration"),
            CoordBody.parse(json.getJSONObject("topPoint"))
        )

        fun parseList(json: JSONObject): List<TrackRef> {
            val list = ArrayList<TrackRef>()
            val arr = json.getJSONArray("tracks")
            for(i in 0..(arr.length() -1)) {
                val item = arr.getJSONObject(i)
                list.add(parse(item.getJSONObject("track")))
            }
            return list
        }
    }
}

data class CoordBody(val coord: Coord,
                     val boatTime: String,
                     val boatTimeMillis: Long,
                     val speed: Speed,
                     val depth: Distance,
                     val waterTemp: Temperature) {
    companion object {
        fun parse(json: JSONObject): CoordBody = CoordBody(
            Coord.parse(json.getJSONObject("coord")),
            json.getString("boatTime"),
            json.getLong("boatTimeMillis"),
            Speed(json.getDouble("speed")),
            Distance(json.getDouble("depth")),
            Temperature(json.getDouble("waterTemp"))
        )
    }
}

data class Coord(val lat: Double, val lng: Double) {
    fun latLng(): LatLng = LatLng(lat, lng)
    fun point(): Point = Point.fromLngLat(lng, lat)
    companion object {
        fun parse(json: JSONObject): Coord =
            Coord(json.getDouble("lat"), json.getDouble("lng"))
    }
}

data class CoordsData(val from: TrackRef, val coords: List<CoordBody>) {
    companion object {
        fun parse(json: JSONObject): CoordsData {
            val coordsArray = json.getJSONArray("coords")
            val coords = mutableListOf<CoordBody>()
            for(i in 0..(coordsArray.length()-1)) {
                val item = coordsArray.getJSONObject(i)
                coords.add(CoordBody.parse(item))
            }
            return CoordsData(
                TrackRef.parse(json.getJSONObject("from")),
                coords
            )
        }
    }
}

data class FullUrl(val proto: String, val hostAndPort: String, val uri: String) {
    val host = hostAndPort.takeWhile { c -> c != ':' }
    val protoAndHost = "$proto://$hostAndPort"
    val url = "$protoAndHost$uri"

    fun append(more: String) = copy(uri = this.uri + more)

    override fun toString(): String = url

    companion object {
        private val pattern = Pattern.compile("(.+)://([^/]+)(/?.*)")

        fun https(domain: String, uri: String): FullUrl = FullUrl("https", dropHttps(domain), uri)

        fun http(domain: String, uri: String): FullUrl = FullUrl("http", dropHttps(domain), uri)

        fun host(domain: String): FullUrl = FullUrl("https", dropHttps(domain), "")

        fun ws(domain: String, uri: String): FullUrl = FullUrl("ws", domain, uri)

        fun wss(domain: String, uri: String): FullUrl = FullUrl("wss", domain, uri)

        fun build(input: String): FullUrl? {
            val m = pattern.matcher(input)
            return if (m.find() && m.groupCount() == 3) {
                FullUrl(m.group(1), m.group(2), m.group(3))
            } else {
                null
            }
        }

        private fun dropHttps(domain: String): String {
            val prefix = "https://"
            return if (domain.startsWith(prefix)) domain.drop(prefix.length) else domain
        }
    }
}

data class SingleError(val key: String, val message: String) {
    companion object {
        fun parse(json: JSONObject): SingleError = SingleError(
            json.getString("key"),
            json.getString("message")
        )
    }
}

data class Errors(val errors: List<SingleError>) {
    companion object {
        fun parse(json: JSONObject): Errors {
            val errors = ArrayList<SingleError>()
            val arr = json.getJSONArray("errors")
            for(i in 0..(arr.length()-1)) {
                errors.add(SingleError.parse(arr.getJSONObject(i)))
            }
            return Errors(errors)
        }
    }
}

data class ResponseException(val error: VolleyError): Exception("Invalid response", error.cause) {
    val response: NetworkResponse = error.networkResponse

    fun isTokenExpired(): Boolean {
        val response = error.networkResponse
        val charset = Charset.forName(HttpHeaderParser.parseCharset(response.headers, "UTF-8"))
        val json = JSONObject(String(response.data, charset))
        return Errors.parse(json).errors.any { e -> e.key == "token_expired" }
    }
}
