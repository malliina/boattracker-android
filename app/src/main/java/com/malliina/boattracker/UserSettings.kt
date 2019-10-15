package com.malliina.boattracker

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
        set(value: Language?) {
            field = value
        }
    val currentLanguage: Language get() = userLanguage ?: Language.English
    val lang: Lang? get() = languages?.let { selectLanguage(currentLanguage, it) }

    fun selectLanguage(lang: Language, available: Languages): Lang {
        return when (lang) {
            Language.Finnish -> available.finnish
            Language.Swedish -> available.swedish
            Language.English -> available.english
        }
    }
}
