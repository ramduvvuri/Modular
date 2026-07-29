package com.example.modular.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton row
    val activeModeId: Long?,
    val startTime: Long,
    val isRunning: Boolean = false,
    val endTimeMillis: Long? = null,
    val isLeaving: Boolean = false,
    val leaveStartTime: Long = 0L,
    val isPaused: Boolean = false,
    val pauseEndTimeMillis: Long? = null
)
