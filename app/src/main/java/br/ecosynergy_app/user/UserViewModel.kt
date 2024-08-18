package br.ecosynergy_app.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.ApiError
import br.ecosynergy_app.home.PasswordRequest
import br.ecosynergy_app.home.UpdateRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserViewModel(private val service: UserService) : ViewModel() {

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> = _user
    private val _users = MutableLiveData<Result<MutableList<UserResponse>>>()
    val users: LiveData<Result<MutableList<UserResponse>>> = _users
    private val _delete = MutableLiveData<Result<Unit>>()
    val delete: LiveData<Result<Unit>> = _delete


    fun fetchUserData(identifier: String?, token: String?) {
        viewModelScope.launch {
            try {
                val response = service.getUserByUsername(identifier, "Bearer $token")
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

    fun getUserById(id: String?, token: String?) {
        viewModelScope.launch {
            try {
                val response = service.getUserById(id, "Bearer $token")
                Log.d("UserViewModel", "User data fetched successfully: $response")
                _user.value = Result.success(response)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during getUserById", e)
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during getUserById", e)
                _user.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during getUserById", e)
                _user.value = Result.failure(e)
            }
        }
    }

    fun getUsersByIds(ids: List<String>, token: String?) {
        viewModelScope.launch {
            val usersList = mutableListOf<UserResponse>()
            for (id in ids) {
                try {
                    val response = service.getUserById(id, "Bearer $token")
                    usersList.add(response)
                    _users.value = Result.success(usersList)
                } catch (e: HttpException) {
                    Log.e("UserViewModel", "HTTP error during getUsersByIds", e)
                    _user.value = Result.failure(e)
                } catch (e: IOException) {
                    Log.e("UserViewModel", "Network error during getUsersByIds", e)
                    _user.value = Result.failure(e)
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Unexpected error during getUsersByIds", e)
                    _user.value = Result.failure(e)
                }
            }

        }
    }

    fun deleteUserData(id: String, token: String?) {
        viewModelScope.launch {
            try {
                val response = service.deleteUser(id, "Bearer $token")
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "User deleted successfully")
                    _delete.value = Result.success(Unit)
                } else {
                    Log.e("UserViewModel", "Error deleting user: ${response.errorBody()?.string()}")
                    _delete.value = Result.failure(HttpException(response))
                }
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during deleteUserData", e)
                _delete.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during deleteUserData", e)
                _delete.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during deleteUserData", e)
                _delete.value = Result.failure(e)
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

    fun resetPassword(username: String, password: String, token: String) {
        viewModelScope.launch {
            try {
                val request = PasswordRequest(username, password)
                service.resetPassword("Bearer $token", request)
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

    fun searchUser(token: String?, username: String?) {
        viewModelScope.launch {
            Log.d("UserViewModel", "$token")
            try {
                val response = service.searchUser("Bearer $token", username)
                Log.d("UserViewModel", "Search Successful")
                _users.value = Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ApiError::class.java)
                Log.e("UserViewModel", "HTTP error during searchUser: ${errorResponse.error} at ${errorResponse.path}")
                _user.value = Result.failure(e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during searchUser", e)
                _user.value = Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during searchUser", e)
                _user.value = Result.failure(e)
            }
        }
    }
}
