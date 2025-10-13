// Source: https://github.com/FirebaseExtended/firebase-video-samples/blob/main/fundamentals/android/auth-email-password/Notes/app/src/main/java/com/notes/app/model/service/AccountService.kt

package com.example.plantpal.model.service

import com.example.plantpal.model.User
import kotlinx.coroutines.flow.Flow


interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}