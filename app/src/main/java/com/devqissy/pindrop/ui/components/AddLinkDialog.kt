package com.devqissy.pindrop.ui.components

import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R
import com.devqissy.pindrop.ui.theme.PinterestRed
import com.devqissy.pindrop.ui.theme.LightTextPrimary
import com.devqissy.pindrop.ui.theme.DarkTextPrimary
import com.devqissy.pindrop.ui.theme.LightTextSecondary
import com.devqissy.pindrop.ui.theme.DarkTextSecondary
import com.devqissy.pindrop.ui.theme.DarkTextTertiary
import com.devqissy.pindrop.ui.theme.LightTextTertiary
import com.devqissy.pindrop.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkDialog(
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    val isDarkTheme = isSystemInDarkTheme()

    val titleText = stringResource(R.string.add_link_title)
    val warningText = stringResource(R.string.add_link_warning)
    val placeholderText = stringResource(R.string.add_link_placeholder)
    val hintText = stringResource(R.string.add_link_hint)
    val emptyErrorText = stringResource(R.string.add_link_empty_error)
    val downloadText = stringResource(R.string.add_link_download_button)
    val cancelText = stringResource(R.string.add_link_cancel_button)
    val pasteDesc = stringResource(R.string.add_link_paste_description)
    val clearDesc = stringResource(R.string.add_link_clear_description)

    val containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val labelColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val iconTint = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val dismissTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val warningTextColor = if (isDarkTheme) WarningOrange else WarningOrange.copy(alpha = 0.8f)
    val hintTextColor = if (isDarkTheme) DarkTextTertiary else LightTextTertiary

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
        unfocusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
        focusedIndicatorColor = PinterestRed,
        unfocusedIndicatorColor = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
        cursorColor = PinterestRed,
        focusedLabelColor = PinterestRed,
        unfocusedLabelColor = labelColor
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                titleText,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
        },
        text = {
            Column {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = warningTextColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = warningText,
                        color = warningTextColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(placeholderText, color = labelColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    trailingIcon = {
                        if (url.isEmpty()) {
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let { url = it }
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.link_2_24px),
                                    pasteDesc,
                                    tint = iconTint
                                )
                            }
                        } else {
                            IconButton(onClick = { url = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    clearDesc,
                                    tint = iconTint
                                )
                            }
                        }
                    }
                )

                Text(
                    text = hintText,
                    color = hintTextColor,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) onDownload(url)
                    else Toast.makeText(context, emptyErrorText, Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(downloadText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText, color = dismissTextColor)
            }
        }
    )
}