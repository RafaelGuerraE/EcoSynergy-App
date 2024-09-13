package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.ecosynergy_app.user.UserResponse

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val gender: String,
    val nationality: String,
    val accessToken: String?,
    val refreshToken: String?
)

fun UserResponse.toUser(accessToken: String?, refreshToken: String?): User {
    return User(
        id = this.id,
        username = this.username,
        fullName = this.fullName,
        email = this.email,
        gender = this.gender,
        nationality = this.nationality,
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}