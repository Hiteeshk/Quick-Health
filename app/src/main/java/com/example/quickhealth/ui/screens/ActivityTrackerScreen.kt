package com.example.quickhealth.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quickhealth.ui.navigation.Destinations
import com.example.quickhealth.ui.viewmodels.ActivityTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTrackerScreen(
    navController: NavController,
    viewModel: ActivityTrackerViewModel = viewModel()
) {
    val dailyGoals by viewModel.dailyGoals.collectAsState()
    var selectedTimeRange by remember { mutableStateOf(TimeRange.DAILY) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Activity Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            actions = {
                IconButton(onClick = { navController.navigate(Destinations.WATER_REMINDER) }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Set Reminders",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Time Range Selector
        TimeRangeSelector(
            selectedRange = selectedTimeRange,
            onRangeSelected = { selectedTimeRange = it },
            viewModel = viewModel
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (selectedTimeRange == TimeRange.DAILY) {
                // Steps Progress Card - Only show in Daily view
                StepsProgressCard(
                    currentSteps = viewModel.automaticSteps.toInt(),
                    goalSteps = dailyGoals?.stepsGoal ?: 10000,
                    isAvailable = viewModel.isStepCounterAvailable
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Statistics Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val averageSteps by viewModel.averageSteps.collectAsState()
                val calories by viewModel.calories.collectAsState()
                val currentTimeRange by viewModel.currentTimeRange.collectAsState()

                StatisticsCard(
                    title = when (currentTimeRange) {
                        TimeRange.DAILY -> "Today's Steps"
                        else -> "Average Steps"
                    },
                    value = when (currentTimeRange) {
                        TimeRange.DAILY -> "${viewModel.automaticSteps.toInt()}"
                        else -> "$averageSteps"
                    },
                    icon = Icons.Default.DirectionsWalk,
                    modifier = Modifier.weight(1f)
                )
                StatisticsCard(
                    title = when (currentTimeRange) {
                        TimeRange.DAILY -> "Today's Calories"
                        else -> "Average Calories"
                    },
                    value = when (currentTimeRange) {
                        TimeRange.DAILY -> "${calculateCalories(viewModel.automaticSteps.toInt())}"
                        else -> "$calories"
                    },
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Tracking Card
            SleepTrackingCard(
                sleepHours = viewModel.sleepHours,
                onSleepHoursChange = { viewModel.sleepHours = it },
                goalHours = dailyGoals?.sleepGoal ?: 8f
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveActivity() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Activity")
                }
            }

            // Error Message
            viewModel.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    viewModel: ActivityTrackerViewModel
) {
    ScrollableTabRow(
        selectedTabIndex = selectedRange.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        TimeRange.values().forEach { range ->
            Tab(
                selected = selectedRange == range,
                onClick = { 
                    onRangeSelected(range)
                    viewModel.calculateAverageSteps(range)
                },
                text = { Text(range.title) }
            )
        }
    }
}

@Composable
fun StepsProgressCard(
    currentSteps: Int,
    goalSteps: Int,
    isAvailable: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAvailable) {
                Text(
                    text = "$currentSteps",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "steps today",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (currentSteps.toFloat() / goalSteps).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    text = "Goal: $goalSteps steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Step counter not available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackingCard(
    sleepHours: String,
    onSleepHoursChange: (String) -> Unit,
    goalHours: Float
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sleep Tracking",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = sleepHours,
                onValueChange = onSleepHoursChange,
                label = { Text("Sleep Hours") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                text = "Goal: ${goalHours}h",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

enum class TimeRange(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

// Helper functions for calculations
private fun calculateAverageSteps(timeRange: TimeRange): Int {
    // TODO: Implement actual calculation based on historical data
    return 8000
}

private fun calculateCalories(steps: Int): Int {
    // Rough estimation: 1 step ≈ 0.04 calories
    return (steps * 0.04).toInt()
}
