package br.ecosynergy_app

import br.ecosynergy_app.user.UserService
import br.ecosynergy_app.signup.RegisterService
import br.ecosynergy_app.readings.ReadingsService
import br.ecosynergy_app.teams.TeamsService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://ec2-44-220-83-117.compute-1.amazonaws.com/"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val registerService: RegisterService by lazy{
            retrofit.create(RegisterService::class.java)
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

    }

