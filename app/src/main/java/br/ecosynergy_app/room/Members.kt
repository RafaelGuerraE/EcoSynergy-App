package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import br.ecosynergy_app.room.Teams

@Entity(
    tableName = "members",
    foreignKeys = [ForeignKey(
        entity = Teams::class,
        parentColumns = ["id"],
        childColumns = ["teamId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Members(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val role: String,
    val teamId: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
)
