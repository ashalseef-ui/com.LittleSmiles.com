package com.LittleSmiles.com

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.LittleSmiles.com.ui.navigation.AppNavigation
import com.LittleSmiles.com.ui.theme.MyApplicationTheme
import com.LittleSmiles.com.core.util.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setOnExitAnimationListener { splashProvider ->
            val fadeOut = ObjectAnimator.ofFloat(splashProvider.view, View.ALPHA, 1f, 0f)
            fadeOut.duration = 400L
            fadeOut.doOnEnd { splashProvider.remove() }
            fadeOut.start()
        }

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        // Production hardening: We let the TTS Singleton live for the process duration
        // to ensure it's always ready for the user across activity recreations.
        super.onDestroy()
    }
}
