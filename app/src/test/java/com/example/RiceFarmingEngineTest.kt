package com.example

import com.example.data.local.RiceDatabaseSeeder
import com.example.domain.calculator.BanglaDate
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.calculator.RiceFarmingEngine
import com.example.domain.model.LandUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiceFarmingEngineTest {

    @Test
    fun `land unit conversions match bangladesh standards`() {
        val oneHectare = RiceFarmingEngine.convertLandArea(1.0, LandUnit.HECTARE)
        assertEquals(247.0, oneHectare.decimals, 0.0001)
        assertEquals(247.0 / 33.0, oneHectare.bighas, 0.0001)

        val oneBigha = RiceFarmingEngine.convertLandArea(1.0, LandUnit.BIGHA)
        assertEquals(33.0, oneBigha.decimals, 0.0001)
        assertEquals(33.0 / 247.0, oneBigha.hectares, 0.0001)
    }

    @Test
    fun `yield conversion uses 40kg per mon and 33 over 247 hectare`() {
        val yield = RiceFarmingEngine.convertYieldFromTonPerHa(7.41, hectaresForLand = 1.0)
        // 7.41 MT/Ha = 7410 kg/Ha -> * (33/247) = 990 kg/Bigha -> 24.75 Mon/Bigha
        assertEquals(990.0, yield.kgPerBigha, 0.01)
        assertEquals(24.75, yield.monPerBigha, 0.001)
    }

    @Test
    fun `growth timeline generates 11 continuous stages summing to lifeCycleDays`() {
        val boro29 = RiceDatabaseSeeder.initialVarieties.first { it.id == 302 } // BRRI dhan29, 160 days
        val sowing = BanglaDate(2026, 11, 20)
        val stages = RiceFarmingEngine.calculateGrowthTimeline(
            variety = boro29,
            sowingDate = sowing,
            today = sowing.plusDays(45)
        )

        assertEquals(11, stages.size)
        assertEquals(boro29.lifeCycleDays, stages.sumOf { it.calculatedDays })
        assertEquals(sowing, stages.first().startDate)
        assertEquals(sowing.plusDays(boro29.lifeCycleDays - 1), stages.last().endDate)

        // Verify continuous calendar progression without gaps
        for (i in 0 until stages.lastIndex) {
            assertEquals(stages[i].endDate.plusDays(1), stages[i + 1].startDate)
        }
    }

    @Test
    fun `fertilizer calculation scales linearly with land area and split percentages`() {
        val boro29 = RiceDatabaseSeeder.initialVarieties.first { it.id == 302 }
        val rules = RiceDatabaseSeeder.initialFertilizerRules
        val result = RiceFarmingEngine.calculate(
            variety = boro29,
            rules = rules,
            inputDate = BanglaDate(2026, 12, 1),
            isTransplantingDate = true,
            landArea = 1.0,
            landUnit = LandUnit.HECTARE
        )

        assertEquals(300.0, result.fertilizerSummary.ureaKg, 0.01)
        assertEquals(120.0, result.fertilizerSummary.tspKg, 0.01)
        assertEquals(150.0, result.fertilizerSummary.mopKg, 0.01)
        assertEquals(300.0, result.fertilizerSplits.sumOf { it.ureaKg }, 0.01)
        assertEquals(150.0, result.fertilizerSplits.sumOf { it.mopKg }, 0.01)
    }

    @Test
    fun `bangla number formatter and parser handle bangla digits`() {
        assertEquals("২৪৭", BanglaFormatters.toBanglaDigits(247))
        assertEquals(33.5, BanglaFormatters.parseNumber("৩৩.৫") ?: 0.0, 0.001)
    }
}
