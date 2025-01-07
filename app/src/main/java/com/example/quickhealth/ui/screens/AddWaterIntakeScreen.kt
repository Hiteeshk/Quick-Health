package com.example.quickhealth.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickhealth.ui.viewmodels.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaterIntakeScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    waterAmount: Float // in milliliters
) {
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Time to Hydrate!") },
        text = {
            Text(
                text = String.format(
                    "Would you like to record drinking %.1fL of water?", 
                    waterAmount / 1000
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    sharedViewModel.updateWaterIntake(waterAmount / 1000)
                    navController.popBackStack()
                }
            ) {
                Text("I Drank It")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Skip")
            }
        }
    )
} 