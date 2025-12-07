// PlantPalHiltApp.kt - Creates Hilt Application for PlantPal App

package com.example.plantpal

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltAndroidApp
class PlantPalHiltApp : Application() {
    @Inject @ApplicationContext lateinit var context: Context
}
