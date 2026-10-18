package com.devqissy.pindrop.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
fun SupportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val emailColor = PinterestRed
    val dismissTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    val dialogTitle = stringResource(R.string.support_dialog_title)
    val dialogMessage = stringResource(R.string.support_dialog_message)
    val emailSubject = stringResource(R.string.support_email_subject)
    val noEmailAppError = stringResource(R.string.support_no_email_app)
    val openEmailError = stringResource(R.string.support_open_email_error)
    val confirmText = stringResource(R.string.support_confirm_button)
    val dismissText = stringResource(R.string.support_dismiss_button)

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
            Column {
                Text(
                    text = dialogMessage,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:dev.qissy@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        }

                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            onDismiss()
                        } else {
                            Toast.makeText(
                                context,
                                noEmailAppError,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            openEmailError,
                            Toast.LENGTH_SHORT
                        ).show()
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