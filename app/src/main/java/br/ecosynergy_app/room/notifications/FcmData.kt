package br.ecosynergy_app.room.notifications

data class FcmTokenRequest(
    val userId: String,
    val fcmToken: String
)