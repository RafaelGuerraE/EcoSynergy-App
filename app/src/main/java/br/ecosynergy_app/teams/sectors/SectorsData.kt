package br.ecosynergy_app.teams.sectors

data class ActivitiesRequest(
    val id: Int
)


data class SectorsResponseItem(
    val id: Int,
    val name: String,
    val activities: List<ActivityData>
)

data class ActivityData(
    val id: Int,
    val name: String
)


data class ActivitiesResponseItem(
    val id: Int,
    val name: String,
    val sector: String
)

class SectorsResponse : ArrayList<SectorsResponseItem>()

class ActivitiesResponse : ArrayList<ActivitiesResponseItem>()