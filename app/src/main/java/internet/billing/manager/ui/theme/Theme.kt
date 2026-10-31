package internet.billing.manager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgDark,
    secondary = AccentGreen,
    onSecondary = BgDark,
    tertiary = AccentPurple,
    background = BgDark,
    surface = BgCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BgCardLight,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun InternetBillingTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = NavBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
