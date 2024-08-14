package br.ecosynergy_app

data class ApiError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val path: String
)