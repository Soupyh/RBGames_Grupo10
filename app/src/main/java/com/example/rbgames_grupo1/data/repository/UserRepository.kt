package com.example.rbgames_grupo1.data.repository

import com.example.rbgames_grupo1.data.local.users.UserDao
import com.example.rbgames_grupo1.data.local.users.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): Result<Unit> {
        val e = email.trim()
        val p = password.trim()
        val u = userDao.findByEmail(e)
        return if (u != null && u.password == p) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Credenciales inválidas"))
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<Unit> {
        return try {
            userDao.insert(
                UserEntity(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    password = password,
                    role = "USUARIO"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRoleByEmail(email: String): String? =
        userDao.getRoleByEmail(email.trim())

    suspend fun updatePhoto(email: String, uri: String?) {
        userDao.updatePhoto(email, uri)
    }

    suspend fun findByEmail(email: String): UserEntity? =
        userDao.findByEmail(email.trim())
}
