package com.devqissy.pindrop.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.ui.theme.*

@Composable
fun ConfirmDeleteDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.delete_dialog_title),
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_dialog_message, fileName),
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    stringResource(R.string.delete_dialog_confirm_button),
                    color = ErrorRed
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    stringResource(R.string.delete_dialog_cancel_button),
                    color = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
                )
            }
        }
    )
}