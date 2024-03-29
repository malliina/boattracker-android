package com.malliina.boattracker.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.malliina.boattracker.BoatApp
import com.malliina.boattracker.PushToken
import com.malliina.boattracker.R
import com.malliina.boattracker.ui.map.MapFragment
import timber.log.Timber

class FirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Timber.i("Got token $token")
        PushService.getInstance((application as BoatApp).http).update(PushToken(token))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received remote message.")
        val title = message.data["title"] ?: ""
        val text = message.data["message"] ?: ""
        val intent = Intent(this, MapFragment::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val channelId = "fcm_default_channel"
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_trophy)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Channel title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
        manager.notify(0, builder.build())
    }
}
