package com.malliina.boattracker

import com.malliina.boattracker.ui.map.MapState

class UserSettings {
    companion object {
        val instance = UserSettings()
    }

    var conf: ClientConf? = null
    private val languages: Languages? get() = conf?.languages
    var profile: BoatUser? = null
        set(value)  {
            field = value
            userLanguage = value?.language
        }
    var userLanguage: Language? = null
    val currentLanguage: Language get() = userLanguage ?: Language.English
    val lang: Lang? get() = languages?.let { selectLanguage(currentLanguage, it) }

    var mapState: MapState? = null
    val user: UserInfo? get() = mapState?.user
    val token: IdToken? get() = user?.idToken

    private fun selectLanguage(lang: Language, available: Languages): Lang {
        return when (lang) {
            Language.Finnish -> available.finnish
            Language.Swedish -> available.swedish
            Language.English -> available.english
        }
    }

    fun clear() {
        conf = null
        profile = null
        mapState = null
    }
}
