package br.ecosynergy_app.room

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun deleteUser() {
        userDao.deleteAllUsers()
    }

    suspend fun getUser(): User? {
        return userDao.getUser()
    }
}