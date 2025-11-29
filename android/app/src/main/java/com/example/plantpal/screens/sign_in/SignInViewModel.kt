package com.example.plantpal.screens.sign_in

import android.util.Log
import com.example.plantpal.Screen
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.PlantPalAppViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val accountService: AccountService
) : PlantPalAppViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val signInLoading = MutableStateFlow(false)
    val signInStatus = MutableStateFlow("")


    fun updateEmail(newEmail: String) {
        email.value = newEmail
        signInStatus.value = ""
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
        signInStatus.value = ""
    }


    fun onSignInClick(openAndPopUp: (String, String) -> Unit){
        launchCatching {
            // Start Loading Composable
            signInLoading.value = true

            // Attempt Sign In
            val status: String? = accountService.signIn(email.value, password.value)

            Log.d("SignInViewModel", "Status: $status")

            delay(2000)

            // Stop Loading Composable
            signInLoading.value = false

            // Assign Status to signInStatus if successful
            if (status == null) {
                signInStatus.value = "Success"
                delay(3000)
                openAndPopUp(Screen.ChatRoute.route, Screen.SignInRoute.route)
                signInStatus.value = ""
            } else {
                signInStatus.value = status
            }

        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(Screen.SignUpRoute.route, Screen.SignInRoute.route)
    }
}