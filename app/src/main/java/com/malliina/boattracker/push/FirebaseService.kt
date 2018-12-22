package com.malliina.boattracker.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.malliina.boattracker.PushToken
import com.malliina.boattracker.R
import com.malliina.boattracker.backend.Env
import com.malliina.boattracker.backend.HttpClient
import com.malliina.boattracker.ui.map.MapActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class FirebaseService: FirebaseMessagingService() {
    init {
        Timber.tag(javaClass.simpleName)
    }
    private val scope = CoroutineScope(Job())

    override fun onNewToken(token: String) {
        Timber.i("Got token $token")
        // Or save the token locally and enable notifications later on-demand?
        scope.launch {
            val http = HttpClient.getInstance(application)
            try {
                val response = http.postData(Env.baseUrl.append("/users/notifications"), enablePayload(PushToken(token)))
                Timber.i("Enabled notifications, response was $response")
            } catch(e: Exception) {
                Timber.e(e, "Failed to enable notifications")
            }

        }
    }

    private fun enablePayload(token: PushToken): JSONObject = JSONObject().also { obj ->
        obj.put("token", token.token)
        obj.put("device", "android")
    }

    private fun disablePayload(token: PushToken): JSONObject = JSONObject().also { obj ->
        obj.put("token", token.token)
        obj.put("device", "android")
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        Timber.i("Received remote message.")
        message?.let { msg ->
            val title = msg.data["title"] ?: "Title here"
            val text = msg.data["message"] ?: "Message here"
            val intent = Intent(this, MapActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val channelId = "fcm_default_channel"
            val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_trophy)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pending)
                .setAutoCancel(true)
            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Channel title", NotificationManager.IMPORTANCE_DEFAULT)
                manager.createNotificationChannel(channel)
            }
            manager.notify(0, builder.build())
        }

    }
}
