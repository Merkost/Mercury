package com.github.merkost.mercury.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMercuryColors = staticCompositionLocalOf { DarkColorScheme }
val LocalMercuryTypography = staticCompositionLocalOf { mercuryTypography() }

object MercuryTheme {
    val colors: MercuryColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryColors.current

    val typography: MercuryTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryTypography.current
}

@Composable
fun MercuryTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val typography = mercuryTypography()

    CompositionLocalProvider(
        LocalMercuryColors provides colorScheme,
        LocalMercuryTypography provides typography,
        content = content
    )
}
