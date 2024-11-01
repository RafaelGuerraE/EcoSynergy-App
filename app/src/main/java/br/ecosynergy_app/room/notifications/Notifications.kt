package br.ecosynergy_app.room.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notifications(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val type: String?,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val teamId: String?,
    val inviteId: String?,
    val read: Boolean
)