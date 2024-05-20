package br.ecosynergy_app.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val service: AuthService) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginRequest>>()
    val loginResult: LiveData<Result<LoginRequest>> = _loginResult

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