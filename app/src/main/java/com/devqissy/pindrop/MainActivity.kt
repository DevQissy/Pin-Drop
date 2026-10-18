package com.devqissy.pindrop

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.devqissy.pindrop.services.DownloadService
import com.devqissy.pindrop.ui.screens.MainScreen
import com.devqissy.pindrop.ui.screens.PermissionRequestScreen
import com.devqissy.pindrop.ui.screens.SplashScreen
import com.devqissy.pindrop.ui.theme.PinDropTheme

class MainActivity : ComponentActivity() {

    private companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MYKEY_UPDATE_CHECK_DELAY_MS = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            checkStoragePermission()
        }

        setContent {
            PinDropTheme {
                AppNavigation()
            }
        }

        checkForMyketUpdateWithDelay()
    }

    @Composable
    private fun AppNavigation() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

        when (currentScreen) {
            Screen.Splash -> {
                SplashScreen(
                    onPermissionsNeeded = { currentScreen = Screen.Permission },
                    onReadyToStart = { currentScreen = Screen.Main }
                )
            }
            Screen.Permission -> {
                PermissionRequestScreen(
                    onAllPermissionsGranted = { currentScreen = Screen.Main }
                )
            }
            Screen.Main -> {
                MainScreen()
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun checkForMyketUpdateWithDelay() {
        if (!shouldCheckUpdateToday()) {
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val packageName = this.packageName
                val url = "myket://check-update?id=$packageName"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                intent.setPackage("ir.mservices.market")

                startActivity(intent)
                saveUpdateCheckToday()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MYKEY_UPDATE_CHECK_DELAY_MS)
    }

    private fun shouldCheckUpdateToday(): Boolean {
        val prefs = getSharedPreferences("update_prefs", MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0)
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastCheckDay = lastCheck / (1000 * 60 * 60 * 24)
        return today != lastCheckDay
    }

    private fun saveUpdateCheckToday() {
        getSharedPreferences("update_prefs", MODE_PRIVATE).edit()
            .putLong("last_update_check", System.currentTimeMillis())
            .apply()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)

            if (isIgnoring && DownloadService.hasActiveDownloads()) {
                DownloadService.start(this)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!DownloadService.hasActiveDownloads()) {
            DownloadService.stop(this)
        }
    }
}

enum class Screen {
    Splash, Permission, Main
}