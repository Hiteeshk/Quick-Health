package com.example.quickhealth.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.quickhealth.R
import com.example.quickhealth.data.model.HealthData
import com.example.quickhealth.data.model.WellnessGoal
import com.example.quickhealth.ui.navigation.Destinations
import com.example.quickhealth.ui.viewmodels.HomeViewModel

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.util.Calendar


// Composable for New Users
@Composable
fun NewUserScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF80D8FF), Color.White),
                    startY = 0f,
                    endY = 400f
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header Section
        Spacer(modifier = Modifier.height(10.dp))
        Image(
            painter = painterResource(id = R.drawable.welcome),
            contentDescription = "Welcome Icon",
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .border(4.dp, Color(0xFF80D8FF), CircleShape)
                .background(MaterialTheme.colorScheme.background)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to QuickHealth!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Track your health, set goals, and stay on top of your wellness journey.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate(Destinations.GOAL) },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0073e6))
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


// Composable for Returning Users
@Composable
fun ReturningUserScreen(
    healthData: HealthData?,
    wellnessGoals: WellnessGoal?,
    userName: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Section with Animated Greeting
        WelcomeSection(userName)

        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Overview Section
        StatsOverviewSection(healthData, wellnessGoals)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Daily Goals Progress Section
        DailyGoalsSection(healthData, wellnessGoals)
    }
}

@Composable
fun WelcomeSection(userName: String) {
    val greeting = remember { getGreeting() }
    
    Column {
        Text(
            text = "$greeting, $userName!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Let's keep track of your health today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}





@Composable
fun StatsOverviewSection(healthData: HealthData?, wellnessGoals: WellnessGoal?) {
    Column {
        Text(
            text = "Today's Overview",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                icon = Icons.Default.DirectionsRun,
                value = "${healthData?.steps ?: 0}",
                label = "Steps",
                goal = wellnessGoals?.stepsGoal?.toString() ?: "0",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.LocalDrink,
                value = "${String.format("%.1f", (healthData?.waterIntake ?: 0) / 1000f)}",
                label = "Water (L)",
                goal = "${wellnessGoals?.waterIntakeGoal ?: 0}L",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sleep Stats Card
        StatCard(
            icon = Icons.Default.Bedtime,
            value = "${String.format("%.1f", healthData?.sleepHours ?: 0f)}",
            label = "Sleep (hrs)",
            goal = "${wellnessGoals?.sleepGoal ?: 0}hrs",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    goal: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Goal: $goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DailyGoalsSection(healthData: HealthData?, wellnessGoals: WellnessGoal?) {
    Column {
        Text(
            text = "Daily Goals Progress",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                GoalProgressBar(
                    label = "Steps",
                    current = healthData?.steps?.toFloat() ?: 0f,
                    goal = wellnessGoals?.stepsGoal?.toFloat() ?: 1f
                )
                Spacer(modifier = Modifier.height(16.dp))
                GoalProgressBar(
                    label = "Water Intake",
                    current = healthData?.waterIntake?.toFloat() ?: 0f,
                    goal = wellnessGoals?.waterIntakeGoal?.times(1000)?.toFloat() ?: 1f
                )
                Spacer(modifier = Modifier.height(16.dp))
                GoalProgressBar(
                    label = "Sleep Duration",
                    current = healthData?.sleepHours ?: 0f,
                    goal = wellnessGoals?.sleepGoal?.toFloat() ?: 1f
                )
            }
        }
    }
}

@Composable
fun GoalProgressBar(
    label: String,
    current: Float,
    goal: Float
) {
    val progress = (current / goal).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val homeViewModel: HomeViewModel = viewModel()
    val isNewUser by homeViewModel.isNewUser.collectAsState()
    val healthData by homeViewModel.healthData.collectAsState()
    val userName by homeViewModel.userName.collectAsState()
    val wellnessGoals by homeViewModel.wellnessGoals.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            isNewUser -> {
                NewUserScreen(navController)
            }
            else -> {
                ReturningUserScreen(
                    healthData = healthData,
                    wellnessGoals = wellnessGoals,
                    userName = userName ?: "User",
                    navController = navController
                )
            }
        }
    }
}



