package com.malliina.boattracker.ui.boats

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.backend.BoatClient
import com.malliina.boattracker.push.PushService
import com.malliina.boattracker.ui.BoatViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * See [StackOverflow answer](https://stackoverflow.com/a/46704702)
 */
class BoatsViewModel(app: Application): BoatViewModel(app) {
    var notificationsEnabled: Boolean = FirebaseMessaging.getInstance().isAutoInitEnabled

    private val boatsData: MutableLiveData<BoatUser> = MutableLiveData<BoatUser>().also {
        settings.token?.let {
            loadBoats(it)
        }
    }
    val boats: LiveData<BoatUser> = boatsData

    fun toggleNotifications(isOn: Boolean) {
        PushService.getInstance(app).toggleNotifications(isOn)
    }

    private fun loadBoats(token: IdToken) {
        val http = BoatClient.build(app, token)
        uiScope.launch {
            try {
                val response = http.me()
                boatsData.value = response
            } catch(e: Exception) {
                Timber.e(e, "Failed to load boats. Token was $token")
            }
        }
    }
}
