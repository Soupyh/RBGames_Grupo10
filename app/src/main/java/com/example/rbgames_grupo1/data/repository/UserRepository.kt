package com.example.rbgames_grupo1.data.repository

import com.example.rbgames_grupo1.data.local.users.UserDao
import com.example.rbgames_grupo1.data.local.users.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): Result<Unit> {
        val u = userDao.findByEmail(email.trim())
        return if (u != null && u.password == password.trim()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<Unit> {
        return try {
            userDao.insert(
                UserEntity(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    password = password,     // (en demo plano)
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
}
