package com.devqissy.pindrop.ui.screens

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devqissy.pindrop.R
import com.devqissy.pindrop.ui.components.AddLinkDialog
import com.devqissy.pindrop.ui.components.AnimatedBackground
import com.devqissy.pindrop.ui.components.FirstLaunchDialog
import com.devqissy.pindrop.ui.theme.PinterestRed
import com.devqissy.pindrop.ui.viewmodel.DownloadViewModel

class DownloadViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DownloadViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val isDarkTheme = isSystemInDarkTheme()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filesLabel = stringResource(R.string.main_bottom_bar_files)
    val addLinkDesc = stringResource(R.string.main_fab_add_link)
    val downloadsLabel = stringResource(R.string.main_bottom_bar_downloads)

    val application = LocalContext.current.applicationContext as Application
    val viewModel: DownloadViewModel = viewModel(
        factory = DownloadViewModelFactory(application)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showFirstLaunchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasSeenWarning = prefs.getBoolean("has_seen_pinterest_warning", false)
        if (!hasSeenWarning) {
            showFirstLaunchDialog = true
        }
    }

    val bottomBarContainerColor = if (isDarkTheme) Color(0xDD1A1A1A) else Color(0xEEFFFFFF)
    val bottomBarContentColor = if (isDarkTheme) Color.White else Color(0xFF111111)
    val activeColor = PinterestRed
    val inactiveColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = if (isDarkTheme) Color.White else Color.Black,
            bottomBar = {
                BottomAppBar(
                    containerColor = bottomBarContainerColor,
                    tonalElevation = 0.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate("files") {
                                popUpTo("files") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = filesLabel,
                                tint = if (currentRoute == "files") activeColor else inactiveColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .size(48.dp)
                            .background(activeColor, RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = addLinkDesc,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            navController.navigate("downloads") {
                                popUpTo("downloads") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = downloadsLabel,
                                tint = if (currentRoute == "downloads") activeColor else inactiveColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "files",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("files") {
                    FilesScreen()
                }
                composable("downloads") {
                    DownloadsScreen(viewModel = viewModel, navController = navController)
                }
            }
        }

        if (showDialog) {
            AddLinkDialog(
                onDismiss = { showDialog = false },
                onDownload = { url ->
                    showDialog = false
                    viewModel.addDownload(url, navController)
                    navController.navigate("downloads") {
                        popUpTo("downloads") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        if (showFirstLaunchDialog) {
            FirstLaunchDialog(
                onConfirm = {
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("has_seen_pinterest_warning", true).apply()
                    showFirstLaunchDialog = false
                }
            )
        }
    }
}