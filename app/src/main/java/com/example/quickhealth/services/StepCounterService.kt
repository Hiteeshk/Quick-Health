package com.example.quickhealth.services

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class StepCounterService(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    private val _isAvailable = MutableStateFlow(stepSensor != null)
    val isAvailable: StateFlow<Boolean> = _isAvailable

    private val _steps = MutableStateFlow(0f)
    val steps: StateFlow<Float> = _steps

    private var initialSteps: Float = -1f
    private var lastSavedSteps: Float = 0f
    @SuppressLint("NewApi")
    private var lastSavedDate: String = LocalDate.now().toString()

    init {
        Log.d("StepCounter", "Step sensor available: ${stepSensor != null}")
        loadLastSavedSteps()
    }

    private fun loadLastSavedSteps() {
        // This will be called from ViewModel to set the last saved steps
        Log.d("StepCounter", "Loading last saved steps: $lastSavedSteps")
    }

    fun setLastSavedSteps(steps: Float) {
        lastSavedSteps = steps
        Log.d("StepCounter", "Set last saved steps to: $lastSavedSteps")
    }

    fun startCounting() {
        if (stepSensor == null) return
        
        val registered = sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
        Log.d("StepCounter", "Sensor registration result: $registered")
    }

    fun stopCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentSteps = it.values[0]
            Log.d("StepCounter", "Raw sensor value: $currentSteps")

            if (initialSteps < 0) {
                initialSteps = currentSteps
                Log.d("StepCounter", "Initialized with steps: $initialSteps")
                return
            }

            val todaySteps = currentSteps - initialSteps + lastSavedSteps
            Log.d("StepCounter", "Calculating steps: Current($currentSteps) - Initial($initialSteps) + LastSaved($lastSavedSteps) = Today($todaySteps)")
            
            if (todaySteps >= 0) {
                _steps.value = todaySteps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("StepCounter", "Accuracy changed: $accuracy")
    }
} 