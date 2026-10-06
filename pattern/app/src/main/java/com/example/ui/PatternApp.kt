package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AnomalyDetailDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LogDayScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.screens.WelcomeHeroScreen
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.PatternViewModel
import kotlinx.coroutines.flow.collectLatest

data class NavItem(
    val tab: AppTab,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun PatternApp(
    viewModel: PatternViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (uiState.showOnboardingHero) {
        WelcomeHeroScreen(
            onGetStarted = {
                viewModel.setShowOnboardingHero(false)
            }
        )
        return
    }

    // Handle back button on sub-tabs
    if (uiState.currentTab != AppTab.HOME) {
        BackHandler {
            viewModel.navigateToTab(AppTab.HOME)
        }
    }

    val navItems = listOf(
        NavItem(AppTab.HOME, "Home", Icons.Default.Home, "nav_tab_home"),
        NavItem(AppTab.LOG, "Log", Icons.Default.AddCircleOutline, "nav_tab_log"),
        NavItem(AppTab.INSIGHTS, "Insights", Icons.Default.Insights, "nav_tab_insights"),
        NavItem(AppTab.TRENDS, "Trends", Icons.Default.ShowChart, "nav_tab_trends"),
        NavItem(AppTab.HISTORY, "History", Icons.Default.History, "nav_tab_history")
    )

    Scaffold(
        modifier = Modifier
            .testTag("pattern_app_scaffold")
            .fillMaxSize(),
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(0.dp)),
                containerColor = SurfaceDark,
                tonalElevation = 0.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = uiState.currentTab == item.tab

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item.tab == AppTab.LOG) {
                                viewModel.openLogScreenForNewDay()
                            } else {
                                viewModel.navigateToTab(item.tab)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentTeal,
                            selectedTextColor = AccentTeal,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.2f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.HOME -> HomeScreen(
                        uiState = uiState,
                        onLogToday = { viewModel.openLogScreenForNewDay() },
                        onEditToday = { entry -> viewModel.openEditScreenForEntry(entry) },
                        onViewAnomalies = { viewModel.navigateToTab(AppTab.INSIGHTS) },
                        onSelectAnomaly = { anomaly -> viewModel.selectAnomalyForDetails(anomaly) },
                        onNavigateTrends = { metric ->
                            viewModel.setTrendMetric(metric)
                            viewModel.navigateToTab(AppTab.TRENDS)
                        },
                        onResetDemoData = { viewModel.resetDemoData() },
                        onClearAllData = { viewModel.clearAllData() }
                    )

                    AppTab.LOG -> LogDayScreen(
                        uiState = uiState,
                        formState = uiState.logForm,
                        onMetricChange = { metric, value -> viewModel.updateFormMetric(metric, value) },
                        onDateChange = { date -> viewModel.updateFormDate(date) },
                        onSave = { viewModel.saveCurrentLogForm() },
                        onCancel = { viewModel.navigateToTab(AppTab.HOME) },
                        onDelete = { viewModel.deleteCurrentLogEntry() }
                    )

                    AppTab.INSIGHTS -> InsightsScreen(
                        uiState = uiState,
                        onTabSelected = { tab -> viewModel.setInsightsTab(tab) },
                        onSelectAnomaly = { anomaly -> viewModel.selectAnomalyForDetails(anomaly) },
                        onSelectMetric = { metric ->
                            viewModel.setTrendMetric(metric)
                            viewModel.navigateToTab(AppTab.TRENDS)
                        }
                    )

                    AppTab.TRENDS -> TrendsScreen(
                        uiState = uiState,
                        onMetricSelect = { metric -> viewModel.setTrendMetric(metric) },
                        onRangeSelect = { range -> viewModel.setTrendRange(range) },
                        onPointClicked = { entry ->
                            // When user clicks a point on trend, open edit or inspect
                            viewModel.openEditScreenForEntry(entry)
                        }
                    )

                    AppTab.HISTORY -> HistoryScreen(
                        uiState = uiState,
                        onSearchChange = { query -> viewModel.setHistorySearchQuery(query) },
                        onToggleAnomaliesOnly = { viewModel.toggleHistoryAnomaliesOnly() },
                        onEditEntry = { entry -> viewModel.openEditScreenForEntry(entry) },
                        onAddNewDay = { viewModel.openLogScreenForNewDay() }
                    )
                }
            }

            // Anomaly detail popup if selected
            uiState.selectedAnomalyForDetails?.let { anomaly ->
                AnomalyDetailDialog(
                    anomaly = anomaly,
                    allEntries = uiState.allEntries,
                    onDismiss = { viewModel.selectAnomalyForDetails(null) },
                    onEditEntry = { entry ->
                        viewModel.selectAnomalyForDetails(null)
                        viewModel.openEditScreenForEntry(entry)
                    }
                )
            }
        }
    }
}
