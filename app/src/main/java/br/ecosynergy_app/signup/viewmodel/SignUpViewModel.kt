package br.ecosynergy_app.signup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.HttpException
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SignUpViewModel(private val service: SignUpService): ViewModel() {

    private val _registerResult = MutableLiveData<Result<CreateUserResponse>>()
    val registerResult: LiveData<Result<CreateUserResponse>> = _registerResult

    private val _verificationCode = MutableLiveData<String>()
    val verificationCode: LiveData<String> = _verificationCode

    fun registerUser(createUserRequest: CreateUserRequest){
        viewModelScope.launch {
            try{
                val response = service.createUser(createUserRequest)
                Log.d("SignUpViewModel", "Request: $createUserRequest")
                Log.d("SignUpViewModel", "Register Successful: $response")
                _registerResult.value = Result.success(response)
            }
            catch(e: HttpException){
                Log.e("SignUpViewModel", "HTTP error during registerUser", e)
                _registerResult.value = Result.failure(e)
            }
            catch(e: Exception){
                Log.e("SignUpViewModel", "Unknown error during registerUser", e)
                _registerResult.value = Result.failure(e)
            }
        }
    }

    fun confirmationCode(userEmail: String, userFullname: String, onComplete: () -> Unit){
        viewModelScope.launch {
            try {
                val response = service.confirmationCode(userEmail, userFullname)
                val code = response.string()
                _verificationCode.value = code

                onComplete()
            }
            catch(e: HttpException){
                Log.e("SignUpViewModel", "HTTP error during confirmationCode", e)
            }
            catch(e: Exception){
                Log.e("SignUpViewModel", "Unknown error during confirmationCode", e)
            }
        }
    }

    fun forgotPasswordCode(userEmail: String, onComplete: () -> Unit){
        viewModelScope.launch {
            try {
                val response = service.forgotPasswordCode(userEmail)
                val code = response.string()
                _verificationCode.value = code

                onComplete()
            }
            catch(e: HttpException){
                Log.e("SignUpViewModel", "HTTP error during forgotPasswordCode", e)
            }
            catch(e: Exception){
                Log.e("SignUpViewModel", "Unknown error during forgotPasswordCode", e)
            }
        }
    }

    private val _usernameExists = MutableLiveData<Boolean>()
    val usernameExists: LiveData<Boolean> get() = _usernameExists

    fun checkUsernameExists(username: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.checkUsernameExists(username)
                val exists = response.string().toBoolean()
                _usernameExists.value = exists
                Log.d("SignUpViewModel", "UsernameExists: $exists")

                onComplete()
            } catch (e: HttpException) {
                Log.e("SignUpViewModel", "HTTP error during checkUsernameExists", e)
                _usernameExists.postValue(false)
            } catch (e: Exception) {
                Log.e("SignUpViewModel", "Unknown error during checkUsernameExists", e)
                _usernameExists.postValue(false)
            }
        }
    }

}