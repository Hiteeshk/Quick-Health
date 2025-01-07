package com.example.quickhealth.ui.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.example.quickhealth.data.model.HealthData
import com.example.quickhealth.data.model.WellnessGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> get() = _isNewUser

    private val _healthData = MutableStateFlow<HealthData?>(null)
    val healthData: StateFlow<HealthData?> get() = _healthData

    private val _wellnessGoals = MutableStateFlow<WellnessGoal?>(null)
    val wellnessGoals: StateFlow<WellnessGoal?> get() = _wellnessGoals

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _userName = MutableStateFlow<String?>("User")
    val userName: StateFlow<String?> get() = _userName


    init {
        checkUserStatus()
    }


    fun checkUserStatus() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getBoolean("isNewUser") == false) {
                    _isNewUser.value = false
                    _userName.value = document.getString("name") ?: "User"
                    fetchHealthData(userId)
                    fetchGoals(userId)
                } else {
                    _isNewUser.value = true
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _isNewUser.value = true
                _isLoading.value = false
            }
    }

    fun fetchGoals(userId: String) {
        db.collection("users").document(userId)
            .collection("wellnessGoals")
            .document("currentGoals")
            .get()
            .addOnSuccessListener { document ->
                _wellnessGoals.value = document.toObject(WellnessGoal::class.java)
            }
            .addOnFailureListener {
                _wellnessGoals.value = null
            }
    }

    @SuppressLint("NewApi")
    fun fetchHealthData(userId: String) {
        val todayId = LocalDate.now().toString()
        
        db.collection("users")
            .document(userId)
            .collection("activities")
            .document(todayId)
            .addSnapshotListener { documentSnapshot, _ ->
                documentSnapshot?.let { document ->
                    _healthData.value = document.toObject(HealthData::class.java)
                    _isLoading.value = false
                } ?: run {
                    _healthData.value = null
                    _isLoading.value = false
                }
            }
    }
}