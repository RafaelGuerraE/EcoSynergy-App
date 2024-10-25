package br.ecosynergy_app.room.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.HomeActivity
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.user.UserDao
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    lateinit var userDao: UserDao

    override fun onCreate() {
        super.onCreate()
        userDao = AppDatabase.getDatabase(applicationContext).userDao()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Checa se a mensagem tem uma carga de notificação
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        sendTokenToServer(token)  // Enviar o novo token para o backend
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

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

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            val userId = userDao.getUserId()  // Consultar o Room Database
            if (userId != null) {
                val tokenRequest = FcmTokenRequest(userId, token)

                RetrofitClient.userService.updateFcmToken(tokenRequest).enqueue(object : retrofit2.Callback<Void> {
                    override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d("FCM", "Token successfully sent to the server.")
                        } else {
                            Log.e("FCM", "Failed to send token: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                        Log.e("FCM", "Error sending token to the server", t)
                    }
                })
            } else {
                Log.e("FCM", "User ID not found in local database")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()  // Cancela a coroutine quando o serviço for destruído
    }

}
