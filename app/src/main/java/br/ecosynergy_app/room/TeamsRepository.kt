package br.ecosynergy_app.room

class TeamsRepository(private val teamsDao: TeamsDao) {

    suspend fun insertTeam(team: Teams) {
        teamsDao.insertTeam(team)
    }

    suspend fun deleteAllTeams() {
        teamsDao.deleteAllTeams()
    }

    suspend fun getAllTeams(): List<Teams> {
        return teamsDao.getAllTeams()
    }

    suspend fun getTeamById(id: Int): Teams? {
        return teamsDao.getTeamById(id)
    }

    suspend fun deleteTeam(team: Teams) {
        teamsDao.deleteTeam(team)
    }
}