package br.ecosynergy_app.room.teams

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Teams::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["userId", "teamId"]
)
data class Members(
    val userId: Int,
    val teamId: Int,
    val role: String,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
    val createdAt: String
)