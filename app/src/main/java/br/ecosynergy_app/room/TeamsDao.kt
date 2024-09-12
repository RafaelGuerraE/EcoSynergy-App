package br.ecosynergy_app.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Teams)

    @Query("DELETE FROM teams")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM teams")
    suspend fun getTeams(): Teams?

    @Delete
    suspend fun deleteTeam(team: Teams)
}