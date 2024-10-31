package br.ecosynergy_app.room.invites

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.ecosynergy_app.room.teams.Teams

@Entity(
    tableName = "invites",
    foreignKeys = [ForeignKey(
        entity = Teams::class,
        parentColumns = ["id"],
        childColumns = ["teamId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["teamId"])]
)
data class Invites (
    @PrimaryKey(autoGenerate = false) val id: Int,
    val senderId: Int,
    val senderUsername: String,
    val recipientId: Int,
    val recipientUsername: String,
    val teamId: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String)