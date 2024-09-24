package br.ecosynergy_app.room

class ReadingsRepository(private val readingsDao: ReadingsDao) {

    suspend fun insertReading(readings: Readings) {
        readingsDao.insertReading(readings)
    }

    suspend fun deleteAllReadings() {
        readingsDao.deleteAllReadings()
    }

    suspend fun getAllReadings(): List<Readings> {
        return readingsDao.getAllReadings()
    }

    suspend fun getReadingsByTeamHandle(teamHandle: String): Readings {
        return readingsDao.getReadingsByTeamHandle(teamHandle)
    }

    suspend fun getReadingsBySensor(sensor: String): List<Readings> {
        return readingsDao.getReadingsBySensor(sensor)
    }

    suspend fun insertReadings(readings: List<Readings>) {
        readingsDao.insertAllReadings(readings)
    }

    suspend fun deleteTeamReadings(teamHandle: String) {
        readingsDao.deleteTeamReadings(teamHandle)
    }
}