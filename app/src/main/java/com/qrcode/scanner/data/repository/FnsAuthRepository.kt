package com.qrcode.scanner.data.repository

import com.qrcode.scanner.data.local.dao.FnsSessionDao
import com.qrcode.scanner.data.local.entity.FnsSessionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FnsAuthRepository @Inject constructor(
    private val sessionDao: FnsSessionDao
) {
    suspend fun saveSession(session: FnsSessionEntity): Long = sessionDao.insert(session)

    suspend fun getActiveSession(): FnsSessionEntity? = sessionDao.getActiveSession()

    suspend fun getAllSessions(): List<FnsSessionEntity> = sessionDao.getAll()

    suspend fun deactivate(id: Long) = sessionDao.deactivate(id)

    suspend fun deactivateAll() = sessionDao.deactivateAll()

    suspend fun updateSession(session: FnsSessionEntity) = sessionDao.update(session)

    suspend fun isLoggedIn(): Boolean = sessionDao.getActiveSession() != null
}
