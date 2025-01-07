package com.example.quickhealth.ui.navigation

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.quickhealth.ui.screens.*
import com.example.quickhealth.ui.screens.HomeScreen
import com.example.quickhealth.ui.viewmodels.SharedViewModel
import com.google.firebase.auth.FirebaseAuth


object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SIGN_UP = "sign_up"
    const val GOAL = "goal"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"
    const val WATER_REMINDER = "water_reminder"
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null) Destinations.HOME else Destinations.LOGIN
) {
    val auth = FirebaseAuth.getInstance()


    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onSignUp = {
                    navController.navigate(Destinations.SIGN_UP)
                }
            )
        }
        composable(Destinations.HOME) {
            HomeScreen(navController = navController)
        }
        composable(Destinations.GOAL) {
            GoalScreen()
        }
        composable(Destinations.ACTIVITY) {
            ActivityTrackerScreen(navController = navController)
        }
        composable(Destinations.PROFILE) {
            ProfileScreen()
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                }, navController = navController,
            )
        }
        composable(Destinations.WATER_REMINDER) {
            WaterReminderScreen(navController = navController)
        }

        composable(Destinations.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_UP) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(
            route = "add_water_intake/{waterAmount}",
            arguments = listOf(
                navArgument("waterAmount") { 
                    type = NavType.FloatType 
                }
            )
        ) { backStackEntry ->
            val waterAmount = backStackEntry.arguments?.getFloat("waterAmount") ?: 0f
            AddWaterIntakeScreen(
                navController = navController,
                sharedViewModel = sharedViewModel,
                waterAmount = waterAmount
            )
        }

    }
}
