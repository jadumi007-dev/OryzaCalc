package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rice_varieties")
data class RiceVarietyEntity(
    @PrimaryKey val id: Int,
    val nameBn: String,
    val nameEn: String,
    val season: String, // AUS, AMAN, BORO
    val lifeCycleDays: Int,
    val seedlingAgeDays: Int,
    val expectedYieldTonPerHa: Double,
    val ureaKgPerHa: Double,
    val tspKgPerHa: Double,
    val mopKgPerHa: Double,
    val gypsumKgPerHa: Double,
    val zincKgPerHa: Double,
    val specialTraitBn: String
)
