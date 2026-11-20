package com.example.domain.model

import com.example.domain.calculator.BanglaDate
import com.example.data.local.entity.RiceVarietyEntity

data class GrowthStageItem(
    val order: Int,
    val phase: GrowthPhase,
    val nameBn: String,
    val nameEn: String,
    val standardRangeBn: String,
    val calculatedDays: Int,
    val startDate: BanglaDate,
    val endDate: BanglaDate,
    val careTipBn: String,
    val isCurrent: Boolean,
    val isCompleted: Boolean
)

data class FertilizerSummary(
    val decimals: Double,
    val bighas: Double,
    val hectares: Double,
    val ureaKg: Double,
    val tspKg: Double,
    val mopKg: Double,
    val gypsumKg: Double,
    val zincKg: Double
)

data class FertilizerSplitItem(
    val stageCode: String,
    val titleBn: String,
    val applicationDate: BanglaDate,
    val daysAfterTransplant: Int,
    val ureaKg: Double,
    val tspKg: Double,
    val mopKg: Double,
    val gypsumKg: Double,
    val zincKg: Double,
    val tipBn: String
)

data class LandConversionResult(
    val decimals: Double,
    val bighas: Double,
    val hectares: Double,
    val acres: Double
)

data class YieldConversionResult(
    val metricTonPerHa: Double,
    val kgPerBigha: Double,
    val monPerBigha: Double,
    val totalKgForLand: Double,
    val totalMonForLand: Double
)

data class MilestoneDraft(
    val titleBn: String,
    val descriptionBn: String,
    val category: String,
    val dueDate: BanglaDate
)

data class CultivationCalculationResult(
    val variety: RiceVarietyEntity,
    val season: RiceSeason,
    val sowingDate: BanglaDate,
    val transplantingDate: BanglaDate,
    val harvestDate: BanglaDate,
    val stages: List<GrowthStageItem>,
    val currentStage: GrowthStageItem?,
    val landConversion: LandConversionResult,
    val fertilizerSummary: FertilizerSummary,
    val fertilizerSplits: List<FertilizerSplitItem>,
    val yieldConversion: YieldConversionResult,
    val recommendedMilestones: List<MilestoneDraft>
)
