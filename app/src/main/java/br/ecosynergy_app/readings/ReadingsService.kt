package br.ecosynergy_app.readings

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReadingsService {
    //MQ7 Readings
    @POST("api/mq7Reading/v1/{id}")
    suspend fun createMq7Reading(@Path("id")id:String) : ReadingVO

    @GET("api/mq7Reading/v1")
    suspend fun fetchMq7Readings(@Header("Authorization") token: String) : MQ7ReadingsResponse

    @GET("api/mq7Reading/v1/{id}")
    suspend fun fetchMq7ReadingById(@Header("Authorization") token: String,
                                    @Path("id") id:String) : MQ7ReadingsResponse

    @GET("api/mq7Reading/v1/team/{teamHandle}")
    suspend fun fetchMq7ReadingsByTeamHandle(@Path("teamHandle") teamHandle:String, @Header("Authorization") token: String) : MQ7ReadingsResponse

    //MQ135 Readings
    @POST("api/mq135Reading/v1/{id}")
    suspend fun createMq135Reading(@Path("id")id:String) : ReadingVO

    @GET("api/mq135Reading/v1")
    suspend fun fetchMq135Reading(@Header("Authorization") token: String) : MQ135ReadingsResponse

    @GET("api/mq135Reading/v1/{id}")
    suspend fun fetchMq135ReadingById(@Header("Authorization") token: String,
                                      @Path("id") id:String) : MQ135ReadingsResponse

    @GET("api/mq135Reading/v1/team/{teamHandle}")
    suspend fun fetchMq135ReadingsByTeamHandle(@Header("Authorization") token: String,
                                               @Path("teamHandle") teamHandle:String) : MQ135ReadingsResponse

    //Fire Readings
    @POST("api/fireReading/v1/{id}")
    suspend fun createFireReading(@Path("id")id:String) : ReadingVO

    @GET("api/fireReading/v1")
    suspend fun fetchFireReading(@Header("Authorization") token: String): FireReadingsResponse

    @GET("api/fireReading/v1/{id}")
    suspend fun fetchFireReadingById(@Header("Authorization") token: String,
                                     @Path("id") id:String): FireReadingsResponse

    @GET("api/fireReading/v1/team/{teamHandle}")
    suspend fun fetchFireReadingsByTeamHandle(@Header("Authorization") token: String,
                                             @Path("teamHandle") teamHandle:String): FireReadingsResponse
}