package br.ecosynergy_app.teams

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.ApiError
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.Teams
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.teams.toTeam
import br.ecosynergy_app.user.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class TeamsViewModel(
    private val service: TeamsService,
    private val teamsRepository: TeamsRepository,
    private val membersRepository: MembersRepository
) : ViewModel() {

    private val _teamsResult = MutableLiveData<Result<List<TeamsResponse>>>()
    val teamsResult: LiveData<Result<List<TeamsResponse>>> get() = _teamsResult

    private val _teamResult = MutableLiveData<Result<TeamsResponse>>()
    val teamResult: LiveData<Result<TeamsResponse>> get() = _teamResult

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> get() = _deleteResult

    private val _allTeamsDB = MutableLiveData<List<Teams>>()
    val allTeamsDB: LiveData<List<Teams>> get() = _allTeamsDB

    private val _teamDB = MutableLiveData<Teams>()
    val teamDB: LiveData<Teams> get() = _teamDB

    private val _allMembersDB = MutableLiveData<List<Members>>()
    val allMembersDB: LiveData<List<Members>> get() = _allMembersDB

    private val _members = MutableLiveData<Result<MutableList<UserResponse>>>()
    val members: LiveData<Result<MutableList<UserResponse>>> = _members

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

    fun getMembersByTeamId(teamId: Int) {
        viewModelScope.launch {
            try {
                val members = membersRepository.getMembersByTeamId(teamId)

                _allMembersDB.value = members

                Log.d("TeamsViewModel", "Members: $members")

            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while getMembersByTeamId", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun searchTeamByPartialHandle(token: String?, handle: String) {
        makeRequest(
            request = { service.searchTeamByPartialHandle("Bearer $token", handle) },
            onResult = { _teamResult.value = it }
        )
    }

    fun editMemberRole(token: String?, teamId: Int, userId: Int, request: RoleRequest) {
        makeRequest(
            request = { service.editMemberRole("Bearer $token", teamId, userId, request) },
            onResult = { _teamResult.value = it }
        )
        Log.d("TeamsViewModel", "Team ID: $teamId, MemberID: $userId, Request: $request")
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
        viewModelScope.launch {
            Log.d("TeamsViewModel", "Token: $token, MemberID: $userId, TeamID: $teamId")
            try {
                val response = service.addMember("Bearer $token", teamId, userId, request)
                Log.d("TeamsViewModel", "AddMember Successful")
                _teamResult.value = Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ApiError::class.java)
                Log.e(
                    "UserViewModel",
                    "HTTP error during addMember: ${errorResponse.error} at ${errorResponse.path} $e"
                )
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

    fun getTeamsByUserId(userId: Int, token: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.getTeamsByUserId(userId, "Bearer $token")
                val teams = response.body() ?: emptyList()

                teams.forEach { team ->
                    Log.d("TeamsViewModel", "$team")
                    val teamEntity = team.toTeam()
                    teamsRepository.insertTeam(teamEntity)

                    getMembersById(team.members, token, team.id)
                    Log.d("TeamsViewModel", "MembersInsertion completed for TeamID: ${team.id}")
                }

                _teamsResult.value = Result.success(teams)
                Log.d("TeamsViewModel", "Added Teams to DB Successfully")

                //getAllTeamsFromDB()

                onComplete()

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while findTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)

            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during addMember", e)
                _teamResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while findTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun getTeamById(teamId: Int) {
        viewModelScope.launch {
            try {
                val team = teamsRepository.getTeamById(teamId)

                _teamDB.value = team

                Log.d("TeamsViewModel", "Team from DB: $team")

            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while getAllTeamsFromDB", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun getAllTeamsFromDB() {
        viewModelScope.launch {
            try {
                val teams = teamsRepository.getAllTeams()
                val members = membersRepository.getAllMembers()

                _allTeamsDB.value = teams
                _allMembersDB.value = members

                Log.d("TeamsViewModel", "Teams and Members: $teams $members")

            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while getAllTeamsFromDB", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }


    private suspend fun getMembersById(membersList: List<Member>, token: String, teamId: Int) {
        val ids = membersList.map { it.id }
        val roles = membersList.map { it.role }
        val usersList = mutableListOf<UserResponse>()

        //Log.d("TeamsViewModel", "Ids: $ids Roles: $roles")

        ids.forEachIndexed { index, id ->
            try {
                val response = service.getMembersById(id, "Bearer $token")
                usersList.add(response)
                _members.value = Result.success(usersList)
                val role = roles[index]

                val member = Members(
                    userId = id!!,
                    role = role,
                    teamId = teamId,
                    username = response.username,
                    fullName = response.fullName,
                    email = response.email,
                    gender = response.gender,
                    nationality = response.nationality,
                    createdAt = response.createdAt
                )

                membersRepository.insertMember(member)
                //Log.d("TeamsViewModel", "Member inserted in DB: $member of TeamID: $teamId")
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during getUsersByIds", e)
                _members.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("TeamsViewModel", "Network error during getUsersByIds", e)
                _members.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Unexpected error during getUsersByIds", e)
                _members.value = Result.failure(e)
            }
        }
    }

    fun deleteTeamsFromDB() {
        viewModelScope.launch {
            try {
                val deleteTeams = teamsRepository.deleteAllTeams()
                val deleteMembers = membersRepository.deleteAllMembers()

                val deleteTeamsState = if (deleteTeams == Unit) "OK" else "ERROR"
                val deleteMembersState = if (deleteMembers == Unit) "OK" else "ERROR"

                Log.d("TeamsViewModel", "Delete Teams from DB: $deleteTeamsState")
                Log.d("TeamsViewModel", "Delete Members from DB: $deleteMembersState")
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Unexpected error during deleteTeamsFromDB", e)
            }
        }
    }

    fun deleteTeam(token: String?, teamId: Int) {
        viewModelScope.launch {
            try {
                service.deleteTeam("Bearer $token", teamId)
                _deleteResult.value = Result.success(Unit)

                val delete = teamsRepository.deleteTeamById(teamId)
                val deleteState = if (delete == Unit) "OK" else "ERROR"
                Log.d("TeamsViewModel", "Delete team ID($teamId): $deleteState")
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