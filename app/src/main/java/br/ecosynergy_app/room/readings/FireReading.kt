package br.ecosynergy_app.room.readings

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.ecosynergy_app.room.teams.Teams

@Entity(
    tableName = "fire_readings",
    foreignKeys = [ForeignKey(
        entity = Teams::class,
        parentColumns = ["handle"],
        childColumns = ["teamHandle"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["teamHandle"])]
)
data class FireReading(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val teamHandle: String,
    val timestamp: String,
    val fire: Boolean
)