package br.ecosynergy_app.teams.sectors

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ecosynergy_app.room.sectors.Activity
import br.ecosynergy_app.room.sectors.Sector
import br.ecosynergy_app.room.sectors.SectorRepository
import br.ecosynergy_app.room.sectors.SectorWithActivities
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SectorsViewModel(
    private val service: SectorsService,
    private val sectorRepository: SectorRepository,
) : ViewModel() {

    private val _activitiesResponse = MutableLiveData<ActivitiesResponse>()
    val activitiesResponse: LiveData<ActivitiesResponse> get() = _activitiesResponse

    private val _sectorsResponse = MutableLiveData<SectorsResponse>()
    val sectorsResponse: LiveData<SectorsResponse> get() = _sectorsResponse

    fun fetchAndStoreSectorsAndActivities(accessToken: String) {
        viewModelScope.launch {
            try {
                val sectorsResponse = service.getAllSectors("Bearer $accessToken")
                _sectorsResponse.value = sectorsResponse

                val sectors = sectorsResponse.map { sectorResponse ->
                    Sector(id = sectorResponse.id, name = sectorResponse.name)
                }

                val activities = sectorsResponse.flatMap { sectorResponse ->
                    sectorResponse.activities.map { activityResponse ->
                        Activity(
                            id = activityResponse.id,
                            name = activityResponse.name,
                            sectorId = sectorResponse.id
                        )
                    }
                }

                sectorRepository.insertSectorsAndActivities(sectors, activities)
                Log.d("SectorsViewModel", "Sectors and Activities stored in DB")

            } catch (e: HttpException) {
                Log.e("SectorsViewModel", "HTTP error while fetching sectors", e)
            } catch (e: Exception) {
                Log.e("SectorsViewModel", "Error while fetching sectors", e)
            }
        }
    }

    fun getAllSectorsWithActivities(): LiveData<List<SectorWithActivities>> {
        val sectorsWithActivitiesLiveData = MutableLiveData<List<SectorWithActivities>>()
        viewModelScope.launch {
            val sectorsWithActivities = sectorRepository.getAllSectorsWithActivities()
            sectorsWithActivitiesLiveData.postValue(sectorsWithActivities)
        }
        return sectorsWithActivitiesLiveData
    }

    fun getSectorWithActivities(sectorId: Int): LiveData<SectorWithActivities> {
        val sectorWithActivitiesLiveData = MutableLiveData<SectorWithActivities>()
        viewModelScope.launch {
            val sectorWithActivities = sectorRepository.getSectorWithActivities(sectorId)
            sectorWithActivitiesLiveData.postValue(sectorWithActivities)
        }
        return sectorWithActivitiesLiveData
    }

    fun deleteSectorsFromDB() {
        viewModelScope.launch {
            try {
                val deleteSectors = sectorRepository.deleteAllSectorsAndActivities()

                val deleteSectorsState = if (deleteSectors == Unit) "OK" else "ERROR"

                Log.d("TeamsViewModel", "Delete Sectors from DB: $deleteSectorsState")
            } catch (e: Exception) {
                Log.e("TeamsViewModel", "Unexpected error during deleteSectorsFromDB", e)
            }
        }
    }
}
