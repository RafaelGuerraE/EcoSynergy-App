package br.ecosynergy_app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.ecosynergy_app.home.HomeActivity

class NotificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("NOTIFICATION_CLICKED", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            homeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "main")
            .setSmallIcon(R.drawable.logo_transparent_globe)
            .setContentTitle("Bom dia, Rafael!")
            .setContentText("Novo relatório disponível, clique aqui para ver!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .setAutoCancel(true) // Close the notification when clicked
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_NOT_STICKY
        }
        with(NotificationManagerCompat.from(this)) {
            notify(1, notification)
        }

        stopSelf()

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val name = "Main Channel"
        val descriptionText = "Channel for EcoSynergy App notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("main", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
