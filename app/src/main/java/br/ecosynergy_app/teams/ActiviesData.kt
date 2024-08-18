package br.ecosynergy_app.teams

    data class ActivitiesRequest(
        val id : Long
    )

    data class ActivitiesResponse(
        val id : Long,
        val name: String,
        val sector: String
    )
