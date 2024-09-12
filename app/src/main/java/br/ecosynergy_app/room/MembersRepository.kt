package br.ecosynergy_app.room

import androidx.lifecycle.LiveData

class MembersRepository(private val membersDao: MembersDao) {

    val allMembers: LiveData<List<Members>> = membersDao.getAllMembers()

    suspend fun insertMember(member: Members) {
        membersDao.insertMember(member)
    }

    suspend fun updateMember(member: Members) {
        membersDao.updateMember(member)
    }

    suspend fun deleteMember(member: Members) {
        membersDao.deleteMember(member)
    }

    suspend fun getMemberById(id: Int): Members? {
        return membersDao.getMemberById(id)
    }
}