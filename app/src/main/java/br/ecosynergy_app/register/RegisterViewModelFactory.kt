package br.ecosynergy_app.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegisterViewModelFactory(private val service: RegisterService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}