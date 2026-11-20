package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_milestones",
    foreignKeys = [
        ForeignKey(
            entity = SavedCultivationPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class ReminderMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,
    val titleBn: String,
    val descriptionBn: String,
    val category: String, // FERTILIZER, WATER, STAGE
    val dueDateEpochDay: Long,
    val isCompleted: Boolean = false
)
