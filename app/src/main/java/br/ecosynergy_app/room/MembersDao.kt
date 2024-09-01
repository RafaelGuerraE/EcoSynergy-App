package br.ecosynergy_app.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MembersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Members)

    @Update
    suspend fun update(user: Members)

    @Delete
    suspend fun delete(user: Members)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): Members?

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<Members>>
}
