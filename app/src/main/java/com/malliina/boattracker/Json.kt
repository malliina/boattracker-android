package com.malliina.boattracker

import com.malliina.boattracker.Json.Companion.fail
import com.malliina.boattracker.ui.callouts.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

class Json {
    companion object {
        val instance = Json()
        val moshi: Moshi get() = instance.moshi

        fun fail(message: String): Nothing =
            throw JsonDataException(message)
    }

    val moshi: Moshi = Moshi.Builder()
        .add(PrimitiveAdapter())
        .add(MarineAdapter())
        .build()
}

class PrimitiveAdapter {
    @FromJson fun push(s: String): PushToken = PushToken(s)
    @ToJson fun writePush(s: PushToken): String = s.token
    @FromJson fun email(s: String): Email = Email(s)
    @ToJson fun writeEmail(s: Email): String = s.email
    @FromJson fun id(s: String): IdToken = IdToken(s)
    @ToJson fun writeId(s: IdToken): String = s.token
    @FromJson fun user(s: String): Username = Username(s)
    @ToJson fun writeUser(s: Username): String = s.name
    @FromJson fun track(s: String): TrackName = TrackName(s)
    @ToJson fun writeTrack(s: TrackName): String = s.name
    @FromJson fun title(s: String): TrackTitle = TrackTitle(s)
    @ToJson fun writeTitle(s: TrackTitle): String = s.name
    @FromJson fun boat(s: String): BoatName = BoatName(s)
    @ToJson fun writeBoat(s: BoatName): String = s.name
    @FromJson fun token(s: String): BoatToken = BoatToken(s)
    @ToJson fun writeToken(s: BoatToken): String = s.token
    @FromJson fun speed(s: Double): Speed = Speed(s)
    @ToJson fun writeSpeed(s: Speed): Double = s.knots
    @FromJson fun distance(d: Double): Distance = Distance(d)
    @ToJson fun writeDistance(d: Distance): Double = d.meters
    @FromJson fun duration(d: Double): Duration = Duration(d)
    @ToJson fun writeDuration(d: Duration): Double = d.seconds
    @FromJson fun temp(t: Double): Temperature = Temperature(t)
    @ToJson fun writeTemp(t: Temperature): Double = t.celsius
    @FromJson fun language(l: String): Language = Language.parse(l)
    @ToJson fun writeLanguage(l: Language): String = l.code
    @FromJson fun url(url: String): FullUrl = FullUrl.build(url) ?: fail("Value '$url' cannot be converted to FullUrl")
    @ToJson fun writeUrl(url: FullUrl): String = url.url
    @FromJson fun nonEmpty(s: String): NonEmptyString? {
        val trimmed = s.trim()
        return if (trimmed.isEmpty()) null else NonEmptyString(trimmed)
    }
    @ToJson fun writeNonEmpty(s: NonEmptyString): String = s.value
    @FromJson fun marineSymbol(raw: MarineSymbolRaw): MarineSymbol = raw.toSymbol()
    @ToJson fun writeMarineSymbol(raw: MarineSymbol): MarineSymbolRaw = fail("todo")
    @FromJson fun fairwayArea(raw: FairwayAreaJson): FairwayArea = raw.toFairway()
    @ToJson fun writeFairwayArea(raw: FairwayArea): FairwayAreaJson = fail("todo")
    @FromJson fun limitArea(raw: LimitAreaJson): LimitArea = raw.toLimitArea()
    @ToJson fun writeLimitArea(raw: LimitArea): LimitAreaJson = fail("todo")
    @FromJson fun trafficSign(raw: TrafficSignRaw): TrafficSign = raw.toSign()
    @ToJson fun writeTrafficSign(raw: TrafficSign): TrafficSignRaw = fail("todo")
}
