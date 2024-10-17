package br.ecosynergy_app.readings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.room.readings.Readings
import br.ecosynergy_app.room.readings.ReadingsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ReadingsViewModel(
    private val service: ReadingsService,
    private val readingsRepository: ReadingsRepository
): ViewModel() {

    fun deleteAllReadingsFromDB(){
        viewModelScope.launch {
            try {
                val delete = readingsRepository.deleteAllReadings()
                val deleteState = if(delete == Unit) "OK" else "ERROR"
                Log.d("ReadingsViewModel", "DeleteReadings: $deleteState")
            }catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during deleteUserInfo", e)
            }
        }
    }

    // MQ7 Readings
    private val _mq7ReadingResult = MutableLiveData<Result<MQ7ReadingsResponse>>()
    val mq7ReadingResult: LiveData<Result<MQ7ReadingsResponse>> get() = _mq7ReadingResult

    private val _mq7ReadingsDB = MutableLiveData<List<Readings>>()
    val mq7ReadingsDB: LiveData<List<Readings>> get() = _mq7ReadingsDB


    fun fetchMQ7ReadingsByTeamHandle(teamHandle: String, token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq7ReadingsByTeamHandle(teamHandle, "Bearer $token")
                _mq7ReadingResult.value = Result.success(response)

                val readings = response.embedded.mQ7ReadingVOList.map { readingVo ->
                    Readings(
                        id = readingVo.id,
                        sensor = "MQ7",
                        teamHandle = readingVo.teamHandle,
                        value = readingVo.value.toFloat(),
                        timestamp = readingVo.timestamp
                    )
                }
                readingsRepository.insertReadings(readings)
                Log.d("ReadingsViewModel", "$readings")
                Log.d("ReadingsViewModel", "MQ7Readings successfully stored in the database.")

                //getReadingsBySensorFromDB("MQ7")

            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while fetching MQ7 readings", e)
                _mq7ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching MQ7 readings", e)
                _mq7ReadingResult.value = Result.failure(e)
            }
        }
    }

    fun getReadingsBySensorFromDB(sensor: String) {
        viewModelScope.launch {
            try {

                val readingsResponse = readingsRepository.getReadingsBySensor(sensor)

                when (sensor) {
                    "MQ7" -> _mq7ReadingsDB.value = readingsResponse
                    "MQ135" -> _mq135ReadingsDB.value = readingsResponse
                    "FIRE" -> _fireReadingsDB.value = readingsResponse
                    else -> Log.e("ReadingsViewModel", "Unknown sensor type: $sensor")
                }

                Log.d("ReadingsViewModel", "$sensor Readings Successfully got from DB.")

            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while fetching MQ7 readings", e)
                _mq7ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching MQ7 readings", e)
                _mq7ReadingResult.value = Result.failure(e)
            }
        }
    }

    // MQ135 Readings
    private val _mq135ReadingResult = MutableLiveData<Result<MQ135ReadingsResponse>>()
    val mq135ReadingResult: LiveData<Result<MQ135ReadingsResponse>> get() = _mq135ReadingResult

    private val _mq135ReadingsDB = MutableLiveData<List<Readings>>()
    val mq135ReadingsDB: LiveData<List<Readings>> get() = _mq135ReadingsDB

    fun fetchMQ135ReadingsByTeamHandle(teamHandle: String, token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq135ReadingsByTeamHandle(teamHandle, "Bearer $token")
                _mq135ReadingResult.value = Result.success(response)

                val readings = response.embedded.mQ135ReadingVOList.map { readingVo ->
                    Readings(
                        id = readingVo.id,
                        sensor = "MQ135",
                        teamHandle = readingVo.teamHandle,
                        value = readingVo.value.toFloat(),
                        timestamp = readingVo.timestamp
                    )
                }
                readingsRepository.insertReadings(readings)
                Log.d("ReadingsViewModel", "$readings")
                Log.d("ReadingsViewModel", "MQ135Readings successfully stored in the database.")

               // getReadingsBySensorFromDB("MQ135")

            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while fetching MQ135 readings", e)
                _mq135ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching MQ135 readings", e)
                _mq135ReadingResult.value = Result.failure(e)
            }
        }
    }

    // Fire Readings
    private val _fireReadingResult = MutableLiveData<Result<FireReadingsResponse>>()
    val fireReadingResult: LiveData<Result<FireReadingsResponse>> get() = _fireReadingResult

    private val _fireReadingsDB = MutableLiveData<List<Readings>>()
    val fireReadingsDB: LiveData<List<Readings>> get() = _fireReadingsDB

    fun fetchFireReadingsByTeamHandle(teamHandle: String, token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchFireReadingsByTeamHandle(teamHandle, "Bearer $token")
                _fireReadingResult.value = Result.success(response)

                val readings = response.embedded.fireReadingVOList.map { readingVo ->
                    Readings(
                        id = readingVo.id,
                        sensor = "FIRE",
                        teamHandle = readingVo.teamHandle,
                        value = readingVo.value.toFloat(),
                        timestamp = readingVo.timestamp
                    )
                }
                readingsRepository.insertReadings(readings)
                Log.d("ReadingsViewModel", "$readings")
                Log.d("ReadingsViewModel", "FireReadings successfully stored in the database.")

              //  getReadingsBySensorFromDB("FIRE")

            } catch (e: HttpException) {
                Log.e("ReadingsViewModel", "HTTP error while fetching Fire readings", e)
                _fireReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("ReadingsViewModel", "Error while fetching Fire readings", e)
                _fireReadingResult.value = Result.failure(e)
            }
        }
    }
}