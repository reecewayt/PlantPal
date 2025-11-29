// Source: Example code from Firebase Youtube Channel: "Getting started with Firebase Authentication on Android"
// GitHub: https://github.com/FirebaseExtended/firebase-video-samples/tree/main/fundamentals/android/auth-email-password/Notes
// Adapted to plant pal by: Reece Wayt

package com.example.plantpal.model.service.impl

import android.util.Log
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AccountServiceImpl @Inject constructor() : AccountService {

    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) })
                }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    override val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    override fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    override suspend fun signIn(email: String, password: String): String? {
        if (email.isEmpty() || password.isEmpty()) {
            Log.e("AccountServiceImpl","Email or Password is Empty")
            return "Email or Password is Empty"
        }

        return try {
            // Use await() to make this call blocking within the coroutine.
            Firebase.auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
            Log.d("AccountServiceImpl", "User Credentials Verified Successfully.")
            // If await() succeeds, it means sign-in was successful. Return null for success.
            null
        } catch (ex: FirebaseAuthException) {
            // If await() fails, it throws an exception. We catch it here.
            val errorCode = ex.errorCode
            Log.e("AccountServiceImpl", "Firebase Auth Error. Code: $errorCode")
            when (errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address."
                "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials."
                else -> "An unexpected error occurred."
            }
        } catch (e: Exception) {
            // Catch any other non-Firebase exceptions.
            Log.e("AccountServiceImpl", "A non-Firebase exception occurred: ${e.message}")
            "An unexpected error occurred."
        }
    }

    override suspend fun signUp(email: String, password: String): String? {
        if (email.isEmpty() || password.isEmpty()) {
            Log.e("AccountServiceImpl", "Email or Password cannot be empty during sign-up.")
            return "Email or Password cannot be empty."
        }

        return try {
            Firebase.auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
            Log.d("AccountServiceImpl", "User account created successfully.")
            // Success, so return null
            null
        } catch (ex: FirebaseAuthException) {
            val errorCode = ex.errorCode
            Log.e("AccountServiceImpl", "Firebase Auth Error during sign-up. Code: $errorCode")
            // Handle specific sign-up errors
            when (errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email address already exists."
                "ERROR_INVALID_EMAIL" -> "The email address is not valid."
                "ERROR_WEAK_PASSWORD" -> "The password is too weak. It must be at least 6 characters."
                else -> "An unexpected error occurred during sign-up."
            }
        } catch (e: Exception) {
            Log.e("AccountServiceImpl", "A non-Firebase exception occurred during sign-up: ${e.message}")
            "An unexpected error occurred."
        }
    }

    override fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun deleteAccount() {
        try {
            Firebase.auth.currentUser?.delete()?.await()
            Log.d("AccountServiceImpl", "User account deleted successfully.")
        } catch (e: Exception) {
            // Log the error but don't crash the app. The calling function will proceed.
            Log.e("AccountServiceImpl", "Error deleting account, proceeding anyway: ${e.message}")
        }
    }
}