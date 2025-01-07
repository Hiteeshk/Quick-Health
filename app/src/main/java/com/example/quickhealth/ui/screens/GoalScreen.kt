package com.example.quickhealth.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quickhealth.ui.viewmodels.GoalViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen() {
    val viewModel: GoalViewModel = viewModel()
    val goals by viewModel.goals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Default goal values
    val defaultStepsGoal = "10000"
    val defaultWaterGoal = "8"
    val defaultSleepGoal = "8"

    // States for goal input fields with default values
    var stepsGoal by remember(goals) { mutableStateOf(goals?.stepsGoal?.toString() ?: defaultStepsGoal) }
    var waterGoal by remember(goals) { mutableStateOf(goals?.waterIntakeGoal?.toString() ?: defaultWaterGoal) }
    var sleepGoal by remember(goals) { mutableStateOf(goals?.sleepGoal?.toString() ?: defaultSleepGoal) }

    // Error states
    var stepsError by remember { mutableStateOf("") }
    var waterError by remember { mutableStateOf("") }
    var sleepError by remember { mutableStateOf("") }

    // Validation functions
    fun validateSteps(value: String): Boolean {
        return try {
            val steps = value.toInt()
            if (steps in 1000..50000) {
                stepsError = ""
                true
            } else {
                stepsError = "Steps should be between 1,000 and 50,000"
                false
            }
        } catch (e: NumberFormatException) {
            stepsError = "Please enter a valid number"
            false
        }
    }

    fun validateWater(value: String): Boolean {
        return try {
            val water = value.toFloat()
            if (water in 3f..20f) {
                waterError = ""
                true
            } else {
                waterError = "Water intake should be atleast 3L to 5L"
                false
            }
        } catch (e: NumberFormatException) {
            waterError = "Please enter a valid number"
            false
        }
    }

    fun validateSleep(value: String): Boolean {
        return try {
            val sleep = value.toFloat()
            if (sleep in 4f..12f) {
                sleepError = ""
                true
            } else {
                sleepError = "Sleep hours should be between 4 and 12"
                false
            }
        } catch (e: NumberFormatException) {
            sleepError = "Please enter a valid number"
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Set Your Daily Goals",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Customize your health targets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Steps Goal Input
                    OutlinedTextField(
                        value = stepsGoal,
                        onValueChange = { 
                            stepsGoal = it
                            validateSteps(it)
                        },
                        label = { Text("Daily Steps Target") },
                        supportingText = { 
                            if (stepsError.isNotEmpty()) {
                                Text(stepsError, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = stepsError.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Water Goal Input
                    OutlinedTextField(
                        value = waterGoal,
                        onValueChange = { 
                            waterGoal = it
                            validateWater(it)
                        },
                        label = { Text("Water Intake (L)") },
                        supportingText = { 
                            if (waterError.isNotEmpty()) {
                                Text(waterError, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Enter water intake goal in liters", 
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        },
                        isError = waterError.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Sleep Goal Input
                    OutlinedTextField(
                        value = sleepGoal,
                        onValueChange = { 
                            sleepGoal = it
                            validateSleep(it)
                        },
                        label = { Text("Sleep Duration (hours)") },
                        supportingText = { 
                            if (sleepError.isNotEmpty()) {
                                Text(sleepError, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = sleepError.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (validateSteps(stepsGoal) && 
                                validateWater(waterGoal) && 
                                validateSleep(sleepGoal)) {
                                viewModel.saveGoals(
                                    stepsGoal.toInt(),
                                    waterGoal.toFloat().toInt(), // Convert to int for storage
                                    sleepGoal.toFloat()
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Save Goals",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Success/Error Message
                    AnimatedVisibility(
                        visible = successMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (successMessage.contains("success", ignoreCase = true))
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = successMessage,
                                modifier = Modifier.padding(16.dp),
                                color = if (successMessage.contains("success", ignoreCase = true))
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}