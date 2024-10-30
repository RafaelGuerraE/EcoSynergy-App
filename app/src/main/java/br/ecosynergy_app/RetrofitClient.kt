package br.ecosynergy_app

import br.ecosynergy_app.user.viewmodel.UserService
import br.ecosynergy_app.signup.viewmodel.SignUpService
import br.ecosynergy_app.readings.ReadingsService
import br.ecosynergy_app.room.invites.Invites
import br.ecosynergy_app.teams.invites.InvitesService
import br.ecosynergy_app.teams.viewmodel.TeamsService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://ec2-44-220-83-117.compute-1.amazonaws.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val signUpService: SignUpService by lazy {
        retrofit.create(SignUpService::class.java)
    }

    val teamsService: TeamsService by lazy {
        retrofit.create(TeamsService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val readingsService: ReadingsService by lazy {
        retrofit.create(ReadingsService::class.java)
    }

    val invitesService: InvitesService by lazy {
        retrofit.create(InvitesService::class.java)
    }

}

