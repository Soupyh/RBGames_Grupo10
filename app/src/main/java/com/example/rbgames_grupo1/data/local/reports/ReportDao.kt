package com.example.rbgames_grupo1.data.local.reports

import androidx.room.*

@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE LOWER(userEmail) = LOWER(:email) ORDER BY createdAt DESC")
    suspend fun getByUser(email: String): List<ReportEntity>

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String): Int

    @Delete
    suspend fun delete(report: ReportEntity): Int
}