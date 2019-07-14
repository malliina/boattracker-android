package com.malliina.boattracker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
data class ProfileInfo(val email: Email, val token: IdToken, val lang: Lang, val trackName: TrackName?): Parcelable {
    companion object {
        const val key = "profile"
    }
}

@Parcelize
data class Link(val text: String, val url: FullUrl): Parcelable {
    companion object {
        fun parse(json: JSONObject): Link {
            return Link(
                json.getString("text"),
                FullUrl.parse(json.getString("url"))
            )
        }
    }
}

@Parcelize
data class AppAttribution(val title: String,
                          val text: String?,
                          val links: List<Link>): Parcelable {
    companion object {
        fun parse(json: JSONObject): AppAttribution {
            return AppAttribution(
                json.getString("title"),
                json.optString("text", null),
                json.parseList("links") { Link.parse(it) }
            )
        }
    }
}

@Parcelize
data class AttributionInfo(val title: String,
                           val attributions: List<AppAttribution>): Parcelable {
    companion object {
        const val key = "attributions"

        fun parse(json: JSONObject): AttributionInfo {
            return AttributionInfo(
                json.getString("title"),
                json.parseList("attributions") { AppAttribution.parse(it) }
            )
        }
    }
}

@Parcelize
data class Lang(val language: Language, val attributions: AttributionInfo): Parcelable {
    companion object {
        val key = "lang"

        fun parse(json: JSONObject): Lang {
            return Lang(
                Language.parse(json.getString("language")),
                AttributionInfo.parse(json.getJSONObject("attributions"))
            )
        }
    }
}

@Parcelize
data class Languages(val finnish: Lang, val swedish: Lang, val english: Lang, val all: List<Lang>): Parcelable {
    companion object {
        fun parse(json: JSONObject): Languages {
            val all = json.keys().asSequence().map { k ->
                Lang.parse(json.getJSONObject(k))
            }
            return Languages(
                Lang.parse(json.getJSONObject("finnish")),
                Lang.parse(json.getJSONObject("swedish")),
                Lang.parse(json.getJSONObject("english")),
                all.toList()
            )
        }
    }
}

@Parcelize
data class ClientConf(val languages: Languages): Parcelable {
    companion object {
        val key = "conf"

        fun parse(json: JSONObject): ClientConf {
            return ClientConf(
                Languages.parse(json.getJSONObject("languages"))
            )
        }
    }
}

fun JSONArray.toList(): List<JSONObject> {
    val list = mutableListOf<JSONObject>()
    for(i in 0 until this.length()) {
        list.add(this.getJSONObject(i))
    }
    return list
}

fun JSONObject.getJSONList(key: String): List<JSONObject> {
    return this.getJSONArray(key).toList()
}

fun <T> JSONObject.parseList(key: String, parse: (JSONObject) -> T): List<T> {
    return this.getJSONList(key).map { item ->
        parse(item)
    }
}
