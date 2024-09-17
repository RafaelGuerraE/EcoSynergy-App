package br.ecosynergy_app.teams

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.ApiError
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.room.toTeam
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TeamsViewModel(
    private val service: TeamsService,
    private val teamsRepository: TeamsRepository): ViewModel() {
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

    fun editMemberRole(token: String?, teamId: Int, userId: Int, request:RoleRequest){
        makeRequest(
            request = {service.editMemberRole("Bearer $token", teamId, userId, request)},
            onResult = { _teamResult.value = it}
        )
        Log.d("TeamsViewModel", "Team ID: $teamId, MemberID: $userId, Request: $request")
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

    fun updateTeam(token: String?, id: Int, request: UpdateRequest) {
        makeRequest(
            request = { service.updateTeam("Bearer $token", id, request) },
            onResult = { _teamResult.value = it }
        )
    }

    fun addMember(token: String?, teamId: Int, userId: Int, request: RoleRequest) {
        viewModelScope.launch{
            Log.d("TeamsViewModel", "Token: $token, MemberID: $userId, TeamID: $teamId")
            try {
                val response = service.addMember("Bearer $token", teamId, userId, request)
                Log.d("TeamsViewModel", "AddMember Successful")
                _teamResult.value = Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ApiError::class.java)
                Log.e("UserViewModel", "HTTP error during addMember: ${errorResponse.error} at ${errorResponse.path} $e")
                _teamResult.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during addMember", e)
                _teamResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during addMember", e)
                _teamResult.value = Result.failure(e)
            }
        }
    }

    fun findTeamsByUserId(userId: Int, token: String) {
        viewModelScope.launch {
            try {

                val response = service.findTeamsByUserId(userId, "Bearer $token")
                if (response.isSuccessful) {
                    val teams = response.body() ?: emptyList()
                    teams.forEach { team ->
                        Log.d("TeamsViewModel", "$team")
                        val teamEntity = team.toTeam()
                        teamsRepository.insertTeam(teamEntity)
                    }
                    _teamsResult.value = Result.success(teams)

                    Log.d("TeamsViewModel", "Added Teams to DB Successfully")
                } else {
                    Log.e("TeamsViewModel", "HTTP error while findTeamsByUserId: ${response.errorBody()?.string()}")
                    _teamsResult.value = Result.failure(HttpException(response))
                }
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while findTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while findTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun deleteTeamsFromDB() {
        viewModelScope.launch {
            try {
                val delete = teamsRepository.deleteAllTeams()
                val deleteState = if(delete == Unit) "OK" else "ERROR"
                Log.d("TeamsViewModel", "DeleteUserTeams: $deleteState")
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Unexpected error during deleteUserTeams", e)
            }
        }
    }

    fun deleteTeam(token: String?, teamId: Int) {
        viewModelScope.launch {
            try {
                service.deleteTeam("Bearer $token", teamId)
                _deleteResult.value = Result.success(Unit)

               // val delete = teamsRepository.deleteTeam()
              //  val deleteState = if(delete == Unit) "OK" else "ERROR"
              //  Log.d("TeamsViewModel", "DeleteUserTeams: $deleteState")
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun removeMember(token: String?, teamId: Int, userId: Int) {
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