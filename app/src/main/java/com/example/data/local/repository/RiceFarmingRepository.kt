package com.example.data.local.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.PlanWithMilestones
import com.example.data.local.entity.FertilizerRuleEntity
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.data.local.entity.RiceVarietyEntity
import com.example.data.local.entity.SavedCultivationPlanEntity
import kotlinx.coroutines.flow.Flow

class RiceFarmingRepository(private val db: AppDatabase) {

    val allVarieties: Flow<List<RiceVarietyEntity>> = db.riceVarietyDao().getAllVarieties()
    val allFertilizerRules: Flow<List<FertilizerRuleEntity>> = db.fertilizerRuleDao().getAllRules()
    val savedPlansWithMilestones: Flow<List<PlanWithMilestones>> =
        db.cultivationPlanDao().getAllPlansWithMilestones()

    suspend fun ensurePrepopulated() {
        AppDatabase.seedIfEmpty(db)
    }

    suspend fun savePlanWithMilestones(
        plan: SavedCultivationPlanEntity,
        milestones: List<ReminderMilestoneEntity>
    ): Long {
        return db.cultivationPlanDao().insertPlanWithMilestones(plan, milestones)
    }

    suspend fun toggleMilestoneCompleted(milestoneId: Long, completed: Boolean) {
        db.cultivationPlanDao().updateMilestoneCompleted(milestoneId, completed)
    }

    suspend fun setPlanRemindersEnabled(planId: Long, enabled: Boolean) {
        db.cultivationPlanDao().updateRemindersEnabled(planId, enabled)
    }

    suspend fun deletePlan(planId: Long) {
        db.cultivationPlanDao().deletePlanById(planId)
    }
}
