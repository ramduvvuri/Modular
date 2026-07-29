package com.modular.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "allowed_apps",
    primaryKeys = ["modeId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = ModeEntity::class,
            parentColumns = ["id"],
            childColumns = ["modeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["modeId"])]
)
data class AllowedAppEntity(
    val modeId: Long,
    val packageName: String,
    val appName: String
)
