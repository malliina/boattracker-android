package com.malliina.boattracker

import com.malliina.boattracker.ui.callouts.MarineAdapter
import com.malliina.boattracker.ui.callouts.MarineSymbol
import com.malliina.boattracker.ui.callouts.MarineSymbolRaw
import com.malliina.boattracker.ui.callouts.NonEmptyString
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

class Json {
    companion object {
        val instance = Json()
        val moshi: Moshi get() = instance.moshi
    }

    val moshi: Moshi = Moshi.Builder().add(PrimitiveAdapter()).add(MarineAdapter()).build()
}

class PrimitiveAdapter {
    @FromJson fun push(s: String): PushToken = PushToken(s)
    @FromJson fun email(s: String): Email = Email(s)
    @FromJson fun id(s: String): IdToken = IdToken(s)
    @FromJson fun user(s: String): Username = Username(s)
    @FromJson fun track(s: String): TrackName = TrackName(s)
    @FromJson fun title(s: String): TrackTitle = TrackTitle(s)
    @FromJson fun boat(s: String): BoatName = BoatName(s)
    @FromJson fun token(s: String): BoatToken = BoatToken(s)
    @FromJson fun speed(s: Double): Speed = Speed(s)
    @ToJson fun writeSpeed(s: Speed): Double = s.knots
    @FromJson fun distance(d: Double): Distance = Distance(d)
    @FromJson fun duration(d: Double): Duration = Duration(d)
    @FromJson fun temp(t: Double): Temperature = Temperature(t)
    @FromJson fun language(l: String): Language = Language.parse(l)
    @FromJson fun url(url: String): FullUrl = FullUrl.build(url) ?: throw JsonDataException("Value '$url' cannot be converted to FullUrl")
    @FromJson fun nonEmpty(s: String): NonEmptyString? {
        val trimmed = s.trim()
        return if (trimmed.isEmpty()) null else NonEmptyString(trimmed)
    }
    @FromJson fun marineSymbol(raw: MarineSymbolRaw): MarineSymbol {
        return raw.toSymbol()
    }
}
