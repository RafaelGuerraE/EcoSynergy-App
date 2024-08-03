package br.ecosynergy_app.login

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthService {
    @POST("auth/signin")
    suspend fun loginUser(@Body loginUserRequest: LoginRequest):LoginResponse

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(
        @Path("username") username: String) : LoginResponse
}
