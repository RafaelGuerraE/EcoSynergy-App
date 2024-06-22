package br.ecosynergy_app.login

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val service: AuthService) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

    private val _checkUserExistenceResult = MutableLiveData<Result<LoginResponse>>()
    val checkUserExistenceResult: LiveData<Result<LoginResponse>> get() = _checkUserExistenceResult

    private val _registerUserResult = MutableLiveData<Result<LoginResponse>>()
    val registerUserResult: LiveData<Result<LoginResponse>> get() = _registerUserResult

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
