package com.example.domain.calculator

import com.example.data.local.entity.FertilizerRuleEntity
import com.example.data.local.entity.RiceVarietyEntity
import com.example.domain.model.CultivationCalculationResult
import com.example.domain.model.FertilizerSplitItem
import com.example.domain.model.FertilizerSummary
import com.example.domain.model.GrowthPhase
import com.example.domain.model.GrowthStageItem
import com.example.domain.model.LandConversionResult
import com.example.domain.model.LandUnit
import com.example.domain.model.MilestoneDraft
import com.example.domain.model.RiceSeason
import com.example.domain.model.YieldConversionResult

object RiceFarmingEngine {

    const val DECIMALS_PER_HECTARE = 247.0
    const val DECIMALS_PER_BIGHA = 33.0
    const val DECIMALS_PER_ACRE = 100.0
    const val KG_PER_MON = 40.0

    fun convertLandArea(value: Double, unit: LandUnit): LandConversionResult {
        val safeValue = value.coerceAtLeast(0.0)
        val decimals = safeValue * unit.decimalsPerUnit
        return LandConversionResult(
            decimals = decimals,
            bighas = decimals / DECIMALS_PER_BIGHA,
            hectares = decimals / DECIMALS_PER_HECTARE,
            acres = decimals / DECIMALS_PER_ACRE
        )
    }

    fun convertYieldFromTonPerHa(
        tonPerHa: Double,
        hectaresForLand: Double = 1.0
    ): YieldConversionResult {
        val safeTon = tonPerHa.coerceAtLeast(0.0)
        val kgPerHa = safeTon * 1000.0
        val kgPerBigha = kgPerHa * (DECIMALS_PER_BIGHA / DECIMALS_PER_HECTARE)
        val monPerBigha = kgPerBigha / KG_PER_MON
        val totalKg = kgPerHa * hectaresForLand.coerceAtLeast(0.0)
        val totalMon = totalKg / KG_PER_MON
        return YieldConversionResult(
            metricTonPerHa = safeTon,
            kgPerBigha = kgPerBigha,
            monPerBigha = monPerBigha,
            totalKgForLand = totalKg,
            totalMonForLand = totalMon
        )
    }

    fun convertYieldFromKgPerBigha(
        kgPerBigha: Double,
        hectaresForLand: Double = 1.0
    ): YieldConversionResult {
        val safeKg = kgPerBigha.coerceAtLeast(0.0)
        val tonPerHa = (safeKg * (DECIMALS_PER_HECTARE / DECIMALS_PER_BIGHA)) / 1000.0
        return convertYieldFromTonPerHa(tonPerHa, hectaresForLand)
    }

    fun convertYieldFromMonPerBigha(
        monPerBigha: Double,
        hectaresForLand: Double = 1.0
    ): YieldConversionResult {
        val safeMon = monPerBigha.coerceAtLeast(0.0)
        return convertYieldFromKgPerBigha(safeMon * KG_PER_MON, hectaresForLand)
    }

