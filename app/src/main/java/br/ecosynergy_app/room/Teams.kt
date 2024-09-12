package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Teams(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val handle: String,
    val name: String,
    val description: String,
    val activityId: Int,
    val activityName: String,
    val activitySector: String,
    val timeZone: String,
    val createdAt: String,
    val updatedAt: String,
    val linksRel: String,
    val linksHref: String
)