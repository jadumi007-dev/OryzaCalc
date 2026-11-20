package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.dao.PlanWithMilestones
import com.example.data.local.entity.ReminderMilestoneEntity
import com.example.domain.calculator.BanglaDate
import com.example.domain.calculator.BanglaFormatters
import com.example.domain.model.LandUnit
import com.example.domain.model.RiceSeason
import com.example.notification.RiceNotificationHelper

@Composable
fun SavedPlansRemindersScreen(
    savedPlans: List<PlanWithMilestones>,
    onToggleMilestone: (ReminderMilestoneEntity) -> Unit,
    onTogglePlanNotifications: (PlanWithMilestones) -> Unit,
    onLoadPlan: (PlanWithMilestones) -> Unit,
    onDeletePlan: (PlanWithMilestones) -> Unit,
    onSendTestNotification: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(RiceNotificationHelper.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reminders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Notification Permission & Test Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সার ও সেচ রিমাইন্ডার ব্যবস্থাপনা",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasNotificationPermission) {
                            "অফলাইন অ্যালার্ম রিমাইন্ডার সক্রিয় রয়েছে। নির্ধারিত দিনে সকাল ৮টায় নোটিফিকেশন পাবেন।"
                        } else {
                            "সময়মতো সার ও সেচের রিমাইন্ডার পেতে নোটিফিকেশন অনুমতি প্রদান করুন।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("request_notification_permission_btn")
                            ) {
                                Text("অনুমতি দিন")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onSendTestNotification,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("test_notification_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নমুনা নোটিফিকেশন পাঠান")
                            }
                        }
                    }
                }
            }
        }

        if (savedPlans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "এখনো কোনো জমির পরিকল্পনা সংরক্ষণ করা হয়নি",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "হিসাব ট্যাব থেকে মৌসুম, ধানের জাত ও জমির পরিমাণ দিয়ে ‘জমির পরিকল্পনা সংরক্ষণ’ বোতামে চাপ দিন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToCalculator,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("নতুন হিসাব তৈরি করুন")
                        }
                    }
                }
            }
        } else {
            items(
                items = savedPlans,
                key = { it.plan.id }
            ) { item ->
                SavedPlanCard(
                    item = item,
                    onToggleMilestone = onToggleMilestone,
                    onToggleNotifications = { onTogglePlanNotifications(item) },
                    onLoadPlan = { onLoadPlan(item) },
                    onDeletePlan = { onDeletePlan(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                )
            }
        }
    }
}

@Composable
private fun SavedPlanCard(
    item: PlanWithMilestones,
    onToggleMilestone: (ReminderMilestoneEntity) -> Unit,
    onToggleNotifications: () -> Unit,
    onLoadPlan: () -> Unit,
    onDeletePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plan = item.plan
    val season = RiceSeason.fromCode(plan.season)
    val unit = LandUnit.fromCode(plan.landUnit)
    val sortedMilestones = remember(item.milestones) {
        item.milestones.sortedBy { it.dueDateEpochDay }
    }

    Card(
        modifier = modifier.testTag("saved_plan_card_${plan.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.fieldName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${season.titleBn} • ${plan.varietyNameBn} • ${BanglaFormatters.formatDecimal(plan.landArea, 2)} ${unit.labelBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeletePlan,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "পরিকল্পনা মুছুন",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onLoadPlan,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("হিসাবে লোড করুন")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "অ্যালার্ম",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = plan.remindersEnabled,
                        onCheckedChange = { onToggleNotifications() }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "করনীয় কাজের সময়সূচি (${BanglaFormatters.toBanglaDigits(sortedMilestones.size)}টি)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            sortedMilestones.forEach { milestone ->
                val dateLabel = BanglaDate.fromEpochDay(milestone.dueDateEpochDay).formatBangla()
                val categoryIcon = when (milestone.category) {
                    "FERTILIZER" -> Icons.Filled.Science
                    "WATER" -> Icons.Filled.WaterDrop
                    else -> Icons.Filled.Agriculture
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = milestone.isCompleted,
                            onCheckedChange = { onToggleMilestone(milestone) }
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 6.dp, end = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = milestone.titleBn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (milestone.isCompleted) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                }
                            )
                            Text(
                                text = milestone.descriptionBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
