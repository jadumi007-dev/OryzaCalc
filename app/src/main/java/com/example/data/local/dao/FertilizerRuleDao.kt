package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FertilizerRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerRuleDao {
    @Query("SELECT * FROM fertilizer_rules WHERE season = :season ORDER BY daysAfterTransplant ASC")
    fun getRulesBySeason(season: String): Flow<List<FertilizerRuleEntity>>

    @Query("SELECT * FROM fertilizer_rules ORDER BY season ASC, daysAfterTransplant ASC")
    fun getAllRules(): Flow<List<FertilizerRuleEntity>>

    @Query("SELECT COUNT(*) FROM fertilizer_rules")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<FertilizerRuleEntity>)
}