    fun calculate(
        variety: RiceVarietyEntity,
        rules: List<FertilizerRuleEntity>,
        inputDate: BanglaDate,
        isTransplantingDate: Boolean,
        landArea: Double,
        landUnit: LandUnit,
        today: BanglaDate = BanglaDate.today()
    ): CultivationCalculationResult {
        val season = RiceSeason.fromCode(variety.season)
        val sowingDate = if (isTransplantingDate) {
            inputDate.minusDays(variety.seedlingAgeDays)
        } else {
            inputDate
        }
        val transplantingDate = sowingDate.plusDays(variety.seedlingAgeDays)

        val stages = calculateGrowthTimeline(
            variety = variety,
            sowingDate = sowingDate,
            today = today
        )
        val harvestDate = stages.last().endDate
        val currentStage = stages.firstOrNull { it.isCurrent }

        val landConv = convertLandArea(landArea, landUnit)
        val ha = landConv.hectares

        val summary = FertilizerSummary(
            decimals = landConv.decimals,
            bighas = landConv.bighas,
            hectares = ha,
            ureaKg = variety.ureaKgPerHa * ha,
            tspKg = variety.tspKgPerHa * ha,
            mopKg = variety.mopKgPerHa * ha,
            gypsumKg = variety.gypsumKgPerHa * ha,
            zincKg = variety.zincKgPerHa * ha
        )

        val seasonRules = rules
            .filter { it.season == variety.season }
            .sortedBy { it.daysAfterTransplant }

        val splits = seasonRules.map { rule ->
            FertilizerSplitItem(
                stageCode = rule.stageCode,
                titleBn = rule.titleBn,
                applicationDate = transplantingDate.plusDays(rule.daysAfterTransplant),
                daysAfterTransplant = rule.daysAfterTransplant,
                ureaKg = summary.ureaKg * (rule.ureaPercent / 100.0),
                tspKg = summary.tspKg * (rule.tspPercent / 100.0),
                mopKg = summary.mopKg * (rule.mopPercent / 100.0),
                gypsumKg = summary.gypsumKg * (rule.gypsumPercent / 100.0),
                zincKg = summary.zincKg * (rule.zincPercent / 100.0),
                tipBn = rule.applicationTipBn
            )
        }

        val yieldConv = convertYieldFromTonPerHa(
            tonPerHa = variety.expectedYieldTonPerHa,
            hectaresForLand = ha
        )

        val milestones = generateMilestones(
            variety = variety,
            stages = stages,
            splits = splits
        )

        return CultivationCalculationResult(
            variety = variety,
            season = season,
            sowingDate = sowingDate,
            transplantingDate = transplantingDate,
            harvestDate = harvestDate,
            stages = stages,
            currentStage = currentStage,
            landConversion = landConv,
            fertilizerSummary = summary,
            fertilizerSplits = splits,
            yieldConversion = yieldConv,
            recommendedMilestones = milestones
        )
    }

