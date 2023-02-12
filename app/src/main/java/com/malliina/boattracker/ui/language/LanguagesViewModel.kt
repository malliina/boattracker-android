package com.malliina.boattracker.ui.language

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.malliina.boattracker.Language
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.ui.BoatViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class LanguagesViewModel(app: Application) : BoatViewModel(app) {
    private val languageData = MutableLiveData<Language>().apply {
        value = settings.currentLanguage
    }
    val language: LiveData<Language> = languageData

    fun changeLanguage(to: Language) {
        userState.token?.let { token ->
            val http = BoatClient.build(app, token)
            uiScope.launch {
                try {
                    val msg = http.changeLanguage(to)
                    settings.changeLanguage(to)
                    languageData.postValue(to)
                    Timber.i(msg.message)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to change language.")
                }
            }
        }
    }
}
