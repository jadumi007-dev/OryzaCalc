package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.data.local.entity.SavedCultivationPlanEntity
import kotlinx.coroutines.flow.Flow

data class PlanWithMilestones(
    @Embedded val plan: SavedCultivationPlanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId"
    )
    val milestones: List<ReminderMilestoneEntity>
)

@Dao
interface CultivationPlanDao {
    @Transaction
    @Query("SELECT * FROM saved_plans ORDER BY createdAtMillis DESC")
    fun getAllPlansWithMilestones(): Flow<List<PlanWithMilestones>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: SavedCultivationPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<ReminderMilestoneEntity>)

    @Transaction
    suspend fun insertPlanWithMilestones(
        plan: SavedCultivationPlanEntity,
        milestones: List<ReminderMilestoneEntity>
    ): Long {
        val planId = insertPlan(plan)
        if (milestones.isNotEmpty()) {
            insertMilestones(milestones.map { it.copy(planId = planId) })
        }
        return planId
    }

    @Query("UPDATE reminder_milestones SET isCompleted = :completed WHERE id = :milestoneId")
    suspend fun updateMilestoneCompleted(milestoneId: Long, completed: Boolean)

    @Query("UPDATE saved_plans SET remindersEnabled = :enabled WHERE id = :planId")
    suspend fun updateRemindersEnabled(planId: Long, enabled: Boolean)

    @Query("DELETE FROM saved_plans WHERE id = :planId")
    suspend fun deletePlanById(planId: Long)
}
