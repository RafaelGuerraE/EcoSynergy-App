package br.ecosynergy_app.room.readings

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FireReadingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<FireReading>)

    @Query("SELECT * FROM fire_readings ORDER BY timestamp DESC")
    suspend fun getAllReadings(): List<FireReading>

    @Query("SELECT * FROM fire_readings WHERE teamHandle = :teamHandle ORDER BY timestamp DESC")
    fun getReadingsByTeamHandle(teamHandle: String): List<FireReading>

    @Query("DELETE FROM fire_readings WHERE teamHandle = :teamHandle")
    suspend fun deleteTeamReadings(teamHandle: String)

    @Query("DELETE FROM fire_readings")
    suspend fun deleteAllReadings()

    @Query("SELECT MAX(timestamp) FROM fire_readings WHERE teamHandle = :teamHandle")
    suspend fun getLatestTimestamp(teamHandle: String): String?
}