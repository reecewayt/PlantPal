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
import javax.inject.Inject

import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.chat_interface.ChatInterfaceScreen
import com.example.plantpal.screens.chat_interface.ChatViewModel
import com.example.plantpal.screens.sign_in.SignInScreen
import com.example.plantpal.screens.sign_in.SignInViewModel
import com.example.plantpal.screens.sign_up.SignUpScreen
import com.example.plantpal.screens.sign_up.SignUpViewModel


const val SHARED_GRAPH_ROUTE = "shared_graph"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var accountService: AccountService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //configFirebaseServices()

        setContent {
            PlantPalTheme(dynamicColor = false) {
                // A surface container using the 'background' color from the theme
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
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = SHARED_GRAPH_ROUTE) {
            navigation(
                startDestination = Screen.SignInRoute.route,
                route = SHARED_GRAPH_ROUTE
            ) {
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

    private fun configFirebaseServices() {
        if(BuildConfig.DEBUG){
            Firebase.auth.useEmulator(LOCALHOST, AUTH_PORT)
            Firebase.functions.useEmulator(LOCALHOST, FIREBASE_FUNCTIONS_PORT)
        }
    }

}
