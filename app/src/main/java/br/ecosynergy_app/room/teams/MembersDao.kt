package br.ecosynergy_app.room.teams

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MembersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Members)

    @Update
    suspend fun updateMember(member: Members)

    @Delete
    suspend fun deleteMember(member: Members)

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("SELECT * FROM members WHERE userId = :userId AND teamId = :teamId")
    suspend fun getMember(userId: Int, teamId: Int): Members

    @Query("SELECT * FROM members WHERE teamId = :teamId")
    suspend fun getMembersByTeamId(teamId: Int): List<Members>

    @Query("SELECT * FROM members")
    suspend fun getAllMembers(): List<Members>
}

