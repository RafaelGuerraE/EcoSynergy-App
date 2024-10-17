package br.ecosynergy_app.signup.viewmodel

data class CreateUserRequest(
    val username: String,
    val fullName: String,
    val email: String,
    val password: String,
    val gender: String,
    val nationality: String)

data class CreateUserResponse(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val password: String,
    val gender: String,
    val nationality: String,
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean,
    val roles: String,
    val authorities: String,
    val _links: Links
) {
    data class Links(
        val self: Href
    ) {
        data class Href(
            val href: String
        )
    }
}
