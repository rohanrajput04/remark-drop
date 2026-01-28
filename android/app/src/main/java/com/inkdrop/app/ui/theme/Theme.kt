package com.inkdrop.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Ink Drop brand colors - deep blue/indigo theme
private val md_theme_light_primary = Color(0xFF3F51B5)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFDDE1FF)
private val md_theme_light_onPrimaryContainer = Color(0xFF001159)
private val md_theme_light_secondary = Color(0xFF5C5D72)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFE1E0F9)
private val md_theme_light_onSecondaryContainer = Color(0xFF191A2C)
private val md_theme_light_tertiary = Color(0xFF78536B)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFFFD8ED)
private val md_theme_light_onTertiaryContainer = Color(0xFF2E1126)
private val md_theme_light_error = Color(0xFFBA1A1A)
private val md_theme_light_errorContainer = Color(0xFFFFDAD6)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFF410002)
private val md_theme_light_background = Color(0xFFFEFBFF)
private val md_theme_light_onBackground = Color(0xFF1B1B1F)
private val md_theme_light_surface = Color(0xFFFEFBFF)
private val md_theme_light_onSurface = Color(0xFF1B1B1F)
private val md_theme_light_surfaceVariant = Color(0xFFE3E1EC)
private val md_theme_light_onSurfaceVariant = Color(0xFF46464F)
private val md_theme_light_outline = Color(0xFF767680)
private val md_theme_light_inverseOnSurface = Color(0xFFF3F0F4)
private val md_theme_light_inverseSurface = Color(0xFF303034)
private val md_theme_light_inversePrimary = Color(0xFFBAC3FF)

private val md_theme_dark_primary = Color(0xFFBAC3FF)
private val md_theme_dark_onPrimary = Color(0xFF08218A)
private val md_theme_dark_primaryContainer = Color(0xFF293CA0)
private val md_theme_dark_onPrimaryContainer = Color(0xFFDDE1FF)
private val md_theme_dark_secondary = Color(0xFFC5C4DD)
private val md_theme_dark_onSecondary = Color(0xFF2E2F42)
private val md_theme_dark_secondaryContainer = Color(0xFF444559)
private val md_theme_dark_onSecondaryContainer = Color(0xFFE1E0F9)
private val md_theme_dark_tertiary = Color(0xFFE8B9D5)
private val md_theme_dark_onTertiary = Color(0xFF46263C)
private val md_theme_dark_tertiaryContainer = Color(0xFF5E3C53)
private val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8ED)
private val md_theme_dark_error = Color(0xFFFFB4AB)
private val md_theme_dark_errorContainer = Color(0xFF93000A)
private val md_theme_dark_onError = Color(0xFF690005)
private val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
private val md_theme_dark_background = Color(0xFF1B1B1F)
private val md_theme_dark_onBackground = Color(0xFFE4E1E6)
private val md_theme_dark_surface = Color(0xFF1B1B1F)
private val md_theme_dark_onSurface = Color(0xFFE4E1E6)
private val md_theme_dark_surfaceVariant = Color(0xFF46464F)
private val md_theme_dark_onSurfaceVariant = Color(0xFFC7C5D0)
private val md_theme_dark_outline = Color(0xFF90909A)
private val md_theme_dark_inverseOnSurface = Color(0xFF1B1B1F)
private val md_theme_dark_inverseSurface = Color(0xFFE4E1E6)
private val md_theme_dark_inversePrimary = Color(0xFF3F51B5)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

@Composable
fun InkDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
