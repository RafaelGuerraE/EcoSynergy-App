package br.ecosynergy_app.room.teams

import kotlinx.coroutines.flow.Flow

class TeamsRepository(private val teamsDao: TeamsDao) {

    suspend fun insertTeam(team: Teams) {
        teamsDao.insertTeam(team)
    }

    suspend fun deleteAllTeams() {
        teamsDao.deleteAllTeams()
    }

    fun getAllTeams(): Flow<List<Teams>> {  // Return Flow now
        return teamsDao.getAllTeams()
    }

    suspend fun getTeamById(id: Int): Teams {
        return teamsDao.getTeamById(id)
    }

    suspend fun insertOrUpdateTeam(team: Teams) {
        if (getTeamById(team.id) == null) {
            insertTeam(team)
        } else {
            updateTeamInfo(team)
        }
    }

    suspend fun deleteTeamById(teamId: Int) {
        teamsDao.deleteTeamById(teamId)
    }

    suspend fun updateTeamInfo(updatedTeam: Teams) {
        teamsDao.updateTeam(updatedTeam)
    }
}
