package br.ecosynergy_app.sensors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SensorsViewModel(private val service: SensorsService): ViewModel() {

    // MQ7 Readings
    private val _mq7ReadingResult = MutableLiveData<Result<MQ7ReadingsResponse>>()
    val mq7ReadingResult: LiveData<Result<MQ7ReadingsResponse>> get() = _mq7ReadingResult

    fun fetchMQ7Readings(token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq7Readings("Bearer $token")
                _mq7ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq7ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq7ReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchMQ7ReadingsByTeamHandle(teamHandle: String,token: String?) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq7ReadingsByTeamHandle(teamHandle, "Bearer $token")
                _mq7ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq7ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq7ReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchMQ7ReadingsById(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq7ReadingById("Bearer $token", id)
                _mq7ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq7ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq7ReadingResult.value = Result.failure(e)
            }
        }
    }

    // MQ135 Readings
    private val _mq135ReadingResult = MutableLiveData<Result<MQ135ReadingsResponse>>()
    val mq135ReadingResult: LiveData<Result<MQ135ReadingsResponse>> get() = _mq135ReadingResult

    fun fetchMQ135Readings(token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq135Reading("Bearer $token")
                _mq135ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq135ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq135ReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchMQ135ReadingsByTeamHandle(token: String, teamHandle: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq135ReadingsByTeamHandle("Bearer $token", teamHandle)
                _mq135ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq135ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq135ReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchMQ135ReadingsById(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchMq135ReadingById("Bearer $token", id)
                _mq135ReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _mq135ReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _mq135ReadingResult.value = Result.failure(e)
            }
        }
    }

    // Fire Readings
    private val _fireReadingResult = MutableLiveData<Result<FireReadingsResponse>>()
    val fireReadingResult: LiveData<Result<FireReadingsResponse>> get() = _fireReadingResult

    fun fetchFireReadings(token: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchFireReading("Bearer $token")
                _fireReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _fireReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _fireReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchFireReadingsByTeamHandle(token: String, teamHandle: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchFireReadingsByTeamHandle("Bearer $token", teamHandle)
                _fireReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _fireReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _fireReadingResult.value = Result.failure(e)
            }
        }
    }

    fun fetchFireReadingsById(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = service.fetchFireReadingById("Bearer $token", id)
                _fireReadingResult.value = Result.success(response)
            } catch (e: HttpException) {
                _fireReadingResult.value = Result.failure(e)
            } catch (e: Exception) {
                _fireReadingResult.value = Result.failure(e)
            }
        }
    }
}