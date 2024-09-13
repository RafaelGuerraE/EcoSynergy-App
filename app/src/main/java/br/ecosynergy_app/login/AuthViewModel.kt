package br.ecosynergy_app.login

import android.util.Log
import androidx.lifecycle.*
import br.ecosynergy_app.room.User
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.room.toUser
import br.ecosynergy_app.user.UserResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(
    private val service: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

    private val _user = MutableLiveData<Result<UserResponse>>()
    val user: LiveData<Result<UserResponse>> get() = _user

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

    fun getUserByUsername(username: String?, access: String?, refresh: String?) {
        viewModelScope.launch {
            try {
                val response = service.getUserByUsername(username, "Bearer $access")
                _user.value = Result.success(response)

                val userStored = response.toUser(access, refresh)
                userRepository.insertUser(userStored)

                Log.d("AuthViewModel", "User data fetched: $response")
                Log.d("AuthViewModel", "UserRepository: $userStored")
            } catch (e: IOException) {
                Log.e("AuthViewModel", "Network error during getUserByUsername", e)
                _user.value = Result.failure(IOException("Network error, please check your connection", e))
            } catch (e: HttpException) {
                Log.e("AuthViewModel", "HTTP error during getUserByUsername", e)
                _user.value = Result.failure(HttpException(e.response()))
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during getUserByUsername", e)
                _user.value = Result.failure(Exception("Unexpected error occurred", e))
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            try {
                val response = userRepository.getUser()
                _userInfo.value = response

                Log.d("AuthViewModel", "GetUserInfo: $response")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during getUserInfo", e)
            }
        }
    }

    fun deleteUserInfo() {
        viewModelScope.launch {
            try {
                val delete = userRepository.deleteUser()
                val deleteState = if(delete == Unit) "OK" else "ERROR"
                Log.d("AuthViewModel", "DeleteUserInfo: $deleteState")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Unexpected error during deleteUserInfo", e)
            }
        }
    }

}
