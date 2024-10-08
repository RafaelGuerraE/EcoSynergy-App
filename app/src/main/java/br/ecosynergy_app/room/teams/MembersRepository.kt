package br.ecosynergy_app.room.teams

class MembersRepository(private val membersDao: MembersDao) {

    suspend fun getAllMembers(): List<Members> {
        return membersDao.getAllMembers()
    }

    suspend fun insertMember(member: Members) {
        membersDao.insertMember(member)
    }

    suspend fun updateMember(member: Members) {
        membersDao.updateMember(member)
    }

    suspend fun deleteMember(member: Members) {
        membersDao.deleteMember(member)
    }

    suspend fun deleteAllMembers() {
        membersDao.deleteAllMembers()
    }

    suspend fun getMember(userId: Int, teamId: Int): Members {
        return membersDao.getMember(userId, teamId)
    }

    suspend fun getMembersByTeamId(teamId: Int): List<Members> {
        return membersDao.getMembersByTeamId(teamId)
    }
}
