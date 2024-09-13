package br.ecosynergy_app.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(readings: Readings)

    @Query("DELETE FROM readings")
    suspend fun deleteAllReadings()

    @Query("SELECT * FROM readings")
    suspend fun getAllReadings(): List<Readings>

    @Query("SELECT * FROM readings WHERE teamHandle = :teamHandle")
    suspend fun getReadingsByTeamHandle(teamHandle: String): Readings?

    @Query("DELETE FROM readings WHERE teamHandle = :teamHandle")
    suspend fun deleteTeamReadings(teamHandle: String)
}