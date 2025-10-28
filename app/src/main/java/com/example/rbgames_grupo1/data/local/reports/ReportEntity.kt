package com.example.rbgames_grupo1.data.local.reports


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userEmail: String,
    val subject: String,
    val message: String,
    val status: String = "ABIERTO",   // ABIERTO | EN_PROCESO | CERRADO
    val createdAt: Long = System.currentTimeMillis()
)