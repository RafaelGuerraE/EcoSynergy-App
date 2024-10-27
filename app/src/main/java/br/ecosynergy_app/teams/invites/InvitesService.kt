package br.ecosynergy_app.teams.invites

import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InvitesService {

    @POST("api/invite/v1")
    suspend fun createInvite(@Body inviteRequest: InviteRequest,
                             @Header("Authorization") accessToken: String) : Response<InviteResponse>

    @GET("api/invite/v1/team/{teamId}")
    suspend fun findInvitesByTeam(@Path("teamId") teamId: Int,
                                  @Header("Authorization") accessToken: String): List<InviteResponse>

    @PUT("api/invite/v1/accept/{inviteId}")
    suspend fun acceptInvite(@Path("inviteId") inviteId:Int,
                             @Header("Authorization") accessToken: String): InviteResponse

    @PUT("api/invite/v1/decline/{inviteId}")
    suspend fun declineInvite(@Path("inviteId") inviteId:Int,
                             @Header("Authorization") accessToken: String): InviteResponse

    @GET("api/invite/v1")
    suspend fun getAllInvites(@Header("Authorization") accessToken: String): InviteApiResponse

    @GET("api/invite/v1/id/{id}")
    suspend fun findInviteById(@Path("id") inviteId:Int,
                               @Header("Authorization") accessToken: String)

    @GET("api/invite/v1/pending/recipient/{recipientId}")
    suspend fun findPendingInvitesByRecipient(@Path("recipientId") recipientId:Int,
                                              @Header("Authorization") accessToken: String)

    @GET("api/invite/v1/sent/sender/{senderId}")
    suspend fun findInvitesSentBySender(@Path("senderId") senderId:Int,
                                        @Header("Authorization") accessToken: String)

    @GET("api/invite/v1/received/recipient/{recipientId}")
    suspend fun findInvitesReceivedByRecipient(@Path("recipientId") recipientId:Int,
                                               @Header("Authorization") accessToken: String)

}