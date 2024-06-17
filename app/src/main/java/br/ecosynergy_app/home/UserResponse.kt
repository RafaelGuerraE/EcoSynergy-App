package br.ecosynergy_app.home

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val password: String,
    val gender: String,
    val nationality: String,
    val accountNonExpired: Boolean,
    val accountNonLocked : Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean,
    val roles: List<String>
)
