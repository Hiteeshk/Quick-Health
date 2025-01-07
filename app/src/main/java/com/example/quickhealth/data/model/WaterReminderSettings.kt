package com.example.quickhealth.data.model

import android.annotation.SuppressLint
import java.time.LocalTime
@SuppressLint("NewApi")
data class WaterReminderSettings(
    val id: String = "",
    val userId: String = "",
     val startTime: LocalTime = LocalTime.of(9, 0), // Default 9 AM
    val endTime: LocalTime = LocalTime.of(19, 0),  // Default 7 PM
    val intervalMinutes: Int = 60,                 // Default 1 hour
    val dailyWaterGoal: Int = 2000,               // Default 2000ml
    val isEnabled: Boolean = true
) 