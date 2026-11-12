package com.ayham.vox.core

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/** خط Cairo (OFL). الملف cairo.ttf ينزّله البناء على GitHub إلى res/font. */
val CairoFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.cairo, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.cairo, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.cairo, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val VoxTypography: Typography = Typography().let { t ->
    Typography(
        displayLarge = t.displayLarge.copy(fontFamily = CairoFamily),
        displayMedium = t.displayMedium.copy(fontFamily = CairoFamily),
        displaySmall = t.displaySmall.copy(fontFamily = CairoFamily),
        headlineLarge = t.headlineLarge.copy(fontFamily = CairoFamily),
        headlineMedium = t.headlineMedium.copy(fontFamily = CairoFamily),
        headlineSmall = t.headlineSmall.copy(fontFamily = CairoFamily),
        titleLarge = t.titleLarge.copy(fontFamily = CairoFamily),
        titleMedium = t.titleMedium.copy(fontFamily = CairoFamily),
        titleSmall = t.titleSmall.copy(fontFamily = CairoFamily),
        bodyLarge = t.bodyLarge.copy(fontFamily = CairoFamily),
        bodyMedium = t.bodyMedium.copy(fontFamily = CairoFamily),
        bodySmall = t.bodySmall.copy(fontFamily = CairoFamily),
        labelLarge = t.labelLarge.copy(fontFamily = CairoFamily),
        labelMedium = t.labelMedium.copy(fontFamily = CairoFamily),
        labelSmall = t.labelSmall.copy(fontFamily = CairoFamily),
    )
}
