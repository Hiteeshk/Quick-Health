package com.example.quickhealth.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.quickhealth.data.model.WellnessGoal
import java.time.temporal.ChronoUnit
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.quickhealth.workers.WaterReminderWorker
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import com.example.quickhealth.utils.SettingsManager
import java.util.Date
import android.util.Log

@SuppressLint("NewApi")
class WaterReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext
    private val workManager by lazy { WorkManager.getInstance(context) }

    var startTime by mutableStateOf(LocalTime.of(9, 0))
        private set
    var endTime by mutableStateOf(LocalTime.of(21, 0))
        private set
    var intervalMinutes by mutableStateOf(30)
        private set
    var isSaving by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _dailyWaterGoal = MutableStateFlow(2.0) // Daily goal in liters
    val dailyWaterGoal: StateFlow<Double> = _dailyWaterGoal

    private val _todayWaterIntake = MutableStateFlow(0.0) // Today's intake in liters
    val todayWaterIntake: StateFlow<Double> = _todayWaterIntake

    private val _waterPerReminder = MutableStateFlow(0)
    val waterPerReminder: StateFlow<Int> = _waterPerReminder

    private var _isSchedulingComplete = MutableStateFlow(false)
    val isSchedulingComplete: StateFlow<Boolean> = _isSchedulingComplete

    init {
        loadSettings()
        loadDailyGoal()
        loadTodayWaterIntake()
        intervalMinutes = 30 // Set default interval to 30 minutes
    }

    fun updateStartTime(hour: Int, minute: Int) {
        startTime = LocalTime.of(hour, minute)
        updateCalculations()
    }

    fun updateEndTime(hour: Int, minute: Int) {
        endTime = LocalTime.of(hour, minute)
        updateCalculations()
    }

    fun updateInterval(minutes: Int) {
        intervalMinutes = minutes
        updateCalculations()
    }

    private fun updateCalculations() {
        val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
        val reminders = totalMinutes / intervalMinutes
        if (reminders > 0) {
            _waterPerReminder.value = calculateWaterPerReminder() // Calculate water per reminder
        } else {
            _waterPerReminder.value = 0 // No reminders
        }
    }

    private fun calculateWaterPerReminder(): Int {
        val remainingWater = dailyWaterGoal.value - todayWaterIntake.value
        if (remainingWater > 0) {
            val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
            val reminders = totalMinutes / intervalMinutes
            return (remainingWater * 1000 / reminders).toInt() // Convert to milliliters
        } else {
            return 0 // Target reached
        }
    }

    private fun loadTodayWaterIntake() {
        val userId = auth.currentUser?.uid ?: return

        val today = java.time.LocalDate.now().toString()
        firestore.collection("users")
            .document(userId)
            .collection("activities")
            .document(today)
            .get()
            .addOnSuccessListener { document ->
                _todayWaterIntake.value = (document.getDouble("waterIntake") ?: 0.0) / 1000 // Convert to liters
                updateCalculations() // Update water per reminder based on today's intake
            }
    }

    fun scheduleReminders() {
        try {
            if (!SettingsManager.areNotificationsEnabled(context)) {
                Log.d("WaterReminder", "Notifications are disabled")
                return
            }

            // Cancel existing reminders first and wait for completion
            workManager.cancelAllWorkByTag("water_reminder")
                .result
                .get()

            if (!validateSettings()) {
                Log.d("WaterReminder", "Invalid settings")
                return
            }

            val now = LocalDateTime.now()
            var currentDateTime = now.with(startTime)

            // If current time is past today's start time, schedule for next valid time
            if (now.toLocalTime().isAfter(startTime)) {
                currentDateTime = currentDateTime.plusMinutes(intervalMinutes.toLong())
            }

            var scheduledCount = 0
            val endDateTime = now.with(endTime)

            while (currentDateTime.isBefore(endDateTime)) {
                val delay = ChronoUnit.MILLIS.between(now, currentDateTime)

                if (delay > 0) {
                    val data = workDataOf(
                        "waterAmount" to waterPerReminder.value,
                        "scheduledTime" to currentDateTime.toLocalTime().toString(),
                        "notificationId" to currentDateTime.toLocalTime().toSecondOfDay() // Unique ID for each notification
                    )

                    val workRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("water_reminder")
                        .build()

                    workManager.enqueue(workRequest)
                        .result
                        .get() // Wait for enqueue to complete

                    scheduledCount++
                    Log.d("WaterReminder", "Scheduled reminder #$scheduledCount for ${currentDateTime.toLocalTime()} with delay ${delay}ms")
                }

                currentDateTime = currentDateTime.plusMinutes(intervalMinutes.toLong())
            }

            Log.d("WaterReminder", "Successfully scheduled $scheduledCount reminders")
        } catch (e: Exception) {
            Log.e("WaterReminder", "Error scheduling reminders", e)
            throw e // Rethrow to be handled by saveSettings
        }
    }

    private fun loadSettings() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("waterReminder")
            .get()
            .addOnSuccessListener { document ->
                document.data?.let { data ->
                    startTime = LocalTime.parse(data["startTime"] as String)
                    endTime = LocalTime.parse(data["endTime"] as String)
                    intervalMinutes = (data["intervalMinutes"] as Long).toInt()
                }
            }
    }

    private fun loadDailyGoal() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("wellnessGoals")
            .document("currentGoals")
            .get()
            .addOnSuccessListener { document ->
                document?.toObject(WellnessGoal::class.java)?.let {
                    _dailyWaterGoal.value = it.waterIntakeGoal.toDouble()
                }
            }
    }

    fun validateSettings(): Boolean {
        val now = LocalTime.now()
        val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)

        // Basic validation
        if (totalMinutes <= 0) return false
        if (totalMinutes < intervalMinutes) return false

        // Check if the time window makes sense
        if (startTime.isAfter(endTime)) return false

        // Ensure reasonable number of reminders (max 24 per day)
        val numberOfReminders = totalMinutes / intervalMinutes
        if (numberOfReminders > 24) return false

        return true
    }

    fun saveSettings() {
        val userId = auth.currentUser?.uid ?: return
        isSaving = true
        errorMessage = null
        _isSchedulingComplete.value = false

        val settings = hashMapOf(
            "startTime" to startTime.toString(),
            "endTime" to endTime.toString(),
            "intervalMinutes" to intervalMinutes,
            "lastUpdated" to Date()
        )

        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("waterReminder")
            .set(settings)
            .addOnSuccessListener {
                isSaving = false
                Log.d("WaterReminder", "Settings saved successfully")
                // Schedule reminders after successfully saving settings
                try {
                    scheduleReminders()
                    _isSchedulingComplete.value = true
                } catch (e: Exception) {
                    Log.e("WaterReminder", "Error scheduling reminders", e)
                    errorMessage = "Failed to schedule reminders: ${e.message}"
                    _isSchedulingComplete.value = false
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to save settings: ${e.message}"
                isSaving = false
                _isSchedulingComplete.value = false
                Log.e("WaterReminder", "Error saving settings", e)
            }
    }

    fun getRemainingWaterIntake(): Double {
        val remainingWater = dailyWaterGoal.value - todayWaterIntake.value
        return if (remainingWater < 0) 0.0 else remainingWater // Prevent negative values
    }

    fun getNumberOfRemindersNeeded(): Int {
        val remainingWater = getRemainingWaterIntake()
        if (remainingWater > 0) {
            val reminders = remainingWater * 1000 / waterPerReminder.value // Convert to milliliters
            return if (reminders > 0) reminders.toInt() else 0
        }
        return 0 // Target reached
    }

    fun getGoalPercentage(): Double {
        val remainingWater = getRemainingWaterIntake()
        return if (remainingWater <= 0) 100.0 else (1 - (remainingWater / dailyWaterGoal.value)) * 100
    }
}