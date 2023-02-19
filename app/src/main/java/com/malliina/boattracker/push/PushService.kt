package com.malliina.boattracker.push

import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.PushToken
import com.malliina.boattracker.TokenPayload
import com.malliina.boattracker.backend.Adapters
import com.malliina.boattracker.backend.BoatHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class PushService(val http: BoatHttpClient) {
    companion object {
        @Volatile
        private var INSTANCE: PushService? = null
        fun getInstance(http: BoatHttpClient) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: PushService(http).also {
                        INSTANCE = it
                    }
            }
    }

    private val scope = CoroutineScope(Job())

    var firebase: FirebaseMessaging = FirebaseMessaging.getInstance()
    var isNotificationsEnabled: Boolean = firebase.isAutoInitEnabled

    fun toggleNotifications(isOn: Boolean) {
        firebase.isAutoInitEnabled = isOn
        firebase.token.addOnSuccessListener { t ->
            val token = PushToken(t)
            register(token, isEnabled = isOn)
        }
    }

    fun update(token: PushToken) {
        if (isNotificationsEnabled) {
            register(token, isEnabled = true)
        }
    }

    // https://docs.boat-tracker.com/endpoints/
    private fun register(token: PushToken, isEnabled: Boolean) {
        val path = if (isEnabled) "/users/notifications" else "/users/notifications/disable"
        scope.launch {
            try {
                val response = http.post(path, TokenPayload(token, "android"), Adapters.token, Adapters.message)
                Timber.i("Toggled notifications to $isEnabled, response was '$response'. Token was '$token'.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle notifications.")
            }
        }
    }
}
