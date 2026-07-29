package com.modular.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ModeWithApps(
    @Embedded val mode: ModeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "modeId"
    )
    val allowedApps: List<AllowedAppEntity>
)
