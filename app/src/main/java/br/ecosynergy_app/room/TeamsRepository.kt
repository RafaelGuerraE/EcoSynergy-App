package br.ecosynergy_app.room

class TeamsRepository(private val teamsDao: TeamsDao) {
    suspend fun insertTeam(team: Teams) {
        teamsDao.insertTeam(team)
    }

    suspend fun deleteTeam(team: Teams) {
        teamsDao.deleteTeam(team)
    }

    suspend fun getTeams(): Teams? {
        return teamsDao.getTeams()
    }
}