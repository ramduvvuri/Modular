package com.example.modular.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modes")
data class ModeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis()
)
