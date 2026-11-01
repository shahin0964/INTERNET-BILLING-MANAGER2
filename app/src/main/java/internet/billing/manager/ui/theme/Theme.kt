package internet.billing.manager.ui.theme

import android.app.Activity
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

@Composable
fun InternetBillingTheme(
    content: @Composable () -> Unit
) {
    val theme = ThemeManager.currentTheme
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = theme.accentCyan,
            onPrimary = theme.onAccent,
            secondary = theme.accentGreen,
            onSecondary = theme.onAccent,
            tertiary = theme.accentPurple,
            background = theme.bgDark,
            surface = theme.bgCard,
            onBackground = theme.textPrimary,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.bgCardLight,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.cardBorder
        )
    } else {
        lightColorScheme(
            primary = theme.accentCyan,
            onPrimary = theme.onAccent,
            secondary = theme.accentGreen,
            onSecondary = theme.onAccent,
            tertiary = theme.accentPurple,
            background = theme.bgDark,
            surface = theme.bgCard,
            onBackground = theme.textPrimary,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.bgCardLight,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.cardBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                window.statusBarColor = theme.bgDark.toArgb()
                window.navigationBarColor = theme.navBg.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !theme.isDark
                insetsController.isAppearanceLightNavigationBars = !theme.isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides theme.textPrimary,
            LocalTextStyle provides TextStyle(color = theme.textPrimary),
            content = content
        )
    }
}
