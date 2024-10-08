package br.ecosynergy_app.teams.sectors

import retrofit2.http.GET
import retrofit2.http.Header

interface SectorsService {

    @GET("api/activity/v1")
    suspend fun getAllActivities(@Header("Authorization")accessToken: String): ActivitiesResponse

    @GET("api/sector/v1")
    suspend fun getAllSectors(@Header("Authorization")accessToken: String): SectorsResponse

}