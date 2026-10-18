package com.devqissy.pindrop.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.devqissy.pindrop.R
import com.devqissy.pindrop.data.model.MediaItem
import com.devqissy.pindrop.data.repository.queryMediaFiles
import com.devqissy.pindrop.ui.components.*
import com.devqissy.pindrop.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        stringResource(R.string.files_tab_all),
        stringResource(R.string.files_tab_videos),
        stringResource(R.string.files_tab_images)
    )

    var refreshKey by remember { mutableStateOf(0) }
    val isDarkTheme = isSystemInDarkTheme()

    var showHelpDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    fun refresh() {
        refreshKey++
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    LaunchedEffect(selectedTab) {
        refresh()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            onHelpClick = { showHelpDialog = true },
            onSupportClick = { showSupportDialog = true },
            onRateClick = { showRateDialog = true }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PinterestRed,
                        selectedLabelColor = Color.White,
                        containerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                        labelColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
                    ),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.height(40.dp)
                )
            }
        }

        key(refreshKey) {
            when (selectedTab) {
                0 -> MediaList(
                    context = context,
                    isVideo = null,
                    onRefresh = { refresh() }
                )
                1 -> MediaList(
                    context = context,
                    isVideo = true,
                    onRefresh = { refresh() }
                )
                2 -> MediaList(
                    context = context,
                    isVideo = false,
                    onRefresh = { refresh() }
                )
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
        RateDialog(onDismiss = { showRateDialog = false })
    }
}

@Composable
fun MediaList(
    context: android.content.Context,
    isVideo: Boolean?,
    onRefresh: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val emptyStateTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val emptyStateSubTextColor = if (isDarkTheme) DarkTextTertiary else LightTextTertiary

    val emptyTitle = when {
        isVideo == true -> stringResource(R.string.files_empty_videos)
        isVideo == false -> stringResource(R.string.files_empty_images)
        else -> stringResource(R.string.files_empty_all)
    }
    val emptySubtitle = stringResource(R.string.files_empty_subtitle)

    val deleteSuccessText = stringResource(R.string.files_delete_success)
    val deleteErrorText = stringResource(R.string.files_delete_error)
    val shareErrorText = stringResource(R.string.files_share_error)
    val shareChooserTitle = stringResource(R.string.files_share_chooser_title)

    var mediaList by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteItem by remember { mutableStateOf<MediaItem?>(null) }

    fun performDelete(media: MediaItem) {
        try {
            val deleted = context.contentResolver.delete(media.uri, null, null)

            if (deleted > 0) {
                mediaList = mediaList.filter { it.uri != media.uri }
                Toast.makeText(context, deleteSuccessText, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, deleteErrorText, Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            // برای اندروید 10 و بالاتر (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    // روش جایگزین برای API 29
                    val deleteIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = media.uri
                    }
                    context.startActivity(deleteIntent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "$deleteErrorText: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, deleteErrorText, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "$deleteErrorText: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pendingDeleteItem = null
            showDeleteDialog = false
        }
    }

    // روش جایگزین برای حذف فایل در API 30 و بالاتر
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.R)
    fun performDeleteWithPendingIntent(media: MediaItem) {
        try {
            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                listOf(media.uri)
            )
            context.startIntentSender(pendingIntent.intentSender, null, 0, 0, 0, null)
            Toast.makeText(context, deleteSuccessText, Toast.LENGTH_SHORT).show()
            onRefresh()
        } catch (e: Exception) {
            Toast.makeText(context, "$deleteErrorText: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pendingDeleteItem = null
            showDeleteDialog = false
        }
    }

    fun confirmDelete(media: MediaItem) {
        pendingDeleteItem = media
        showDeleteDialog = true
    }

    fun shareFile(media: MediaItem) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (media.isVideo) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, media.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, shareErrorText, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isVideo) {
        isLoading = true
        val items = withContext(Dispatchers.IO) {
            when (isVideo) {
                true -> queryMediaFiles(context, true)
                false -> queryMediaFiles(context, false)
                null -> {
                    val videos = queryMediaFiles(context, true)
                    val images = queryMediaFiles(context, false)
                    (videos + images).sortedByDescending { it.dateAdded }
                }
            }
        }
        mediaList = items
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PinterestRed)
        }
    } else if (mediaList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = emptyTitle,
                    color = emptyStateTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = emptySubtitle,
                    color = emptyStateSubTextColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mediaList, key = { it.uri.toString() }) { media ->
                MediaListItem(
                    media = media,
                    isVideo = media.isVideo,
                    onDelete = { confirmDelete(it) },
                    onShare = { shareFile(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteDialog && pendingDeleteItem != null) {
        ConfirmDeleteDialog(
            fileName = pendingDeleteItem!!.name,
            onConfirm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    performDeleteWithPendingIntent(pendingDeleteItem!!)
                } else {
                    performDelete(pendingDeleteItem!!)
                }
            },
            onDismiss = {
                showDeleteDialog = false
                pendingDeleteItem = null
            }
        )
    }
}