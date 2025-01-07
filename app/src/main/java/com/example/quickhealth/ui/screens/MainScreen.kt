package com.example.quickhealth.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quickhealth.ui.navigation.MainNavHost
import com.example.quickhealth.ui.navigation.Destinations
import com.example.quickhealth.ui.viewmodels.SharedViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val sharedViewModel: SharedViewModel = viewModel()

    // List of screens that should show the bottom navigation bar
    val screensWithBottomNavBar = listOf(
        Destinations.HOME,
        Destinations.PROFILE,
        Destinations.GOAL,
        Destinations.ACTIVITY,
        Destinations.SETTINGS,
        Destinations.WATER_REMINDER
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomNavBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainNavHost(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
    }
}









