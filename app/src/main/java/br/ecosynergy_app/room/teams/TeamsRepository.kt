package br.ecosynergy_app.room.teams

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

    suspend fun getTeamById(id: Int): Teams {
        return teamsDao.getTeamById(id)
    }

    suspend fun getTeamByHandle(handle: String): Teams {
        return teamsDao.getTeamByHandle(handle)
    }

    suspend fun deleteTeamById(teamId: Int) {
        teamsDao.deleteTeamById(teamId)
    }

    suspend fun updateTeamInfo(updatedTeam: Teams) {
        teamsDao.updateTeam(updatedTeam)
    }
}