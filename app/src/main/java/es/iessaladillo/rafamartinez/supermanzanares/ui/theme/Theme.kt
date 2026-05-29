package es.iessaladillo.rafamartinez.supermanzanares.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = AzulManzanares,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E2FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = VerdeManzanares,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBDF0C1),
    onSecondaryContainer = Color(0xFF002107),
    tertiary = AmarilloManzanares,
    onTertiary = Color(0xFF2A1A00),
    tertiaryContainer = Color(0xFFFFE08A),
    onTertiaryContainer = Color(0xFF251A00),
    error = RojoManzanares,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextVariant,
    surfaceContainer = Color(0xFFF1F2F6),
    surfaceContainerHigh = Color(0xFFECEEF3),
    surfaceContainerHighest = Color(0xFFE6E8EE),
    outline = LightOutline,
    outlineVariant = Color(0xFFC7CAD2),
    inverseSurface = Color(0xFF303035),
    inverseOnSurface = Color(0xFFF2F0F5),
    inversePrimary = Color(0xFFADC6FF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E68),
    primaryContainer = Color(0xFF004493),
    onPrimaryContainer = Color(0xFFD7E2FF),
    secondary = Color(0xFF8EDC95),
    onSecondary = Color(0xFF00390E),
    secondaryContainer = Color(0xFF00531A),
    onSecondaryContainer = Color(0xFFA9F8AE),
    tertiary = Color(0xFFFFC94A),
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFF5D4300),
    onTertiaryContainer = Color(0xFFFFDF88),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextVariant,
    surfaceContainer = Color(0xFF1E2026),
    surfaceContainerHigh = Color(0xFF292B31),
    surfaceContainerHighest = Color(0xFF34363D),
    outline = DarkOutline,
    outlineVariant = Color(0xFF44474F),
    inverseSurface = Color(0xFFE3E2E8),
    inverseOnSurface = Color(0xFF303035),
    inversePrimary = AzulManzanares,
)

@Composable
fun SuperManzanaresTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
