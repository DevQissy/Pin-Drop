package com.devqissy.pindrop.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import com.devqissy.pindrop.ui.theme.DarkTextPrimary
import com.devqissy.pindrop.ui.theme.LightTextPrimary

@Composable
fun ScreenHeader(
    title: String = stringResource(R.string.app_name),
    onHelpClick: () -> Unit,
    onSupportClick: () -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraActions: @Composable RowScope.() -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val headerTextColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = headerTextColor
        )

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            extraActions()
            OptionsMenu(
                onHelpClick = onHelpClick,
                onSupportClick = onSupportClick,
                onRateClick = onRateClick
            )
        }
    }
}