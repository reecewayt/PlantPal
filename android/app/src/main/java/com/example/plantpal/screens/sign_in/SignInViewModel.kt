package com.example.plantpal.screens.sign_in

import com.example.plantpal.SIGN_IN_SCREEN
import com.example.plantpal.CHAT_SCREEN
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.PlantPalAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val accountService: AccountService
) : PlantPalAppViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun updateEmail(newEmail: String) {
        email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
    }

    fun onSignInClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            accountService.signIn(email.value, password.value)
            openAndPopUp(CHAT_SCREEN, SIGN_IN_SCREEN)
        }
    }
// TODO: SignUp flow needs to be defined once implemented
//    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
//        openAndPopUp(SIGN_UP_SCREEN, SIGN_IN_SCREEN)
//    }
}