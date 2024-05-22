// UserService.kt
package br.ecosynergy_app

import br.ecosynergy_app.home.UserResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserService {
    @GET("api/user/v1/findUsername/{username}")
    fun getUser(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): Call<UserResponse>
}
