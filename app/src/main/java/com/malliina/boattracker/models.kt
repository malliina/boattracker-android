package com.malliina.boattracker

import android.os.Parcelable
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.backend.read
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import java.nio.charset.Charset
import java.util.regex.Pattern

interface Primitive {
    val value: String
}

data class PushToken(val token: String) {
    override fun toString(): String = token
}

@Parcelize
data class Email(val email: String): Parcelable, Primitive {
    override val value: String get() = email
    override fun toString(): String = email
}

@Parcelize
data class IdToken(val token: String): Parcelable, Primitive {
    companion object {
        const val key = "idToken"
    }

    override val value: String get() = token
    override fun toString(): String = token
}

data class UserInfo(val email: Email, val idToken: IdToken)

data class Username(val name: String): Primitive {
    override val value: String get() = name
    override fun toString(): String = name
}

@Parcelize
data class TrackName(val name: String): Parcelable, Primitive {
    companion object {
        const val key = "trackName"
    }
    override val value: String get() = name
    override fun toString(): String = name
}

data class TrackTitle(val name: String): Primitive {
    override val value: String get() = name
    override fun toString(): String = name
}

data class BoatName(val name: String): Primitive {
    override val value: String get() = name
    override fun toString() = name
}

data class BoatToken(val token: String): Primitive {
    override val value: String get() = token
    override fun toString() = token
}

data class Speed(val knots: Double) {
    companion object {
        val key = "speed"
        fun format(s: Speed): String = "%.2f kn".format(s.knots)
    }

    fun formatted(): String = format(this)

    override fun toString() = formatted()
}

data class Distance(val meters: Double) {
    fun formatted(): String = "%.2f km".format(meters / 1000)

    override fun toString() = formatted()
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

data class Boat(val id: Int, val name: BoatName, val token: BoatToken, val addedMillis: Long)

enum class Language(val code: String) {
    Swedish("sv-SE"), Finnish("fi-FI"), English("en-US");

    companion object {
        fun parse(s: String): Language {
            return when (s) {
                Swedish.code -> Swedish
                Finnish.code -> Finnish
                English.code -> English
                else -> English
            }
        }
    }
}

data class ChangeLanguage(val language: String)

data class BoatUser(val id: Int,
                    val username: Username,
                    val email: String?,
                    val language: Language,
                    val boats: List<Boat>,
                    val addedMillis: Long)

data class UserResponse(val user: BoatUser)

data class Timing(val date: String,
                  val time: String,
                  val dateTime: String,
                  val millis: Long)

data class Times(val start: Timing, val end: Timing, val range: String)

data class TrackRef(val trackName: TrackName,
                    val trackTitle: TrackTitle?,
                    val boatName: BoatName,
                    val times: Times,
                    val distanceMeters: Distance,
                    val topSpeed: Speed?,
                    val avgSpeed: Speed?,
                    val avgWaterTemp: Temperature?,
                    val duration: Duration,
                    val topPoint: CoordBody)

data class TracksResponse(val tracks: List<TrackRef>)

data class CoordBody(val coord: Coord,
                     val boatTime: String,
                     val boatTimeMillis: Long,
                     val speed: Speed,
                     val depthMeters: Distance,
                     val waterTemp: Temperature)

data class Coord(val lat: Double, val lng: Double) {
    fun latLng(): LatLng = LatLng(lat, lng)
    fun point(): Point = Point.fromLngLat(lng, lat)
}

data class CoordsData(val from: TrackRef, val coords: List<CoordBody>)

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

data class SimpleMessage(val message: String)

data class SingleError(val key: String, val message: String)

data class Errors(val errors: List<SingleError>)

data class ResponseException(val error: VolleyError): Exception("Invalid response", error.cause) {
    val response: NetworkResponse = error.networkResponse

    fun errors(): Errors {
        val response = response
        val charset = Charset.forName(HttpHeaderParser.parseCharset(response.headers, "UTF-8"))
        val str = String(response.data, charset)
        return BoatClient.errorsAdapter.read(str)
    }

    fun isTokenExpired(): Boolean = errors().errors.any { e -> e.key == "token_expired" }
}
