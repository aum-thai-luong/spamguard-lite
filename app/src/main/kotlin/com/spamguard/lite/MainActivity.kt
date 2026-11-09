package com.spamguard.lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.lite.navigation.SpamGuardNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SpamGuardTheme {
                SpamGuardNavigation()
            }
        }
    }
}
