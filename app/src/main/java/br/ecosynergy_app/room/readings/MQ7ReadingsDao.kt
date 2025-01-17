package br.ecosynergy_app.room.readings

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MQ7ReadingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<MQ7Reading>)

    @Query("SELECT * FROM mq7_readings ORDER BY timestamp DESC")
    suspend fun getAllReadings(): List<MQ7Reading>

    @Query("SELECT * FROM mq7_readings WHERE teamHandle = :teamHandle ORDER BY timestamp DESC")
    fun getReadingsByTeamHandle(teamHandle: String): List<MQ7Reading>

    @Query("DELETE FROM mq7_readings WHERE teamHandle = :teamHandle")
    suspend fun deleteTeamReadings(teamHandle: String)

    @Query("DELETE FROM mq7_readings")
    suspend fun deleteAllReadings()

    @Query("SELECT MAX(timestamp) FROM mq7_readings WHERE teamHandle = :teamHandle")
    suspend fun getLatestTimestamp(teamHandle: String): String?
}