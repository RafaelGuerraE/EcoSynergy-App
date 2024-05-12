package br.ecosynergy_app.login

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

//URL = http://ec2-44-214-120-49.compute-1.amazonaws.com
interface AuthService {

    @POST("auth/signup")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest)

    @POST("auth/signin")
    suspend fun loginUser(@Body loginUserRequest: LoginRequest)

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(@Path("username") username: String)

    @PUT("auth/delete/{username}")
    suspend fun deleteUser(@Path("username") username: String)
}
