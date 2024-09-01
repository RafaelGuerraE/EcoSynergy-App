package br.ecosynergy_app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.room.UserRepository

class AuthViewModelFactory(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authService, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