    fun calculateGrowthTimeline(
        variety: RiceVarietyEntity,
        sowingDate: BanglaDate,
        today: BanglaDate = BanglaDate.today()
    ): List<GrowthStageItem> {
        val germinationDays = 3
        val seedlingDays = (variety.seedlingAgeDays - germinationDays).coerceAtLeast(15)
        val recoveryDays = 8

        val panicleDays = 6
        val bootingDays = 9
        val headingDays = 9
        val floweringDays = 5
        val reproductiveTotal = panicleDays + bootingDays + headingDays + floweringDays // 29

        val milkingDays = 9
        val doughDays = 9
        val maturityDays = 11
        val ripeningTotal = milkingDays + doughDays + maturityDays // 29

        val tilleringDays = (
            variety.lifeCycleDays -
                (germinationDays + seedlingDays + recoveryDays + reproductiveTotal + ripeningTotal)
            ).coerceAtLeast(12)

        data class StageBlueprint(
            val order: Int,
            val phase: GrowthPhase,
            val nameBn: String,
            val nameEn: String,
            val standardRangeBn: String,
            val days: Int,
            val tipBn: String
        )

        val blueprints = listOf(
            StageBlueprint(
                1, GrowthPhase.VEGETATIVE,
                "অঙ্কুরোদগম স্তর", "Germination", "২-৩ দিন", germinationDays,
                "পরিশোধিত ও পুষ্ট বীজ ২৪ ঘণ্টা ভিজিয়ে এবং ৪৮-৭২ ঘণ্টা জাগ দিয়ে অঙ্কুরিত করে কাদা বীজতলায় বপন করুন।"
            ),
            StageBlueprint(
                2, GrowthPhase.VEGETATIVE,
                "চারা স্তর", "Seedling Stage", "৩০-৪০ দিন", seedlingDays,
                "বীজতলায় ছিপছিপে পানি রাখুন ও প্রয়োজনে শীতকালে পলিথিন দিয়ে চারা ঢেকে শৈত্যপ্রবাহ থেকে রক্ষা করুন।"
            ),
            StageBlueprint(
                3, GrowthPhase.VEGETATIVE,
                "রোপণ ও পুনরুদ্ধার স্তর", "Transplanting & Recovery", "৭-৮ দিন", recoveryDays,
                "চারা রোপণের পর প্রথম ৭ দিন জমিতে ২-৩ সে.মি. ছিপছিপে পানি ধরে রাখুন যাতে শিকড় দ্রুত মাটিতে বসে।"
            ),
            StageBlueprint(
                4, GrowthPhase.VEGETATIVE,
                "কুঁশি স্তর", "Tillering Stage", "৪০-৪২ দিন", tilleringDays,
                "আগাছা দমন করুন, সময়মতো ইউরিয়া উপরিপ্রয়োগ করুন এবং পানির অপচয় কমাতে AWD পদ্ধতি অনুসরণ করুন।"
            ),
            StageBlueprint(
                5, GrowthPhase.REPRODUCTIVE,
                "কাইচ থোড় স্তর", "Panicle Initiation", "৫-৭ দিন", panicleDays,
                "অত্যন্ত সংবেদনশীল ধাপ! জমিতে ৩-৫ সে.মি. পানি ধরে রাখুন এবং শেষ কিস্তি ইউরিয়া ও পটাশ প্রয়োগ নিশ্চিত করুন।"
            ),
            StageBlueprint(
                6, GrowthPhase.REPRODUCTIVE,
                "থোড় স্তর", "Booting Stage", "৮-১০ দিন", bootingDays,
                "রসের অভাব হলে ধানের দানা চিটা হয়ে যাবে—এই সময়ে জমিতে পর্যাপ্ত আর্দ্রতা বা পানি বজায় রাখুন।"
            ),
            StageBlueprint(
                7, GrowthPhase.REPRODUCTIVE,
                "শীর্ষ বের হওয়া স্তর", "Heading Stage", "৮-১০ দিন", headingDays,
                "ব্লাস্ট বা খোলপোড়া রোগ এবং মাজরা পোকার আক্রমণ নিয়মিত পর্যবেক্ষণ করুন।"
            ),
            StageBlueprint(
                8, GrowthPhase.REPRODUCTIVE,
                "ফুল অবস্থা স্তর", "Flowering Stage", "৪-৫ দিন", floweringDays,
                "পরাগায়ন নিশ্চিত করতে রোদ ও আবহাওয়া অনুকূল রাখা এবং জমিতে ২-৪ সে.মি. পানি থাকা জরুরি; এ সময় কীটনাশক স্প্রে এড়িয়ে চলুন।"
            ),
            StageBlueprint(
                9, GrowthPhase.RIPENING,
                "দুধ অবস্থা স্তর", "Milking Stage", "৮-১০ দিন", milkingDays,
                "দানা পুষ্ট হওয়ার জন্য জমিতে হালকা ভেজা অবস্থা বজায় রাখুন এবং কারেন্ট পোকা (বাদামি গাছফড়িং) নজরে রাখুন।"
            ),
            StageBlueprint(
                10, GrowthPhase.RIPENING,
                "ক্ষীর অবস্থা স্তর", "Dough Stage", "৮-১০ দিন", doughDays,
                "ধান শক্ত হতে শুরু করলে সেচ কমান এবং কাটার ১০-১২ দিন আগে জমি থেকে পানি বের করে দিন।"
            ),
            StageBlueprint(
                11, GrowthPhase.RIPENING,
                "পরিপক্ক স্তর", "Maturity Stage", "১০-১২ দিন", maturityDays,
                "শীষের ৮০% ধান সোনালী রঙের হয়ে পাকলে এবং দাঁতে কাটলে কট শব্দ হলে ধান কেটে ফেলুন।"
            )
        )

        var cursor = sowingDate
        return blueprints.map { bp ->
            val start = cursor
            val end = cursor.plusDays(bp.days - 1)
            cursor = end.plusDays(1)
            val isCurrent = today >= start && today <= end
            val isCompleted = today > end
            GrowthStageItem(
                order = bp.order,
                phase = bp.phase,
                nameBn = bp.nameBn,
                nameEn = bp.nameEn,
                standardRangeBn = bp.standardRangeBn,
                calculatedDays = bp.days,
                startDate = start,
                endDate = end,
                careTipBn = bp.tipBn,
                isCurrent = isCurrent,
                isCompleted = isCompleted
            )
        }
    }

