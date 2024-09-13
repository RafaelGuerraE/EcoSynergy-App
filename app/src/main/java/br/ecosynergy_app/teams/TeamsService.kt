package br.ecosynergy_app.teams

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TeamsService {
    //Basic Team Functions
    @POST("api/team/v1")
    suspend fun createTeam(@Header("Authorization") token: String?,
                           @Body request: TeamsRequest) : TeamsResponse

    @DELETE("api/team/v1/{id}")
    suspend fun deleteTeam(@Header("Authorization") token: String?,
                           @Path("id") id:Int)

    @PUT("api/team/v1/{id}")
    suspend fun updateTeam(@Header("Authorization") token: String?,
                           @Path("id") id:Int,
                           @Body request: UpdateRequest) : TeamsResponse

    //Finding Teams

    @GET("api/team/v1")
    suspend fun findAllTeams(@Header("Authorization") token: String?): AllTeamsResponse

    @GET("api/team/v1/id/{id}")
    suspend fun findTeamById(@Header("Authorization") token: String?,
                             @Path("id") id:String) : TeamsResponse

    @GET("api/team/v1/handle/{handle}")
    suspend fun findTeamByHandle(@Header("Authorization") token: String?,
                                 @Path("handle") handle:String?) : TeamsResponse

    @GET("api/team/v1/search/{handle}")
    suspend fun searchTeamByPartialHandle(@Header("Authorization") token: String?,
                                          @Path("handle") handle:String) : TeamsResponse

    //User Managing

    @GET("api/team/v1/user/{id}")
    suspend fun findTeamsByUserId(@Path("id") userId:Int, @Header("Authorization") token: String?,
                                ) : Response<List<TeamsResponse>>

    @POST("api/team/v1/{teamId}/user/{userId}")
    suspend fun addMember(@Header("Authorization") token: String?,
                          @Path("teamId") teamId: Int,
                          @Path("userId") userId: Int,
                          @Body request: RoleRequest) : TeamsResponse

    @DELETE("api/team/v1/{teamId}/user/{userId}")
    suspend fun removeMember(@Header("Authorization") token: String?,
                             @Path("teamId") teamId: Int,
                             @Path("userId") userId: Int) : Response<Result<Unit>>

    @PUT("api/team/v1/{teamId}/user/{userId}")
    suspend fun editMemberRole(@Header("Authorization") token: String?,
                             @Path("teamId") teamId: Int,
                             @Path("userId") userId: Int,
                             @Body request: RoleRequest): TeamsResponse
}