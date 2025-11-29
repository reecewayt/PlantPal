package com.example.plantpal.screens.sign_up

import android.util.Log
import com.example.plantpal.Screen
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.PlantPalAppViewModel
import com.example.plantpal.screens.sign_in.SignInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val accountService: AccountService
) : PlantPalAppViewModel() {

    val tempEmail = MutableStateFlow("")
    val tempPassword = MutableStateFlow("")
    val signUpLoading = MutableStateFlow(false)
    val signUpStatus = MutableStateFlow("")

    fun updateEmail(newEmail: String) {
        tempEmail.value = newEmail
        signUpStatus.value = ""
    }

    fun updatePassword(newPassword: String) {
        tempPassword.value = newPassword
        signUpStatus.value = ""
    }

    fun clearState() {
        tempEmail.value = ""
        tempPassword.value = ""
        signUpStatus.value = ""
        signUpLoading.value = false
    }


    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            signUpLoading.value = true

            val status: String? = accountService.signUp(tempEmail.value, tempPassword.value)

            Log.d("SignUpViewModel", "Status: $status")

            delay(2000)

            signUpLoading.value = false

            if (status == null) {
                signUpStatus.value = "Success"
                delay(3000)
                openAndPopUp(Screen.SignInRoute.route, Screen.SignUpRoute.route)
                signUpStatus.value = ""
                clearState()
            } else {
                signUpStatus.value = status
            }
        }
    }
}