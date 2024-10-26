package br.ecosynergy_app.room.readings

import androidx.lifecycle.LiveData

class ReadingsRepository(
    private val mq7ReadingsDao: MQ7ReadingsDao,
    private val mq135ReadingsDao: MQ135ReadingsDao,
    private val fireReadingsDao: FireReadingsDao
) {
    suspend fun insertMQ7Readings(readings: List<MQ7Reading>) = mq7ReadingsDao.insertAll(readings)

    suspend fun insertMQ135Readings(readings: List<MQ135Reading>) = mq135ReadingsDao.insertAll(readings)

    suspend fun insertFireReadings(readings: List<FireReading>) = fireReadingsDao.insertAll(readings)

    suspend fun getAllMQ7Readings() = mq7ReadingsDao.getAllReadings()

    suspend fun getAllMQ135Readings() = mq135ReadingsDao.getAllReadings()

    suspend fun getAllFireReadings() = fireReadingsDao.getAllReadings()

    fun getMQ7ReadingsByTeamHandle(teamHandle: String): List<MQ7Reading> {
        return mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)
    }

    fun getMQ135ReadingsByTeamHandle(teamHandle: String): List<MQ135Reading> {
        return mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)
    }

    fun getFireReadingsByTeamHandle(teamHandle: String): List<FireReading> {
        return fireReadingsDao.getReadingsByTeamHandle(teamHandle)
    }

    suspend fun deleteReadingsForTeam(teamHandle: String) {
        mq7ReadingsDao.deleteTeamReadings(teamHandle)
        mq135ReadingsDao.deleteTeamReadings(teamHandle)
        fireReadingsDao.deleteTeamReadings(teamHandle)
    }

    suspend fun getLatestTimestamp(sensor: String, teamHandle: String): String {
        return when(sensor) {
            "MQ7" -> mq7ReadingsDao.getLatestTimestamp(teamHandle) ?: "1970-01-01T00:00:00Z"
            "MQ135" -> mq135ReadingsDao.getLatestTimestamp(teamHandle) ?: "1970-01-01T00:00:00Z"
            "FIRE" -> fireReadingsDao.getLatestTimestamp(teamHandle) ?: "1970-01-01T00:00:00Z"
            else -> "1970-01-01T00:00:00Z"
        }
    }
}
