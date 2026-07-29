package com.example.modular.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton row
    val activeModeId: Long?,
    val startTime: Long,
    val durationMinutes: Int = 0,
    val isRunning: Boolean,
    val isLeaving: Boolean = false,
    val leaveStartTime: Long = 0L
)
