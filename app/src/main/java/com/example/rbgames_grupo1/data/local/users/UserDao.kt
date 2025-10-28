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

    // ✅ case-insensitive
    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    // ✅ case-insensitive
    @Query("SELECT role FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getRoleByEmail(email: String): String?
}
