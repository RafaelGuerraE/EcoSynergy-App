package br.ecosynergy_app.room.teams

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TeamsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Teams)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()

    @Query("SELECT * FROM teams")
    suspend fun getAllTeams(): List<Teams>

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: Int): Teams

    @Query("SELECT * FROM teams WHERE handle = :handle")
    suspend fun getTeamByHandle(handle: String): Teams

    @Query("DELETE FROM teams WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: Int)

    @Update
    suspend fun updateTeam(team: Teams)
}