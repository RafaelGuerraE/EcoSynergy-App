package br.ecosynergy_app.room.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ecosynergy_app.R
import br.ecosynergy_app.home.AppSettingsActivity
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.user.NotificationActivity
import br.ecosynergy_app.user.UserSettingsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val type = remoteMessage.data["type"] ?: "default"

            val targetActivity = when (type) {
                "invite" -> NotificationActivity::class.java
                "fire" -> DashboardActivity::class.java
                else -> HomeActivity::class.java
            }
            sendNotification(it.title, it.body, targetActivity)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?, targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_transparent_globe)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Default Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
    }
}
