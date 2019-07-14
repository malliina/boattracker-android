package com.malliina.boattracker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
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
            val linksArr = json.getJSONArray("links")
            val links = mutableListOf<Link>()
            for(i in 0 until linksArr.length()) {
                val item = linksArr.getJSONObject(i)
                links.add(Link.parse(item))
            }
            return AppAttribution(
                json.getString("title"),
                json.optString("text", null),
                links
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
            val attrsArr = json.getJSONArray("attributions")
            val attrs = mutableListOf<AppAttribution>()
            for(i in 0 until attrsArr.length()) {
                val item = attrsArr.getJSONObject(i)
                attrs.add(AppAttribution.parse(item))
            }
            return AttributionInfo(
                json.getString("title"),
                attrs
            )
        }
    }
}

@Parcelize
data class Lang(val attributions: AttributionInfo): Parcelable {
    companion object {
        val key = "lang"

        fun parse(json: JSONObject): Lang {
            return Lang(
                AttributionInfo.parse(json.getJSONObject("attributions"))
            )
        }
    }
}

@Parcelize
data class Languages(val finnish: Lang, val swedish: Lang, val english: Lang): Parcelable {
    companion object {
        fun parse(json: JSONObject): Languages {
            return Languages(
                Lang.parse(json.getJSONObject("finnish")),
                Lang.parse(json.getJSONObject("swedish")),
                Lang.parse(json.getJSONObject("english"))
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
