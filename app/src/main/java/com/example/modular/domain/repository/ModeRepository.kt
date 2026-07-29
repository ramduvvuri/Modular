package com.example.modular.domain.repository

import com.example.modular.data.local.AllowedAppEntity
import com.example.modular.data.local.ModeEntity
import com.example.modular.data.local.SessionEntity
import com.example.modular.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface ModeRepository {
    fun getAllModes(): Flow<List<ModeEntity>>
    suspend fun getModeById(modeId: Long): ModeEntity?
    suspend fun createMode(name: String, icon: String, allowedApps: List<AllowedAppEntity>): Long
    suspend fun deleteMode(mode: ModeEntity)
    fun getAppsForMode(modeId: Long): Flow<List<AllowedAppEntity>>
    suspend fun getAppsForModeSync(modeId: Long): List<AllowedAppEntity>

    fun getSession(): Flow<SessionEntity?>
    suspend fun getSessionSync(): SessionEntity?
    suspend fun updateSession(session: SessionEntity)
    suspend fun clearSession()

    // Notification Inbox
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    suspend fun insertNotification(notification: NotificationEntity)
    suspend fun clearAllNotifications()
}
