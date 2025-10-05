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
import androidx.lifecycle.lifecycleScope
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {

    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functions = Firebase.functions
        // uncomment when testing locally with firebase functions emulator
        functions.useEmulator("10.0.2.2", 5001)
        setContent {
            PlantPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State to hold the response from the Firebase Function
                    var greetingMessage by remember { mutableStateOf("") }
                    // LaunchedEffect will run the coroutine when the composable enters the composition
                    LaunchedEffect(Unit) {
                        greetingMessage = try {
                            plantPalChat(message = "Will to tell me what the moisture is reading on my sensor?")

                        } catch (e:Exception) {
                            "Error: ${e.message}"
                        }
                    }

                    Greeting(message = greetingMessage)
                }
            }
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


