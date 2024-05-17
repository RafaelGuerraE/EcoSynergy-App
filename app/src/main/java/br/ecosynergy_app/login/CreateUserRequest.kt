package br.ecosynergy_app.login

data class CreateUserRequest(
    val userName: String,
    val fullName: String,
    val email: String,
    val password: String,
    val gender: String,
    val nationality: String)
