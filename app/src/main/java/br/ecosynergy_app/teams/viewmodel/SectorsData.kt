package br.ecosynergy_app.teams.viewmodel

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

data class Activity(
    val activities_id: Int,
    val activities_br: String
)

data class Sector(
    val sector: String,
    val sector_id: Int,
    val sector_br: String,
    val activities: List<Activity>
)

