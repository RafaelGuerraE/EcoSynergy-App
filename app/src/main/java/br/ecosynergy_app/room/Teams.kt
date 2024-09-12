package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.ecosynergy_app.teams.ActivitiesResponse
import br.ecosynergy_app.teams.Member
import br.ecosynergy_app.teams.TeamLinks

@Entity(tableName = "teams")
data class Teams(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val handle: String,
    val name: String,
    val description: String,
    val activity: ActivitiesResponse,
    val timeZone: String,
    val createdAt: String,
    val updatedAt: String,
    val members: List<Member>,
    val _links: TeamLinks
)