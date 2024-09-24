package br.ecosynergy_app.room

import androidx.lifecycle.LiveData

class MembersRepository(private val membersDao: MembersDao) {

    suspend fun getAllMembers(): List<Members>{
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

    suspend fun deleteAllMembers(){
        membersDao.deleteAllMembers()
    }

    suspend fun getMemberById(id: Int): Members {
        return membersDao.getMemberById(id)
    }

    suspend fun getMembersByTeamId(id: Int): List<Members>{
        return membersDao.getMembersByTeamId(id)
    }
}