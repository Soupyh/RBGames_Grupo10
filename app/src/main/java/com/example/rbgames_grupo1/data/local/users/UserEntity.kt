package com.example.rbgames_grupo1.data.local.users

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey(autoGenerate = true)    // Clave primaria autoincremental
    val id: Long = 0L,

    val name: String,                   // Nombre completo del usuario
    val email: String,                  // Correo (idealmente único a nivel de negocio)
    val phone: String,                  // Teléfono del usuario (⚠️ agregado)
    val password: String,
    val role: String = "USUARIO",        // Rol del usuario (por defecto: "USUARIO")
    val photoUri: String? = null         // URI de la foto de perfil (puede ser nulo)
)


