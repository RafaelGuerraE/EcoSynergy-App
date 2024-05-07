package br.ecosynergy_app

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

//http://ec2-44-214-120-49.compute-1.amazonaws.com
interface AuthService {

    @GET("auth/signup")
    suspend fun createUser()
    @POST("auth/signin")
    suspend fun loginUser()
    @PUT("auth/refresh/{username}")
    suspend fun deleteUser(@Path("username") username:String = "")

}