package br.ecosynergy_app.room.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ecosynergy_app.R
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.teams.DashboardActivity
import br.ecosynergy_app.teams.TeamInfoActivity
import br.ecosynergy_app.home.fragments.NotificationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationsRepository: NotificationsRepository

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        notificationsRepository = NotificationsRepository(db.notificationsDao())
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "No title"
        val body = remoteMessage.data["body"] ?: "No body"
        val type = remoteMessage.data["type"]
        val inviteId = remoteMessage.data["inviteId"]?.toInt()
        val teamId = remoteMessage.data["teamId"]?.toInt()

        Log.d("MyFirebaseService", "${remoteMessage.data}")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date(System.currentTimeMillis()))

        val targetActivity = when(type){
            "fire"-> NotificationActivity::class.java
            "invite" -> NotificationActivity::class.java
            else -> HomeActivity::class.java
        }

        sendNotification(title, body, targetActivity, type, teamId, inviteId, timestamp)

        saveNotificationToDatabase(type, title, body, teamId, inviteId, timestamp)
    }

    private fun sendNotification(title: String, messageBody: String, targetActivity: Class<*>, type: String?, teamId: Int?, inviteId: Int?, timestamp: String) {
        val notificationIntent = Intent(this, targetActivity).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (type == "invite") {
                putExtra("TYPE", type)
                putExtra("TEAM_ID", teamId)
                putExtra("INVITE_ID", inviteId)
                putExtra("TIMESTAMP", timestamp)
            }
        }

        val pendingIntent = TaskStackBuilder.create(this).apply {
            addNextIntentWithParentStack(Intent(this@MyFirebaseMessagingService, HomeActivity::class.java))
            addNextIntent(notificationIntent)
        }.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_transparent_globe)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)


        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    private fun saveNotificationToDatabase(type: String?, title: String, body: String, teamId: Int?, inviteId: Int?, timestamp: String) {
        val notification = Notifications(
            id = 0,
            type = type,
            title = title,
            subtitle = body,
            timestamp = timestamp,
            teamId = teamId,
            inviteId = inviteId,
            read = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.addNotification(notification)
            Log.d("MyFirebaseService", "Notification Stored: $notification")
        }
    }

    override fun onNewToken(token: String) {
        Log.d("MyFirebaseService", "Refreshed token: $token")
    }
}
