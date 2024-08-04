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
    @GET("api/user/v1/findUsername/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String?,
        @Header("Authorization") token: String?
    ): UserResponse

    @GET("api/user/v1/findId/{id}")
    suspend fun getUserById(
        @Path("id") id: String?,
        @Header("Authorization") token: String
    ): UserResponse

    @POST("api/user/v1/recoverPassword")
    suspend fun recoverPassword(
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
}