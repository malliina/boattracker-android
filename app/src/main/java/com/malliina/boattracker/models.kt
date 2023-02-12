package com.malliina.boattracker

import android.os.Parcelable
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.backend.RequestConf
import com.malliina.boattracker.backend.read
import com.malliina.boattracker.ui.callouts.MeasuredCoord
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import timber.log.Timber
import java.nio.charset.Charset
import java.util.regex.Pattern

interface Primitive {
    val value: String
}

data class PushToken(val token: String) {
    override fun toString(): String = token
}

data class Email(val email: String) : Primitive {
    override val value: String get() = email
    override fun toString(): String = email
}

data class IdToken(val token: String) : Primitive {
    override val value: String get() = token
    override fun toString(): String = token
}

data class UserInfo(val email: Email, val idToken: IdToken)

data class Username(val name: String) : Primitive {
    override val value: String get() = name
    override fun toString(): String = name
}

data class Mmsi(override val value: String) : Primitive {
    override fun toString(): String = value
}

@Parcelize
data class TrackName(val name: String) : Primitive, Parcelable {
    override val value: String get() = name
    override fun toString(): String = name
}

data class TrackTitle(val name: String) : Primitive {
    override val value: String get() = name
    override fun toString(): String = name
}

data class BoatName(val name: String) : Primitive {
    override val value: String get() = name
    override fun toString() = name
}

data class BoatToken(val token: String) : Primitive {
    override val value: String get() = token
    override fun toString() = token
}

data class Speed(val knots: Double) : Comparable<Speed> {
    companion object {
        const val key = "speed"
        const val knotInKmh = 1.852
        fun format(s: Speed): String = "%.2f kn".format(s.knots)
    }

    var inKmh = knots * knotInKmh
    fun formatKmh(): String = "%.1f km/h".format(inKmh)
    fun formatKmhInt(): String = "%.0f km/h".format(inKmh)
    fun formatKn(): String = format(this)
    fun formatted(): String = formatKn()

    override fun compareTo(other: Speed): Int = compareValuesBy(this, other, { it.knots })
    override fun toString() = formatted()
}

fun Double.kmh(): Speed = Speed(this / Speed.knotInKmh)

data class Distance(val meters: Double) {
    fun formatKilometers(): String = "%.2f km".format(meters / 1000)
    fun formatMeters(): String = "%.1f m".format(meters)

    override fun toString() = formatKilometers()
}

fun Double.meters(): Distance = Distance(this)

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

@JsonClass(generateAdapter = true)
data class Boat(val id: Int, val name: BoatName, val token: BoatToken, val addedMillis: Long)

enum class Language(val code: String) {
    Swedish("sv-SE"), Finnish("fi-FI"), English("en-US");

    companion object {
        fun parse(s: String): Language = when (s) {
            Swedish.code -> Swedish
            Finnish.code -> Finnish
            English.code -> English
            else -> English
        }
    }
}

@JsonClass(generateAdapter = true)
data class ChangeLanguage(val language: String)

@JsonClass(generateAdapter = true)
data class ChangeTitle(val title: TrackTitle)

@JsonClass(generateAdapter = true)
data class BoatUser(
    val id: Int,
    val username: Username,
    val email: String?,
    val language: Language,
    val boats: List<Boat>,
    val addedMillis: Long
)

@JsonClass(generateAdapter = true)
data class UserResponse(val user: BoatUser)

@JsonClass(generateAdapter = true)
data class Timing(
    val date: String,
    val time: String,
    val dateTime: String,
    val millis: Long
)

@JsonClass(generateAdapter = true)
data class Times(val start: Timing, val end: Timing, val range: String)

@JsonClass(generateAdapter = true)
data class TrackRef(
    val trackName: TrackName,
    val trackTitle: TrackTitle?,
    val boatName: BoatName,
    val times: Times,
    val distanceMeters: Distance,
    val topSpeed: Speed?,
    val avgSpeed: Speed?,
    val avgWaterTemp: Temperature?,
    val duration: Duration,
    val topPoint: CoordBody
)

@JsonClass(generateAdapter = true)
data class TrackResponse(val track: TrackRef)

