package br.ecosynergy_app.room.teams

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.ecosynergy_app.teams.viewmodel.TeamsResponse

@Entity(tableName = "teams",
    indices = [Index(value = ["handle"], unique = true)])
data class Teams(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val handle: String,
    val name: String,
    val description: String,
    val activityId: Int,
    val activityName: String,
    val activitySector: String,
    val dailyGoal: Double,
    val weeklyGoal: Double,
    val monthlyGoal: Double,
    val annualGoal: Double,
    val timeZone: String,
    val createdAt: String,
    val updatedAt: String,
    val linksRel: String,
    val linksHref: String
)

fun TeamsResponse.toTeam(): Teams {
    return Teams(
        id = this.id,
        handle = this.handle,
        name = this.name,
        description = this.description,
        activityId = this.activity.id,
        activityName = this.activity.name,
        activitySector = this.activity.sector,
        dailyGoal = this.dailyGoal,
        weeklyGoal = this.weeklyGoal,
        monthlyGoal = this.monthlyGoal,
        annualGoal = this.annualGoal,
        timeZone = this.timeZone,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        linksRel = this.links?.self?.href ?: "Unknown", // Default value in case of null
        linksHref = this.links?.self?.href ?: "Unknown"
    )
}