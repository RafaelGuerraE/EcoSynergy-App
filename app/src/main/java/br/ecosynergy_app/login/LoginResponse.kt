package br.ecosynergy_app.login

data class LoginResponse(
    val username: String,
    val authenticated: Boolean,
    val created: String,
    val expiration: String,
    val accessToken: String,
    val refreshToken: String
)
