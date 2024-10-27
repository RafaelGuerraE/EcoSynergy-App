package br.ecosynergy_app.room.invites


class InvitesRepository(private val invitesDao: InvitesDao) {

    suspend fun insertInvite(invite: Invites) {
        invitesDao.insertInvite(invite)
    }

    suspend fun getInvitesByTeam(teamId: Int): List<Invites> {
        return invitesDao.findInvitesByTeam(teamId)
    }
}