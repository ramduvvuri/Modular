package com.modular.app.data.repository

import com.modular.app.data.local.dao.SessionDao
import com.modular.app.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    fun getActiveSession(): Flow<SessionEntity?> = sessionDao.getActiveSessionFlow()

    suspend fun getActiveSessionSync(): SessionEntity? = sessionDao.getActiveSessionSync()

    suspend fun startSession(modeId: Long) {
        val session = SessionEntity(
            id = 1,
            modeId = modeId,
            startTime = System.currentTimeMillis(),
            isRunning = true,
            isExiting = false,
            exitTimerStartedAt = null
        )
        sessionDao.setActiveSession(session)
    }

    suspend fun startExitTimer() {
        sessionDao.updateExitState(
            isExiting = true,
            startTime = System.currentTimeMillis()
        )
    }

    suspend fun resetExitTimer() {
        sessionDao.updateExitState(
            isExiting = false,
            startTime = null
        )
    }

    suspend fun stopSession() {
        sessionDao.clearActiveSession()
    }
}
