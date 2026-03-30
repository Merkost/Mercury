package com.github.merkost.mercury.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class MercuryTypography(
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val codeLarge: TextStyle,
    val codeMedium: TextStyle,
    val codeSmall: TextStyle
)

fun mercuryTypography(
    sansFamily: FontFamily = FontFamily.Default,
    monoFamily: FontFamily = FontFamily.Monospace
): MercuryTypography = MercuryTypography(
    bodyLarge = TextStyle(fontFamily = sansFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = sansFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = sansFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    titleLarge = TextStyle(fontFamily = sansFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = sansFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = sansFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = sansFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelMedium = TextStyle(fontFamily = sansFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp),
    labelSmall = TextStyle(fontFamily = sansFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, lineHeight = 12.sp, letterSpacing = 0.5.sp),
    codeLarge = TextStyle(fontFamily = monoFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    codeMedium = TextStyle(fontFamily = monoFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    codeSmall = TextStyle(fontFamily = monoFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
)
