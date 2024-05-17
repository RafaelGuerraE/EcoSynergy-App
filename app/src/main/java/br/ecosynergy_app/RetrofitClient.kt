package br.ecosynergy_app

import androidx.lifecycle.ViewModel
import br.ecosynergy_app.login.AuthService
import br.ecosynergy_app.login.AuthViewModel
import br.ecosynergy_app.teams.TeamsService
import com.google.android.gms.auth.api.Auth
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://ec2-44-214-120-49.compute-1.amazonaws.com"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val authService: AuthService by lazy {
            retrofit.create(AuthService::class.java)
        }

        val teamsService: TeamsService by lazy {
            retrofit.create(TeamsService::class.java)
        }

    }

