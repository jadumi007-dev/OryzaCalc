package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.dao.PlanWithMilestones
import com.example.data.local.entity.FertilizerRuleEntity
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.data.local.entity.RiceVarietyEntity
import com.example.data.local.entity.SavedCultivationPlanEntity
import com.example.data.local.repository.RiceFarmingRepository
import com.example.domain.calculator.BanglaDate
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.calculator.RiceFarmingEngine
import com.example.domain.model.CultivationCalculationResult
import com.example.domain.model.LandConversionResult
import com.example.domain.model.LandUnit
import com.example.domain.model.RiceSeason
import com.example.domain.model.YieldConversionResult
import com.example.notification.RiceNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val titleBn: String) {
    CALCULATOR("হিসাব"),
    TIMELINE("বৃদ্ধি পর্যায়"),
    FERTILIZER_CONVERTER("সার ও রূপান্তর"),
    REMINDERS("রিমাইন্ডার")
}

enum class YieldConverterInputMode(val labelBn: String) {
    TON_PER_HA("মেঃ টন/হেক্টর"),
    KG_PER_BIGHA("কেজি/বিঘা"),
    MON_PER_BIGHA("মণ/বিঘা")
}

data class ConverterState(
    val landInputText: String = "33",
    val landInputUnit: LandUnit = LandUnit.DECIMAL,
    val landResult: LandConversionResult = RiceFarmingEngine.convertLandArea(33.0, LandUnit.DECIMAL),
    val yieldInputText: String = "7.5",
    val yieldInputMode: YieldConverterInputMode = YieldConverterInputMode.TON_PER_HA,
    val yieldResult: YieldConversionResult = RiceFarmingEngine.convertYieldFromTonPerHa(7.5)
)

data class RiceUiState(
    val isLoading: Boolean = true,
    val activeTab: AppTab = AppTab.CALCULATOR,
    val selectedSeason: RiceSeason = RiceSeason.BORO,
    val seasonVarieties: List<RiceVarietyEntity> = emptyList(),
    val selectedVariety: RiceVarietyEntity? = null,
    val inputDate: BanglaDate = BanglaDate.today(),
    val isTransplantingDate: Boolean = true,
    val landAreaText: String = "1",
    val selectedLandUnit: LandUnit = LandUnit.BIGHA,
    val calculation: CultivationCalculationResult? = null,
    val converter: ConverterState = ConverterState(),
    val savedPlans: List<PlanWithMilestones> = emptyList(),
    val statusMessage: String? = null
)

class RiceCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RiceFarmingRepository(AppDatabase.getInstance(application))

    private data class UserInputState(
        val activeTab: AppTab = AppTab.CALCULATOR,
        val selectedSeason: RiceSeason = RiceSeason.BORO,
        val selectedVarietyId: Int? = null,
        val inputDate: BanglaDate = BanglaDate.today(),
        val isTransplantingDate: Boolean = true,
        val landAreaText: String = "1",
        val selectedLandUnit: LandUnit = LandUnit.BIGHA,
        val converter: ConverterState = ConverterState(),
        val statusMessage: String? = null
    )

    private val userInput = MutableStateFlow(UserInputState())

    val uiState: StateFlow<RiceUiState> = combine(
        repository.allVarieties,
        repository.allFertilizerRules,
        repository.savedPlansWithMilestones,
        userInput
    ) { varieties, rules, plans, input ->
        if (varieties.isEmpty()) {
            RiceUiState(isLoading = true, activeTab = input.activeTab)
        } else {
            val seasonVarieties = varieties.filter { it.season == input.selectedSeason.code }
            val selectedVariety = seasonVarieties.firstOrNull { it.id == input.selectedVarietyId }
                ?: seasonVarieties.firstOrNull()
                ?: varieties.first()

            val parsedLand = BanglaFormatters.parseNumber(input.landAreaText) ?: 0.0
            val calculation = RiceFarmingEngine.calculate(
                variety = selectedVariety,
                rules = rules,
                inputDate = input.inputDate,
                isTransplantingDate = input.isTransplantingDate,
                landArea = parsedLand,
                landUnit = input.selectedLandUnit
            )

            RiceUiState(
                isLoading = false,
                activeTab = input.activeTab,
                selectedSeason = input.selectedSeason,
                seasonVarieties = seasonVarieties,
                selectedVariety = selectedVariety,
                inputDate = input.inputDate,
                isTransplantingDate = input.isTransplantingDate,
                landAreaText = input.landAreaText,
                selectedLandUnit = input.selectedLandUnit,
                calculation = calculation,
                converter = input.converter,
                savedPlans = plans,
                statusMessage = input.statusMessage
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RiceUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensurePrepopulated()
        }
    }

    fun selectTab(tab: AppTab) {
        userInput.update { it.copy(activeTab = tab) }
    }

    fun selectSeason(season: RiceSeason) {
        userInput.update {
            it.copy(
                selectedSeason = season,
                selectedVarietyId = null
            )
        }
    }

    fun selectVariety(variety: RiceVarietyEntity) {
        userInput.update { it.copy(selectedVarietyId = variety.id) }
    }

    fun updateInputDate(date: BanglaDate) {
        userInput.update { it.copy(inputDate = date) }
    }

    fun setIsTransplantingDate(isTransplanting: Boolean) {
        userInput.update { it.copy(isTransplantingDate = isTransplanting) }
    }

    fun updateLandAreaText(text: String) {
        userInput.update { it.copy(landAreaText = text) }
    }

    fun selectLandUnit(unit: LandUnit) {
        userInput.update { it.copy(selectedLandUnit = unit) }
    }

    fun updateConverterLandInput(text: String, unit: LandUnit? = null) {
        userInput.update { current ->
            val targetUnit = unit ?: current.converter.landInputUnit
            val numeric = BanglaFormatters.parseNumber(text) ?: 0.0
            current.copy(
                converter = current.converter.copy(
                    landInputText = text,
                    landInputUnit = targetUnit,
                    landResult = RiceFarmingEngine.convertLandArea(numeric, targetUnit)
                )
            )
        }
    }

    fun updateConverterYieldInput(text: String, mode: YieldConverterInputMode? = null) {
        userInput.update { current ->
            val targetMode = mode ?: current.converter.yieldInputMode
            val numeric = BanglaFormatters.parseNumber(text) ?: 0.0
            val result = when (targetMode) {
                YieldConverterInputMode.TON_PER_HA ->
                    RiceFarmingEngine.convertYieldFromTonPerHa(numeric)
                YieldConverterInputMode.KG_PER_BIGHA ->
                    RiceFarmingEngine.convertYieldFromKgPerBigha(numeric)
                YieldConverterInputMode.MON_PER_BIGHA ->
                    RiceFarmingEngine.convertYieldFromMonPerBigha(numeric)
            }
            current.copy(
                converter = current.converter.copy(
                    yieldInputText = text,
                    yieldInputMode = targetMode,
                    yieldResult = result
                )
            )
        }
    }

    fun saveCurrentPlan(fieldNameInput: String, scheduleNotifications: Boolean) {
        val currentState = uiState.value
        val calc = currentState.calculation ?: return
        val parsedLand = BanglaFormatters.parseNumber(currentState.landAreaText) ?: 1.0
        val cleanName = fieldNameInput.trim().ifEmpty {
            "${calc.season.titleBn} - ${calc.variety.nameBn} জমি"
        }

        viewModelScope.launch {
            val plan = SavedCultivationPlanEntity(
                fieldName = cleanName,
                varietyId = calc.variety.id,
                varietyNameBn = calc.variety.nameBn,
                season = calc.season.code,
                inputDateEpochDay = currentState.inputDate.toEpochDay(),
                isTransplantingDate = currentState.isTransplantingDate,
                landArea = parsedLand,
                landUnit = currentState.selectedLandUnit.code,
                remindersEnabled = scheduleNotifications
            )
            val milestones = calc.recommendedMilestones.map { draft ->
                ReminderMilestoneEntity(
                    planId = 0L,
                    titleBn = draft.titleBn,
                    descriptionBn = draft.descriptionBn,
                    category = draft.category,
                    dueDateEpochDay = draft.dueDate.toEpochDay()
                )
            }
            val savedId = repository.savePlanWithMilestones(plan, milestones)

            if (scheduleNotifications) {
                // Schedule alarms using generated milestone IDs once Room emits them,
                // and immediately show confirmation.
                userInput.update {
                    it.copy(
                        activeTab = AppTab.REMINDERS,
                        statusMessage = "‘$cleanName’ সংরক্ষিত হয়েছে ও রিমাইন্ডার সক্রিয় করা হয়েছে।"
                    )
                }
            } else {
                userInput.update {
                    it.copy(
                        activeTab = AppTab.REMINDERS,
                        statusMessage = "‘$cleanName’ অফলাইন পরিকল্পনায় সংরক্ষিত হয়েছে।"
                    )
                }
            }

            // Trigger alarm scheduling for newly saved plan milestones
            if (scheduleNotifications) {
                val context = getApplication<Application>()
                val withIds = milestones.mapIndexed { idx, entity ->
                    entity.copy(id = (savedId * 100L) + idx + 1L, planId = savedId)
                }
                RiceNotificationHelper.scheduleMilestoneAlarms(context, withIds)
            }
        }
    }

    fun loadSavedPlan(planWithMilestones: PlanWithMilestones) {
        val plan = planWithMilestones.plan
        userInput.update {
            it.copy(
                activeTab = AppTab.CALCULATOR,
                selectedSeason = RiceSeason.fromCode(plan.season),
                selectedVarietyId = plan.varietyId,
                inputDate = BanglaDate.fromEpochDay(plan.inputDateEpochDay),
                isTransplantingDate = plan.isTransplantingDate,
                landAreaText = plan.landArea.toString().removeSuffix(".0"),
                selectedLandUnit = LandUnit.fromCode(plan.landUnit),
                statusMessage = "‘${plan.fieldName}’ লোড করা হয়েছে।"
            )
        }
    }

    fun toggleMilestoneCompleted(milestone: ReminderMilestoneEntity) {
        viewModelScope.launch {
            repository.toggleMilestoneCompleted(milestone.id, !milestone.isCompleted)
        }
    }

    fun togglePlanNotifications(planWithMilestones: PlanWithMilestones) {
        val plan = planWithMilestones.plan
        val newEnabled = !plan.remindersEnabled
        viewModelScope.launch {
            repository.setPlanRemindersEnabled(plan.id, newEnabled)
            val context = getApplication<Application>()
            if (newEnabled) {
                RiceNotificationHelper.scheduleMilestoneAlarms(context, planWithMilestones.milestones)
                userInput.update { it.copy(statusMessage = "নোটিফিকেশন রিমাইন্ডার চালু করা হয়েছে।") }
            } else {
                RiceNotificationHelper.cancelMilestoneAlarms(context, planWithMilestones.milestones)
                userInput.update { it.copy(statusMessage = "এই পরিকল্পনার রিমাইন্ডার বন্ধ করা হয়েছে।") }
            }
        }
    }

    fun deletePlan(planWithMilestones: PlanWithMilestones) {
        viewModelScope.launch {
            RiceNotificationHelper.cancelMilestoneAlarms(
                getApplication(),
                planWithMilestones.milestones
            )
            repository.deletePlan(planWithMilestones.plan.id)
            userInput.update {
                it.copy(statusMessage = "‘${planWithMilestones.plan.fieldName}’ মুছে ফেলা হয়েছে।")
            }
        }
    }

    fun sendTestNotification() {
        val calc = uiState.value.calculation
        val title = calc?.let { "${it.variety.nameBn}: সার ও সেচ রিমাইন্ডার" }
            ?: "ধান চাষ ক্যালকুলেটর রিমাইন্ডার"
        val body = calc?.recommendedMilestones?.firstOrNull()?.descriptionBn
            ?: "সঠিক সময়ে ইউরিয়া সার উপরিপ্রয়োগ ও ছিপছিপে পানি সংরক্ষণ নিশ্চিত করুন।"
        RiceNotificationHelper.showNotification(
            context = getApplication(),
            notificationId = 9091,
            title = title,
            body = body
        )
        userInput.update { it.copy(statusMessage = "পরীক্ষামূলক নোটিফিকেশন পাঠানো হয়েছে।") }
    }

    fun clearStatusMessage() {
        userInput.update { it.copy(statusMessage = null) }
    }
}
