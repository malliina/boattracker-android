package com.malliina.boattracker.ui.language

import android.app.Application
import androidx.lifecycle.*
import com.malliina.boattracker.Language
import com.malliina.boattracker.UserSettings
import com.malliina.boattracker.backend.BoatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class LanguagesViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LanguagesViewModel(app) as T
    }
}

class LanguagesViewModel(val app: Application) : AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val languageData = MutableLiveData<Language>().apply {
        value = UserSettings.instance.currentLanguage
    }
    val language: LiveData<Language> = languageData

    fun changeLanguage(to: Language) {
        UserSettings.instance.token?.let { token ->
            val http = BoatClient.build(app, token)
            uiScope.launch {
                try {
                    val msg = http.changeLanguage(to)
                    UserSettings.instance.userLanguage = to
                    languageData.postValue(to)
                    Timber.i(msg.message)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to change language.")
                }
            }
        }
    }
}
