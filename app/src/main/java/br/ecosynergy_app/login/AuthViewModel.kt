package br.ecosynergy_app.login

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val service: AuthService) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> get() = _loginResult

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
}
