package br.ecosynergy_app.teams.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.room.invites.Invites
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.Members
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.Teams
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.teams.toTeam
import br.ecosynergy_app.teams.invites.InviteRequest
import br.ecosynergy_app.teams.invites.InviteResponse
import br.ecosynergy_app.teams.invites.InvitesService
import br.ecosynergy_app.user.UserResponse
import br.ecosynergy_app.user.viewmodel.UserService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TeamsViewModel(
    private val service: TeamsService,
    private val teamsRepository: TeamsRepository,
    private val invitesService: InvitesService,
    private val membersRepository: MembersRepository,
    private val invitesRepository: InvitesRepository
) : ViewModel() {

    private val _teamsResult = MutableLiveData<Result<List<TeamsResponse>>>()
    val teamsResult: LiveData<Result<List<TeamsResponse>>> get() = _teamsResult

    private val _inviteResult = MutableLiveData<Response<InviteResponse>>()
    val inviteResult: LiveData<Response<InviteResponse>> get() = _inviteResult

    private val _teamResult = MutableLiveData<Result<TeamsResponse>>()
    val teamResult: LiveData<Result<TeamsResponse>> get() = _teamResult

    private val _teamResponse = MutableLiveData<Response<TeamsResponse>>()
    val teamResponse: LiveData<Response<TeamsResponse>> get() = _teamResponse

    private val _updateResponse = MutableLiveData<Result<Response<TeamsResponse>>>()
    val updateResponse: LiveData<Result<Response<TeamsResponse>>> get() = _updateResponse

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> get() = _deleteResult

    private val _allTeamsDB = MutableLiveData<Flow<List<Teams>>>()
    val allTeamsDB: LiveData<Flow<List<Teams>>> get() = _allTeamsDB

    private val _invitesList = MutableLiveData<List<Invites>>()
    val invitesList: LiveData<List<Invites>> = _invitesList

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

    fun createTeam(accessToken: String, request: TeamsRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = service.createTeam("Bearer $accessToken", request)
                Log.d("TeamsViewModel", "API - createTeam Successful")

                val response = result.body()

                if (response != null) {
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

                    val memberData = response.members.first()

                    val memberResponse =
                        service.getMembersById(memberData.id, "Bearer $accessToken")
                    val role = memberData.role

                    val member = Members(
                        userId = memberResponse.id,
                        role = role,
                        teamId = response.id,
                        username = memberResponse.username,
                        fullName = memberResponse.fullName,
                        email = memberResponse.email,
                        gender = memberResponse.gender,
                        nationality = memberResponse.nationality,
                        createdAt = memberResponse.createdAt
                    )

                    membersRepository.insertMember(member)

                    onComplete()
                }
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

    private fun insertTeamDB(team: Teams) {
        viewModelScope.launch {
            try {
                teamsRepository.insertTeam(team)
                Log.d("TeamsViewModel", "DB - InsertTeam Successful")

            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while InsertTeamDB", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun updateTeam(accessToken: String, teamId: Int, request: TeamUpdateRequest) {
        viewModelScope.launch {
            try {
                val result = service.updateTeam("Bearer $accessToken", teamId, request)
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
                    Log.e(
                        "TeamsViewModel",
                        "Error response from API: ${result.errorBody()?.string()}"
                    )
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

    fun updateTeamGoals(
        accessToken: String,
        teamId: Int,
        dailyGoal: Double,
        weeklyGoal: Double,
        monthlyGoal: Double,
        annualGoal: Double,
        onComplete: () -> Unit
    ) {
        val request = TeamUpdateRequest(
            dailyGoal = dailyGoal,
            weeklyGoal = weeklyGoal,
            monthlyGoal = monthlyGoal,
            annualGoal = annualGoal
        )

        viewModelScope.launch {
            try {
                val result = service.updateTeam("Bearer $accessToken", teamId, request)
                _updateResponse.value = Result.success(result)
                if (result.isSuccessful) {
                    val response = result.body()
                    if (response != null) {
                        Log.d("TeamsViewModel", "API - UpdateTeamGoals Successful")

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
                        onComplete()
                    } else {
                        Log.e("TeamsViewModel", "Null response from the API")
                    }
                } else {
                    Log.e(
                        "TeamsViewModel",
                        "Error response from API: ${result.errorBody()?.string()}"
                    )
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
                Log.e("UserViewModel", "HTTP error during addMember", e)
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

    private fun insertMemberDB(member: Members) {
        viewModelScope.launch {
            try {
                membersRepository.insertMember(member)

                val memberName = member.fullName
                Log.e("UserViewModel", "DB - Member: $memberName added successfully")
            } catch (e: Exception) {

                Log.e("UserViewModel", "Unexpected error during insertMemberDB", e)
            }
        }
    }

    fun getTeamsByUserId(userId: Int, accessToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.getTeamsByUserId(userId, "Bearer $accessToken")
                val fetchedTeams = response.body() ?: emptyList()

                val dbTeams = teamsRepository.getAllTeams().first()

                val updatedTeams = fetchedTeams.filter { fetchedTeam ->
                    dbTeams.none { dbTeam ->
                        dbTeam.id == fetchedTeam.id && dbTeam.updatedAt == fetchedTeam.updatedAt
                    }
                }

                updatedTeams.forEach { team ->
                    val teamEntity = team.toTeam()
                    teamsRepository.insertOrUpdateTeam(teamEntity)
                    getMembersById(team.members, accessToken, team.id)
                    Log.d("TeamsViewModel", "Members insertion completed for Team: ${team.id}")
                }

                val teamsToDelete = dbTeams.filter { dbTeam ->
                    fetchedTeams.none { fetchedTeam -> fetchedTeam.id == dbTeam.id }
                }

                teamsToDelete.forEach { team ->
                    teamsRepository.deleteTeamById(team.id)
                    Log.d("TeamsViewModel", "Deleted TeamID: ${team.id}")
                }

                _teamsResult.value = Result.success(fetchedTeams)
                Log.d("TeamsViewModel", "Teams updated in the DB successfully")

                onComplete()

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error while getTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("TeamsViewModel", "Network error during getTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error while getTeamsByUserId", e)
                _teamsResult.value = Result.failure(e)
            }
        }
    }

    fun getTeamByIdFromDB(teamId: Int) {
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

    fun getTeamById(teamId: Int, accessToken: String, onComplete: () -> Unit){
        viewModelScope.launch{
            try {
                val response = service.getTeamById(teamId, "Bearer $accessToken")

                if (response.isSuccessful) {
                    Log.d("TeamsViewModel","Team $teamId was successfully fetched!")
                    _teamResponse.value = Response.success(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during getTeamById: ${response.code()} - $errorBody"
                    )
                    _teamResponse.value = Response.error(
                        response.code(),
                        errorBody?.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                }

                onComplete()
            }catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during findInviteById", e)
                _deleteResult.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during findInviteById", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun getAllTeamsFromDB(): Flow<List<Teams>> {
        Log.i("TeamsViewModel", "GetAllTeamsCalled")
        return teamsRepository.getAllTeams()
    }


    private suspend fun getMembersById(
        membersList: List<Member>,
        accessToken: String,
        teamId: Int
    ) {
        val ids = membersList.map { it.id }
        val roles = membersList.map { it.role }
        val usersList = mutableListOf<UserResponse>()

        ids.forEachIndexed { index, id ->
            try {
                val response = service.getMembersById(id, "Bearer $accessToken")
                usersList.add(response)
                _members.value = Result.success(usersList)
                val role = roles[index]

                val member = Members(
                    userId = id,
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

    fun deleteTeamsFromDB(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val deleteTeams = teamsRepository.deleteAllTeams()

                val deleteTeamsState = if (deleteTeams == Unit) "OK" else "ERROR"

                onComplete()

                Log.d("TeamsViewModel", "Delete Teams from DB: $deleteTeamsState")
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

    fun deleteTeamFromDB(teamId: Int) {
        viewModelScope.launch {
            try {
                val delete = teamsRepository.deleteTeamById(teamId)
                val deleteState = if (delete == Unit) "OK" else "ERROR"

                Log.d("TeamsViewModel", "Delete team ID($teamId): $deleteState")
            } catch (e: Exception) {
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

    fun createInvite(inviteRequest: InviteRequest, accessToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = invitesService.createInvite(inviteRequest, "Bearer $accessToken")

                if (response.isSuccessful) {
                    Log.d("TeamsViewModel","Invite sent to UserID: ${inviteRequest.recipientId} for teamID: ${inviteRequest.teamId}")
                    _inviteResult.value = Response.success(response.body())

                    val invite = response.body() ?: InviteResponse(1, 1, 1, 1, "", "", "")
                    val recipientUsername = invitesService.getUserById(invite.recipientId,"Bearer $accessToken").username
                    val senderUsername = invitesService.getUserById(invite.senderId, "Bearer $accessToken").username

                    val inviteDB = Invites(
                        id = invite.id,
                        senderId = invite.senderId,
                        senderUsername = senderUsername,
                        recipientId = invite.recipientId,
                        recipientUsername = recipientUsername,
                        teamId = invite.teamId,
                        status = invite.status,
                        createdAt = invite.createdAt,
                        updatedAt = invite.updatedAt
                    )

                    invitesRepository.insertOrUpdateInvite(inviteDB)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during createInvite: ${response.code()} - $errorBody"
                    )
                    _inviteResult.value = Response.error(
                        response.code(),
                        errorBody?.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                }

                onComplete()
            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during createInvite", e)
                _inviteResult.value = Response.error(
                    e.code(),
                    e.message?.toResponseBody("application/json".toMediaTypeOrNull())
                )
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during createInvite", e)
                _inviteResult.value = Response.error(
                    500,
                    e.message?.toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
        }
    }

    fun findInvitesByTeam(teamId: Int, accessToken: String) {
        viewModelScope.launch {
            try {
                val response = invitesService.findInvitesByTeam(teamId, "Bearer $accessToken")

                if(response.isSuccessful) {

                    response.body()?.forEach { invite ->
                        val recipientUsername = invitesService.getUserById(
                            invite.recipientId,
                            "Bearer $accessToken"
                        ).username
                        val senderUsername =
                            invitesService.getUserById(
                                invite.senderId,
                                "Bearer $accessToken"
                            ).username

                        val inviteDB = Invites(
                            id = invite.id,
                            senderId = invite.senderId,
                            senderUsername = senderUsername,
                            recipientId = invite.recipientId,
                            recipientUsername = recipientUsername,
                            teamId = invite.teamId,
                            status = invite.status,
                            createdAt = invite.createdAt,
                            updatedAt = invite.updatedAt
                        )

                        invitesRepository.insertOrUpdateInvite(inviteDB)
                    }

                    Log.d("TeamsViewModel", "Invites of $teamId were successfully stored")
                }
                else{
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during findInvitesByTeam: ${response.code()} - $errorBody"
                    )
                }

            } catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during findInvitesByTeam", e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during findInvitesByTeam", e)
            }
        }
    }

    fun getInvitesByTeam(teamId: Int) {
        viewModelScope.launch {
            try {
                val response = invitesRepository.getInvitesByTeam(teamId)
                _invitesList.value = response

            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during getInvitesByTeam", e)
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun findInviteById(inviteId:Int, accessToken: String, onComplete: () -> Unit){
        viewModelScope.launch{
            try {
                val response = invitesService.findInviteById(inviteId, "Bearer $accessToken")

                if (response.isSuccessful) {
                    Log.d("TeamsViewModel","Invite $inviteId was successfully fetched!")
                    _inviteResult.value = Response.success(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during findInviteById: ${response.code()} - $errorBody"
                    )
                    _inviteResult.value = Response.error(
                        response.code(),
                        errorBody?.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                }

                onComplete()
            }catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during findInviteById", e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during findInviteById", e)
            }
        }
    }

    fun acceptInvite(inviteId:Int, accessToken: String, onComplete: () -> Unit){
        viewModelScope.launch{
            try {
                val response = invitesService.acceptInvite(inviteId, "Bearer $accessToken")

                if (response.isSuccessful) {
                    Log.d("TeamsViewModel","Invite $inviteId was successfully accepted!")
                    _inviteResult.value = Response.success(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during acceptInvite: ${response.code()} - $errorBody"
                    )
                    _inviteResult.value = Response.error(
                        response.code(),
                        errorBody?.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                }

                onComplete()
            }catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during acceptInvite", e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during acceptInvite", e)
            }
        }
    }

    fun declineInvite(inviteId:Int, accessToken: String, onComplete: () -> Unit){
        viewModelScope.launch{
            try {
                val response = invitesService.declineInvite(inviteId, "Bearer $accessToken")

                if (response.isSuccessful) {
                    Log.d("TeamsViewModel","Invite $inviteId was declined!")
                    _inviteResult.value = Response.success(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "TeamsViewModel",
                        "Error during declineInvite: ${response.code()} - $errorBody"
                    )
                    _inviteResult.value = Response.error(
                        response.code(),
                        errorBody?.toResponseBody("application/json".toMediaTypeOrNull())
                    )
                }

                onComplete()
            }catch (e: HttpException) {
                Log.e("TeamsViewModel", "HTTP error during declineInvite", e)
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Error during declineInvite", e)
            }
        }
    }
}