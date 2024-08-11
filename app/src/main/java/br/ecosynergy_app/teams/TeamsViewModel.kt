package br.ecosynergy_app.teams

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

class TeamsViewModel(private val service: TeamsService): ViewModel() {
    private val _teamsResult = MutableLiveData<Result<List<TeamsResponse>>>()
    val teamsResult: LiveData<Result<List<TeamsResponse>>> get() = _teamsResult

    private val _teamResult = MutableLiveData<Result<TeamsResponse>>()
    val teamResult: LiveData<Result<TeamsResponse>> get() = _teamResult

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> get() = _deleteResult

    private val _allTeams = MutableLiveData<Result<AllTeamsResponse>>()
    val allTeams: LiveData<Result<AllTeamsResponse>> get() = _allTeams

    private fun makeRequest(
        request: suspend () -> TeamsResponse,
        onResult: (Result<TeamsResponse>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = request()
                Log.d("TeamsViewModel", "Response: $response")
                onResult(Result.success(response))
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while TeamRequesting", e)
                onResult(Result.failure(e))
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while TeamRequesting", e)
                onResult(Result.failure(e))
            }
        }
    }

    fun findTeamByHandle(token: String?, handle: String?) {
        makeRequest(
            request = { service.findTeamByHandle("Bearer $token", handle) },
            onResult = { _teamResult.value = it }
        )
    }

    fun searchTeamByPartialHandle(token: String?, handle: String) {
        makeRequest(
            request = { service.searchTeamByPartialHandle("Bearer $token", handle) },
            onResult = { _teamResult.value = it }
        )
    }

    fun findAllTeams(token: String?) {
        viewModelScope.launch {
            try {
                val response = service.findAllTeams("Bearer $token")
                Log.d("TeamsViewModel", "Response: $response")
                _allTeams.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during Find All Teams", e)
                _allTeams.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during Find All Teams", e)
                _allTeams.value = Result.failure(e)
            }
        }
    }

    fun findTeamById(token: String?, id: String) {
        makeRequest(
            request = { service.findTeamById("Bearer $token", id) },
            onResult = { _teamResult.value = it }
        )
    }

    fun createTeam(token: String?, request: TeamsRequest) {
        makeRequest(
            request = { service.createTeam("Bearer $token", request) },
            onResult = { _teamResult.value = it }
        )
    }

    fun updateTeam(token: String?, id: String?, request: UpdateRequest) {
        makeRequest(
            request = { service.updateTeam("Bearer $token", id, request) },
            onResult = { _teamResult.value = it }
        )
    }

    fun addMember(token: String?, teamId: String, userId: String) {
        makeRequest(
            request = { service.addMember("Bearer $token", teamId, userId) },
            onResult = { _teamResult.value = it }
        )
    }

    fun findTeamsByUserId(id: String,token: String?) {
        viewModelScope.launch {
            try {
                val response = service.findTeamsByUserId(id, "Bearer $token")
                if (response.isSuccessful) {
                    Log.d("TeamsViewModel", "Response: ${response.body()}")
                    _teamsResult.value = Result.success(response.body() ?: emptyList())
                } else {
                    Log.e("TeamsViewModel", "HTTP error while TeamRequesting: ${response.errorBody()?.string()}")
                    _teamsResult.value = Result.failure(HttpException(response))
                }
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while TeamRequesting", e)
                _teamsResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while TeamRequesting", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }


    fun deleteTeam(token: String?, id: String?) {
        viewModelScope.launch {
            try {
                service.deleteTeam("Bearer $token", id)
                _deleteResult.value = Result.success(Unit)
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun removeMember(token: String?, teamId: String?, userId: String?) {
        viewModelScope.launch {
            try {
                service.removeMember("Bearer $token", teamId, userId)
                _deleteResult.value = Result.success(Unit)
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during Remove Member", e)
                _deleteResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during Remove Member", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }
}