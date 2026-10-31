package internet.billing.manager.ui.localization

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LanguageManager {
    private const val PREFS_NAME = "ibm_language_prefs"
    private const val KEY_LANG = "selected_language"

    var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.ENGLISH)
        private set

    val strings: AppStrings
        get() = if (currentLanguage == AppLanguage.BANGLA) BanglaStrings else EnglishStrings

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANG, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        currentLanguage = AppLanguage.fromCode(saved)
    }

    fun setLanguage(language: AppLanguage, context: Context? = null) {
        currentLanguage = language
        context?.let {
            val prefs = it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LANG, language.code).apply()
        }
    }
}

val appStrings: AppStrings
    get() = LanguageManager.strings
