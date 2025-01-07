package com.example.quickhealth.ui.screens

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import com.example.quickhealth.ui.viewmodels.WaterReminderViewModel
import com.example.quickhealth.ui.viewmodels.SharedViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.util.Log
import androidx.compose.foundation.clickable

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterReminderScreen(
    navController: NavHostController,
    viewModel: WaterReminderViewModel = viewModel()
) {
    val isSchedulingComplete by viewModel.isSchedulingComplete.collectAsState()
    val dailyWaterGoal by viewModel.dailyWaterGoal.collectAsState()
    val todayWaterIntake by viewModel.todayWaterIntake.collectAsState(0.0)
    val remainingWaterIntake = viewModel.getRemainingWaterIntake()
    val goalPercentage = viewModel.getGoalPercentage()
    val remindersNeeded = viewModel.getNumberOfRemindersNeeded()

    // Effect to handle navigation after successful scheduling
    LaunchedEffect(isSchedulingComplete) {
        if (isSchedulingComplete) {
            navController.popBackStack()
        }
    }

    // Show loading dialog while saving/scheduling
    if (viewModel.isSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Please Wait") },
            text = { Text("Saving settings and scheduling reminders...") },
            confirmButton = { }
        )
    }

    // Show error dialog if there's an error
    viewModel.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Use the singleton instance
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    var showValidationError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Water Reminder Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Start Time Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartTimePicker = true }
        ) {
            ListItem(
                headlineContent = { Text("Start Time") },
                supportingContent = { Text(viewModel.startTime.format(timeFormatter)) },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = "Edit Start Time") }
            )
        }

        // End Time Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEndTimePicker = true }
        ) {
            ListItem(
                headlineContent = { Text("End Time") },
                supportingContent = { Text(viewModel.endTime.format(timeFormatter)) },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = "Edit End Time") }
            )
        }

        // Interval Selection
        Text(
            text = "Reminder Interval",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Ensure unique intervals
            IntervalChip(30, viewModel)
            IntervalChip(60, viewModel)
            IntervalChip(90, viewModel)
            IntervalChip(120, viewModel)
        }

        // Daily Summary Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Daily Summary",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SummaryItem("Daily Goal", "${dailyWaterGoal}L")
                SummaryItem("Today's Intake", "${todayWaterIntake}L")
                SummaryItem("Remaining Intake", if (remainingWaterIntake > 0) "${remainingWaterIntake}L" else "Target Reached")
                SummaryItem("Goal Percentage", "${goalPercentage.toInt()}%")
                SummaryItem("Reminders Needed", "${remindersNeeded} reminders needed")

                if (showValidationError) {
                    Text(
                        "Selected time range is invalid",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save Button
        Button(
            onClick = {
                val isValid = viewModel.validateSettings()
                if (isValid) {
                    viewModel.saveSettings()
                } else {
                    showValidationError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isSaving
        ) {
            Text(if (viewModel.isSaving) "Saving..." else "Save Settings")
        }

        // Show Start Time Picker
        if (showStartTimePicker) {
            TimePickerDialog(
                LocalContext.current,
                { _, hour, minute ->
                    viewModel.updateStartTime(hour, minute)
                    showStartTimePicker = false
                },
                viewModel.startTime.hour,
                viewModel.startTime.minute,
                false
            ).show()
        }

        // Show End Time Picker
        if (showEndTimePicker) {
            TimePickerDialog(
                LocalContext.current,
                { _, hour, minute ->
                    val selectedTime = LocalTime.of(hour, minute)
                    if (selectedTime.isBefore(viewModel.startTime)) {
                        showValidationError = true
                    } else {
                        viewModel.updateEndTime(hour, minute)
                        showEndTimePicker = false
                    }
                },
                viewModel.endTime.hour,
                viewModel.endTime.minute,
                false
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalChip(
    minutes: Int,
    viewModel: WaterReminderViewModel
) {
    FilterChip(
        selected = viewModel.intervalMinutes == minutes,
        onClick = {
            viewModel.updateInterval(minutes) // Update interval
        },
        label = {
            Text(
                when (minutes) {
                    60 -> "1h" // Display 1 hour for 60 minutes
                    90 -> "1.5h" // Display 1.5 hours for 90 minutes
                    else -> "${minutes}min" // Display minutes for other intervals
                }
            )
        }
    )
}

@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}