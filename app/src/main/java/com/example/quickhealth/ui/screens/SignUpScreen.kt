package com.example.quickhealth.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quickhealth.ui.navigation.Destinations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.quickhealth.R
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    initiateSignUp(
                        username, email, password, confirmPassword,
                        context, auth, isLoading = { isLoading = it },
                        onSignUpSuccess, navController
                    )
                }
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up button
        Button(
            onClick = {
                initiateSignUp(
                    username, email, password, confirmPassword,
                    context, auth, isLoading = { isLoading = it },
                    onSignUpSuccess, navController
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0073e6))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(text = "Sign Up", fontSize = 20.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Login redirect
        TextButton(
            onClick = {
                navController.navigate(Destinations.LOGIN) {
                    popUpTo(Destinations.SIGN_UP) { inclusive = true }
                }
            }
        ) {
            Text(
                text = "Already have an account? Login",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

fun initiateSignUp(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    context: android.content.Context,
    auth: FirebaseAuth,
    isLoading: (Boolean) -> Unit,
    onSignUpSuccess: () -> Unit,
    navController: NavHostController
) {
    // Validate input
    if (password != confirmPassword) {
        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
        return
    }

    if (username.isBlank() || email.isBlank() || password.isBlank()) {
        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        return
    }

    // Email format validation
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
        return
    }

    // Password strength check (minimum 6 characters as an example)
    if (password.length < 6) {
        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        return
    }

    isLoading(true)

    // Sign up with Firebase Authentication
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            isLoading(false)
            if (task.isSuccessful) {
                // Update display name after successful sign up
                auth.currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                )?.addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Optionally, store additional user data in Firestore
                        FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid)
                            .set(mapOf("username" to username, "email" to email))
                            .addOnSuccessListener {
                                Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                                onSignUpSuccess()

                                // Navigate to the home screen after successful sign up
                                navController.navigate(Destinations.HOME) {
                                    popUpTo(Destinations.SIGN_UP) { inclusive = true }
                                }
                            }
                    }
                }
            } else {
                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

