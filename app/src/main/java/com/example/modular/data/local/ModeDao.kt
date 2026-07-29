package com.example.modular.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY createdAt DESC")
    fun getAllModes(): Flow<List<ModeEntity>>

    @Query("SELECT * FROM modes WHERE id = :modeId")
    suspend fun getModeById(modeId: Long): ModeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeEntity): Long

    @Delete
    suspend fun deleteMode(mode: ModeEntity)

    @Query("SELECT * FROM allowed_apps WHERE modeId = :modeId")
    fun getAppsForMode(modeId: Long): Flow<List<AllowedAppEntity>>

    @Query("SELECT * FROM allowed_apps WHERE modeId = :modeId")
    suspend fun getAppsForModeSync(modeId: Long): List<AllowedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedApps(apps: List<AllowedAppEntity>)

    @Query("DELETE FROM allowed_apps WHERE modeId = :modeId")
    suspend fun deleteAppsForMode(modeId: Long)

    @Transaction
    suspend fun updateModeApps(modeId: Long, apps: List<AllowedAppEntity>) {
        deleteAppsForMode(modeId)
        insertAllowedApps(apps)
    }
}
