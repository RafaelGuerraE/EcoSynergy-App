package br.ecosynergy_app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ecosynergy_app.room.invites.Invites
import br.ecosynergy_app.room.invites.InvitesDao
import br.ecosynergy_app.room.notifications.Notifications
import br.ecosynergy_app.room.notifications.NotificationsDao
import br.ecosynergy_app.room.readings.FireReading
import br.ecosynergy_app.room.readings.FireReadingsDao
import br.ecosynergy_app.room.readings.MQ135Reading
import br.ecosynergy_app.room.readings.MQ135ReadingsDao
import br.ecosynergy_app.room.readings.MQ7Reading
import br.ecosynergy_app.room.readings.MQ7ReadingsDao
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersDao
import br.ecosynergy_app.room.teams.Teams
import br.ecosynergy_app.room.teams.TeamsDao
import br.ecosynergy_app.room.user.User
import br.ecosynergy_app.room.user.UserDao

@Database(
    entities = [Members::class, User::class, Teams::class, MQ7Reading::class, MQ135Reading::class, FireReading::class, Notifications::class, Invites::class],
    version= 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun membersDao(): MembersDao
    abstract fun userDao(): UserDao
    abstract fun teamsDao(): TeamsDao
    abstract fun mq7ReadingsDao(): MQ7ReadingsDao
    abstract fun mq135ReadingsDao(): MQ135ReadingsDao
    abstract fun fireReadingsDao(): FireReadingsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun invitesDao(): InvitesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecosynergy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

