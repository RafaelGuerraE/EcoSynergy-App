package br.ecosynergy_app.room.user

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun deleteUser() {
        userDao.deleteAllUsers()
    }

    suspend fun getUser(): User {
        return userDao.getUser()
    }

    suspend fun updateUser(userId: Int,
                           newUsername: String,
                           newFullName: String,
                           newEmail: String,
                           newGender: String,
                           newNationality: String,
                           newAccessToken: String,
                           newRefreshToken: String){
        return userDao.updateUser(userId, newUsername, newFullName, newEmail, newGender, newNationality, newAccessToken, newRefreshToken)
    }

    suspend fun getUserId(): Int {
        return userDao.getUserId()
    }
}