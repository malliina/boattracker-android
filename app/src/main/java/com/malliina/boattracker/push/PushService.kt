package com.malliina.boattracker.push

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.malliina.boattracker.PushToken
import com.malliina.boattracker.ResponseException
import com.malliina.boattracker.backend.Env
import com.malliina.boattracker.backend.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class PushService(ctx: Context) {
    companion object {
        @Volatile
        private var INSTANCE: PushService? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: PushService(context).also {
                        INSTANCE = it
                    }
            }
    }

    private val scope = CoroutineScope(Job())
    private val http = HttpClient.getInstance(ctx.applicationContext)

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
                val response = http.post(Env.baseUrl.append(path), payload(token))
                Timber.i("Toggled notifications to $isEnabled, response was '$response'. Token was '$token'.")
            } catch (e: ResponseException) {
                Timber.e(e, "HTTP errors: ${e.errors()}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle notifications.")
            }
        }
    }

    private fun payload(token: PushToken): JSONObject = JSONObject().also { obj ->
        obj.put("token", token.token)
        obj.put("device", "android")
    }
}
