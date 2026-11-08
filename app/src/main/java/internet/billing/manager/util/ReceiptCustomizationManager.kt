package internet.billing.manager.util

import android.content.Context
import android.content.SharedPreferences

data class ReceiptCustomizationSettings(
    val autoReceiptPopupEnabled: Boolean = true,
    val receiptTitle: String = "পেমেন্ট রসিদ (PAYMENT RECEIPT)",
    val footerNote: String = "আমাদের ইন্টারনেট সেবা ব্যবহার করার জন্য আপনাকে ধন্যবাদ",
    val specialNotes: String = "যেকোনো সেবা বা বিল সংক্রান্ত তথ্যের জন্য আমাদের হটলাইনে যোগাযোগ করুন।",
    val showMobileBanking: Boolean = true,
    val bkashNumber: String = "",
    val nagadNumber: String = "",
    val signatureName: String = "অনুমোদিত স্বাক্ষরকারী",
    val corporateMotto: String = "Stay Connected, Stay Ahead",
    val showPhone: Boolean = true,
    val showUsername: Boolean = true,
    val showPackage: Boolean = true,
    val showAddress: Boolean = true,
    val showRemainingDue: Boolean = true,
    val showPaymentMethod: Boolean = true
)

object ReceiptCustomizationManager {
    private const val PREFS_NAME = "ibm_receipt_customization_prefs"

    private const val KEY_AUTO_POPUP = "key_auto_receipt_popup"
    private const val KEY_RECEIPT_TITLE = "key_receipt_title"
    private const val KEY_FOOTER_NOTE = "key_footer_note"
    private const val KEY_SPECIAL_NOTES = "key_special_notes"
    private const val KEY_SHOW_MOBILE_BANKING = "key_show_mobile_banking"
    private const val KEY_BKASH_NUMBER = "key_bkash_number"
    private const val KEY_NAGAD_NUMBER = "key_nagad_number"
    private const val KEY_SIGNATURE_NAME = "key_signature_name"
    private const val KEY_CORPORATE_MOTTO = "key_corporate_motto"
    private const val KEY_SHOW_PHONE = "key_show_phone"
    private const val KEY_SHOW_USERNAME = "key_show_username"
    private const val KEY_SHOW_PACKAGE = "key_show_package"
    private const val KEY_SHOW_ADDRESS = "key_show_address"
    private const val KEY_SHOW_REMAINING_DUE = "key_show_remaining_due"
    private const val KEY_SHOW_PAYMENT_METHOD = "key_show_payment_method"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoPopupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_POPUP, true)
    }

    fun getSettings(context: Context): ReceiptCustomizationSettings {
        val prefs = getPrefs(context)
        return ReceiptCustomizationSettings(
            autoReceiptPopupEnabled = prefs.getBoolean(KEY_AUTO_POPUP, true),
            receiptTitle = prefs.getString(KEY_RECEIPT_TITLE, "পেমেন্ট রসিদ (PAYMENT RECEIPT)") ?: "পেমেন্ট রসিদ (PAYMENT RECEIPT)",
            footerNote = prefs.getString(KEY_FOOTER_NOTE, "আমাদের ইন্টারনেট সেবা ব্যবহার করার জন্য আপনাকে ধন্যবাদ") ?: "আমাদের ইন্টারনেট সেবা ব্যবহার করার জন্য আপনাকে ধন্যবাদ",
            specialNotes = prefs.getString(KEY_SPECIAL_NOTES, "যেকোনো সেবা বা বিল সংক্রান্ত তথ্যের জন্য আমাদের হটলাইনে যোগাযোগ করুন।") ?: "যেকোনো সেবা বা বিল সংক্রান্ত তথ্যের জন্য আমাদের হটলাইনে যোগাযোগ করুন।",
            showMobileBanking = prefs.getBoolean(KEY_SHOW_MOBILE_BANKING, true),
            bkashNumber = prefs.getString(KEY_BKASH_NUMBER, "") ?: "",
            nagadNumber = prefs.getString(KEY_NAGAD_NUMBER, "") ?: "",
            signatureName = prefs.getString(KEY_SIGNATURE_NAME, "অনুমোদিত স্বাক্ষরকারী") ?: "অনুমোদিত স্বাক্ষরকারী",
            corporateMotto = prefs.getString(KEY_CORPORATE_MOTTO, "Stay Connected, Stay Ahead") ?: "Stay Connected, Stay Ahead",
            showPhone = prefs.getBoolean(KEY_SHOW_PHONE, true),
            showUsername = prefs.getBoolean(KEY_SHOW_USERNAME, true),
            showPackage = prefs.getBoolean(KEY_SHOW_PACKAGE, true),
            showAddress = prefs.getBoolean(KEY_SHOW_ADDRESS, true),
            showRemainingDue = prefs.getBoolean(KEY_SHOW_REMAINING_DUE, true),
            showPaymentMethod = prefs.getBoolean(KEY_SHOW_PAYMENT_METHOD, true)
        )
    }

    fun saveSettings(context: Context, settings: ReceiptCustomizationSettings) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_AUTO_POPUP, settings.autoReceiptPopupEnabled)
            putString(KEY_RECEIPT_TITLE, settings.receiptTitle)
            putString(KEY_FOOTER_NOTE, settings.footerNote)
            putString(KEY_SPECIAL_NOTES, settings.specialNotes)
            putBoolean(KEY_SHOW_MOBILE_BANKING, settings.showMobileBanking)
            putString(KEY_BKASH_NUMBER, settings.bkashNumber)
            putString(KEY_NAGAD_NUMBER, settings.nagadNumber)
            putString(KEY_SIGNATURE_NAME, settings.signatureName)
            putString(KEY_CORPORATE_MOTTO, settings.corporateMotto)
            putBoolean(KEY_SHOW_PHONE, settings.showPhone)
            putBoolean(KEY_SHOW_USERNAME, settings.showUsername)
            putBoolean(KEY_SHOW_PACKAGE, settings.showPackage)
            putBoolean(KEY_SHOW_ADDRESS, settings.showAddress)
            putBoolean(KEY_SHOW_REMAINING_DUE, settings.showRemainingDue)
            putBoolean(KEY_SHOW_PAYMENT_METHOD, settings.showPaymentMethod)
            apply()
        }
    }

    fun resetToDefaults(context: Context): ReceiptCustomizationSettings {
        val defaults = ReceiptCustomizationSettings()
        saveSettings(context, defaults)
        return defaults
    }
}
