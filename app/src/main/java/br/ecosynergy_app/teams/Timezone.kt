package br.ecosynergy_app.teams

data class Timezone (
    val value: String,
    val abbr: String,
    val offset: Float,
    val isdst: Boolean,
    val text: String,
    val utc: List<String>
)