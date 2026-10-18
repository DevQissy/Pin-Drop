package com.devqissy.pindrop.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.devqissy.pindrop.R
import com.devqissy.pindrop.data.model.DownloadStatus
import com.devqissy.pindrop.ui.components.*
import com.devqissy.pindrop.ui.theme.*
import com.devqissy.pindrop.ui.viewmodel.DownloadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadViewModel,
    navController: NavHostController
) {
    val downloads by viewModel.downloads.collectAsState()
    val history by viewModel.history.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    var showHelpDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showConditionalRatingDialog by remember { mutableStateOf(false) }

    val activeTasks = downloads.filter { task ->
        task.status == DownloadStatus.PENDING ||
                task.status == DownloadStatus.DOWNLOADING ||
                (task.status == DownloadStatus.FAILED && task.progress < 1f)
    }

    val emptyStateTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val sectionHeaderColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)

    LaunchedEffect(history) {
        if (viewModel.shouldShowRatingDialog()) {
            showConditionalRatingDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // هدر فقط با سه نقطه - بدون دکمه اضافی
        ScreenHeader(
            onHelpClick = { showHelpDialog = true },
            onSupportClick = { showSupportDialog = true },
            onRateClick = { showRateDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeTasks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.downloads_screen_active_section),
                        color = sectionHeaderColor
                    )
                }

                items(activeTasks, key = { it.id }) { task ->
                    UnifiedActiveDownloadItem(
                        task = task,
                        onCancel = { viewModel.cancelDownload(task.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (history.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.downloads_screen_history_section),
                        color = sectionHeaderColor,
                        extraAction = {
                            TextButton(
                                onClick = { showClearHistoryDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.downloads_screen_clear_button),
                                    modifier = Modifier.size(16.dp),
                                    tint = ErrorRed.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.downloads_screen_clear_button),
                                    fontSize = 12.sp,
                                    color = ErrorRed.copy(alpha = 0.7f)
                                )
                            }
                        }
                    )
                }

                items(history, key = { it.id }) { item ->
                    UnifiedHistoryItem(
                        item = item,
                        onRerun = {
                            viewModel.addDownload(item.url, navController)
                        }
                    )
                }
            }

            if (activeTasks.isEmpty() && history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = emptyStateTextColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.downloads_screen_no_downloads),
                                color = emptyStateTextColor,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(R.string.downloads_screen_add_hint),
                                color = emptyStateTextColor.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }
    if (showRateDialog) {
        RateDialog(
            onDismiss = { showRateDialog = false },
            onRate = {
                viewModel.markUserRated()
                showRateDialog = false
            }
        )
    }
    if (showClearHistoryDialog) {
        ConfirmDialog(
            title = stringResource(R.string.downloads_screen_clear_history_title),
            message = stringResource(R.string.downloads_screen_clear_history_message),
            confirmText = stringResource(R.string.downloads_screen_clear_history_confirm),
            dismissText = stringResource(R.string.downloads_screen_clear_history_cancel),
            onConfirm = {
                viewModel.clearHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }
    if (showConditionalRatingDialog) {
        RateDialog(
            onDismiss = {
                viewModel.markRatingDialogDismissed()
                showConditionalRatingDialog = false
            },
            onRate = {
                viewModel.markUserRated()
                showConditionalRatingDialog = false
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    color: Color,
    extraAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            letterSpacing = 0.5.sp
        )
        extraAction?.invoke()
    }
}