@JsonClass(generateAdapter = true)
data class TracksResponse(val tracks: List<TrackRef>)

@JsonClass(generateAdapter = true)
data class CoordBody(
    override val coord: Coord,
    val boatTime: String,
    val boatTimeMillis: Long,
    override val speed: Speed,
    val depthMeters: Distance,
    val waterTemp: Temperature
) : MeasuredCoord

@JsonClass(generateAdapter = true)
data class Coord(val lat: Double, val lng: Double) {
    fun latLng(): LatLng = LatLng(lat, lng)
    fun point(): Point = Point.fromLngLat(lng, lat)

    companion object {
        fun fromPoint(point: Point) = Coord(point.latitude(), point.longitude())
    }
}

@JsonClass(generateAdapter = true)
data class CoordsData(val from: TrackRef, val coords: List<CoordBody>)

@JsonClass(generateAdapter = true)
data class CoordsMessage(val body: CoordsData)

@JsonClass(generateAdapter = true)
data class Vessel(
    val mmsi: Mmsi,
    val name: String,
    val heading: Double?,
//    val shipType: Int,
    val coord: Coord,
    val sog: Speed,
    val cog: Double,
    val draft: Distance,
    val destination: String?,
    val eta: Double,
    val time: Timing
) {
    companion object {
        const val headingKey = "heading"
    }
}

@JsonClass(generateAdapter = true)
data class VesselData(val vessels: List<Vessel>)

@JsonClass(generateAdapter = true)
data class VesselMessage(val body: VesselData)

interface Stats {
    val label: String
    val distance: Distance
    val duration: Duration
    val days: Long
}

@JsonClass(generateAdapter = true)
data class MonthlyStats(
    override val label: String,
    val year: Int,
    val month: Int,
    val trackCount: Long,
    override val distance: Distance,
    override val duration: Duration,
    override val days: Long
) : Stats

@JsonClass(generateAdapter = true)
data class YearlyStats(
    override val label: String,
    val year: Int,
    val trackCount: Long,
    override val distance: Distance,
    override val duration: Duration,
    override val days: Long,
    val monthly: List<MonthlyStats>
) : Stats

@JsonClass(generateAdapter = true)
data class StatsResponse(val yearly: List<YearlyStats>)

data class FullUrl(val proto: String, val hostAndPort: String, val uri: String) {
    private val host: String = hostAndPort.takeWhile { c -> c != ':' }
    private val protoAndHost = "$proto://$hostAndPort"
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
            return build(input)
                ?: throw JSONException("Value $input cannot be converted to FullUrl")
        }

        fun build(input: String): FullUrl? {
            val m = pattern.matcher(input)
            return if (m.find() && m.groupCount() == 3) {
                m.group(1)?.let { proto ->
                    m.group(2)?.let { host -> m.group(3)?.let { uri -> FullUrl(proto, host, uri) } }
                }
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

@JsonClass(generateAdapter = true)
data class SimpleMessage(val message: String)

@JsonClass(generateAdapter = true)
data class SingleError(val key: String, val message: String) {
    companion object {
        fun backend(message: String) = SingleError("backend", message)
    }
}

@JsonClass(generateAdapter = true)
data class Errors(val errors: List<SingleError>) {
    companion object {
        fun input(message: String) = single("input", message)
        fun single(key: String, message: String): Errors = Errors(listOf(SingleError(key, message)))
    }
}

data class ResponseException(val error: VolleyError, val req: RequestConf) :
    Exception("Invalid response", error.cause) {
    private val url = req.url
    private val response: NetworkResponse? = error.networkResponse

    fun errors(): Errors {
        return if (response != null) {
            val response = response
            try {
                val charset =
                    Charset.forName(HttpHeaderParser.parseCharset(response.headers, "UTF-8"))
                val str = String(response.data, charset)
                BoatClient.Adapters.errors.read(str)
            } catch (e: Exception) {
                val msg = "Unable to parse response from '$url'."
                Timber.e(e, msg)
                Errors.input(msg)
            }
        } else {
            Errors.single("network", "Network error from '$url'.")
        }
    }

    fun isTokenExpired(): Boolean = errors().errors.any { e -> e.key == "token_expired" }
}
