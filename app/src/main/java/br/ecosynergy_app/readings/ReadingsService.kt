package br.ecosynergy_app.readings

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReadingsService {

    @GET("api/mq7Reading/v1/team/{teamHandle}?direction=desc")
    suspend fun fetchMq7ReadingsByTeamHandle(
        @Path("teamHandle") teamHandle: String,
        @Header("Authorization") accessToken: String
    ): MQ7ReadingsResponse

    @GET("api/mq135Reading/v1/team/{teamHandle}?direction=desc")
    suspend fun fetchMq135ReadingsByTeamHandle(
        @Path("teamHandle") teamHandle: String,
        @Header("Authorization") accessToken: String
    ): MQ135ReadingsResponse

    @GET("api/fireReading/v1/team/{teamHandle}?direction=desc")
    suspend fun fetchFireReadingsByTeamHandle(
        @Path("teamHandle") teamHandle: String,
        @Header("Authorization") accessToken: String
    ): FireReadingsResponse

}