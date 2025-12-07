/**
 *  @file: MainActivity.kt
 *  @brief: Entry Point for PlantPal App
 *
 *      @author: Reece Wayt, Truong Le, Gemini
 *      @date: 11/29/2025
 *
 *      @description: This file setup the Android entry point, and creates the following items:
 *          - Navigation Controller and Host
 *          - Composable for Sign In Screen
 *          - Composable for Sign Up Screen
 *          - Composable for Chat Interface Screen
 *          When the app is booted up, a navigation controller and host are created to establish a navigation
 *      system for the app. Using openAndPopUp, this lambda is passed into each screen composable so that each screen
 *      can use the lambda to navigate to a specified screen using the target object within a sealed class located in
 *      AppRoutes.kt. In addition, each screen has a separate VM that is passed to each composable.
 * 
 *  @note: This code has been developed using the assistance of Google Gemini and its code generation tools
 */

package com.example.plantpal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.functions
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.remember

import com.example.plantpal.screens.chat_interface.ChatInterfaceScreen
import com.example.plantpal.screens.chat_interface.ChatViewModel
import com.example.plantpal.screens.sign_in.SignInScreen
import com.example.plantpal.screens.sign_in.SignInViewModel
import com.example.plantpal.screens.sign_up.SignUpScreen
import com.example.plantpal.screens.sign_up.SignUpViewModel

const val SHARED_GRAPH_ROUTE = "shared_graph"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //configFirebaseServices()

        setContent {
            PlantPalTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlantPalNavHost()
                }
            }
        }
    }

    @Composable
    private fun PlantPalNavHost() {

        // Create NavController and setup NavHost with all 3 screens
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = SHARED_GRAPH_ROUTE) {
            navigation(
                startDestination = Screen.SignInRoute.route,
                route = SHARED_GRAPH_ROUTE
            ) {
                // Each composable gets set up in the NavHost. openAndPopUp and the screen
                // composable's viewmodel are passed as args into the screen composables.

                composable(Screen.SignInRoute.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(SHARED_GRAPH_ROUTE)
                    }
                    val signInVM: SignInViewModel = hiltViewModel(parentEntry)
                    SignInScreen(
                        openAndPopUp = { route, popUp ->
                            navController.navigate(route) {
                                popUpTo(popUp) { inclusive = true }
                            }
                        },
                        viewModel = signInVM
                    )
                }
                composable(Screen.ChatRoute.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(SHARED_GRAPH_ROUTE)
                    }
                    val chatVM: ChatViewModel = hiltViewModel(parentEntry)
                    ChatInterfaceScreen(
                        openAndPopUp = { route, popUp ->
                            navController.navigate(route) {
                                popUpTo(popUp) { inclusive = true }
                            }
                        },
                        viewModel = chatVM
                    )
                }
                composable(Screen.SignUpRoute.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(SHARED_GRAPH_ROUTE)
                    }
                    val signUpVM: SignUpViewModel = hiltViewModel(parentEntry)
                    SignUpScreen(
                        openAndPopUp = { route, popUp ->
                            navController.navigate(route) {
                                popUpTo(popUp) { inclusive = true }
                            }
                        },
                        viewModel = signUpVM
                    )
                }
            }
        }
    }

    // For debugging locally with Firebase, this function is used to enable the emulator
    private fun configFirebaseServices() {
        if(BuildConfig.DEBUG){
            Firebase.auth.useEmulator(LOCALHOST, AUTH_PORT)
            Firebase.functions.useEmulator(LOCALHOST, FIREBASE_FUNCTIONS_PORT)
        }
    }

}
