package com.LittleSmiles.com

import android.app.Application
import com.LittleSmiles.com.core.domain.repository.DevPreferencesRepository
import com.LittleSmiles.com.core.util.logging.CrashlyticsTree
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LittleBudsApp : Application() {
    
    @Inject
    lateinit var devPreferencesRepository: DevPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        
        // Ensure Firebase is initialized before any DI or repository access
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        
        Timber.i("LittleBudsApp started")
    }
}
