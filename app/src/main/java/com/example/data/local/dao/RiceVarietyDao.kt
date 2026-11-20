package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RiceVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiceVarietyDao {
    @Query("SELECT * FROM rice_varieties ORDER BY season ASC, id ASC")
    fun getAllVarieties(): Flow<List<RiceVarietyEntity>>

    @Query("SELECT * FROM rice_varieties WHERE season = :season ORDER BY id ASC")
    fun getVarietiesBySeason(season: String): Flow<List<RiceVarietyEntity>>

    @Query("SELECT * FROM rice_varieties WHERE id = :id LIMIT 1")
    suspend fun getVarietyById(id: Int): RiceVarietyEntity?

    @Query("SELECT COUNT(*) FROM rice_varieties")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(varieties: List<RiceVarietyEntity>)
}
