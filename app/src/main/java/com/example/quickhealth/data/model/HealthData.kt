package com.example.quickhealth.data.model

data class HealthData(
    val steps: Int = 0,
    val waterIntake: Int = 0,
    val sleepHours: Float = 0f
) {
    fun getWaterIntakeInLiters(): Float = waterIntake / 1000f
}