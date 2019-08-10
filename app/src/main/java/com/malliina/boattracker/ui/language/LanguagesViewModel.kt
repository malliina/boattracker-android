package com.malliina.boattracker.ui.language

import android.app.Application
import androidx.lifecycle.*
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.Language
import com.malliina.boattracker.UserSettings
import com.malliina.boattracker.backend.BoatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class LanguagesViewModelFactory(val app: Application, val token: IdToken): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LanguagesViewModel(app, token) as T
    }
}

class LanguagesViewModel(val app: Application, val token: IdToken): AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val language: MutableLiveData<Language> by lazy {
        MutableLiveData<Language>().also { it.value = UserSettings.instance.currentLanguage }
    }

    fun getLanguage(): LiveData<Language> {
        return language
    }


    fun changeLanguage(to: Language) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            try {
                val msg = http.changeLanguage(to)
                UserSettings.instance.userLanguage = to
                language.postValue(to)
                Timber.i(msg.message)
            } catch(e: Exception) {
                Timber.e(e, "Failed to change language.")
            }
        }
    }
}
