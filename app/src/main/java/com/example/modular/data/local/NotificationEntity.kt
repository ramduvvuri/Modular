package com.example.modular.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String, // E.g., "WhatsApp"
    val title: String,   // E.g., "Mom"
    val text: String,    // E.g., "Call me back!"
    val timestamp: Long
)
