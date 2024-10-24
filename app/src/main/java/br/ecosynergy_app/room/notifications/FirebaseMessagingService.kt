package br.ecosynergy_app.room.notifications

import android.util.Log
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Lógica para exibir notificações
        // remoteMessage.data ou remoteMessage.notification.body

        override fun onNewToken(token: String) {
            Log.d("FCM", "New token: $token")
            sendTokenToServer(token)  // Envia o novo token para o backend
        }
    }
}