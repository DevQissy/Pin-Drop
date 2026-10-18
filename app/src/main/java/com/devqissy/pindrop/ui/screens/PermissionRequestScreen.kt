package com.devqissy.pindrop.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.devqissy.pindrop.R
import com.devqissy.pindrop.services.DownloadService
import com.devqissy.pindrop.ui.components.AnimatedBackground
import com.devqissy.pindrop.ui.components.GlassBox
import com.devqissy.pindrop.ui.theme.PinterestRed
import com.devqissy.pindrop.utils.BatteryOptimizationHelper

@Composable
fun PermissionRequestScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    var batteryOptimizationGranted by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val allGranted = batteryOptimizationGranted && notificationGranted

    // Launcher برای مجوز نوتیفیکیشن
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationGranted = isGranted
    }

    // بررسی خودکار هر بار که اپ به foreground میاد
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                batteryOptimizationGranted = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // وقتی همه مجوزها گرفته شد، به صفحه اصلی برو
    LaunchedEffect(allGranted) {
        if (allGranted) {
            DownloadService.start(context)
            kotlinx.coroutines.delay(500)
            onAllPermissionsGranted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(isDarkTheme = isDarkTheme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.permission_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.permission_description),
                fontSize = 14.sp,
                color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // کارت مجوز باتری با GlassBox
            GlassBox(
                modifier = Modifier.fillMaxWidth()
            ) {
                PermissionCardContent(
                    title = stringResource(R.string.battery_permission_title),
                    description = stringResource(R.string.battery_permission_message),
                    isGranted = batteryOptimizationGranted,
                    onRequest = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // کارت مجوز نوتیفیکیشن با GlassBox (فقط برای اندروید 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                GlassBox(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PermissionCardContent(
                        title = stringResource(R.string.notification_permission_title),
                        description = stringResource(R.string.notification_permission_message),
                        isGranted = notificationGranted,
                        onRequest = {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCardContent(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Header with title and status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
            )

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PinterestRed.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.permission_status_granted),
                            fontSize = 12.sp,
                            color = PinterestRed,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PinterestRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // دکمه فعال‌سازی در پایین
        if (!isGranted) {
            Button(
                onClick = onRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PinterestRed,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(stringResource(R.string.permission_button_enable), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PinterestRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PinterestRed.copy(alpha = 0.5f))
                ),
                enabled = false
            ) {
                Text(stringResource(R.string.permission_button_already_enabled), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}