package br.ecosynergy_app.room.readings

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import br.ecosynergy_app.room.teams.Teams

@Entity(
    tableName = "readings",
    foreignKeys = [ForeignKey(
        entity = Teams::class,
        parentColumns = ["handle"],
        childColumns = ["teamHandle"],
        onDelete = ForeignKey.CASCADE
    )])
data class Readings (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val sensor: String,
    val teamHandle: String,
    val value: Float,
    val timestamp: String
)