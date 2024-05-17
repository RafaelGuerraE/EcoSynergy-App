package br.ecosynergy_app.login

class UserResponse(val user: User)
data class User(
    val id: Long,
    val userName: String,
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
