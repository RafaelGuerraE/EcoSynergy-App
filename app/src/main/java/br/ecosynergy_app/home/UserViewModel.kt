package br.ecosynergy_app.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserViewModel(private val service: UserService) : ViewModel() {

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> = _user

    fun fetchUserData(identifier: String, token: String) {
        viewModelScope.launch {
            try {
                val response = service.getUser(identifier, "Bearer $token")
                Log.d("UserViewModel", "User data fetched successfully: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during fetchUserData", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during fetchUserData", e)
                _user.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during fetchUserData", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun deleteUserData(id: Long) {
        viewModelScope.launch {
            try {
                val response = service.deleteUser(id)
                Log.d("UserViewModel", "User deleted successfully: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during deleteUserData", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during deleteUserData", e)
                _user.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during deleteUserData", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun updateUserData(id: String, token: String?, username: String, fullName: String, email: String, gender: String, nationality: String) {
        viewModelScope.launch {
            try {
                val request = UpdateRequest(username, fullName, email, gender, nationality)
                val response = service.updateUser(id, "Bearer $token", request)
                Log.d("UserViewModel", "User updated successfully: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during updateUserData", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during updateUserData", e)
                _user.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during updateUserData", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun recoverPassword(username: String, password: String, token: String) {
        viewModelScope.launch {
            try {
                val request = PasswordRequest(username, password)
                service.recoverPassword("Bearer $token", request)
                Log.d("UserViewModel", "Password recovery successful for user: $username")
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during recoverPassword", e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during recoverPassword", e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during recoverPassword", e)
            }
        }
    }
}
