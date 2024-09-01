package br.ecosynergy_app.room

import androidx.lifecycle.LiveData

class MembersRepository(private val membersDao: MembersDao) {

    val allUsers: LiveData<List<Members>> = membersDao.getAllUsers()

    suspend fun insert(member: Members) {
        membersDao.insert(member)
    }

    suspend fun update(member: Members) {
        membersDao.update(member)
    }

    suspend fun delete(member: Members) {
        membersDao.delete(member)
    }

    suspend fun getUserById(id: Int): Members? {
        return membersDao.getUserById(id)
    }
}
