package com.devqissy.pindrop.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.data.model.MediaItem
import com.devqissy.pindrop.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MediaListItem(
    media: MediaItem,
    isVideo: Boolean,
    onDelete: (MediaItem) -> Unit,
    onShare: (MediaItem) -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val openFileErrorText = stringResource(R.string.media_open_file_error)

    val textPrimary = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textTertiary = if (isDarkTheme) DarkTextTertiary else LightTextTertiary
    val deleteColor = Color(0xFFE53935)
    val iconTint = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f)

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(media.dateAdded) {
        dateFormat.format(Date(media.dateAdded * 1000))
    }

    val fileSize = remember(media.uri) {
        getFileSize(context, media.uri)
    }

    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(media.uri, if (isVideo) "video/*" else "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, openFileErrorText, Toast.LENGTH_SHORT).show()
                }
            },
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                MediaThumbnail(media = media, isVideo = isVideo)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = media.name,
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        color = textTertiary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )

                    if (fileSize != null) {
                        Text(
                            text = "•",
                            color = textTertiary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = formatFileSize(fileSize),
                            color = textTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onShare(media) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.media_share_button_desc),
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onDelete(media) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.media_delete_button_desc),
                        tint = deleteColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getFileSize(context: Context, uri: Uri): Long? {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
    }
}