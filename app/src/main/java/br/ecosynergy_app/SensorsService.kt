package br.ecosynergy_app

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SensorService {
    @GET("api/mq7Reading/v1")
    suspend fun fetchMq7Readings()

    @GET("api/mq7Reading/v1/{id}")
    suspend fun fetchMq7Reading(@Path("id") id:String ="")

    @GET("api/mq135Reading/v1")
    suspend fun fetchMq135Readings()

    @GET("api/mq135Reading/v1/{id}")
    suspend fun fetchMq135Reading(@Path("id") id:String ="")

    @GET("api/fireReading/v1")
    suspend fun fetchFireReadings()

    @GET("api/fireReading/v1/{id}")
    suspend fun fetchFireReading(@Path("id") id:String ="")

}