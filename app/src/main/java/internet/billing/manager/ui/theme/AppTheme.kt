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
    // 1. Midnight Cyber (Default) - Deep Space Dark AMOLED
    val MidnightCyber = ThemePalette(
        id = "midnight_cyber",
        name = "Midnight Cyber (Default)",
        subtitle = "Deep Space Dark AMOLED",
        isDark = true,
        bgDark = Color(0xFF070B14),
        bgCard = Color(0xFF0F192E),
        bgCardLight = Color(0xFF172440),
        cardBorder = Color(0xFF22355A),
        cardBorderSubtle = Color(0xFF172540),
        accentCyan = Color(0xFF00E5FF),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFFA855F7),
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
        navBg = Color(0xFF0A1224),
        navSelected = Color(0xFF00E5FF),
        navUnselected = Color(0xFF64748B),
        onAccent = Color(0xFF070B14),
        previewColors = listOf(Color(0xFF070B14), Color(0xFF0F192E), Color(0xFF00E5FF), Color(0xFF00E676))
    )

    // 2. Liquid Glass (Light) - Soft Frost Ice & Ocean Blue (Deep Garo Black Text)
    val LiquidGlass = ThemePalette(
        id = "liquid_glass",
        name = "Liquid Glass (Light)",
        subtitle = "Soft Frost Ice & Ocean Blue",
        isDark = false,
        bgDark = Color(0xFFF1F5F9),
        bgCard = Color(0xFFFFFFFF),
        bgCardLight = Color(0xFFF8FAFC),
        cardBorder = Color(0xFFCBD5E1),
        cardBorderSubtle = Color(0xFFE2E8F0),
        accentCyan = Color(0xFF0066CC),
        accentGreen = Color(0xFF059669),
        accentPurple = Color(0xFF7C3AED),
        accentOrange = Color(0xFFD97706),
        accentRed = Color(0xFFDC2626),
        accentBlue = Color(0xFF0284C7),
        statusActiveBg = Color(0xFFDCFCE7),
        statusActiveText = Color(0xFF166534),
        statusSuspendedBg = Color(0xFFFEF3C7),
        statusSuspendedText = Color(0xFF92400E),
        statusInactiveBg = Color(0xFFFEE2E2),
        statusInactiveText = Color(0xFF991B1B),
        textPrimary = Color(0xFF000000),      // Pure Pitch Black
        textSecondary = Color(0xFF0F172A),    // Slate-900 Garo Black
        textMuted = Color(0xFF334155),        // Slate-700 Dark Crisp Charcoal
        navBg = Color(0xFFFFFFFF),
        navSelected = Color(0xFF0066CC),
        navUnselected = Color(0xFF334155),    // Sharp Dark Unselected Nav Text
        onAccent = Color.White,
        previewColors = listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF), Color(0xFF0066CC), Color(0xFF059669))
    )

    // 3. AMOLED Pitch Black - Pure OLED Battery Saver
    val AmoledPitch = ThemePalette(
        id = "amoled_pitch",
        name = "AMOLED Pitch Black",
        subtitle = "True 0% Black OLED Minimalist",
        isDark = true,
        bgDark = Color(0xFF000000),
        bgCard = Color(0xFF0D0D0D),
        bgCardLight = Color(0xFF171717),
        cardBorder = Color(0xFF2E2E2E),
        cardBorderSubtle = Color(0xFF1C1C1C),
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
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF71717A),
        navBg = Color(0xFF000000),
        navSelected = Color(0xFF00E5FF),
        navUnselected = Color(0xFF71717A),
        onAccent = Color(0xFF000000),
        previewColors = listOf(Color(0xFF000000), Color(0xFF171717), Color(0xFF00E5FF), Color(0xFF00E676))
    )

    // 4. Emerald Cyber - Matrix Mint Neon
    val EmeraldCyber = ThemePalette(
        id = "emerald_cyber",
        name = "Emerald Cyber",
        subtitle = "Matrix Mint Neon Dark",
        isDark = true,
        bgDark = Color(0xFF040F08),
        bgCard = Color(0xFF0A1C12),
        bgCardLight = Color(0xFF112B1D),
        cardBorder = Color(0xFF1C452F),
        cardBorderSubtle = Color(0xFF133322),
        accentCyan = Color(0xFF00E676),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFF81C784),
        accentOrange = Color(0xFFFFB74D),
        accentRed = Color(0xFFE57373),
        accentBlue = Color(0xFF29B6F6),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FFB74D),
        statusSuspendedText = Color(0xFFFFB74D),
        statusInactiveBg = Color(0x26E57373),
        statusInactiveText = Color(0xFFE57373),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF9ECEB5),
        textMuted = Color(0xFF689B7F),
        navBg = Color(0xFF06140C),
        navSelected = Color(0xFF00E676),
        navUnselected = Color(0xFF689B7F),
        onAccent = Color(0xFF040F08),
        previewColors = listOf(Color(0xFF040F08), Color(0xFF0A1C12), Color(0xFF00E676), Color(0xFF81C784))
    )

    // 5. Royal Nebula - Deep Velvet Purple & Lavender
    val RoyalNebula = ThemePalette(
        id = "royal_nebula",
        name = "Royal Nebula",
        subtitle = "Deep Velvet Purple & Lavender",
        isDark = true,
        bgDark = Color(0xFF0A0617),
        bgCard = Color(0xFF150D2B),
        bgCardLight = Color(0xFF20153F),
        cardBorder = Color(0xFF38256E),
        cardBorderSubtle = Color(0xFF281952),
        accentCyan = Color(0xFFB388FF),
        accentGreen = Color(0xFF69F0AE),
        accentPurple = Color(0xFFB388FF),
        accentOrange = Color(0xFFFFAB40),
        accentRed = Color(0xFFFF5252),
        accentBlue = Color(0xFF7C4DFF),
        statusActiveBg = Color(0x2669F0AE),
        statusActiveText = Color(0xFF69F0AE),
        statusSuspendedBg = Color(0x26FFAB40),
        statusSuspendedText = Color(0xFFFFAB40),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFB8A9DF),
        textMuted = Color(0xFF8674B2),
        navBg = Color(0xFF0C071C),
        navSelected = Color(0xFFB388FF),
        navUnselected = Color(0xFF8674B2),
        onAccent = Color(0xFF0A0617),
        previewColors = listOf(Color(0xFF0A0617), Color(0xFF150D2B), Color(0xFFB388FF), Color(0xFF69F0AE))
    )

    // 6. Sunset Amber - Warm Golden Honey Dark
    val SunsetAmber = ThemePalette(
        id = "sunset_amber",
        name = "Sunset Amber",
        subtitle = "Warm Golden Honey Dark",
        isDark = true,
        bgDark = Color(0xFF120A04),
        bgCard = Color(0xFF201308),
        bgCardLight = Color(0xFF301D0E),
        cardBorder = Color(0xFF4A2D17),
        cardBorderSubtle = Color(0xFF362010),
        accentCyan = Color(0xFFFF9100),
        accentGreen = Color(0xFF00E676),
        accentPurple = Color(0xFFFF7043),
        accentOrange = Color(0xFFFF9100),
        accentRed = Color(0xFFFF5252),
        accentBlue = Color(0xFFFFB74D),
        statusActiveBg = Color(0x2600E676),
        statusActiveText = Color(0xFF00E676),
        statusSuspendedBg = Color(0x26FF9100),
        statusSuspendedText = Color(0xFFFF9100),
        statusInactiveBg = Color(0x26FF5252),
        statusInactiveText = Color(0xFFFF5252),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFD4B39C),
        textMuted = Color(0xFFA17F67),
        navBg = Color(0xFF120A04),
        navSelected = Color(0xFFFF9100),
        navUnselected = Color(0xFFA17F67),
        onAccent = Color(0xFF120A04),
        previewColors = listOf(Color(0xFF120A04), Color(0xFF201308), Color(0xFFFF9100), Color(0xFFFFD700))
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
