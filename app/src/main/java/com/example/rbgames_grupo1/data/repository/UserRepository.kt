package com.example.rbgames_grupo1.data.repository

import com.example.rbgames_grupo1.data.local.users.UserDao
import com.example.rbgames_grupo1.data.local.users.UserEntity


// Repositorio: orquesta reglas de negocio para login/registro sobre el DAO.
class UserRepository(
    private val userDao: UserDao // Inyección del DAO
) {

    // Login: busca por email y valida contraseña
    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)                         // Busca usuario
        return if (user != null && user.password == password) {      // Verifica pass
            Result.success(user)                                     // Éxito
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas")) // Error
        }
    }

    // Registro: valida no duplicado y crea nuevo usuario (con teléfono)
    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long> {
        val exists = userDao.getByEmail(email) != null               // ¿Correo ya usado?
        if (exists) {
            return Result.failure(IllegalStateException("El correo ya está registrado"))
        }
        val id = userDao.insert(                                     // Inserta nuevo
            UserEntity(
                name = name,
                email = email,
                phone = phone,                                       // Teléfono incluido
                password = password
            )
        )
        return Result.success(id)                                    // Devuelve ID generado
    }
}