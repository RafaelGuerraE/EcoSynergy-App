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
                Log.d("UserViewModel LOGIN", "Login successful: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel LOGIN", "HTTP error during login", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel LOGIN", "HTTP error during login", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun deleteUserData(id: Long){
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userService.deleteUser(id)
                Log.d("UserViewModel DELETE", "User successfully deleted: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel DELETE", "HTTP error during DeleteUser", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel DELETE", "HTTP error during DeleteUser", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun recoverPassword(username: String, password: String, token: String) {
        viewModelScope.launch {
            try {
                val request = PasswordRequest(username, password)
                RetrofitClient.userService.recoverPassword("Bearer $token", request)
                Log.d("UserViewModel RECOVER", "Password recovery successful for user: $username")
            } catch (e: HttpException) {
                Log.e("UserViewModel RECOVER", "HTTP error during password recovery", e)
            } catch (e: IOException) {
                Log.e("UserViewModel RECOVER", "Network error during password recovery", e)
            }
        }
    }
}
