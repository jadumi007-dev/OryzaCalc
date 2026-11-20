package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.RiceVarietyEntity
import com.example.domain.calculator.BanglaDate
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.model.CultivationCalculationResult
import com.example.domain.model.LandUnit
import com.example.domain.model.RiceSeason
import com.example.ui.viewmodel.RiceUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalculatorHomeScreen(
    uiState: RiceUiState,
    onSelectSeason: (RiceSeason) -> Unit,
    onSelectVariety: (RiceVarietyEntity) -> Unit,
    onUpdateDate: (BanglaDate) -> Unit,
    onToggleDateType: (Boolean) -> Unit,
    onUpdateLandArea: (String) -> Unit,
    onSelectLandUnit: (LandUnit) -> Unit,
    onSavePlan: (fieldName: String, withNotifications: Boolean) -> Unit,
    onOpenTimelineTab: () -> Unit,
    onOpenFertilizerTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var varietyMenuExpanded by remember { mutableStateOf(false) }

    val calc = uiState.calculation

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Hero Paddy Banner
        item {
            HeroSeasonCard(
                season = uiState.selectedSeason,
                calc = calc,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // 2. Season & Variety Selection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "১. মৌসুম ও ধানের জাত নির্বাচন",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Season Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiceSeason.entries.forEach { season ->
                            val selected = uiState.selectedSeason == season
                            val seasonIcon = when (season) {
                                RiceSeason.AUS -> Icons.Filled.WbSunny
                                RiceSeason.AMAN -> Icons.Filled.WaterDrop
                                RiceSeason.BORO -> Icons.Filled.Eco
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSeason(season) },
                                label = {
                                    Text(
                                        text = season.titleBn,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (selected) Icons.Filled.Check else seasonIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("season_chip_${season.code.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${uiState.selectedSeason.seasonWindowBn} • ${uiState.selectedSeason.waterNoteBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Variety Dropdown
                    val currentVariety = uiState.selectedVariety
                    ExposedDropdownMenuBox(
                        expanded = varietyMenuExpanded,
                        onExpandedChange = { varietyMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentVariety?.let {
                                "${it.nameBn} (${BanglaFormatters.toBanglaDigits(it.lifeCycleDays)} দিন)"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ধানের জাত নির্বাচন করুন") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = varietyMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("variety_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = varietyMenuExpanded,
                            onDismissRequest = { varietyMenuExpanded = false }
                        ) {
                            uiState.seasonVarieties.forEach { variety ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${variety.nameBn} (${variety.nameEn})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "জীবনকাল: ${BanglaFormatters.toBanglaDigits(variety.lifeCycleDays)} দিন • সম্ভাব্য ফলন: ${BanglaFormatters.formatDecimal(variety.expectedYieldTonPerHa, 1)} মেঃ টন/হেঃ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectVariety(variety)
                                        varietyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (currentVariety != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentVariety.specialTraitBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "আদর্শ চারার বয়স: ${BanglaFormatters.toBanglaDigits(currentVariety.seedlingAgeDays)} দিন • জীবনকাল: ${BanglaFormatters.toBanglaDigits(currentVariety.lifeCycleDays)} দিন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Date & Land Area Input Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "২. রোপণ/বপনের তারিখ ও জমির পরিমাণ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle: Transplanting Date vs Sowing Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.isTransplantingDate,
                            onClick = { onToggleDateType(true) },
                            label = { Text("চারা রোপণের তারিখ") },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_transplanting_date")
                        )
                        FilterChip(
                            selected = !uiState.isTransplantingDate,
                            onClick = { onToggleDateType(false) },
                            label = { Text("বীজতলায় বপনের তারিখ") },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_sowing_date")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date selector surface
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { showDatePicker = true }
                            .minimumInteractiveComponentSize()
                            .testTag("date_picker_button"),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "তারিখ নির্বাচন করুন",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (uiState.isTransplantingDate) {
                                            "মূল জমিতে রোপণের তারিখ"
                                        } else {
                                            "বীজতলায় বীজ বপনের তারিখ"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.inputDate.formatBangla(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(
                                text = "পরিবর্তন",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Land Area input
                    OutlinedTextField(
                        value = uiState.landAreaText,
                        onValueChange = onUpdateLandArea,
                        label = { Text("জমির পরিমাণ (${uiState.selectedLandUnit.labelBn})") },
                        placeholder = { Text("যেমন: ১ বা ৩৩") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("land_area_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(LandUnit.DECIMAL, LandUnit.BIGHA, LandUnit.HECTARE).forEach { unit ->
                            FilterChip(
                                selected = uiState.selectedLandUnit == unit,
                                onClick = { onSelectLandUnit(unit) },
                                label = { Text(unit.labelBn) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("land_unit_${unit.code.lowercase()}")
                            )
                        }
                    }

                    if (calc != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "রূপান্তর: ${BanglaFormatters.formatDecimal(calc.landConversion.decimals, 1)} শতাংশ = ${BanglaFormatters.formatDecimal(calc.landConversion.bighas, 2)} বিঘা = ${BanglaFormatters.formatDecimal(calc.landConversion.hectares, 4)} হেক্টর",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Calculation Summary KPIs
        if (calc != null) {
            item {
                CalculationSummarySection(
                    calc = calc,
                    onOpenTimelineTab = onOpenTimelineTab,
                    onOpenFertilizerTab = onOpenFertilizerTab,
                    onOpenSaveDialog = { showSaveDialog = true },
                    modifier = Modifier.widthIn(max = 640.dp)
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.inputDate.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onUpdateDate(BanglaDate.fromUtcMillis(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSaveDialog && calc != null) {
        SavePlanDialog(
            defaultName = "${calc.season.titleBn} - ${calc.variety.nameBn}",
            onDismiss = { showSaveDialog = false },
            onConfirm = { name, withNotifications ->
                onSavePlan(name, withNotifications)
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun HeroSeasonCard(
    season: RiceSeason,
    calc: CultivationCalculationResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_paddy_hero),
                contentDescription = "সবুজ ও সোনালী ধানের ক্ষেত",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x22000000),
                                Color(0xCC0C2E11)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xD9F6C453),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${season.titleBn} মৌসুম (${season.englishName})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF281800),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Column {
                    Text(
                        text = calc?.variety?.nameBn ?: "ধানের জাত নির্বাচন করুন",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (calc != null) {
                        val stageLabel = calc.currentStage?.nameBn
                            ?: "সম্ভাব্য কাটার তারিখ: ${calc.harvestDate.formatBangla()}"
                        Text(
                            text = if (calc.currentStage != null) {
                                "চলমান স্তর: $stageLabel • কর্তন: ${calc.harvestDate.formatShortBangla()}"
                            } else {
                                "সম্ভাব্য ধান কাটার তারিখ: ${calc.harvestDate.formatBangla()}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE3F5E4)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationSummarySection(
    calc: CultivationCalculationResult,
    onOpenTimelineTab: () -> Unit,
    onOpenFertilizerTab: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Harvest & Timeline Quick Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "চাষের সময়সূচি সারসংক্ষেপ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "${BanglaFormatters.toBanglaDigits(calc.variety.lifeCycleDays)} দিন",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                DateSummaryRow("বীজতলায় বপন", calc.sowingDate.formatBangla())
                DateSummaryRow("মূল জমিতে রোপণ", calc.transplantingDate.formatBangla())
                DateSummaryRow("সম্ভাব্য ধান কাটা", calc.harvestDate.formatBangla(), highlight = true)

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenTimelineTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("goto_timeline_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("১১টি বৃদ্ধি পর্যায় ও তারিখ দেখুন")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Yield & Fertilizer Quick Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "সম্ভাব্য মোট ফলন",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${BanglaFormatters.formatDecimal(calc.yieldConversion.totalMonForLand, 1)} মণ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "(${BanglaFormatters.formatDecimal(calc.yieldConversion.totalKgForLand, 0)} কেজি)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "বিঘা প্রতি: ${BanglaFormatters.formatDecimal(calc.yieldConversion.monPerBigha, 1)} মণ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "মোট সার",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ইউরিয়া: ${BanglaFormatters.formatKgOrGram(calc.fertilizerSummary.ureaKg)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "টিএসপি: ${BanglaFormatters.formatKgOrGram(calc.fertilizerSummary.tspKg)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "এমওপি: ${BanglaFormatters.formatKgOrGram(calc.fertilizerSummary.mopKg)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বিস্তারিত দেখুন →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onOpenFertilizerTab)
                            .padding(vertical = 2.dp)
                            .testTag("goto_fertilizer_link")
                    )
                }
            }
        }

        // Save Plan & Reminder CTA
        Button(
            onClick = onOpenSaveDialog,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("save_plan_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BookmarkAdd,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "জমির পরিকল্পনা সংরক্ষণ ও রিমাইন্ডার সেট করুন",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun DateSummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SavePlanDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, withNotifications: Boolean) -> Unit
) {
    var nameText by remember { mutableStateOf(defaultName) }
    var enableReminders by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("জমির পরিকল্পনা সংরক্ষণ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "জমিটি অফলাইনে সংরক্ষণ করলে সার ও সেচ ব্যবস্থাপনার তারিখ অনুযায়ী রিমাইন্ডার পাবেন।",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("জমির নাম / পরিচয়") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_plan_name_input")
                )
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { enableReminders = !enableReminders }
                        .minimumInteractiveComponentSize()
                ) {
                    Checkbox(
                        checked = enableReminders,
                        onCheckedChange = { enableReminders = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "সার ও সেচ রিমাইন্ডার নোটিফিকেশন চালু রাখুন",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameText, enableReminders) },
                modifier = Modifier.testTag("confirm_save_plan_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
