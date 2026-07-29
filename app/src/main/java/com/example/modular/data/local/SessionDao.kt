package com.example.modular.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE id = 1")
    fun getSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = 1")
    suspend fun getSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSession()
}
