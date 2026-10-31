package internet.billing.manager.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val id: String,
    val name: String,
    val subtitle: String,
    val isDark: Boolean,
    val bgDark: Color,
    val bgCard: Color,
    val bgCardLight: Color,
    val cardBorder: Color,
    val cardBorderSubtle: Color,
    val accentCyan: Color,
    val accentGreen: Color = Color(0xFF00E676),
    val accentPurple: Color = Color(0xFF7C4DFF),
    val accentOrange: Color = Color(0xFFFF9100),
    val accentRed: Color = Color(0xFFFF5252),
    val accentBlue: Color = Color(0xFF2979FF),
    val accentGold: Color = Color(0xFFFFD700),
    val accentPink: Color = Color(0xFFFF4081),
    val statusActiveBg: Color = Color(0x2600E676),
    val statusActiveText: Color = Color(0xFF00E676),
    val statusSuspendedBg: Color = Color(0x26FF9100),
    val statusSuspendedText: Color = Color(0xFFFF9100),
    val statusInactiveBg: Color = Color(0x26FF5252),
    val statusInactiveText: Color = Color(0xFFFF5252),
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val navBg: Color,
    val navSelected: Color,
    val navUnselected: Color,
    val onAccent: Color,
    val previewColors: List<Color>
)

object AppThemes {
    // 1. Midnight Cyber (Default) - Current dark AMOLED theme
    val MidnightCyber = ThemePalette(
        id = "midnight_cyber",
        name = "Midnight Cyber (Default)",
        subtitle = "Deep Space Dark AMOLED",
        isDark = true,
        bgDark = Color(0xFF090E1A),
        bgCard = Color(0xFF131D31),
        bgCardLight = Color(0xFF1B2844),
        cardBorder = Color(0xFF26375A),
        cardBorderSubtle = Color(0xFF1C2B47),
        accentCyan = Color(0xFF00E5FF),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFF7C4DFF),
        accentOrange = Color(0xFFFF9100),
        accentRed = Color(0xFFFF5252),
        accentBlue = Color(0xFF2979FF),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FF9100),
        statusSuspendedText = Color(0xFFFF9100),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B),
        navBg = Color(0xFF0D1424),
        navSelected = Color(0xFF00E5FF),
        navUnselected = Color(0xFF64748B),
        onAccent = Color(0xFF090E1A),
        previewColors = listOf(Color(0xFF090E1A), Color(0xFF131D31), Color(0xFF00E5FF), Color(0xFF00E676))
    )

    // 2. Liquid Glass (Light) - Soft Frost Ice & Ocean Blue (From user demo)
    val LiquidGlass = ThemePalette(
        id = "liquid_glass",
        name = "Liquid Glass (Light)",
        subtitle = "Soft Frost Ice & Ocean Blue",
        isDark = false,
        bgDark = Color(0xFFEEF5FD),
        bgCard = Color(0xFFFFFFFF),
        bgCardLight = Color(0xFFF3F8FE),
        cardBorder = Color(0xFFD6E6F8),
        cardBorderSubtle = Color(0xFFE5F0FC),
        accentCyan = Color(0xFF0084FF),
        accentGreen = Color(0xFF10B981),
        accentPurple = Color(0xFF6366F1),
        accentOrange = Color(0xFFF59E0B),
        accentRed = Color(0xFFEF4444),
        accentBlue = Color(0xFF0284C7),
        statusActiveBg = Color(0xFFD1FAE5),
        statusActiveText = Color(0xFF059669),
        statusSuspendedBg = Color(0xFFFEF3C7),
        statusSuspendedText = Color(0xFFD97706),
        statusInactiveBg = Color(0xFFFEE2E2),
        statusInactiveText = Color(0xFFDC2626),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF5A6F8A),
        textMuted = Color(0xFF8FA3BD),
        navBg = Color(0xFFFFFFFF),
        navSelected = Color(0xFF0084FF),
        navUnselected = Color(0xFF8FA3BD),
        onAccent = Color.White,
        previewColors = listOf(Color(0xFFEEF5FD), Color(0xFFFFFFFF), Color(0xFF0084FF), Color(0xFF10B981))
    )

    // 3. AMOLED Pitch Black - Pure OLED Battery Saver
    val AmoledPitch = ThemePalette(
        id = "amoled_pitch",
        name = "AMOLED Pitch Black",
        subtitle = "True 0% Black OLED Minimalist",
        isDark = true,
        bgDark = Color(0xFF000000),
        bgCard = Color(0xFF111111),
        bgCardLight = Color(0xFF1C1C1C),
        cardBorder = Color(0xFF2C2C2C),
        cardBorderSubtle = Color(0xFF1F1F1F),
        accentCyan = Color(0xFF00E5FF),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFF7C4DFF),
        accentOrange = Color(0xFFFF9100),
        accentRed = Color(0xFFFF5252),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FF9100),
        statusSuspendedText = Color(0xFFFF9100),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF71717A),
        navBg = Color(0xFF000000),
        navSelected = Color(0xFF00E5FF),
        navUnselected = Color(0xFF71717A),
        onAccent = Color(0xFF000000),
        previewColors = listOf(Color(0xFF000000), Color(0xFF1C1C1C), Color(0xFF00E5FF), Color(0xFF00E676))
    )

    // 4. Emerald Cyber - Matrix Mint Neon
    val EmeraldCyber = ThemePalette(
        id = "emerald_cyber",
        name = "Emerald Cyber",
        subtitle = "Matrix Mint Neon Dark",
        isDark = true,
        bgDark = Color(0xFF06120B),
        bgCard = Color(0xFF0E2217),
        bgCardLight = Color(0xFF153323),
        cardBorder = Color(0xFF1F4732),
        cardBorderSubtle = Color(0xFF173827),
        accentCyan = Color(0xFF00E676),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFF81C784),
        accentOrange = Color(0xFFFFB74D),
        accentRed = Color(0xFFE57373),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FFB74D),
        statusSuspendedText = Color(0xFFFFB74D),
        statusInactiveBg = Color(0x26E57373),
        statusInactiveText = Color(0xFFE57373),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF8BB59F),
        textMuted = Color(0xFF5E8570),
        navBg = Color(0xFF07140D),
        navSelected = Color(0xFF00E676),
        navUnselected = Color(0xFF5E8570),
        onAccent = Color(0xFF06120B),
        previewColors = listOf(Color(0xFF06120B), Color(0xFF0E2217), Color(0xFF00E676), Color(0xFF81C784))
    )

    // 5. Royal Nebula - Deep Velvet Purple & Lavender
    val RoyalNebula = ThemePalette(
        id = "royal_nebula",
        name = "Royal Nebula",
        subtitle = "Deep Velvet Purple & Lavender",
        isDark = true,
        bgDark = Color(0xFF0D091B),
        bgCard = Color(0xFF191233),
        bgCardLight = Color(0xFF241A47),
        cardBorder = Color(0xFF3B2B6E),
        cardBorderSubtle = Color(0xFF2D2154),
        accentCyan = Color(0xFFB388FF),
        accentGreen = Color(0xFF69F0AE),
        accentPurple = Color(0xFFB388FF),
        accentOrange = Color(0xFFFFAB40),
        accentRed = Color(0xFFFF5252),
        statusActiveBg = Color(0x2669F0AE),
        statusActiveText = Color(0xFF69F0AE),
        statusSuspendedBg = Color(0x26FFAB40),
        statusSuspendedText = Color(0xFFFFAB40),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA89BCB),
        textMuted = Color(0xFF776999),
        navBg = Color(0xFF0E0A1D),
        navSelected = Color(0xFFB388FF),
        navUnselected = Color(0xFF776999),
        onAccent = Color(0xFF0D091B),
        previewColors = listOf(Color(0xFF0D091B), Color(0xFF191233), Color(0xFFB388FF), Color(0xFF69F0AE))
    )

    // 6. Sunset Amber - Warm Golden Honey Dark
    val SunsetAmber = ThemePalette(
        id = "sunset_amber",
        name = "Sunset Amber",
        subtitle = "Warm Golden Honey Dark",
        isDark = true,
        bgDark = Color(0xFF140D07),
        bgCard = Color(0xFF24170E),
        bgCardLight = Color(0xFF362215),
        cardBorder = Color(0xFF4F321F),
        cardBorderSubtle = Color(0xFF3B2517),
        accentCyan = Color(0xFFFF9100),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFFFF7043),
        accentOrange = Color(0xFFFF9100),
        accentRed = Color(0xFFFF5252),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FF9100),
        statusSuspendedText = Color(0xFFFF9100),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFC7A791),
        textMuted = Color(0xFF91745E),
        navBg = Color(0xFF140D07),
        navSelected = Color(0xFFFF9100),
        navUnselected = Color(0xFF91745E),
        onAccent = Color(0xFF140D07),
        previewColors = listOf(Color(0xFF140D07), Color(0xFF24170E), Color(0xFFFF9100), Color(0xFFFFD700))
    )

    val allThemes: List<ThemePalette> = listOf(
        MidnightCyber,
        LiquidGlass,
        AmoledPitch,
        EmeraldCyber,
        RoyalNebula,
        SunsetAmber
    )
}

object ThemeManager {
    private const val PREFS_NAME = "ibm_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    var currentTheme: ThemePalette by mutableStateOf(AppThemes.MidnightCyber)
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_THEME_ID, AppThemes.MidnightCyber.id) ?: AppThemes.MidnightCyber.id
        currentTheme = AppThemes.allThemes.firstOrNull { it.id == savedId } ?: AppThemes.MidnightCyber
    }

    fun setTheme(theme: ThemePalette, context: Context? = null) {
        currentTheme = theme
        context?.let {
            val prefs = it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_THEME_ID, theme.id).apply()
        }
    }
}
