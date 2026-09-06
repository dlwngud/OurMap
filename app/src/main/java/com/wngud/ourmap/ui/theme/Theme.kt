package com.wngud.ourmap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB77D),
    onPrimary = Color(0xFF4D2600),
    primaryContainer = Color(0xFF6C3908),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFDCC2AE),
    secondaryContainer = Color(0xFF584334),
    onSecondaryContainer = Color(0xFFFFDCC0),
    tertiary = Color(0xFFB8CEA5),
    background = Color(0xFF19130F),
    onBackground = Color(0xFFF0E0D4),
    surface = Color(0xFF211A15),
    onSurface = Color(0xFFF0E0D4),
    surfaceVariant = Color(0xFF50453B),
    onSurfaceVariant = Color(0xFFD5C4B5),
    outlineVariant = Color(0xFF50453B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF9B4600),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC0),
    onPrimaryContainer = Color(0xFF321300),
    secondary = Color(0xFF745943),
    secondaryContainer = Color(0xFFFFEBD9),
    onSecondaryContainer = Color(0xFF523821),
    tertiary = Color(0xFF506543),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF28221D),
    surface = Color(0xFFFFFBF7),
    onSurface = Color(0xFF28221D),
    surfaceVariant = Color(0xFFF1E5D9),
    onSurfaceVariant = Color(0xFF6F6054),
    outlineVariant = Color(0xFFE4D7CB)
)

@Composable
fun OurMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
        ),
        content = content
    )
}
