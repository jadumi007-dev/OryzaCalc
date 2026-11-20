package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.RiceNotificationHelper
import com.example.ui.RiceAppShell
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RiceFarmingTheme
import com.example.ui.viewmodel.RiceCalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RiceNotificationHelper.createNotificationChannel(this)

        setContent {
            RiceFarmingTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                val viewModel: RiceCalculatorViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Crossfade(targetState = showSplash, label = "splash_transition") { splash ->
                    if (splash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        RiceAppShell(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
