package br.ecosynergy_app.room.readings

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.ecosynergy_app.room.teams.Teams

@Entity(
    tableName = "mq135_readings",
    foreignKeys = [ForeignKey(
        entity = Teams::class,
        parentColumns = ["handle"],
        childColumns = ["teamHandle"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["teamHandle"])]
)
data class MQ135Reading(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val teamHandle: String,
    val value: Float,
    val timestamp: String
)