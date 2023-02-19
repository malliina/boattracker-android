package com.malliina.boattracker.ui.boats

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.push.PushService
import com.malliina.boattracker.ui.BoatViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * See [StackOverflow answer](https://stackoverflow.com/a/46704702)
 */
class BoatsViewModel(app: Application) : BoatViewModel(app) {
    var notificationsEnabled: Boolean = FirebaseMessaging.getInstance().isAutoInitEnabled

    private val boatsData: MutableLiveData<BoatUser> = MutableLiveData<BoatUser>().also {
        loadBoats()
    }
    val boats: LiveData<BoatUser> = boatsData

    fun toggleNotifications(isOn: Boolean) {
        PushService.getInstance(http).toggleNotifications(isOn)
    }

    private fun loadBoats() {
        uiScope.launch {
            try {
                boatsData.value = http.me()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load boats.")
            }
        }
    }
}
