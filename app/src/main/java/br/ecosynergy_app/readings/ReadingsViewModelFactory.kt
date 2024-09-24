package br.ecosynergy_app.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.room.ReadingsRepository

class ReadingsViewModelFactory(
    private val service: ReadingsService,
    private val readingsRepository: ReadingsRepository
): ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadingsViewModel::class.java)) {
            return ReadingsViewModel(service,
                readingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}