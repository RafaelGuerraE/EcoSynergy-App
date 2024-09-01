package br.ecosynergy_app.login

import br.ecosynergy_app.user.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthService {
    @POST("auth/signin")
    suspend fun loginUser(@Body loginUserRequest: LoginRequest):LoginResponse

    @GET("api/user/v1/username/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String?,
        @Header("Authorization") token: String?
    ): UserResponse
}
