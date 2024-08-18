package br.ecosynergy_app.user

import br.ecosynergy_app.home.PasswordRequest
import br.ecosynergy_app.home.UpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("api/user/v1/username/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String?,
        @Header("Authorization") token: String?
    ): UserResponse

    @GET("api/user/v1/id/{id}")
    suspend fun getUserById(
        @Path("id") id: String?,
        @Header("Authorization") token: String
    ): UserResponse

    @GET("api/user/v1/email/{email}")
    suspend fun getUserByEmail(
        @Path("email") email: String?,
        @Header("Authorization") token: String
    ): UserResponse

    @POST("api/user/v1/resetPassword")
    suspend fun resetPassword(
        @Header("Authorization")token: String,
        @Body request: PasswordRequest
    )

    @DELETE("api/user/v1/{id}")
    suspend fun deleteUser(@Path("id") id: String,
                           @Header("Authorization") token: String): Response<Result<Unit>>

    @PUT("api/user/v1/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: UpdateRequest
    ) : UserResponse

    @GET("api/user/v1/search/{username}")
    suspend fun searchUser(@Header("Authorization") token: String?,
                           @Path("username") username: String?): MutableList<UserResponse>
}
