package br.ecosynergy_app.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotification(notification: Notifications)

    @Update
    suspend fun updateNotification(notification: Notifications)

    @Delete
    suspend fun deleteNotification(notification: Notifications)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("SELECT * FROM notifications WHERE type = :type")
    suspend fun getNotificationsByType(type: String): List<Notifications>

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<Notifications>
}