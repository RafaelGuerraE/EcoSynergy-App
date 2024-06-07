package br.ecosynergy_app.register

data class CreateUserResponse(
    val id: Int,
    val userName: String,
    val fullName: String,
    val email: String,
    val password: String,
    val gender: String,
    val nationality: String,
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean,
    val username: String,
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
