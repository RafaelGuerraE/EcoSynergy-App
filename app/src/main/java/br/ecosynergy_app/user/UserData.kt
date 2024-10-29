package br.ecosynergy_app.user

data class UserResponse(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
    val createdAt: String,
    val accountNonExpired: Boolean,
    val accountNonLocked : Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean
)

data class ForgotRequest(
    val email: String,
    val password: String
)