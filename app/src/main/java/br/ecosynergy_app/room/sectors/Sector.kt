package br.ecosynergy_app.room.sectors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sectors")
data class Sector(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String
)
