package br.ecosynergy_app.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.login.AuthService
import br.ecosynergy_app.login.AuthViewModel

class SensorsViewModelFactory(private val service: SensorsService): ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorsViewModel::class.java)) {
            return SensorsViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}