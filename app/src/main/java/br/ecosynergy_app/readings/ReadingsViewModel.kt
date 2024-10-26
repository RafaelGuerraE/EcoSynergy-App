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
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    fun updateMQ7Readings(teamHandle: String, accessToken: String) {
        viewModelScope.launch {
            try {
                val latestTimestamp = readingsRepository.getLatestTimestamp("MQ7", teamHandle)
                val response = service.fetchMq7ReadingsByTeamHandle(teamHandle, "Bearer $accessToken")

                val readings = response.embedded?.readings
                if (readings != null) {
                    val newReadings = readings.filter { it.timestamp > latestTimestamp }
                        .map { MQ7Reading(it.id, it.teamHandle, it.value.toFloat(), it.timestamp) }

                    readingsRepository.insertMQ7Readings(newReadings)
                    Log.d("ReadingsViewModel", "MQ7 readings updated successfully for $teamHandle.")
                } else {
                    Log.i("ReadingsViewModel", "MQ7Readings are null for teamHandle: $teamHandle")
                }
            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while updating MQ7 readings", e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while updating MQ7 readings", e)
            }
        }
    }

    fun updateMQ135Readings(teamHandle: String, accessToken: String) {
        viewModelScope.launch {
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
                    Log.i("ReadingsViewModel", "MQ135Readings are null for teamHandle: $teamHandle")
                }
            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while updating MQ135 readings", e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while updating MQ135 readings", e)
            }
        }
    }

    fun updateFireReadings(teamHandle: String, accessToken: String) {
        viewModelScope.launch {
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
                    Log.i("ReadingsViewModel", "FireReadings are null for teamHandle: $teamHandle")
                }
            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while updating Fire readings", e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while updating Fire readings", e)
            }
        }
    }

    fun getReadingsFromDB(teamHandle: String){
        viewModelScope.launch {
            try {
                val mq7Response = readingsRepository.getMQ7ReadingsByTeamHandle(teamHandle)
                val mq135Response = readingsRepository.getMQ135ReadingsByTeamHandle(teamHandle)
                val fireResponse = readingsRepository.getFireReadingsByTeamHandle(teamHandle)

                _mq7ReadingsByTeamHandle.value = mq7Response
                _mq135ReadingsByTeamHandle.value =  mq135Response
                _fireReadingsByTeamHandle.value = fireResponse
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while updating Getting Readings From DB", e)
            }
        }
    }
}
