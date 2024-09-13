package br.ecosynergy_app.teams

    data class ActivitiesRequest(
        val id : Int
    )

    data class ActivitiesResponse(
        val id : Int,
        val name: String,
        val sector: String
    )
