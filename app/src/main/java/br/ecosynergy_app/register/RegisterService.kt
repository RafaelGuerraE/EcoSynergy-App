package br.ecosynergy_app.register

import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterService {
    @POST("auth/signup")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): CreateUserResponse
}