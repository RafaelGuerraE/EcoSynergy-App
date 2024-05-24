package br.ecosynergy_app.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.UserResponse
import okio.IOException

class UserViewModel : ViewModel() {

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> = _user

    fun fetchUserData(username: String, token: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userService.getUser(username, "Bearer $token")
                Log.d("UserViewModel", "Login successful: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during login", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "HTTP error during login", e)
                _user.value = Result.failure(e)
            }
        }
    }
}
