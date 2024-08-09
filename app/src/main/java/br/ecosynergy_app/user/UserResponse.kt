package br.ecosynergy_app.user

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
    val accountNonExpired: Boolean,
    val accountNonLocked : Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean
)
