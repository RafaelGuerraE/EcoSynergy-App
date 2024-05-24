package br.ecosynergy_app.home

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserService {
    @GET("api/user/v1/findUsername/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): UserResponse
}
