package br.ecosynergy_app.home

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("api/user/v1/findUsername/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): UserResponse

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(
        @Path("username") username: String)

    @POST("api/user/v1/recoverPassword")
    suspend fun recoverPassword(
        @Header("Authorization")token: String,
        @Body request: PasswordRequest)

    @DELETE("api/user/v1/{id}")
    suspend fun deleteUser(@Path("id") id: Long) : UserResponse

    @PUT("api/user/v1/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: UpdateRequest
    ) : UserResponse
}
