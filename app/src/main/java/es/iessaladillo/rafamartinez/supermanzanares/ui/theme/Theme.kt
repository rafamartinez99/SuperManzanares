package es.iessaladillo.rafamartinez.supermanzanares.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = AzulManzanares,
    onPrimary = Color.White,
    secondary = VerdeManzanares,
    onSecondary = Color.White,
    tertiary = AmarilloManzanares,
    onTertiary = Color.White,
    error = RojoManzanares,
    onError = Color.White,
    background = BlancoFondo,
    onBackground = GrisOscuro,
    surface = GrisSuave,
    onSurface = GrisOscuro,
    primaryContainer = TurquesaManzanares,
    onPrimaryContainer = Color.White,
)

private val DarkColorScheme = lightColorScheme( // usamos lightColorScheme para mantener los mismos tonos
    primary = Color(0xFF257AF6),
    onPrimary = Color.White,
    secondary = VerdeManzanares,
    onSecondary = Color(0xFF2C2C2C),
    tertiary = AmarilloManzanares,
    onTertiary = Color.White,
    error = RojoManzanares,
    onError = Color.White,
    background = Color(0xFF1E1E1E), // gris oscuro moderno
    onBackground = Color.White,
    surface = Color(0xFF2C2C2C),    // gris un poco más claro para tarjetas, campos, etc.
    onSurface = Color.White,
    primaryContainer = TurquesaManzanares,
    onPrimaryContainer = Color.White,
    onSurfaceVariant = Color.White
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
            if (darkTheme) dynamicLightColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme // antes tenías LightColorScheme aquí
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
