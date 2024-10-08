package br.ecosynergy_app.room.sectors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SectorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<Sector>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<Activity>)

    @Transaction
    @Query("SELECT * FROM sectors WHERE id = :sectorId")
    suspend fun getSectorWithActivities(sectorId: Int): SectorWithActivities

    @Transaction
    @Query("SELECT * FROM sectors")
    suspend fun getAllSectorsWithActivities(): List<SectorWithActivities>

    @Query("DELETE FROM sectors")
    suspend fun deleteAllSectors()

    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()

    @Transaction
    suspend fun deleteAllSectorsAndActivities() {
        deleteAllActivities()
        deleteAllSectors()
    }
}
