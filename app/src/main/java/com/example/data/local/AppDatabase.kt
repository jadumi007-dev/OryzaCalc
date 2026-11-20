package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CultivationPlanDao
import com.example.data.local.dao.FertilizerRuleDao
import com.example.data.local.dao.RiceVarietyDao
import com.example.data.local.entity.FertilizerRuleEntity
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.data.local.entity.RiceVarietyEntity
import com.example.data.local.entity.SavedCultivationPlanEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RiceVarietyEntity::class,
        FertilizerRuleEntity::class,
        SavedCultivationPlanEntity::class,
        ReminderMilestoneEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun riceVarietyDao(): RiceVarietyDao
    abstract fun fertilizerRuleDao(): FertilizerRuleDao
    abstract fun cultivationPlanDao(): CultivationPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rice_farming_offline.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedIfEmpty(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedIfEmpty(db: AppDatabase) {
            if (db.riceVarietyDao().getCount() == 0) {
                db.riceVarietyDao().insertAll(RiceDatabaseSeeder.initialVarieties)
            }
            if (db.fertilizerRuleDao().getCount() == 0) {
                db.fertilizerRuleDao().insertAll(RiceDatabaseSeeder.initialFertilizerRules)
            }
        }
    }
}
