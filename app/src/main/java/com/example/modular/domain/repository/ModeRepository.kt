package com.example.modular.domain.repository

import com.example.modular.data.local.AllowedAppEntity
import com.example.modular.data.local.ModeEntity
import com.example.modular.data.local.SessionEntity
import kotlinx.coroutines.flow.Flow

interface ModeRepository {
    fun getAllModes(): Flow<List<ModeEntity>>
    suspend fun getModeById(modeId: Long): ModeEntity?
    suspend fun createMode(name: String, icon: String, durationMinutes: Int, allowedApps: List<AllowedAppEntity>): Long
    suspend fun deleteMode(mode: ModeEntity)
    fun getAppsForMode(modeId: Long): Flow<List<AllowedAppEntity>>
    suspend fun getAppsForModeSync(modeId: Long): List<AllowedAppEntity>

    fun getSession(): Flow<SessionEntity?>
    suspend fun getSessionSync(): SessionEntity?
    suspend fun updateSession(session: SessionEntity)
    suspend fun clearSession()
}
