package com.devqissy.pindrop.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.ui.theme.*

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val dividerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val hintColor = if (isDarkTheme) DarkTextTertiary else LightTextTertiary

    val dialogTitle = stringResource(R.string.help_dialog_title)
    val aboutTitle = stringResource(R.string.help_section_about_title)
    val aboutContent = stringResource(R.string.help_section_about_content)
    val stepsTitle = stringResource(R.string.help_section_steps_title)
    val stepsContent = stringResource(R.string.help_section_steps_content)
    val storageTitle = stringResource(R.string.help_section_storage_title)
    val storageContent = stringResource(R.string.help_section_storage_content)
    val albumsTitle = stringResource(R.string.help_section_albums_title)
    val albumsContent = stringResource(R.string.help_section_albums_content)
    val historyTitle = stringResource(R.string.help_section_history_title)
    val historyContent = stringResource(R.string.help_section_history_content)
    val qualityTitle = stringResource(R.string.help_section_quality_title)
    val qualityContent = stringResource(R.string.help_section_quality_content)
    val noticeText = stringResource(R.string.help_dialog_notice)
    val confirmText = stringResource(R.string.help_dialog_confirm_button)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                dialogTitle,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HelpSection(
                    icon = Icons.Default.Info,
                    title = aboutTitle,
                    content = aboutContent,
                    isDarkTheme = isDarkTheme
                )

                HelpSection(
                    icon = Icons.Default.Download,
                    title = stepsTitle,
                    content = stepsContent,
                    isDarkTheme = isDarkTheme
                )

                HelpSection(
                    icon = Icons.Default.FolderOpen,
                    title = storageTitle,
                    content = storageContent,
                    isDarkTheme = isDarkTheme
                )

                HelpSection(
                    icon = Icons.Default.Collections,
                    title = albumsTitle,
                    content = albumsContent,
                    isDarkTheme = isDarkTheme
                )

                HelpSection(
                    icon = Icons.Default.History,
                    title = historyTitle,
                    content = historyContent,
                    isDarkTheme = isDarkTheme
                )

                HelpSection(
                    icon = Icons.Default.HighQuality,
                    title = qualityTitle,
                    content = qualityContent,
                    isDarkTheme = isDarkTheme
                )

                Divider(color = dividerColor, thickness = 1.dp)

                Text(
                    text = noticeText,
                    color = hintColor,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(confirmText, color = PinterestRed)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun HelpSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    isDarkTheme: Boolean
) {
    val titleColor = PinterestRed
    val textColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
        }
    }
}