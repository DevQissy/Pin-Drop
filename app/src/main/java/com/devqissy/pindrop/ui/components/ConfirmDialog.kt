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
fun ConfirmDialog(
    title: String = stringResource(R.string.confirm_dialog_default_title),
    message: String = stringResource(R.string.confirm_dialog_default_message),
    confirmText: String = stringResource(R.string.confirm_dialog_confirm_button),
    dismissText: String = stringResource(R.string.confirm_dialog_dismiss_button),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val messageColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val dismissTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = message,
                color = messageColor,
                fontSize = 15.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    color = ErrorRed
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = dismissTextColor
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}