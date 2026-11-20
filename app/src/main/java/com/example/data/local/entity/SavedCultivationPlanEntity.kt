package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_plans")
data class SavedCultivationPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fieldName: String,
    val varietyId: Int,
    val varietyNameBn: String,
    val season: String,
    val inputDateEpochDay: Long,
    val isTransplantingDate: Boolean,
    val landArea: Double,
    val landUnit: String,
    val remindersEnabled: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis()
)
