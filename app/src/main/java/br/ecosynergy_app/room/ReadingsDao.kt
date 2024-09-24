package br.ecosynergy_app.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: Readings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReadings(readings: List<Readings>)

    @Query("DELETE FROM readings")
    suspend fun deleteAllReadings()

    @Query("SELECT * FROM readings")
    suspend fun getAllReadings(): List<Readings>

    @Query("SELECT * FROM readings WHERE teamHandle = :teamHandle")
    suspend fun getReadingsByTeamHandle(teamHandle: String): Readings

    @Query("SELECT * FROM readings WHERE sensor = :sensor")
    suspend fun getReadingsBySensor(sensor: String): List<Readings>

    @Query("DELETE FROM readings WHERE teamHandle = :teamHandle")
    suspend fun deleteTeamReadings(teamHandle: String)
}