package br.ecosynergy_app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ecosynergy_app.room.notifications.Notifications
import br.ecosynergy_app.room.notifications.NotificationsDao
import br.ecosynergy_app.room.readings.Readings
import br.ecosynergy_app.room.readings.ReadingsDao
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersDao
import br.ecosynergy_app.room.teams.Teams
import br.ecosynergy_app.room.teams.TeamsDao
import br.ecosynergy_app.room.user.User
import br.ecosynergy_app.room.user.UserDao

@Database(
    entities = [Members::class, User::class, Teams::class, Readings::class, Notifications::class],
    version= 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun membersDao(): MembersDao
    abstract fun userDao(): UserDao
    abstract fun teamsDao(): TeamsDao
    abstract fun readingsDao(): ReadingsDao
    abstract fun notificationsDao(): NotificationsDao

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

