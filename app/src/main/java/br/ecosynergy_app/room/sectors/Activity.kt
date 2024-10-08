package br.ecosynergy_app.room.sectors

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sectorId"])]
)
data class Activity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val sectorId: Int // Foreign key that points to the Sector
)
