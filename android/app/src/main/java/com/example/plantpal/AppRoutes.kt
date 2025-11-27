package com.example.plantpal

sealed class Screen(val route: String) {
    object SignInRoute: Screen("SignInRoute")
    object ChatRoute: Screen("ChatRoute")
    object SignUpRoute: Screen("SignUpRoute") //TODO: Add SignUp Route
    object SplashRoute: Screen("SplashRoute") //TODO: Add Splash Route
}


const val LOCALHOST = "10.0.2.2"
const val AUTH_PORT = 9099
const val FIREBASE_FUNCTIONS_PORT = 5001