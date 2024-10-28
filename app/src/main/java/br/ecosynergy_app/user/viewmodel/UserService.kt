package br.ecosynergy_app.user.viewmodel

import br.ecosynergy_app.home.PasswordRequest
import br.ecosynergy_app.home.UpdateRequest
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.LoginResponse
import br.ecosynergy_app.user.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @POST("auth/signin")
    suspend fun loginUser(@Body loginUserRequest: LoginRequest): Response<LoginResponse>

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(
        @Path("username") username: String,
        @Header("Authorization") refreshToken: String
    ): LoginResponse

    @GET("api/user/v1/username/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String?,
        @Header("Authorization") token: String?
    ): UserResponse

    @GET("api/user/v1/id/{id}")
    suspend fun getUserById(
        @Path("id") id: String?,
        @Header("Authorization") token: String
    ): UserResponse

    @POST("api/user/v1/resetPassword")
    suspend fun resetPassword(
        @Header("Authorization") token: String,
        @Body request: PasswordRequest
    )

    @DELETE("api/user/v1/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<Result<Unit>>

    @PUT("api/user/v1/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body request: UpdateRequest
    ): UserResponse

    @GET("api/user/v1/search/{username}")
    suspend fun searchUser(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): MutableList<UserResponse>

    @POST("api/token/v1/save")
    suspend fun saveOrUpdateFcmToken(
        @Header("Authorization") accessToken: String,
        @Query("userId") userId: Int,
        @Query("fcmToken") fcmToken: String,
        @Query("expiresAt") expiresAt:String,
        @Query("deviceType") deviceType: String = "android"
    ): Response<Void>

    @POST("api/token/v1/remove")
    suspend fun removeFCMToken(
        @Query("userId") userId: Int,
        @Header("Authorization") accessToken: String,
        @Query("deviceType") deviceType: String = "android"
    ): Response<Void>

    @POST("api/token/v1/remove-all")
    suspend fun removeAllFCMTokens(@Query("userId") userId: Int): Response<Void>

    @GET("api/token/v1/get")
    suspend fun getFCMTokenByUserIdAndDeviceType(
        @Query("userId") userId: Int,
        @Query("deviceType") deviceType: String = "android"
    ): Response<Void>
}