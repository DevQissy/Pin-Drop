package com.devqissy.pindrop.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.data.model.DownloadHistoryItem
import com.devqissy.pindrop.data.model.DownloadStatus
import com.devqissy.pindrop.data.model.DownloadTask
import com.devqissy.pindrop.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UnifiedActiveDownloadItem(
    task: DownloadTask,
    onCancel: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val textPrimary = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textTertiary = if (isDarkTheme) DarkTextTertiary else LightTextTertiary
    val iconTint = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f)

    val cancelDescription = stringResource(R.string.download_item_cancel_description)

    val statusBarColor = when (task.status) {
        DownloadStatus.PENDING -> Color.Gray
        DownloadStatus.DOWNLOADING -> PinterestRed
        DownloadStatus.COMPLETED -> SuccessGreen
        DownloadStatus.FAILED -> ErrorRed
    }

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        borderColor = null,
        backgroundColor = null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(55.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusBarColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.url,
                    color = textPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.currentMessage,
                    color = textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING) {
                    Spacer(modifier = Modifier.height(6.dp))

                    if (task.totalFiles > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "File ${task.completedFiles + 1}/${task.totalFiles}",
                                color = textTertiary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${(task.progress * 100).toInt()}%",
                                color = textTertiary,
                                fontSize = 10.sp
                            )
                        }
                        LinearProgressIndicator(
                            progress = { task.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = PinterestRed,
                            trackColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { task.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = PinterestRed,
                            trackColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                        )
                    }
                }
            }

            if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        cancelDescription,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
        }
    }
}

@Composable
fun UnifiedHistoryItem(
    item: DownloadHistoryItem,
    onCopyLink: () -> Unit = {},
    onRerun: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val linkCopiedText = stringResource(R.string.history_item_link_copied)
    val copyLinkButtonText = stringResource(R.string.history_item_copy_link_button)

    val textPrimary = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textTertiary = if (isDarkTheme) DarkTextTertiary else LightTextTertiary

    val statusBarColor = when (item.status) {
        DownloadStatus.COMPLETED -> SuccessGreen
        DownloadStatus.FAILED -> ErrorRed
        else -> Color.Gray
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) {
        dateFormat.format(Date(item.timestamp))
    }

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        borderColor = null,
        backgroundColor = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(65.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusBarColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.url,
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        color = textTertiary,
                        fontSize = 10.sp
                    )
                    if (item.totalFiles > 1) {
                        Text(
                            text = "• ${item.completedFiles}/${item.totalFiles} files",
                            color = textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    color = textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(item.url))
                        Toast.makeText(context, linkCopiedText, Toast.LENGTH_SHORT).show()
                        onCopyLink()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = copyLinkButtonText,
                        tint = textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                onRerun?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Download again",
                            tint = textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}