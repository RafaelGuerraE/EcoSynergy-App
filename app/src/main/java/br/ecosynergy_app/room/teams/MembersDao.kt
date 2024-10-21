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

    @Query("DELETE FROM members WHERE userId = :userId AND teamId = :teamId")
    suspend fun deleteMember(userId: Int, teamId: Int)

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("SELECT * FROM members WHERE userId = :userId AND teamId = :teamId")
    suspend fun getMember(userId: Int, teamId: Int): Members

    @Query("SELECT * FROM members WHERE teamId = :teamId")
    suspend fun getMembersByTeamId(teamId: Int): List<Members>

    @Query("SELECT * FROM members")
    suspend fun getAllMembers(): List<Members>

    @Query("UPDATE members SET role = :newRole WHERE userId = :userId AND teamId = :teamId")
    suspend fun updateUserRole(userId: Int, teamId: Int, newRole: String)
}

