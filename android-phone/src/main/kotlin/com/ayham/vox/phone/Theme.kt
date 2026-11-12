package com.ayham.vox.phone

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ayham.vox.core.VoxTypography

object VoxColors {
    val Black = Color(0xFF05050A)
    val Card = Color(0xFF111018)
    val Purple = Color(0xFF7B2FF7)
    val PurpleDeep = Color(0xFF4B1D9A)
    val White = Color(0xFFF5F5F7)
    val Gray = Color(0xFF9A9AA8)
}

@Composable
fun VoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = VoxColors.Purple,
            onPrimary = VoxColors.White,
            background = VoxColors.Black,
            onBackground = VoxColors.White,
            surface = VoxColors.Card,
            onSurface = VoxColors.White,
            surfaceVariant = VoxColors.Card,
            onSurfaceVariant = VoxColors.Gray,
        ),
        typography = VoxTypography,
        content = content,
    )
}
