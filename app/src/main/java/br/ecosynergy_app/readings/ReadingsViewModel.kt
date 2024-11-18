package br.ecosynergy_app.readings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.room.readings.FireReading
import br.ecosynergy_app.room.readings.MQ7Reading
import br.ecosynergy_app.room.readings.MQ135Reading
import br.ecosynergy_app.room.readings.ReadingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReadingsViewModel(
    private val service: ReadingsService,
    private val readingsRepository: ReadingsRepository
) : ViewModel() {

    private val _mq7ReadingsByTeamHandle = MutableLiveData<List<MQ7Reading>>()
    val mq7ReadingsByTeamHandle: LiveData<List<MQ7Reading>> get() = _mq7ReadingsByTeamHandle

    private val _mq135ReadingsByTeamHandle = MutableLiveData<List<MQ135Reading>>()
    val mq135ReadingsByTeamHandle: LiveData<List<MQ135Reading>> get() = _mq135ReadingsByTeamHandle

    private val _fireReadingsByTeamHandle = MutableLiveData<List<FireReading>>()
    val fireReadingsByTeamHandle: LiveData<List<FireReading>> get() = _fireReadingsByTeamHandle

    private var _aggregatedReadingsForLastWeek = MutableLiveData<Map<String, Float>>()
    val aggregatedReadingsForLastWeek: LiveData<Map<String, Float>> get() = _aggregatedReadingsForLastWeek

    private var _aggregatedReadingsForDate = MutableLiveData<Map<String, Float>>()
    val aggregatedReadingsForDate: LiveData<Map<String, Float>> get() = _aggregatedReadingsForDate


    private var _aggregatedReadingsForMonth = MutableLiveData<Map<String, Float>>()
    val aggregatedReadingsForMonth: LiveData<Map<String, Float>> get() = _aggregatedReadingsForMonth

    private var _aggregatedReadingsForYear = MutableLiveData<Map<String, Float>>()
    val aggregatedReadingsForYear: LiveData<Map<String, Float>> get() = _aggregatedReadingsForYear

    private val _fireReadingsByHour = MutableLiveData<Map<Int, Int>>()
    val fireReadingsByHour: LiveData<Map<Int, Int>> = _fireReadingsByHour

    private val _mq135ReadingsByHour = MutableLiveData<Map<Int, Int>>()
    val mq135ReadingsByHour: LiveData<Map<Int, Int>> get() = _mq135ReadingsByHour

    private val _isFetchComplete = MutableLiveData<Boolean>()
    val isFetchComplete: LiveData<Boolean> get() = _isFetchComplete

    fun fetchAllReadings(listTeamHandles: List<String>, accessToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val updateTasks = listTeamHandles.flatMap { teamHandle ->
                    listOf(
                        async { updateMQ7Readings(teamHandle, accessToken) },
                        async { updateMQ135Readings(teamHandle, accessToken) },
                        async { updateFireReadings(teamHandle, accessToken) }
                    )
                }
                updateTasks.awaitAll()
                _isFetchComplete.value = true
                Log.d("ReadingsViewModel", "$_isFetchComplete")
                onComplete()
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error fetching all readings", e)
                _isFetchComplete.value = false
            }
        }
    }

    private suspend fun updateMQ7Readings(teamHandle: String, accessToken: String) {
        try {
            Log.d("ReadingsViewModel", "Starting MQ7 updates for teamHandle: $teamHandle")

            val latestTimestamp = readingsRepository.getLatestTimestamp("MQ7", teamHandle)
            val response = service.fetchMq7ReadingsByTeamHandle(teamHandle, "Bearer $accessToken")

            val readings = response.embedded?.readings
            if (readings != null) {
                val newReadings = readings.filter { it.timestamp > latestTimestamp }
                    .map { MQ7Reading(it.id, it.teamHandle, it.value.toFloat(), it.timestamp) }

                readingsRepository.insertMQ7Readings(newReadings)
                Log.d("ReadingsViewModel", "MQ7 readings updated successfully for $teamHandle.")
            } else {
                Log.i("ReadingsViewModel", "MQ7 Readings are null for teamHandle: $teamHandle")
            }
        } catch (e: HttpException) {
            Log.e("ReadingsViewModel", "HTTP error while updating MQ7 readings", e)
        } catch (e: Exception) {
            Log.e("ReadingsViewModel", "Error while updating MQ7 readings", e)
        }
    }

    private suspend fun updateMQ135Readings(teamHandle: String, accessToken: String) {
        try {
            val latestTimestamp = readingsRepository.getLatestTimestamp("MQ135", teamHandle)
            val response = service.fetchMq135ReadingsByTeamHandle(teamHandle, "Bearer $accessToken")

            val readings = response.embedded?.readings
            if (readings != null) {
                val newReadings = readings.filter { it.timestamp > latestTimestamp }
                    .map { MQ135Reading(it.id, it.teamHandle, it.value.toFloat(), it.timestamp) }

                readingsRepository.insertMQ135Readings(newReadings)
                Log.d("ReadingsViewModel", "MQ135 readings updated successfully for $teamHandle.")
            } else {
                Log.i("ReadingsViewModel", "MQ135 Readings are null for teamHandle: $teamHandle")
            }
        } catch (e: HttpException) {
            Log.e("ReadingsViewModel", "HTTP error while updating MQ135 readings", e)
        } catch (e: Exception) {
            Log.e("ReadingsViewModel", "Error while updating MQ135 readings", e)
        }
    }

    private suspend fun updateFireReadings(teamHandle: String, accessToken: String) {
        try {
            val latestTimestamp = readingsRepository.getLatestTimestamp("FIRE", teamHandle)
            val response = service.fetchFireReadingsByTeamHandle(teamHandle, "Bearer $accessToken")

            val readings = response.embedded?.readings
            if (readings != null) {
                val newReadings = readings
                    .filter { it.fire && it.timestamp > latestTimestamp }
                    .map { FireReading(it.id, it.teamHandle, it.timestamp, it.fire) }

                readingsRepository.insertFireReadings(newReadings)
                Log.d("ReadingsViewModel", "Fire readings updated successfully for $teamHandle.")
            } else {
                Log.i("ReadingsViewModel", "Fire Readings are null for teamHandle: $teamHandle")
            }
        } catch (e: HttpException) {
            Log.e("ReadingsViewModel", "HTTP error while updating Fire readings", e)
        } catch (e: Exception) {
            Log.e("ReadingsViewModel", "Error while updating Fire readings", e)
        }
    }

    fun getReadingsFromDB(teamHandle: String) {
        viewModelScope.launch {
            try {
                val mq7Response = readingsRepository.getMQ7ReadingsByTeamHandle(teamHandle)
                val mq135Response = readingsRepository.getMQ135ReadingsByTeamHandle(teamHandle)
                val fireResponse = readingsRepository.getFireReadingsByTeamHandle(teamHandle)

                _mq7ReadingsByTeamHandle.value = mq7Response
                _mq135ReadingsByTeamHandle.value = mq135Response
                _fireReadingsByTeamHandle.value = fireResponse


                Log.d("ReadingsViewModel", "MQ7: $mq7Response")
                Log.d("ReadingsViewModel", "Successfully got all readings from DB for $teamHandle")
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while Getting Readings From DB", e)
            }
        }
    }

    fun getAggregatedReadingsForLastWeek(teamHandle: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = readingsRepository.getAggregatedReadingsForLastWeek(teamHandle)
                _aggregatedReadingsForLastWeek.value = response

                Log.d("ReadingsViewModel", "LastWeekAggregated Complete: $response")
                onComplete()
            } catch (e: Exception) {
                Log.e(
                    "ReadingsViewModel",
                    "Error while updating getAggregatedReadingsForLastWeek",
                    e
                )
            }
        }
    }

    fun getAggregatedReadingsForDate(teamHandle: String, date: Date, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = readingsRepository.getAggregatedReadingsForDate(teamHandle, date)
                _aggregatedReadingsForDate.value = response

                Log.d("ReadingsViewModel", "DateAggregated Complete: $response")
                onComplete()
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while updating getAggregatedReadingsForDate", e)
            }
        }
    }

    fun getFireReadingsByHour(teamHandle: String) {
        viewModelScope.launch {
            try {
                val readings = readingsRepository.getFireReadingsByHour(teamHandle)
                _fireReadingsByHour.value = readings
                Log.d("ReadingsViewModel", "Fire Readings by Hour: $readings")
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error fetching fire readings by hour", e)
            }
        }
    }

    fun getMQ135ReadingsByHour(teamHandle: String) {
        viewModelScope.launch {
            try {
                val readings = readingsRepository.getMQ135ReadingsByHour(teamHandle)
                _mq135ReadingsByHour.value = readings
                Log.d("ReadingsViewModel", "MQ135 Readings by Hour: $readings")
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error fetching MQ135 readings by hour", e)
            }
        }
    }

    fun getAggregatedReadingsForMonth(teamHandle: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = readingsRepository.getAggregatedReadingsForLastMonth(teamHandle)
                _aggregatedReadingsForMonth.value = response
                Log.d("ReadingsViewModel", "MonthlyAggregated Complete: $response")
                onComplete()
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching aggregated readings for month", e)
            }
        }
    }

    fun getAggregatedReadingsForYear(teamHandle: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = readingsRepository.getAggregatedReadingsForLastYear(teamHandle)
                _aggregatedReadingsForYear.value = response
                Log.d("ReadingsViewModel", "YearlyAggregated Complete: $response")
                onComplete()
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching aggregated readings for year", e)
            }
        }
    }
}
