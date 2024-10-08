package br.ecosynergy_app.room.sectors

import androidx.room.Embedded
import androidx.room.Relation

data class SectorWithActivities(
    @Embedded val sector: Sector,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectorId"
    )
    val activities: List<Activity>
)
