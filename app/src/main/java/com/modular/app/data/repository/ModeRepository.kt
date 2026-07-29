package com.modular.app.data.repository

import com.modular.app.data.local.dao.ModeDao
import com.modular.app.data.local.entity.AllowedAppEntity
import com.modular.app.data.local.entity.ModeEntity
import com.modular.app.data.local.entity.ModeWithApps
import kotlinx.coroutines.flow.Flow

class ModeRepository(private val modeDao: ModeDao) {

    fun getAllModes(): Flow<List<ModeWithApps>> = modeDao.getAllModesWithApps()

    fun getModeById(modeId: Long): Flow<ModeWithApps?> = modeDao.getModeWithAppsById(modeId)

    suspend fun getModeByIdSync(modeId: Long): ModeWithApps? = modeDao.getModeWithAppsByIdSync(modeId)

    suspend fun saveMode(mode: ModeEntity, allowedPackageNames: List<Pair<String, String>>): Long {
        val modeId = if (mode.id == 0L) {
            modeDao.insertMode(mode)
        } else {
            modeDao.updateMode(mode)
            mode.id
        }

        val allowedEntities = allowedPackageNames.map { (pkg, name) ->
            AllowedAppEntity(modeId = modeId, packageName = pkg, appName = name)
        }

        modeDao.setAllowedAppsForMode(modeId, allowedEntities)
        return modeId
    }

    suspend fun deleteMode(mode: ModeEntity) {
        modeDao.deleteMode(mode)
    }
}
