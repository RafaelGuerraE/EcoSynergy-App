package br.ecosynergy_app.room.notifications

class NotificationsRepository(private val notificationsDao: NotificationsDao) {

    suspend fun addNotification(notification: Notifications){
        notificationsDao.addNotification(notification)
    }

    suspend fun updateNotification(notification: Notifications) {
        notificationsDao.updateNotification(notification)
    }

    suspend fun deleteNotification(notification: Notifications) {
        notificationsDao.deleteNotification(notification)
    }

    suspend fun deleteAllNotifications(){
        notificationsDao.deleteAllNotifications()
    }

    suspend fun getNotificationsByType(type: String): List<Notifications> {
        return notificationsDao.getNotificationsByType(type)
    }

    suspend fun getAllNotifications(): List<Notifications>{
        return notificationsDao.getAllNotifications()
    }

    suspend fun markAsRead(notificationId: Int) {
        notificationsDao.markAsRead(notificationId)
    }
}