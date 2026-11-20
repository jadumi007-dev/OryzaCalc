package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.model.CultivationCalculationResult
import com.example.domain.model.FertilizerSplitItem
import com.example.domain.model.LandUnit
import com.example.ui.viewmodel.ConverterState
import com.example.ui.viewmodel.YieldConverterInputMode

@Composable
fun FertilizerAndConverterScreen(
    calculation: CultivationCalculationResult?,
    converterState: ConverterState,
    onUpdateLandInput: (String, LandUnit?) -> Unit,
    onUpdateYieldInput: (String, YieldConverterInputMode?) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("fertilizer_converter_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sub-navigation segmented control
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SegmentedButton(
                selected = subTabIndex == 0,
                onClick = { subTabIndex = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Science,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("subtab_fertilizer")
            ) {
                Text("সারের হিসাব ও কিস্তি")
            }
            SegmentedButton(
                selected = subTabIndex == 1,
                onClick = { subTabIndex = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("subtab_converter")
            ) {
                Text("জমি ও ফলন রূপান্তর")
            }
        }

        if (subTabIndex == 0) {
            FertilizerCalculatorContent(calculation = calculation)
        } else {
            UnitConverterContent(
                converterState = converterState,
                onUpdateLandInput = onUpdateLandInput,
                onUpdateYieldInput = onUpdateYieldInput
            )
        }
    }
}

@Composable
private fun FertilizerCalculatorContent(
    calculation: CultivationCalculationResult?
) {
    if (calculation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("তথ্য লোড হচ্ছে...")
        }
        return
    }

    val fert = calculation.fertilizerSummary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${calculation.variety.nameBn} — মোট সারের চাহিদা",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "জমি: ${BanglaFormatters.formatDecimal(fert.decimals, 1)} শতাংশ (${BanglaFormatters.formatDecimal(fert.bighas, 2)} বিঘা / ${BanglaFormatters.formatDecimal(fert.hectares, 4)} হেক্টর)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FertilizerDoseRow("ইউরিয়া (Urea)", fert.ureaKg, "৩ কিস্তিতে উপরিপ্রয়োগ")
                    FertilizerDoseRow("টিএসপি (TSP)", fert.tspKg, "জমি তৈরির শেষ চাষে")
                    FertilizerDoseRow("এমওপি / পটাশ (MoP)", fert.mopKg, "মূল ও কাইচ থোড়ে")
                    FertilizerDoseRow("জিপসাম (Gypsum)", fert.gypsumKg, "জমি তৈরির শেষ চাষে")
                    FertilizerDoseRow("জিংক সালফেট (Zinc)", fert.zincKg, "টিএসপি-র সাথে মেশাবেন না")
                }
            }
        }

        item {
            Text(
                text = "সার প্রয়োগের কিস্তি ও সঠিক তারিখ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(top = 4.dp)
            )
        }

        items(
            items = calculation.fertilizerSplits,
            key = { it.stageCode }
        ) { split ->
            FertilizerSplitCard(
                split = split,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun FertilizerDoseRow(name: String, kg: Double, note: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = BanglaFormatters.formatKgOrGram(kg),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FertilizerSplitCard(
    split: FertilizerSplitItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = split.titleBn,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = split.applicationDate.formatShortBangla(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val doseLabels = buildList {
                if (split.ureaKg > 0.01) add("ইউরিয়া: ${BanglaFormatters.formatKgOrGram(split.ureaKg)}")
                if (split.tspKg > 0.01) add("টিএসপি: ${BanglaFormatters.formatKgOrGram(split.tspKg)}")
                if (split.mopKg > 0.01) add("এমওপি: ${BanglaFormatters.formatKgOrGram(split.mopKg)}")
                if (split.gypsumKg > 0.01) add("জিপসাম: ${BanglaFormatters.formatKgOrGram(split.gypsumKg)}")
                if (split.zincKg > 0.01) add("জিংক: ${BanglaFormatters.formatKgOrGram(split.zincKg)}")
            }

            Text(
                text = doseLabels.joinToString("  •  "),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = split.tipBn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnitConverterContent(
    converterState: ConverterState,
    onUpdateLandInput: (String, LandUnit?) -> Unit,
    onUpdateYieldInput: (String, YieldConverterInputMode?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("unit_converter_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Land Area Converter
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "জমির পরিমাপ রূপান্তর",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "মানদণ্ড: ২৪৭ শতাংশ = ১ হেক্টর • ৩৩ শতাংশ = ১ বিঘা • ১০০ শতাংশ = ১ একর",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = converterState.landInputText,
                        onValueChange = { onUpdateLandInput(it, null) },
                        label = { Text("পরিমাণ লিখুন (${converterState.landInputUnit.labelBn})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_land_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LandUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = converterState.landInputUnit == unit,
                                onClick = { onUpdateLandInput(converterState.landInputText, unit) },
                                label = { Text(unit.labelBn) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    val r = converterState.landResult
                    ConversionResultRow("শতাংশ (Decimal)", "${BanglaFormatters.formatDecimal(r.decimals, 2)} শতাংশ")
                    ConversionResultRow("বিঘা (Bigha)", "${BanglaFormatters.formatDecimal(r.bighas, 3)} বিঘা")
                    ConversionResultRow("হেক্টর (Hectare)", "${BanglaFormatters.formatDecimal(r.hectares, 4)} হেক্টর")
                    ConversionResultRow("একর (Acre)", "${BanglaFormatters.formatDecimal(r.acres, 3)} একর")
                }
            }
        }

        // 2. Yield Converter
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ধানের ফলন রূপান্তর",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "মানদণ্ড: ৪০ কেজি = ১ মণ • ৩৩ শতাংশ = ১ বিঘা • ২৪৭ শতাংশ = ১ হেক্টর",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = converterState.yieldInputText,
                        onValueChange = { onUpdateYieldInput(it, null) },
                        label = { Text("ফলনের হার (${converterState.yieldInputMode.labelBn})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_yield_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        YieldConverterInputMode.entries.forEach { mode ->
                            FilterChip(
                                selected = converterState.yieldInputMode == mode,
                                onClick = { onUpdateYieldInput(converterState.yieldInputText, mode) },
                                label = { Text(mode.labelBn) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    val yr = converterState.yieldResult
                    ConversionResultRow(
                        "হেক্টর প্রতি ফলন",
                        "${BanglaFormatters.formatDecimal(yr.metricTonPerHa, 2)} মেঃ টন/হেক্টর"
                    )
                    ConversionResultRow(
                        "বিঘা প্রতি (কেজি)",
                        "${BanglaFormatters.formatDecimal(yr.kgPerBigha, 1)} কেজি/বিঘা"
                    )
                    ConversionResultRow(
                        "বিঘা প্রতি (মণ)",
                        "${BanglaFormatters.formatDecimal(yr.monPerBigha, 2)} মণ/বিঘা"
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversionResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
