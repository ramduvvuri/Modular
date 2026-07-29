package com.modular.app.data.local.dao

import androidx.room.*
import com.modular.app.data.local.entity.AllowedAppEntity
import com.modular.app.data.local.entity.ModeEntity
import com.modular.app.data.local.entity.ModeWithApps
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeDao {
    @Transaction
    @Query("SELECT * FROM modes ORDER BY createdAt DESC")
    fun getAllModesWithApps(): Flow<List<ModeWithApps>>

    @Transaction
    @Query("SELECT * FROM modes WHERE id = :modeId")
    fun getModeWithAppsById(modeId: Long): Flow<ModeWithApps?>

    @Transaction
    @Query("SELECT * FROM modes WHERE id = :modeId")
    suspend fun getModeWithAppsByIdSync(modeId: Long): ModeWithApps?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeEntity): Long

    @Update
    suspend fun updateMode(mode: ModeEntity)

    @Delete
    suspend fun deleteMode(mode: ModeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedApps(apps: List<AllowedAppEntity>)

    @Query("DELETE FROM allowed_apps WHERE modeId = :modeId")
    suspend fun deleteAllowedAppsForMode(modeId: Long)

    @Transaction
    suspend fun setAllowedAppsForMode(modeId: Long, apps: List<AllowedAppEntity>) {
        deleteAllowedAppsForMode(modeId)
        insertAllowedApps(apps)
    }
}
