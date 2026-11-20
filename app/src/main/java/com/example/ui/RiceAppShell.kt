package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.RiceHeaderBar
import com.example.ui.screens.CalculatorHomeScreen
import com.example.ui.screens.FertilizerAndConverterScreen
import com.example.ui.screens.GrowthTimelineScreen
import com.example.ui.screens.SavedPlansRemindersScreen
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.RiceCalculatorViewModel
import com.example.ui.viewmodel.RiceUiState

private data class TabNavMeta(
    val tab: AppTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navItems = listOf(
    TabNavMeta(AppTab.CALCULATOR, Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_calculator"),
    TabNavMeta(AppTab.TIMELINE, Icons.Filled.Timeline, Icons.Outlined.Timeline, "nav_timeline"),
    TabNavMeta(AppTab.FERTILIZER_CONVERTER, Icons.Filled.Science, Icons.Outlined.Science, "nav_fertilizer"),
    TabNavMeta(AppTab.REMINDERS, Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsActive, "nav_reminders")
)

@Composable
fun RiceAppShell(
    uiState: RiceUiState,
    viewModel: RiceCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Return to primary Calculator tab on system Back when on secondary tabs
    BackHandler(enabled = uiState.activeTab != AppTab.CALCULATOR) {
        viewModel.selectTab(AppTab.CALCULATOR)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            topBar = { RiceHeaderBar() },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = uiState.activeTab == item.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.tab.titleBn
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.tab.titleBn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = uiState.activeTab == item.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.tab.titleBn
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.tab.titleBn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Crossfade(
                            targetState = uiState.activeTab,
                            label = "tab_crossfade"
                        ) { tab ->
                            when (tab) {
                                AppTab.CALCULATOR -> CalculatorHomeScreen(
                                    uiState = uiState,
                                    onSelectSeason = viewModel::selectSeason,
                                    onSelectVariety = viewModel::selectVariety,
                                    onUpdateDate = viewModel::updateInputDate,
                                    onToggleDateType = viewModel::setIsTransplantingDate,
                                    onUpdateLandArea = viewModel::updateLandAreaText,
                                    onSelectLandUnit = viewModel::selectLandUnit,
                                    onSavePlan = viewModel::saveCurrentPlan,
                                    onOpenTimelineTab = { viewModel.selectTab(AppTab.TIMELINE) },
                                    onOpenFertilizerTab = { viewModel.selectTab(AppTab.FERTILIZER_CONVERTER) }
                                )

                                AppTab.TIMELINE -> GrowthTimelineScreen(
                                    calculation = uiState.calculation
                                )

                                AppTab.FERTILIZER_CONVERTER -> FertilizerAndConverterScreen(
                                    calculation = uiState.calculation,
                                    converterState = uiState.converter,
                                    onUpdateLandInput = viewModel::updateConverterLandInput,
                                    onUpdateYieldInput = viewModel::updateConverterYieldInput
                                )

                                AppTab.REMINDERS -> SavedPlansRemindersScreen(
                                    savedPlans = uiState.savedPlans,
                                    onToggleMilestone = viewModel::toggleMilestoneCompleted,
                                    onTogglePlanNotifications = viewModel::togglePlanNotifications,
                                    onLoadPlan = viewModel::loadSavedPlan,
                                    onDeletePlan = viewModel::deletePlan,
                                    onSendTestNotification = viewModel::sendTestNotification,
                                    onNavigateToCalculator = { viewModel.selectTab(AppTab.CALCULATOR) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
