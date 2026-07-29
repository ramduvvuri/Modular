package com.example.modular.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allowed_apps",
    foreignKeys = [
        ForeignKey(
            entity = ModeEntity::class,
            parentColumns = ["id"],
            childColumns = ["modeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("modeId")]
)
data class AllowedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modeId: Long,
    val packageName: String,
    val appName: String
)
