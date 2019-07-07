package com.malliina.boattracker

import android.os.Parcelable
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.regex.Pattern

data class PushToken(val token: String) {
    override fun toString(): String = token
}

data class Email(val email: String) {
    override fun toString(): String = email
}

data class IdToken(val token: String) {
    override fun toString(): String = token
}

data class UserInfo(val email: Email, val idToken: IdToken)

data class Username(val name: String) {
    override fun toString(): String = name
}

data class TrackName(val name: String) {
    override fun toString(): String = name
}

data class BoatName(val name: String) {
    override fun toString() = name
}

data class BoatToken(val token: String) {
    override fun toString() = token
}

data class Speed(val knots: Double) {
    companion object {
        fun format(s: Speed): String = "%.2f kn".format(s.knots)
    }

    fun formatted(): String = format(this)

    override fun toString() = formatted()
}

data class Distance(val meters: Double) {
    companion object {
        fun millis(mm: Double): Distance = Distance(mm / 1000)
    }

    override fun toString() = formatted()

    fun formatted(): String = "%.2f km".format(meters / 1000)
}

data class Duration(val seconds: Double) {
    companion object {
        private fun formatSeconds(seconds: Long): String {
            val s = seconds % 60
            val m = (seconds / 60) % 60
            val h = (seconds / (60 * 60)) % 24
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        }

        fun seconds(seconds: Long): Duration = Duration(seconds.toDouble())
    }

    override fun toString() = formatted()

    fun formatted(): String = formatSeconds(seconds.toLong())
}

data class Temperature(val celsius: Double) {
    override fun toString() = formatted()

    fun formatted() = "%.2f ℃".format(celsius)
}

data class Boat(val id: Int, val name: BoatName, val token: BoatToken, val addedMillis: Long) {
    companion object {
        fun parse(json: JSONObject): Boat = Boat(
            json.getInt("id"),
            BoatName(json.getString("name")),
            BoatToken(json.getString("token")),
            json.getLong("addedMillis")
        )
    }
}

data class BoatUser(val id: Int,
                    val username: Username,
                    val email: String?,
                    val boats: List<Boat>,
                    val addedMillis: Long) {
    companion object {
        fun parse(json: JSONObject): BoatUser {
            val boatsArr = json.getJSONArray("boats")
            val boats = mutableListOf<Boat>()
            for(i in 0 until boatsArr.length()) {
                val item = boatsArr.getJSONObject(i)
                boats.add(Boat.parse(item))
            }
            return BoatUser(
                json.getInt("id"),
                Username(json.getString("username")),
                json.optString("email", null),
                boats,
                json.getLong("addedMillis")
            )
        }
    }
}

data class Timing(val date: String,
                  val time: String,
                  val dateTime: String,
                  val millis: Long) {
    companion object {
        fun parse(json: JSONObject): Timing {
            return Timing(
                json.getString("date"),
                json.getString("time"),
                json.getString("dateTime"),
                json.getLong("millis")
            )
        }
    }
}

data class Times(val start: Timing, val end: Timing, val range: String) {
    companion object {
        fun parse(json: JSONObject): Times {
            return Times(
                Timing.parse(json.getJSONObject("start")),
                Timing.parse(json.getJSONObject("end")),
                json.getString("range")
            )
        }
    }
}

data class TrackRef(val trackName: TrackName,
                    val boatName: BoatName,
                    val times: Times,
                    val distance: Distance,
                    val topSpeed: Speed?,
                    val avgSpeed: Speed?,
                    val avgWaterTemp: Temperature?,
                    val duration: Duration,
                    val topPoint: CoordBody) {
//    fun formatStart() = start.take(10)

    companion object {
        fun parse(json: JSONObject): TrackRef {
            val top = json.optDouble("topSpeed")
            val avg = json.optDouble("avgSpeed")
            val temp = json.optDouble("avgWaterTemp")
            return TrackRef(
                TrackName(json.getString("trackName")),
                BoatName(json.getString("boatName")),
                Times.parse(json.getJSONObject("times")),
                Distance(json.getDouble("distanceMeters")),
                if (top.isNaN()) null else Speed(top),
                if (avg.isNaN()) null else Speed(avg),
                if (temp.isNaN()) null else Temperature(temp),
                Duration.seconds(json.getLong("duration")),
                CoordBody.parse(json.getJSONObject("topPoint"))
            )
        }

        fun parseList(json: JSONObject): List<TrackRef> {
            val list = ArrayList<TrackRef>()
            val arr = json.getJSONArray("tracks")
            for(i in 0..(arr.length() -1)) {
                val item = arr.getJSONObject(i)
                list.add(parse(item))
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
            Distance.millis(json.getDouble("depth")),
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
            for(i in 0 until coordsArray.length()) {
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

@Parcelize
data class FullUrl(val proto: String, val hostAndPort: String, val uri: String): Parcelable {
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

        fun parse(input: String): FullUrl {
            return build(input) ?: throw JSONException("Value $input cannot be converted to FullUrl")
        }

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
            for(i in 0 until arr.length()) {
                errors.add(SingleError.parse(arr.getJSONObject(i)))
            }
            return Errors(errors)
        }
    }
}

data class ResponseException(val error: VolleyError): Exception("Invalid response", error.cause) {
    val response: NetworkResponse = error.networkResponse

    fun errors(): Errors {
        val response = response
        val charset = Charset.forName(HttpHeaderParser.parseCharset(response.headers, "UTF-8"))
        val json = JSONObject(String(response.data, charset))
        return Errors.parse(json)
    }

    fun isTokenExpired(): Boolean = errors().errors.any { e -> e.key == "token_expired" }
}
