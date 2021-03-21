package com.malliina.boattracker

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.malliina.boattracker.backend.BoatClient
import com.squareup.moshi.JsonAdapter
import timber.log.Timber

class UserSettings(private val prefs: SharedPreferences) {
    companion object {
        fun load(ctx: Context): UserSettings {
            val prefs = ctx.getSharedPreferences(
                "com.malliina.boattracker.prefs",
                Context.MODE_PRIVATE
            )
            return UserSettings(prefs)
        }
    }

    var profile: BoatUser? = null
    private val languages: Languages? get() = conf?.languages
    private val userLanguage: Language? get() = profile?.language
    val currentLanguage: Language get() = userLanguage ?: Language.English
    val lang: Lang? get() = languages?.let { selectLanguage(currentLanguage, it) }

    fun changeLanguage(to: Language) {
        profile = profile?.copy(language = to)
    }

    private fun selectLanguage(lang: Language, available: Languages): Lang = when (lang) {
        Language.Finnish -> available.finnish
        Language.Swedish -> available.swedish
        Language.English -> available.english
    }

    private val confKey = "conf-2021-03-21"
    private var cachedConf: ClientConf? = null
    var conf: ClientConf?
        set(value) {
            cachedConf = value
            if (value == null) clear(confKey)
            else save(value, BoatClient.Adapters.conf, confKey)
        }
        get() = cachedConf ?: loadOpt(confKey, BoatClient.Adapters.conf)

    private fun <T> load(key: String, adapter: JsonAdapter<T>, default: T): T {
        return loadOpt(key, adapter) ?: default
    }

    private fun <T> loadOpt(key: String, adapter: JsonAdapter<T>): T? {
        val str = prefs.getString(key, null)
        return str?.let { adapter.fromJson(it) }
    }

    private fun clear(key: String) {
        prefs.edit {
            remove(key)
            Timber.d("Cleared $key.")
        }
    }

    private fun <T> save(item: T, adapter: JsonAdapter<T>, to: String) {
        prefs.edit {
            val json = adapter.toJson(item)
            putString(to, json)
//            Timber.d("Saved $json.")
        }
    }
}
