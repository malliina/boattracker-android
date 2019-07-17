package com.malliina.boattracker.ui.boats

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.push.PushService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * See [StackOverflow answer](https://stackoverflow.com/a/46704702)
 */
class BoatsViewModelFactory(val app: Application, val token: IdToken): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BoatsViewModel(app, token) as T
    }
}

class BoatsViewModel(val app: Application, val token: IdToken): AndroidViewModel(app) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var notificationsEnabled: Boolean = FirebaseMessaging.getInstance().isAutoInitEnabled

    private val boats: MutableLiveData<BoatUser> by lazy {
        MutableLiveData<BoatUser>().also {
            loadBoats(token)
        }
    }

    fun getBoats(): LiveData<BoatUser> {
        return boats
    }

    fun toggleNotifications(isOn: Boolean) {
        PushService.getInstance(app).toggleNotifications(isOn)
    }

    private fun loadBoats(token: IdToken) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            try {
                val response = http.me()
                boats.value = response
            } catch(e: Exception) {
                Timber.e(e, "Failed to load boats. Token was $token")
            }
        }
    }
}
