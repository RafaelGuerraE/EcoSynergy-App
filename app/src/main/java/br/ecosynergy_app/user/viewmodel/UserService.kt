package br.ecosynergy_app.user.viewmodel

import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.LoginResponse
import br.ecosynergy_app.user.ForgotRequest
import br.ecosynergy_app.user.PasswordRequest
import br.ecosynergy_app.user.PreferencesResponse
import br.ecosynergy_app.user.UpdatePreferencesRequest
import br.ecosynergy_app.user.UpdateRequest
import br.ecosynergy_app.user.UserResponse
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
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): UserResponse

    @POST("api/user/v1/resetPassword")
    suspend fun resetPassword(
        @Header("Authorization") token: String,
        @Body request: PasswordRequest
    )

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body forgotRequest: ForgotRequest
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
    ): Response<UserResponse>

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
        @Query("platform") platform: String = "ANDROID"
    ): Response<Void>

    @DELETE("api/token/v1/remove")
    suspend fun removeFCMToken(
        @Header("Authorization") accessToken: String,
        @Query("platform") platform: String = "ANDROID"
    ): Response<Void>

    @POST("api/token/v1/remove-all")
    suspend fun removeAllFCMTokens(@Query("userId") userId: Int): Response<Void>

    @GET("api/token/v1/get")
    suspend fun getFCMTokenByUserIdAndDeviceType(
        @Query("userId") userId: Int,
        @Header("Authorization") accessToken: String,
        @Query("platform") platform: String = "ANDROID"
    ): Response<Void>

    @GET("api/notifications/preferences/v1/")
    suspend fun getNotificationPreferencesByUser(
        @Header("Authorization") accessToken: String
    ): Response<List<PreferencesResponse>>

    @GET("api/notifications/preferences/v1/{platform}")
    suspend fun getNotificationPreferencesByPlatform(
        @Query("userId") userId: Int,
        @Header("Authorization") accessToken: String,
        @Path("platform") platform: String = "ANDROID"
    ): Response<Void>

    @PUT("api/notifications/preferences/v1/")
    suspend fun updateNotificationPreferences(
        @Body updatePreferencesRequest: UpdatePreferencesRequest,
        @Header("Authorization") accessToken: String
    ): Response<PreferencesResponse>
}