/**
 *  @file: AccountService.kt
 *  @brief: Account Service Interface for PlantPal App
 *
 *      @author: Reece Wayt, Truong Le
 *      @date: 12/6/2025
 *
 *      @description: This interface creates the outline functions for AccountService
 *
 *  @note: Source: https://github.com/FirebaseExtended/firebase-video-samples/blob/main/fundamentals/android/auth-email-password/Notes/app/src/main/java/com/notes/app/model/service/AccountService.kt
 *      Adapted to Plant Pal by: Reece Wayt
 */


package com.example.plantpal.model.service

import com.example.plantpal.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String): String?
    suspend fun signUp(email: String, password: String): String?
    fun signOut()
    suspend fun deleteAccount()
}