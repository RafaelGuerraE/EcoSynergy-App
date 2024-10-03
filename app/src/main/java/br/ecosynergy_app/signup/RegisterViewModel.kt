package br.ecosynergy_app.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.HttpException
import kotlinx.coroutines.launch

class RegisterViewModel(private val service: RegisterService): ViewModel() {

    private val _registerResult = MutableLiveData<Result<CreateUserResponse>>()
    val registerResult: LiveData<Result<CreateUserResponse>> = _registerResult

    fun registerUser(createUserRequest: CreateUserRequest){
        viewModelScope.launch {
            try{
                val response = service.createUser(createUserRequest)
                Log.d("RegisterViewModel", "Register Successful: $response")
                _registerResult.value = Result.success(response)
            }
            catch(e: HttpException){
                Log.e("RegisterViewModel", "HTTP error during login", e)
                _registerResult.value = Result.failure(e)
            }
            catch(e: Exception){
                Log.e("RegisterViewModel", "Unknown error during login", e)
                _registerResult.value = Result.failure(e)
            }
        }
    }

}