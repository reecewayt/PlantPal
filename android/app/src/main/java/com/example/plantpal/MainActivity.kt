package com.example.plantpal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plantpal.screens.sign_in.SignInScreen
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var functions: FirebaseFunctions
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functions = Firebase.functions
        auth = Firebase.auth
        configFirebaseServices()

        setContent {
            PlantPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
                    if (isSignedIn) {
                        // State to hold the response from the Firebase Function
                        var greetingMessage by remember { mutableStateOf("") }
                        // LaunchedEffect will run the coroutine when the composable enters the composition
                        LaunchedEffect(Unit) {
                            greetingMessage = try {
                                plantPalChat(message = "Will you tell me what the moisture is reading on my sensor?")

                            } catch (e: Exception) {
                                "Error: ${e.message}"
                            }
                        }

                        Greeting(message = greetingMessage)
                    } else {
                        SignInScreen(openAndPopUp = { _, _ -> isSignedIn = true })
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            Log.d("MainActivity", "User is signed in")
            Log.d("MainActivity", "User ID: ${auth.currentUser?.uid}")
            Log.d("MainActivity", "User email: ${auth.currentUser?.email}")
            Log.d("MainActivity", "Signing user out")
            auth.signOut()
        }
    }

    private fun configFirebaseServices() {
        if(BuildConfig.DEBUG){
            Firebase.auth.useEmulator(LOCALHOST, AUTH_PORT)
            Firebase.functions.useEmulator(LOCALHOST, FIREBASE_FUNCTIONS_PORT)
        }
    }


    // Change this to a suspend function
    @Suppress("UNCHECKED_CAST")
    private suspend fun backendHelloWorld(text: String): String {
        val data = hashMapOf(
            "text" to text
        )

        // Call the function and await the result
        val result = functions
            .getHttpsCallable("hello_world_firebase")
            .call(data)
            .await() // This suspends the coroutine until the task is complete

        val resultMap = result.data as? Map<String, Any>
        return resultMap?.get("message") as? String ?: "Error"
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun plantPalChat(message: String, chatThreadId: String? = "abc_123"): String {
        val data = hashMapOf(
            "message" to message,
            "thread_id" to chatThreadId
        )

        try {
            // Call the function and await the result
            val result = functions
                .getHttpsCallable("plantpal_chat")
                .call(data)
                .await() // This suspends the coroutine until the task is complete

            val resultMap = result.data as? Map<String, Any>
            val success = resultMap?.get("success") as? Boolean ?: false

            return if (success) {
                resultMap?.get("response") as? String ?: "No response received"
            } else {
                "Error: Failed to get response from PlantPal"
            }

        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}

@Composable
fun Greeting(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello Android!",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlantPalTheme {
        Greeting("Hello from Firebase Functions (Python)!")
    }
}


