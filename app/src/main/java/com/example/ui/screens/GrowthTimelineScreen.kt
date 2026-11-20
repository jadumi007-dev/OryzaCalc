package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.model.CultivationCalculationResult
import com.example.domain.model.GrowthPhase
import com.example.domain.model.GrowthStageItem
import com.example.ui.theme.PhaseReproductiveColor
import com.example.ui.theme.PhaseRipeningColor
import com.example.ui.theme.PhaseVegetativeColor

@Composable
fun GrowthTimelineScreen(
    calculation: CultivationCalculationResult?,
    modifier: Modifier = Modifier
) {
    if (calculation == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("তথ্য লোড হচ্ছে...")
        }
        return
    }

    val vegetativeDays = calculation.stages
        .filter { it.phase == GrowthPhase.VEGETATIVE }
        .sumOf { it.calculatedDays }
    val reproductiveDays = calculation.stages
        .filter { it.phase == GrowthPhase.REPRODUCTIVE }
        .sumOf { it.calculatedDays }
    val ripeningDays = calculation.stages
        .filter { it.phase == GrowthPhase.RIPENING }
        .sumOf { it.calculatedDays }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("growth_timeline_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${calculation.variety.nameBn} — বৃদ্ধি পর্যায় ও স্তরসমূহ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বপন: ${calculation.sowingDate.formatBangla()} • রোপণ: ${calculation.transplantingDate.formatBangla()} • কর্তন: ${calculation.harvestDate.formatBangla()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhaseBadgeChip(
                            title = "দৈহিক বৃদ্ধি",
                            days = vegetativeDays,
                            color = PhaseVegetativeColor,
                            modifier = Modifier.weight(1f)
                        )
                        PhaseBadgeChip(
                            title = "প্রজনন পর্যায়",
                            days = reproductiveDays,
                            color = PhaseReproductiveColor,
                            modifier = Modifier.weight(1f)
                        )
                        PhaseBadgeChip(
                            title = "পাকা পর্যায়",
                            days = ripeningDays,
                            color = PhaseRipeningColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Grouped Phases & 11 Stages
        GrowthPhase.entries.forEach { phase ->
            val phaseStages = calculation.stages.filter { it.phase == phase }
            item(key = "header_${phase.name}") {
                PhaseSectionHeader(
                    phase = phase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }
            items(
                items = phaseStages,
                key = { it.order }
            ) { stage ->
                StageTimelineCard(
                    stage = stage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }
        }
    }
}

@Composable
private fun PhaseBadgeChip(
    title: String,
    days: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${BanglaFormatters.toBanglaDigits(days)} দিন",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PhaseSectionHeader(
    phase: GrowthPhase,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (phase) {
        GrowthPhase.VEGETATIVE -> PhaseVegetativeColor
        GrowthPhase.REPRODUCTIVE -> PhaseReproductiveColor
        GrowthPhase.RIPENING -> PhaseRipeningColor
    }
    Row(
        modifier = modifier.padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(phaseColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${phase.titleBn} (${phase.englishTitle})",
            style = MaterialTheme.typography.titleMedium,
            color = phaseColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StageTimelineCard(
    stage: GrowthStageItem,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (stage.phase) {
        GrowthPhase.VEGETATIVE -> PhaseVegetativeColor
        GrowthPhase.REPRODUCTIVE -> PhaseReproductiveColor
        GrowthPhase.RIPENING -> PhaseRipeningColor
    }

    val containerColor = if (stage.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .then(
                if (stage.isCurrent) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                } else {
                    Modifier
                }
            )
            .testTag("stage_card_${stage.order}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (stage.isCurrent) 4.dp else 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(phaseColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.toBanglaDigits(stage.order),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stage.nameBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stage.nameEn} • আদর্শ: ${stage.standardRangeBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = phaseColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                stage.isCurrent -> Icons.Filled.PlayCircle
                                stage.isCompleted -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = null,
                            tint = phaseColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (stage.isCurrent) {
                                "চলমান (${BanglaFormatters.toBanglaDigits(stage.calculatedDays)} দিন)"
                            } else {
                                "${BanglaFormatters.toBanglaDigits(stage.calculatedDays)} দিন"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = phaseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "সময়কাল: ${stage.startDate.formatBangla()} – ${stage.endDate.formatBangla()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = phaseColor,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stage.careTipBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
