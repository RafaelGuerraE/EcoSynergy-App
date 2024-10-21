package br.ecosynergy_app.teams.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.Teams
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.teams.toTeam
import br.ecosynergy_app.user.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
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

    private val _updateResponse = MutableLiveData<Result<Response<TeamsResponse>>>()
    val updateResponse: LiveData<Result<Response<TeamsResponse>>> get() = _updateResponse

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

    fun findTeamByHandle(accessToken: String, handle: String) {
        makeRequest(
            request = { service.findTeamByHandle("Bearer $accessToken", handle) },
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

    fun searchTeamByPartialHandle(accessToken: String, handle: String) {
        makeRequest(
            request = { service.searchTeamByPartialHandle("Bearer $accessToken", handle) },
            onResult = { _teamResult.value = it }
        )
    }

    fun editMemberRole(accessToken: String, teamId: Int, userId: Int, request: RoleRequest) {
        viewModelScope.launch {
            try {
                service.editMemberRole("Bearer $accessToken", teamId, userId, request)
                Log.d("TeamsViewModel", "editMemberRole Successful")

                membersRepository.updateUserRole(userId, teamId, request.role)

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while editMemberRole", e)
                _teamsResult.value = Result.failure(e)

            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during editMemberRole", e)
                _teamResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while editMemberRole", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun findTeamById(accessToken: String, id: String) {
        makeRequest(
            request = { service.findTeamById("Bearer $accessToken", id) },
            onResult = { _teamResult.value = it }
        )
    }

    fun createTeam(accessToken: String, request: TeamsRequest) {
        viewModelScope.launch {
            try {
                val response = service.createTeam("Bearer $accessToken", request)
                Log.d("TeamsViewModel", "API - createTeam Successful")

                insertTeamDB(
                    Teams(
                        id = response.id,
                        handle = response.handle,
                        name = response.name,
                        description = response.description,
                        activityId = response.activity.id,
                        activityName = response.activity.name,
                        activitySector = response.activity.sector,
                        dailyGoal = response.dailyGoal,
                        weeklyGoal = response.weeklyGoal,
                        monthlyGoal = response.monthlyGoal,
                        annualGoal = response.annualGoal,
                        timeZone = response.timeZone,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt,
                        linksRel = response.links.self.href,
                        linksHref = response.links.self.href
                    )
                )
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while createTeam", e)
                _teamsResult.value = Result.failure(e)

            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during createTeam", e)
                _teamResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while createTeam", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    private fun insertTeamDB(team: Teams){
        viewModelScope.launch {
            try {
                val response = teamsRepository.insertTeam(team)

                Log.d("TeamsViewModel", "DB - InsertTeam Successful")
            }catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while InsertTeamDB", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun updateTeam(accessToken: String, id: Int, request: UpdateRequest) {
        viewModelScope.launch {
            try {
                val result = service.updateTeam("Bearer $accessToken", id, request)
                _updateResponse.value = Result.success(result)
                if (result.isSuccessful) {
                    val response = result.body()
                    if (response != null) {
                        Log.d("TeamsViewModel", "API - UpdateTeam Successful: $response")

                        val team = Teams(
                            id = response.id,
                            handle = response.handle,
                            name = response.name,
                            description = response.description,
                            activityId = response.activity.id,
                            activityName = response.activity.name,
                            activitySector = response.activity.sector,
                            dailyGoal = response.dailyGoal,
                            weeklyGoal = response.weeklyGoal,
                            monthlyGoal = response.monthlyGoal,
                            annualGoal = response.annualGoal,
                            timeZone = response.timeZone,
                            createdAt = response.createdAt,
                            updatedAt = response.updatedAt,
                            linksRel = response.links.self.href,
                            linksHref = response.links.self.href
                        )

                        teamsRepository.updateTeamInfo(team)

                        Log.d("TeamsViewModel", "DB - UpdateTeam Successful")
                    } else {
                        Log.e("TeamsViewModel", "Null response from the API")
                    }
                } else {
                    Log.e("TeamsViewModel", "Error response from API: ${result.errorBody()?.string()}")
                }

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while UpdateTeam", e)
            } catch (e: IOException) {
                Log.e("TeamsViewModel", "Network error during UpdateTeam", e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while UpdateTeam", e)
            }
        }
    }


    fun addMember(accessToken: String, teamId: Int, userId: Int, request: RoleRequest) {
        viewModelScope.launch {
            try {
                val response = service.addMember("Bearer $accessToken", teamId, userId, request)
                Log.d("TeamsViewModel", "AddMember Successful")
                _teamResult.value = Result.success(response)

            } catch (e: HttpException) {
                Log.e("UserViewModel","HTTP error during addMember", e)
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

    private fun insertMemberDB(member: Members){
        viewModelScope.launch {
            try {
                membersRepository.insertMember(member)

                val memberName = member.fullName
                Log.e("UserViewModel", "DB - Member: $memberName added successfully")
            }catch (e: Exception){

                Log.e("UserViewModel", "Unexpected error during insertMemberDB", e)
            }
        }
    }

    fun getTeamsByUserId(userId: Int, accessToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.getTeamsByUserId(userId, "Bearer $accessToken")
                val teams = response.body() ?: emptyList()

                teams.forEach { team ->
                    Log.d("TeamsViewModel", "$team")
                    val teamEntity = team.toTeam()
                    insertTeamDB(teamEntity)

                    getMembersById(team.members, accessToken, team.id)
                    Log.d("TeamsViewModel", "MembersInsertion completed for TeamID: ${team.id}")
                }

                _teamsResult.value = Result.success(teams)
                Log.d("TeamsViewModel", "Added Teams to DB Successfully")

                //getAllTeamsFromDB()

                onComplete()

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while getTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)

            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during getTeamsByUserId", e)
                _teamResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while getTeamsByUserId", e)
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


    private suspend fun getMembersById(
        membersList: List<Member>,
        accessToken: String,
        teamId: Int
    ) {
        val ids = membersList.map { it.id }
        val roles = membersList.map { it.role }
        val usersList = mutableListOf<UserResponse>()

        //Log.d("TeamsViewModel", "Ids: $ids Roles: $roles")

        ids.forEachIndexed { index, id ->
            try {
                val response = service.getMembersById(id, "Bearer $accessToken")
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

    fun deleteTeam(accessToken: String, teamId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                service.deleteTeam("Bearer $accessToken", teamId)
                _deleteResult.value = Result.success(Unit)

                deleteTeamFromDB(teamId)

                onComplete()

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "API - Error during Delete Team", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun deleteTeamFromDB(teamId: Int){
        viewModelScope.launch {
            try {
                val delete = teamsRepository.deleteTeamById(teamId)
                val deleteState = if (delete == Unit) "OK" else "ERROR"

                Log.d("TeamsViewModel", "Delete team ID($teamId): $deleteState")
            }catch (e: Exception) {
                Log.e("TeamsViewModel", "DB - Error during DeleteTeam", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun removeMember(accessToken: String, teamId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                service.removeMember("Bearer $accessToken", teamId, userId)
                _deleteResult.value = Result.success(Unit)

                membersRepository.deleteMember(userId, teamId)

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