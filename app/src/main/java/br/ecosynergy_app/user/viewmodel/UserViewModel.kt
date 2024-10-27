package br.ecosynergy_app.user.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.home.PasswordRequest
import br.ecosynergy_app.home.UpdateRequest
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.login.LoginResponse
import br.ecosynergy_app.room.user.User
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.room.user.toUser
import br.ecosynergy_app.user.UserResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UserViewModel(
    private val service: UserService,
    val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> = _user

    private val _fcmRequest = MutableLiveData<Response<Void>>()
    val fcmRequest: LiveData<Response<Void>> get() = _fcmRequest

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

    fun searchUser(username: String, accessToken: String) {
        viewModelScope.launch {
            try {
                val response = service.searchUser(username, "Bearer $accessToken")
                _users.value = Result.success(response)

                Log.d("UserViewModel", "Search users Successful")
            } catch (e: HttpException) {
                Log.e("UserViewModel", "HTTP error during searchUser", e)
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

    fun saveOrUpdateFcmToken(accessToken:String, userId: Int, fcmToken: String, deviceType: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.saveOrUpdateFcmToken("Bearer $accessToken", userId, fcmToken, deviceType ,"2024-12-31T12:20:20Z")
                if (response.isSuccessful) {
                    _fcmRequest.value = response
                    Log.d("UserViewModel", "FCM Token saved or updated successfully for userId: $userId")
                } else {
                    _fcmRequest.value = response
                    Log.e("UserViewModel", "Failed to save or update FCM Token: ${response.errorBody()?.string()}")
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during saveOrUpdateFcmToken", e)
            }
        }
    }

    fun getFCMTokenByUserIdAndDeviceType(userId: Int, deviceType: String) {
        viewModelScope.launch {
            try {
                val response = service.getFCMTokenByUserIdAndDeviceType(userId, deviceType)
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "FCM Token retrieved successfully for userId: $userId")
                } else {
                    Log.e("UserViewModel", "Failed to retrieve FCM Token: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during getFCMTokenByUserIdAndDeviceType", e)
            }
        }
    }

    fun removeFCMToken(userId: Int, deviceType: String) {
        viewModelScope.launch {
            try {
                val response = service.removeFCMToken(userId, deviceType)
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "FCM Token removed successfully for userId: $userId")
                } else {
                    Log.e("UserViewModel", "Failed to remove FCM Token: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during removeFCMToken", e)
            }
        }
    }

    fun removeAllFCMTokens(userId: Int) {
        viewModelScope.launch {
            try {
                val response = service.removeAllFCMTokens(userId)
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "All FCM Tokens removed successfully for userId: $userId")
                } else {
                    Log.e("UserViewModel", "Failed to remove all FCM Tokens: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Unexpected error during removeAllFCMTokens", e)
            }
        }
    }
}
