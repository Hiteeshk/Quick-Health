package com.example.quickhealth.ui.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quickhealth.ui.navigation.Destinations
import com.example.quickhealth.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    // Elevation transition effect
    val elevation by animateDpAsState(
        targetValue = if (currentDestination == Destinations.HOME) 12.dp else 4.dp,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    NavigationBar(
        containerColor = Color(0xFF324D85),
        tonalElevation = elevation,
        modifier = Modifier.height(60.dp)
    ) {
        NavigationItem(
            iconRes = R.drawable.home,
            label = "Home",
            isSelected = currentDestination == Destinations.HOME,
            onClick = { navController.navigateIfNotCurrent(Destinations.HOME) }
        )

        NavigationItem(
            iconRes = R.drawable.goals,
            label = "Goal",
            isSelected = currentDestination == Destinations.GOAL,
            onClick = { navController.navigateIfNotCurrent(Destinations.GOAL) }
        )

        NavigationItem(
            iconRes = R.drawable.activity,
            label = "Activity",
            isSelected = currentDestination == Destinations.ACTIVITY,
            onClick = { navController.navigateIfNotCurrent(Destinations.ACTIVITY) }
        )


        NavigationItem(
            iconRes = R.drawable.settings,
            label = "Settings",
            isSelected = currentDestination == Destinations.SETTINGS,
            onClick = { navController.navigateIfNotCurrent(Destinations.SETTINGS) }
        )

    }
}

@Composable
fun RowScope.NavigationItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = Color.White
    val unselectedColor = Color(0xFFD5EDFF)
    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 25.dp,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    NavigationBarItem(
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) selectedColor else unselectedColor,
                    modifier = Modifier.size(iconSize)
                )
                Text(
                    text = label,
                    color = if (isSelected) selectedColor else unselectedColor,
                    style = TextStyle(fontSize = 10.sp)
                )
            }
        },
        selected = isSelected,
        onClick = { onClick() },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            unselectedIconColor = unselectedColor,
            selectedTextColor = selectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = Color.Transparent // No background color for selected icons
        )
    )
}

fun NavController.navigateIfNotCurrent(destination: String) {
    if (currentBackStackEntry?.destination?.route != destination) {
        navigate(destination)
    }
}
