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

    override suspend fun signUp(email: String, password: String) {
        // Disable account creation for now
        //Firebase.auth.createUserWithEmailAndPassword(email, password).await()
        throw NotImplementedError("Account creation is not yet implemented")
    }

    override fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun deleteAccount() {
        // Disable account deletion for now
        //Firebase.auth.currentUser!!.delete().await()
        throw NotImplementedError("Account deletion is not yet implemented")

    }
}