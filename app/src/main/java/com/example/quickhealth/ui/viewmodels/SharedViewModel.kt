package com.example.quickhealth.ui.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Date

class SharedViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Function to update water intake
    @SuppressLint("NewApi")
    fun updateWaterIntake(amount: Float) {
        val amountInMl = (amount * 1000).toInt() // Convert liters to milliliters
        val today = java.time.LocalDate.now().toString()

        viewModelScope.launch {
            userId?.let { uid ->
                firestore.collection("users")
                    .document(uid)
                    .collection("activities")
                    .document(today)
                    .get()
                    .addOnSuccessListener { document ->
                        val currentWaterIntake = document.getLong("waterIntake")?.toInt() ?: 0
                        val newWaterIntake = currentWaterIntake + amountInMl

                        if (document.exists()) {
                            document.reference.update("waterIntake", newWaterIntake)
                        } else {
                            val newActivity = hashMapOf(
                                "waterIntake" to newWaterIntake,
                                "steps" to 0,
                                "date" to Date(),
                                "userId" to uid
                            )
                            document.reference.set(newActivity)
                        }
                    }
            }
        }
    }
} 