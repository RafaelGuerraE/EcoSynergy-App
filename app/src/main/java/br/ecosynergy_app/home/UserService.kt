package br.ecosynergy_app.home

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("api/user/v1/findUsername/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): UserResponse

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(@Path("username") username: String)

    @DELETE("auth/delete/{username}")
    suspend fun deleteUser(@Path("username") username: String)
}
