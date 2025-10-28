package com.example.rbgames_grupo1.data.local.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    // ✅ Trae el usuario completo por email (case-insensitive)
    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    // ✅ Solo el rol (case-insensitive)
    @Query("SELECT role FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getRoleByEmail(email: String): String?

    // ✅ Actualiza foto (devuelve filas afectadas). Alternativa con LOWER(...) si prefieres:
    // @Query("UPDATE users SET photoUri = :uri WHERE LOWER(email) = LOWER(:email)")
    @Query("UPDATE users SET photoUri = :uri WHERE email = :email COLLATE NOCASE")
    suspend fun updatePhoto(email: String, uri: String?): Int
}
