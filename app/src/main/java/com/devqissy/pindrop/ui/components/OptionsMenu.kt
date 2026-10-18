package com.devqissy.pindrop.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devqissy.pindrop.R

@Composable
fun OptionsMenu(
    onHelpClick: () -> Unit,
    onSupportClick: () -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    val menuContainerColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFFFFFFF)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF111111)

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.options_menu_content_description),
                tint = if (isDarkTheme) Color.White else Color(0xFF555555),
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = menuContainerColor
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.options_menu_help),
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    expanded = false
                    onHelpClick()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.options_menu_support),
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    expanded = false
                    onSupportClick()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.options_menu_rate),
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    expanded = false
                    onRateClick()
                }
            )
        }
    }
}