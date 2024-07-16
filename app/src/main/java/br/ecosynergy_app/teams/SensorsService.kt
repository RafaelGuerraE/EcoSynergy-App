package br.ecosynergy_app.teams

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SensorsService {
    @GET("api/mq7Reading/v1")
    suspend fun fetchMq7Readings()

    @GET("api/mq7Reading/v1/{id}")
    suspend fun fetchMq7ReadingById(@Path("id") id:String)

    @GET("api/mq135Reading/v1")
    suspend fun fetchMq135Reading()

    @GET("api/mq135Reading/v1/{id}")
    suspend fun fetchMq135ReadingById(@Path("id") id:String)

    @GET("api/fireReading/v1")
    suspend fun fetchFireReading()

    @GET("api/fireReading/v1/{id}")
    suspend fun fetchFireReadingById(@Path("id") id:String)

}