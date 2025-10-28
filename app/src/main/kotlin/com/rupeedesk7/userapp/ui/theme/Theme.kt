package com.rupeedesk7.userapp.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColors = lightColors(
    primary = androidx.compose.ui.graphics.Color(0xFF1F6FEB)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = LightColors) {
        content()
    }
}
