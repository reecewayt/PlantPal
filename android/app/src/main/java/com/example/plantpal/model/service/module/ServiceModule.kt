// Source: https://github.com/FirebaseExtended/firebase-video-samples/blob/main/fundamentals/android/auth-email-password/Notes/app/src/main/java/com/notes/app/model/service/module/ServiceModule.kt

package com.example.plantpal.model.service.module

import com.example.plantpal.model.service.AccountService
import com.example.plantpal.model.service.impl.AccountServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt Dependency Injection Module for Application Services.
 *
 * @Module Annotates the class as a Hilt module, which is a container for dependency-providing instructions.
 *
 * @InstallIn(SingletonComponent::class) This instruction installs the module in the SingletonComponent.
 * This means that any dependency provided by this module (e.g., AccountService) will be scoped as a
 * singleton, ensuring a single, shared instance is used throughout the entire application's lifecycle.
 *
 * This class uses @Binds to efficiently tell Hilt which concrete implementation to use
 * whenever an interface is requested as a dependency.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds abstract fun provideAccountService(impl: AccountServiceImpl): AccountService
}