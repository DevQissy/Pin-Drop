package com.devqissy.pindrop.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.devqissy.pindrop.R
import com.devqissy.pindrop.services.DownloadService
import com.devqissy.pindrop.ui.theme.DarkBackground
import com.devqissy.pindrop.ui.theme.LightBackground
import com.devqissy.pindrop.utils.BatteryOptimizationHelper
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onPermissionsNeeded: () -> Unit,
    onReadyToStart: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val splashImageRes = if (isDarkTheme) {
        R.drawable.background_splash_dark
    } else {
        R.drawable.background_splash_light
    }

    LaunchedEffect(Unit) {
        delay(1500)

        val hasBatteryPermission = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val allPermissionsGranted = hasBatteryPermission && hasNotificationPermission

        if (allPermissionsGranted) {
            DownloadService.start(context)
            onReadyToStart()
        } else {
            onPermissionsNeeded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkBackground else LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = splashImageRes),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}