package com.example.rbgames_grupo1.data.repository

import com.example.rbgames_grupo1.data.local.reports.ReportDao
import com.example.rbgames_grupo1.data.local.reports.ReportEntity

class ReportRepository(private val dao: ReportDao) {

    suspend fun submit(email: String, subject: String, message: String): Result<Long> =
        runCatching {
            dao.insert(ReportEntity(userEmail = email.trim(), subject = subject.trim(), message = message.trim()))
        }

    suspend fun listAll(): List<ReportEntity> = dao.getAll()

    suspend fun listMine(email: String): List<ReportEntity> = dao.getByUser(email.trim())

    suspend fun setStatus(id: Long, status: String): Result<Int> =
        runCatching { dao.updateStatus(id, status) }

    suspend fun delete(report: ReportEntity): Result<Int> =
        runCatching { dao.delete(report) }
}
