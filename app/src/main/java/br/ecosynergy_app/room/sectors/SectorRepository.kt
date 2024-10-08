package br.ecosynergy_app.room.sectors

class SectorRepository(private val sectorDao: SectorDao) {

    suspend fun insertSectorsAndActivities(sectors: List<Sector>, activities: List<Activity>) {
        sectorDao.insertSectors(sectors)
        sectorDao.insertActivities(activities)
    }

    suspend fun getAllSectorsWithActivities(): List<SectorWithActivities> {
        return sectorDao.getAllSectorsWithActivities()
    }

    suspend fun getSectorWithActivities(sectorId: Int): SectorWithActivities {
        return sectorDao.getSectorWithActivities(sectorId)
    }

    suspend fun deleteAllSectors() {
        sectorDao.deleteAllSectors()
    }

    suspend fun deleteAllActivities() {
        sectorDao.deleteAllActivities()
    }

    suspend fun deleteAllSectorsAndActivities() {
        sectorDao.deleteAllSectorsAndActivities()
    }
}
