package com.devqissy.pindrop.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.ui.theme.*

@Composable
fun RateDialog(
    onDismiss: () -> Unit,
    onRate: () -> Unit = onDismiss
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val dismissTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    val dialogTitle = stringResource(R.string.rate_dialog_title)
    val dialogMessage = stringResource(R.string.rate_dialog_message)
    val errorText = stringResource(R.string.rate_dialog_error)
    val confirmText = stringResource(R.string.rate_dialog_confirm_button)
    val dismissText = stringResource(R.string.rate_dialog_dismiss_button)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                text = dialogTitle,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = dialogMessage,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 24.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_EDIT).apply {
                            data = Uri.parse("myket://comment?id=${context.packageName}")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://myket.ir/app/${context.packageName}")
                            }
                            context.startActivity(webIntent)
                        }
                        onRate()
                        onDismiss()
                    } catch (e: Exception) {
                        try {
                            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://myket.ir/app/${context.packageName}")
                            }
                            context.startActivity(webIntent)
                            onRate()
                            onDismiss()
                        } catch (ex: Exception) {
                            Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Text(confirmText, color = PinterestRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = dismissTextColor)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}