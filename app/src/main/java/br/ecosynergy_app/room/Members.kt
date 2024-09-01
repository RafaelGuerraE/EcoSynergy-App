package br.ecosynergy_app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Members(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val username: String,
    val email: String,
    val age: Int
)