package br.ecosynergy_app.login

import android.util.Log
import androidx.lifecycle.*
import br.ecosynergy_app.RetrofitClient.authService
import br.ecosynergy_app.home.UserResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val service: AuthService) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

    private val _checkUserExistenceResult = MutableLiveData<Result<UserResponse>>()
    val checkUserExistenceResult: LiveData<Result<UserResponse>> get() = _checkUserExistenceResult

    private val _registerUserResult = MutableLiveData<Result<UserResponse>>()
    val registerUserResult: LiveData<Result<UserResponse>> get() = _registerUserResult

    fun loginUser(loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = service.loginUser(loginRequest)
                _loginResult.value = Result.success(response)
            } catch (e: HttpException) {
                _loginResult.value = Result.failure(e)
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun checkUserExistence(email: String) {
        viewModelScope.launch {
            try {
                val response = service.checkUser(email)
                _checkUserExistenceResult.value = Result.success(response)
            } catch (e: HttpException) {
                _checkUserExistenceResult.value = Result.failure(e)
            } catch (e: Exception) {
                _checkUserExistenceResult.value = Result.failure(e)
            }
        }
    }

    fun registerUser(userData: GoogleUserData) {
        viewModelScope.launch {
            try {
                val response = service.registerUser(userData)
                _registerUserResult.value = Result.success(response)
            } catch (e: HttpException) {
                _registerUserResult.value = Result.failure(e)
            } catch (e: Exception) {
                _registerUserResult.value = Result.failure(e)
            }
        }
    }
}
