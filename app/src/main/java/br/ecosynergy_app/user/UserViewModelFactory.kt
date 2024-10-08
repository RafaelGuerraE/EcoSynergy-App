package br.ecosynergy_app.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.room.user.UserRepository

class UserViewModelFactory(
    private val service: UserService,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(service, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}