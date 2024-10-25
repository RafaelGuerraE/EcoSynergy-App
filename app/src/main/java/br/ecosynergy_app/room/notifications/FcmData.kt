package br.ecosynergy_app.room.notifications

data class FcmTokenRequest(
    val userId: String,  // Assuming you're sending a userId along with the token
    val fcmToken: String
)