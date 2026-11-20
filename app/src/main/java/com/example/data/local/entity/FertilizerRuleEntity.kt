package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fertilizer_rules")
data class FertilizerRuleEntity(
    @PrimaryKey val id: Int,
    val season: String, // AUS, AMAN, BORO
    val stageCode: String, // BASAL, UREA_1, UREA_2, UREA_3
    val titleBn: String,
    val daysAfterTransplant: Int,
    val ureaPercent: Double,
    val tspPercent: Double,
    val mopPercent: Double,
    val gypsumPercent: Double,
    val zincPercent: Double,
    val applicationTipBn: String
)
