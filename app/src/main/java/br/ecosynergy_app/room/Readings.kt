package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class Readings (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val teamHandle: String,
    val value: Float,
    val timestamp: String
)