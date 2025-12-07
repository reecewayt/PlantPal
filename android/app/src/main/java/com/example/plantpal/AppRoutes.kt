/**
 *  @file: AppRoutes.kt
 *  @brief: General constants for App Usage
 *
 *      @author: Reece Wayt, Truong Le
 *      @date: 12/6/2025
 *
 *      @description: This file contains a sealed class that holds object paths to screens. Also, the file
 *      has some constants for Firebase.
 */


package com.example.plantpal

sealed class Screen(val route: String) {
    object SignInRoute: Screen("SignInRoute")
    object ChatRoute: Screen("ChatRoute")
    object SignUpRoute: Screen("SignUpRoute")
    object SplashRoute: Screen("SplashRoute") //TODO: Add Splash Route
}

const val LOCALHOST = "10.0.2.2"
const val AUTH_PORT = 9099
const val FIREBASE_FUNCTIONS_PORT = 5001