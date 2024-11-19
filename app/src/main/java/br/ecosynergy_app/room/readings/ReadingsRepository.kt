package br.ecosynergy_app.room.readings

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReadingsRepository(
    private val mq7ReadingsDao: MQ7ReadingsDao,
    private val mq135ReadingsDao: MQ135ReadingsDao,
    private val fireReadingsDao: FireReadingsDao
) {
    suspend fun insertMQ7Readings(readings: List<MQ7Reading>) = mq7ReadingsDao.insertAll(readings)

    suspend fun insertMQ135Readings(readings: List<MQ135Reading>) = mq135ReadingsDao.insertAll(readings)

    suspend fun insertFireReadings(readings: List<FireReading>) = fireReadingsDao.insertAll(readings)

    suspend fun getMQ7ReadingsByTeamHandle(teamHandle: String): List<MQ7Reading> {
        return withContext(Dispatchers.IO) { mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)}
    }

    suspend fun getMQ135ReadingsByTeamHandle(teamHandle: String): List<MQ135Reading> {
        return withContext(Dispatchers.IO) { mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)}
    }

    suspend fun getFireReadingsByTeamHandle(teamHandle: String): List<FireReading> {
        return withContext(Dispatchers.IO) { fireReadingsDao.getReadingsByTeamHandle(teamHandle)}
    }

    suspend fun deleteReadingsForTeam(teamHandle: String) {
        mq7ReadingsDao.deleteTeamReadings(teamHandle)
        mq135ReadingsDao.deleteTeamReadings(teamHandle)
        fireReadingsDao.deleteTeamReadings(teamHandle)
    }

    suspend fun getAggregatedReadingsForDate(teamHandle: String, date: Date): Map<String, Float> {
        return withContext(Dispatchers.IO) {
            val emissionsPerDay = mutableMapOf<String, Float>()

            val dayStart = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayStart)

            emissionsPerDay[formattedDate] = 0f

            val mq7Readings = mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)
            val mq135Readings = mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)

            mq7Readings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && isSameDay(readingDate, dayStart)) {
                    emissionsPerDay[formattedDate] = (emissionsPerDay[formattedDate] ?: 0f) + reading.value
                }
            }

            mq135Readings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && isSameDay(readingDate, dayStart)) {
                    emissionsPerDay[formattedDate] = (emissionsPerDay[formattedDate] ?: 0f) + reading.value
                }
            }

            Log.d("ReadingsRepository", "$emissionsPerDay")
            emissionsPerDay
        }
    }

    suspend fun getAggregatedReadingsForLastWeek(teamHandle: String): Map<String, Float> {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val emissionsPerDay = mutableMapOf<String, Float>()

            val today = Date()
            calendar.time = today
            val sevenDaysAgo = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, -6)
            }.time

            for (i in 0..6) {
                calendar.time = today
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                emissionsPerDay[date] = 0f
            }

            val mq7Readings = mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)
            val mq135Readings = mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)

            mq7Readings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && readingDate.after(sevenDaysAgo)) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(readingDate)
                    emissionsPerDay[formattedDate] = (emissionsPerDay[formattedDate] ?: 0f) + reading.value
                }
            }

            mq135Readings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && readingDate.after(sevenDaysAgo)) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(readingDate)
                    emissionsPerDay[formattedDate] = (emissionsPerDay[formattedDate] ?: 0f) + reading.value
                }
            }

            val orderedEmissions = LinkedHashMap<String, Float>()
            val dateKeys = emissionsPerDay.keys.sorted()

            for (key in dateKeys) {
                orderedEmissions[key] = emissionsPerDay[key] ?: 0f
            }

            Log.d("ReadingsRepository","$orderedEmissions")
            orderedEmissions
        }
    }

    suspend fun getAggregatedReadingsForLastMonth(teamHandle: String): Map<String, Float> {
        return withContext(Dispatchers.IO) {
            val emissionsPerDay = mutableMapOf<String, Float>()

            val calendar = Calendar.getInstance()
            val today = Date()
            calendar.time = today

            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.time

            while (calendar.time <= today) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                emissionsPerDay[date] = 0f
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val mq7Readings = mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)
            val mq135Readings = mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)

            mq7Readings.forEach { reading ->
                aggregateReading(emissionsPerDay, reading.timestamp, reading.value, startOfMonth)
            }

            mq135Readings.forEach { reading ->
                aggregateReading(emissionsPerDay, reading.timestamp, reading.value, startOfMonth)
            }

            Log.d("ReadingsRepository","$emissionsPerDay")
            emissionsPerDay
        }
    }

    suspend fun getAggregatedReadingsForLastYear(teamHandle: String): Map<String, Float> {
        return withContext(Dispatchers.IO) {
            val emissionsPerMonth = mutableMapOf<String, Float>()

            val calendar = Calendar.getInstance()
            val today = Date()
            calendar.time = today

            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val startOfYear = calendar.time

            while (calendar.time <= today) {
                val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
                emissionsPerMonth[date] = 0f
                calendar.add(Calendar.MONTH, 1)
            }

            val mq7Readings = mq7ReadingsDao.getReadingsByTeamHandle(teamHandle)
            val mq135Readings = mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)

            mq7Readings.forEach { reading ->
                aggregateReading(emissionsPerMonth, reading.timestamp, reading.value, startOfYear, format = "yyyy-MM")
            }

            mq135Readings.forEach { reading ->
                aggregateReading(emissionsPerMonth, reading.timestamp, reading.value, startOfYear, format = "yyyy-MM")
            }

            Log.d("ReadingsRepository","$emissionsPerMonth")
            emissionsPerMonth
        }
    }

    private fun aggregateReading(map: MutableMap<String, Float>, timestamp: String, value: Float, startDate: Date, format: String = "yyyy-MM-dd") {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val readingDate = dateFormat.parse(timestamp)

        if (readingDate != null && readingDate.after(startDate)) {
            val formattedDate = SimpleDateFormat(format, Locale.getDefault()).format(readingDate)
            map[formattedDate] = (map[formattedDate] ?: 0f) + value
        }
    }

    suspend fun getFireReadingsByHour(teamHandle: String, date: Date): Map<Int, Int> {
        return withContext(Dispatchers.IO) {
            val fireReadings = fireReadingsDao.getReadingsByTeamHandle(teamHandle)
            val readingsByHour = mutableMapOf<Int, Int>()

            fireReadings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && isSameDay(readingDate, date)) {
                    val calendar = Calendar.getInstance().apply { time = readingDate }
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    readingsByHour[hour] = readingsByHour.getOrDefault(hour, 0) + 1
                }
            }

            for (hour in 0..23) {
                readingsByHour.putIfAbsent(hour, 0)
            }

            readingsByHour
        }
    }

    suspend fun getMQ135ReadingsByHour(teamHandle: String, date: Date): Map<Int, Int> {
        return withContext(Dispatchers.IO) {
            val mq135Readings = mq135ReadingsDao.getReadingsByTeamHandle(teamHandle)
            val readingsByHour = mutableMapOf<Int, Int>()

            mq135Readings.forEach { reading ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val readingDate = dateFormat.parse(reading.timestamp)

                if (readingDate != null && isSameDay(readingDate, date)) {
                    val calendar = Calendar.getInstance().apply { time = readingDate }
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    readingsByHour[hour] = readingsByHour.getOrDefault(hour, 0) + 1
                }
            }

            for (hour in 0..23) {
                readingsByHour.putIfAbsent(hour, 0)
            }

            readingsByHour
        }
    }


    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
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
