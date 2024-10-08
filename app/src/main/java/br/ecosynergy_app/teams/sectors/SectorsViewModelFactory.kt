package br.ecosynergy_app.teams.sectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.room.sectors.SectorRepository

class SectorsViewModelFactory(private val service: SectorsService,
                            private val sectorRepository: SectorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SectorsViewModel::class.java)) {
            return SectorsViewModel(service, sectorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}