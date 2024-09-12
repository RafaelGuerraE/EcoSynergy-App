package br.ecosynergy_app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Members::class, User::class, Teams::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun membersDao(): MembersDao
    abstract fun userDao(): UserDao
    abstract fun teamsDao(): TeamsDao

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

