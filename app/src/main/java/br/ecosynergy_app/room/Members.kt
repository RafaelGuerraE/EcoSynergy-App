package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Members(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val role: String,
    val teamId: Int
)