package br.ecosynergy_app.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SignUpViewModelFactory(private val service: SignUpService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}