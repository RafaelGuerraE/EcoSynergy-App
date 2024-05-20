package br.ecosynergy_app.login

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthService {

    @POST("auth/signup")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest)

    @POST("auth/signin")
    suspend fun loginUser(@Body loginUserRequest: LoginRequest): LoginRequest

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(@Path("username") username: String)

    @DELETE("auth/delete/{username}")
    suspend fun deleteUser(@Path("username") username: String)
}
