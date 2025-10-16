package com.example.rbgames_grupo1.data.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,                                      // Nombre
    val email: String,                                     // Email (lo usamos como “id”)
    val phone: String,                                     // Teléfono
    val pass: String
)