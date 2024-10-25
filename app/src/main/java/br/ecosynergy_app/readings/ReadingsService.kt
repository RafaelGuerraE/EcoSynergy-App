package br.ecosynergy_app.readings

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReadingsService {

    //MQ7 Readings
    @GET("api/mq7Reading/v1/team/{teamHandle}")
    suspend fun fetchMq7ReadingsByTeamHandle(@Path("teamHandle") teamHandle:String, @Header("Authorization") token: String) : MQ7ReadingsResponse

    //MQ135 Readings
    @GET("api/mq135Reading/v1/team/{teamHandle}")
    suspend fun fetchMq135ReadingsByTeamHandle(@Path("teamHandle") teamHandle:String,
                                               @Header("Authorization") token: String
                                               ) : MQ135ReadingsResponse

    //Fire Readings
    @GET("api/fireReading/v1/team/{teamHandle}")
    suspend fun fetchFireReadingsByTeamHandle(@Path("teamHandle") teamHandle:String,
                                              @Header("Authorization") token: String): FireReadingsResponse
}