package com.example.modular.data.repository

import com.example.modular.data.local.AllowedAppEntity
import com.example.modular.data.local.ModeDao
import com.example.modular.data.local.ModeEntity
import com.example.modular.data.local.SessionDao
import com.example.modular.data.local.SessionEntity
import com.example.modular.domain.repository.ModeRepository
import kotlinx.coroutines.flow.Flow

class ModeRepositoryImpl(
    private val modeDao: ModeDao,
    private val sessionDao: SessionDao
) : ModeRepository {

    override fun getAllModes(): Flow<List<ModeEntity>> = modeDao.getAllModes()

    override suspend fun getModeById(modeId: Long): ModeEntity? = modeDao.getModeById(modeId)

    override suspend fun createMode(name: String, icon: String, durationMinutes: Int, allowedApps: List<AllowedAppEntity>): Long {
        val mode = ModeEntity(name = name, icon = icon, durationMinutes = durationMinutes)
        val modeId = modeDao.insertMode(mode)
        
        val appsWithModeId = allowedApps.map { it.copy(modeId = modeId) }
        modeDao.insertAllowedApps(appsWithModeId)
        
        return modeId
    }

    override suspend fun deleteMode(mode: ModeEntity) {
        modeDao.deleteMode(mode)
    }

    override fun getAppsForMode(modeId: Long): Flow<List<AllowedAppEntity>> = modeDao.getAppsForMode(modeId)

    override suspend fun getAppsForModeSync(modeId: Long): List<AllowedAppEntity> = modeDao.getAppsForModeSync(modeId)

    override fun getSession(): Flow<SessionEntity?> = sessionDao.getSessionFlow()

    override suspend fun getSessionSync(): SessionEntity? = sessionDao.getSession()

    override suspend fun updateSession(session: SessionEntity) {
        sessionDao.updateSession(session)
    }

    override suspend fun clearSession() {
        sessionDao.clearSession()
    }
}
