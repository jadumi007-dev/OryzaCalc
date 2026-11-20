package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ধান চাষ ক্যালকুলেটর", appName)
    }

    @Test
    fun `room database seeds all seasons varieties and fertilizer rules`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            AppDatabase.seedIfEmpty(db)

            val allVarieties = db.riceVarietyDao().getAllVarieties().first()
            val aus = db.riceVarietyDao().getVarietiesBySeason("AUS").first()
            val aman = db.riceVarietyDao().getVarietiesBySeason("AMAN").first()
            val boro = db.riceVarietyDao().getVarietiesBySeason("BORO").first()
            val rules = db.fertilizerRuleDao().getAllRules().first()

            assertTrue(allVarieties.size >= 17)
            assertTrue(aus.isNotEmpty())
            assertTrue(aman.isNotEmpty())
            assertTrue(boro.isNotEmpty())
            assertEquals(12, rules.size)
        } finally {
            db.close()
        }
    }
}