    private fun generateMilestones(
        variety: RiceVarietyEntity,
        stages: List<GrowthStageItem>,
        splits: List<FertilizerSplitItem>
    ): List<MilestoneDraft> {
        val list = mutableListOf<MilestoneDraft>()

        splits.forEach { split ->
            val parts = buildList {
                if (split.ureaKg > 0.05) add("ইউরিয়া ${BanglaFormatters.formatKgOrGram(split.ureaKg)}")
                if (split.tspKg > 0.05) add("টিএসপি ${BanglaFormatters.formatKgOrGram(split.tspKg)}")
                if (split.mopKg > 0.05) add("এমওপি ${BanglaFormatters.formatKgOrGram(split.mopKg)}")
                if (split.gypsumKg > 0.05) add("জিপসাম ${BanglaFormatters.formatKgOrGram(split.gypsumKg)}")
                if (split.zincKg > 0.05) add("জিংক ${BanglaFormatters.formatKgOrGram(split.zincKg)}")
            }.joinToString(", ")
            list.add(
                MilestoneDraft(
                    titleBn = "${variety.nameBn}: ${split.titleBn}",
                    descriptionBn = "প্রয়োজনীয় সার: $parts। ${split.tipBn}",
                    category = "FERTILIZER",
                    dueDate = split.applicationDate
                )
            )
        }

        val recovery = stages.firstOrNull { it.order == 3 }
        if (recovery != null) {
            list.add(
                MilestoneDraft(
                    titleBn = "সেচ: চারা রোপণ ও ছিপছিপে পানি সংরক্ষণ",
                    descriptionBn = "চারা লেগে যাওয়ার জন্য আগামী ৭-৮ দিন ২-৩ সে.মি. পানি ধরে রাখুন।",
                    category = "WATER",
                    dueDate = recovery.startDate
                )
            )
        }

        val panicle = stages.firstOrNull { it.order == 5 }
        if (panicle != null) {
            list.add(
                MilestoneDraft(
                    titleBn = "সেচ: কাইচ থোড় স্তরে পানি নিশ্চিত করুন",
                    descriptionBn = "কাইচ থোড় ও ফুল অবস্থায় পানির ঘাটতি হলে ফলন মারাত্মক কমে যায়; ৩-৫ সে.মি. পানি রাখুন।",
                    category = "WATER",
                    dueDate = panicle.startDate
                )
            )
        }

        val maturity = stages.firstOrNull { it.order == 11 }
        if (maturity != null) {
            list.add(
                MilestoneDraft(
                    titleBn = "পানি নিষ্কাশন ও কাটার প্রস্তুতি",
                    descriptionBn = "ধান কাটার ১০-১২ দিন আগে জমি থেকে পানি বের করে দিন যাতে মাটি শুকিয়ে ধান কাটা সহজ হয়।",
                    category = "WATER",
                    dueDate = maturity.startDate
                )
            )
            list.add(
                MilestoneDraft(
                    titleBn = "ধান কর্তন (${variety.nameBn})",
                    descriptionBn = "মাঠের ৮০% ধান পেকে সোনালী হলে সংগ্রহ ও মাড়াই শুরু করুন।",
                    category = "STAGE",
                    dueDate = maturity.endDate
                )
            )
        }

        return list.sortedBy { it.dueDate.toEpochDay() }
    }
}
