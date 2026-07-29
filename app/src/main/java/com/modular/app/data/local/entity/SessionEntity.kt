package com.modular.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Single active session state
    val modeId: Long,
    val startTime: Long,
    val isRunning: Boolean,
    val isExiting: Boolean = false,
    val exitTimerStartedAt: Long? = null
)
