package com.modular.app.data.local.dao

import androidx.room.*
import com.modular.app.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = 1")
    suspend fun clearActiveSession()

    @Query("UPDATE sessions SET isExiting = :isExiting, exitTimerStartedAt = :startTime WHERE id = 1")
    suspend fun updateExitState(isExiting: Boolean, startTime: Long?)
}
