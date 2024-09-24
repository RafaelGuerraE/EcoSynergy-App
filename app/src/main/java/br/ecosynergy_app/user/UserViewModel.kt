package br.ecosynergy_app.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.ApiError
import br.ecosynergy_app.home.PasswordRequest
import br.ecosynergy_app.home.UpdateRequest
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.LoginResponse
import br.ecosynergy_app.room.User
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.room.toUser
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserViewModel(
    private val service: UserService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> = _user

    private val _users = MutableLiveData<Result<MutableList<UserResponse>>>()
    val users: LiveData<Result<MutableList<UserResponse>>> = _users

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

    private val _refreshResult = MutableLiveData<Result<LoginResponse>>()
    val refreshResult: LiveData<Result<LoginResponse>> get() = _refreshResult

    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> get() = _userInfo

    fun loginUser(loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                val loginResponse = service.loginUser(loginRequest)
                _loginResult.value = Result.success(loginResponse)

                getUserByUsername(loginRequest.identifier, loginResponse.accessToken, loginResponse.refreshToken)

            } catch (e: HttpException) {
                _loginResult.value = Result.failure(e)
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun refreshToken(username: String, refreshToken: String){
        viewModelScope.launch {
            try {
                val refreshResponse = service.refreshToken(username, "Bearer $refreshToken")
                _refreshResult.value = Result.success(refreshResponse)

                getUserByUsername(username, refreshResponse.accessToken, refreshResponse.refreshToken)

                Log.d("UserViewModel", "RefreshTokenOK")

            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during refreshToken", e)
                _refreshResult.value = Result.failure(IOException("Network error, please check your connection", e))
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during refreshToken", e)
                _refreshResult.value = Result.failure(HttpException(e.response()))
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during refreshToken", e)
                _refreshResult.value = Result.failure(Exception("Unexpected error occurred", e))
            }
        }
    }

    private fun getUserByUsername(username: String, access: String, refresh: String) {
        viewModelScope.launch {
            try {
                val response = service.getUserByUsername(username, "Bearer $access")
                _user.value = Result.success(response)

                Log.d("UserViewModel", "User data fetched: $response")
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during getUserByUsername", e)
                _user.value = Result.failure(IOException("Network error, please check your connection", e))
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during getUserByUsername", e)
                _user.value = Result.failure(HttpException(e.response()))
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during getUserByUsername", e)
                _user.value = Result.failure(Exception("Unexpected error occurred", e))
            }
        }
    }

    fun insertUserInfoDB(response: UserResponse, access: String, refresh: String){
        viewModelScope.launch {
            val userStored = response.toUser(access, refresh)
            userRepository.insertUser(userStored)

            Log.d("UserViewModel", "User inserted in DB: $userStored")
        }
    }

    fun updateUserInfoDB(userId: Int,
                         newUsername: String,
                         newFullName: String,
                         newEmail: String,
                         newGender: String,
                         newNationality: String,
                         newAccessToken: String,
                         newRefreshToken: String,
                         onComplete: () -> Unit){
        viewModelScope.launch {
            try {
                userRepository.updateUser(userId, newUsername, newFullName, newEmail, newGender, newNationality, newAccessToken, newRefreshToken)
                //getUserInfoFromDB()
                //Log.d("UserViewModel", "UpdatedInfo: $userId, $newUsername, $newFullName, $newEmail, $newGender, $newNationality, $newAccessToken, $newRefreshToken")
                Log.d("UserViewModel", "Update user info completed on DB")

                onComplete()
            }
             catch (e: IOException) {
                Log.e("UserViewModel", "Network error during updateUserInfoDB", e)
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during updateUserInfoDB", e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during updateUserInfoDB", e)
            }
        }
    }

    fun updateUserData(userId: Int, accessToken: String, refreshToken: String, username: String, fullName: String, email: String, gender: String, nationality: String) {
        viewModelScope.launch {
            try {
                val request = UpdateRequest(username, fullName, email, gender, nationality)
                val response = service.updateUser(userId, "Bearer $accessToken", request)

                Log.d("UserViewModel", "User updated successfully on API")
                _user.value = Result.success(response)

                updateUserInfoDB(
                    userId,
                    username,
                    fullName,
                    email,
                    gender,
                    nationality,
                    accessToken,
                    refreshToken
                ){}
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

    fun getUserInfoFromDB(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = userRepository.getUser()
                _userInfo.value = response

                Log.d("UserViewModel", "User got from DB: $response")
                onComplete()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during getUserInfo", e)
            }
        }
    }

    fun deleteUserInfoFromDB() {
        viewModelScope.launch {
            try {
                val delete = userRepository.deleteUser()
                val deleteState = if(delete == Unit) "OK" else "ERROR"
                Log.d("UserViewModel", "DeleteUserInfo: $deleteState")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during deleteUserInfo", e)
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

    fun deleteUserData(id: Int, token: String?) {
        viewModelScope.launch {
            try {
                val response = service.deleteUser(id, "Bearer $token")
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "User deleted successfully from API")
                } else {
                    Log.e("UserViewModel", "Error deleting user: ${response.errorBody()?.string()}")
                }

                deleteUserInfoFromDB()

            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during deleteUserData", e)
            } catch (e: IOException) {
                Log.e("UserViewModel", "Network error during deleteUserData", e)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during deleteUserData", e)
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
