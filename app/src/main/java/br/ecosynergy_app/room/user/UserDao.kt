package br.ecosynergy_app.room.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): User

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE user SET " +
            "username = :newUsername, " +
            "fullName = :newFullName, " +
            "email = :newEmail, " +
            "gender = :newGender, " +
            "nationality = :newNationality, " +
            "accessToken = :newAccessToken, " +
            "refreshToken = :newRefreshToken " +
            "WHERE id = :userId")
    suspend fun updateUser(
        userId: Int,
        newUsername: String,
        newFullName: String,
        newEmail: String,
        newGender: String,
        newNationality: String,
        newAccessToken: String,
        newRefreshToken: String
    )

    @Query("SELECT id FROM User LIMIT 1")
    suspend fun getUserId(): Int
}