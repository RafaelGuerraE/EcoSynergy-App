package br.ecosynergy_app.home

data class UpdateRequest(
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String
)
