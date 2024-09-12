package br.ecosynergy_app.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MembersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Members)

    @Update
    suspend fun updateMember(member: Members)

    @Delete
    suspend fun deleteMember(member: Members)

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Int): Members?

    @Query("SELECT * FROM members")
    fun getAllMembers(): LiveData<List<Members>>
}
