/**
 *  @file: SignInViewModel.kt
 *  @brief: Sign In View Model for PlantPal App
 *
 *      @author: Reece Wayt, Truong Le, Gemini
 *      @date: 11/29/2025
 *
 *      @description: This viewmodel creates the states and functions for the sign-in screen. It includes
 *      update functions, sign in functions and navigation functions.
 *
 *  @note: This code has been developed using the assistance of Google Gemini and its code generation tools
 */

package com.example.plantpal.screens.sign_in

import android.util.Log
import com.example.plantpal.Screen
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.PlantPalAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val accountService: AccountService
) : PlantPalAppViewModel() {

    // State variables for SignIn VM
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val signInLoading = MutableStateFlow(false)
    val signInStatus = MutableStateFlow("")

    // --- State Var Update Functions ---
    fun updateEmail(newEmail: String) {
        email.value = newEmail
        signInStatus.value = ""
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
        signInStatus.value = ""
    }

    // --- UI Interface Functions ---
    fun onSignInClick(openAndPopUp: (String, String) -> Unit){
        // When invoked, the app will attempt to connect to the Firebase backend with the provided credentials.
        // Status will determine if the credentials are valid or not. If not, the status will be
        // returned, but if the credentials are valid, the app will transition to the chat screen.

        launchCatching {
            // Start Loading Bar Composable
            signInLoading.value = true

            // Attempt Sign In
            val status: String? = accountService.signIn(email.value, password.value)
            Log.d("SignInViewModel", "Status (null = success): $status")
            delay(1500)

            // Stop Loading Composable
            signInLoading.value = false

            // Assign Status to signInStatus if successful
            if (status == null) {
                signInStatus.value = "Success"
                delay(3000)
                openAndPopUp(Screen.ChatRoute.route, Screen.SignInRoute.route)
                signInStatus.value = ""
            // Else return the error message to the user
            } else {
                signInStatus.value = status
            }
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        // Open Sign Up Screen when invoked
        openAndPopUp(Screen.SignUpRoute.route, Screen.SignInRoute.route)
    }
}