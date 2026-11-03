package com.spamguard.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.spamguard.core.ui.R

/**
 * Material 3 cho mọi màn hình Compose, khớp với `Theme.SpamGuard` (XML):
 * Dynamic Color trên Android 12+, màu thương hiệu `brand_*` trên máy cũ hơn.
 */
@Composable
fun SpamGuardTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // values-night/colors.xml cho sẵn màu tối, nên chỉ cần chọn bảng màu nền.
        val primary = colorResource(R.color.brand_primary)
        val onPrimary = colorResource(R.color.brand_on_primary)
        if (darkTheme) {
            darkColorScheme(primary = primary, onPrimary = onPrimary)
        } else {
            lightColorScheme(primary = primary, onPrimary = onPrimary)
        }
    }
    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}
