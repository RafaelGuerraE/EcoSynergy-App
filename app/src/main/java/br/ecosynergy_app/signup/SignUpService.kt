package br.ecosynergy_app.signup

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SignUpService {
    @POST("auth/signup")
    suspend fun createUser(@Body createUserRequest: CreateUserRequest): CreateUserResponse

    @POST("auth/signup/send-confirmation-code")
    suspend fun confirmationCode(@Query("email") userEmail: String): ResponseBody

    @POST("auth/forgot-password/send-confirmation-code")
    suspend fun forgotPasswordCode(@Query("email") userEmail: String): ResponseBody

    @GET("api/user/v1/exists/{username}")
    suspend fun checkUsernameExists(@Path("username") username:String): ResponseBody
}