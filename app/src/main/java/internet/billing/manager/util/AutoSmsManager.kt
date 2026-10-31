package internet.billing.manager.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.IspSettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class SmsTemplateItem(
    val key: String,
    val title: String,
    val description: String,
    val defaultText: String
)

data class SmsSendSummary(
    val total: Int,
    val successful: Int,
    val failed: Int
)

object AutoSmsManager {
    private const val PREFS_NAME = "ibm_auto_sms_prefs"
    private const val KEY_AUTO_SMS_ENABLED = "auto_sms_enabled"
    private const val KEY_AUTO_BILL_SMS = "auto_bill_sms"
    private const val KEY_AUTO_PAYMENT_SMS = "auto_payment_sms"
    private const val KEY_SMS_DELAY_SECONDS = "sms_delay_seconds"
    private const val TEMPLATE_PREFIX = "template_"

    var isAutoSmsEnabled by mutableStateOf(false)
        private set

    var isAutoBillSmsEnabled by mutableStateOf(false)
        private set

    var isAutoPaymentSmsEnabled by mutableStateOf(false)
        private set

    var smsDelaySeconds by mutableStateOf(3) // 3 seconds delay between SMS by default
        private set

    val defaultTemplates = listOf(
        SmsTemplateItem(
            key = "bill_generated",
            title = "মাসিক বিল নোটিশ",
            description = "প্রতি মাসের ১ তারিখে বিল তৈরির পর পাঠানো হয়",
            defaultText = "প্রিয় {NAME}, আপনার {MONTH} মাসের ইন্টারনেট বিল ৳{BILL} টাকা। নিরবচ্ছিন্ন সেবার জন্য আগামী ১০ তারিখের মধ্যে বিল পরিশোধের অনুরোধ রইল। হেল্পলাইন: {PHONE}। - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "payment_received",
            title = "বিল পরিশোধের রসিদ",
            description = "গ্রাহক বিল পরিশোধ করলে স্বয়ংক্রিয়ভাবে পাঠানো হয়",
            defaultText = "প্রিয় {NAME}, {MONTH} মাসের বিল বাবদ ৳{AMOUNT} টাকা সফলভাবে জমা হয়েছে। অবশিষ্ট বকেয়া: ৳{DUE} টাকা। {COMPANY}-র সাথে থাকার জন্য ধন্যবাদ।"
        ),
        SmsTemplateItem(
            key = "due_reminder",
            title = "বকেয়া বিলের তাগাদা",
            description = "বকেয়া বিল পরিশোধের জন্য তাগাদাপত্র",
            defaultText = "প্রিয় {NAME}, আপনার মোট বকেয়া বিল ৳{DUE} টাকা। সংযোগ সচল রাখতে দ্রুত বিল পরিশোধ করার অনুরোধ রইল। হেল্পলাইন: {PHONE}। - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "line_suspended",
            title = "সংযোগ স্থগিত নোটিশ",
            description = "বকেয়া বিলের কারণে লাইন সাময়িক স্থগিত হলে পাঠানো হয়",
            defaultText = "প্রিয় {NAME}, বকেয়া বিল ৳{DUE} টাকা অপরিশোধিত থাকায় আপনার ইন্টারনেট সংযোগ সাময়িকভাবে স্থগিত করা হয়েছে। বিল পরিশোধ করে দ্রুত সংযোগ চালু করুন। হেল্পলাইন: {PHONE}। - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "line_reconnected",
            title = "সংযোগ পুনঃসচল বার্তা",
            description = "বিল পরিশোধের পর লাইন সচল হলে পাঠানো হয়",
            defaultText = "প্রিয় {NAME}, আপনার ইন্টারনেট সংযোগ পুনরায় সফলভাবে চালু করা হয়েছে। বিল পরিশোধের জন্য আপনাকে ধন্যবাদ। শুভ ব্রাউজিং! - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "welcome_customer",
            title = "নতুন গ্রাহক স্বাগতম বার্তা",
            description = "নতুন গ্রাহক নিবন্ধন করার পর পাঠানো হয়",
            defaultText = "{COMPANY}-তে আপনাকে স্বাগতম! আপনার ইউজার আইডি: {PPPOE}, প্যাকেজ: {PACKAGE}। যেকোনো প্রয়োজনে হেল্পলাইন: {PHONE}। আমাদের বেছে নেওয়ার জন্য ধন্যবাদ!"
        ),
        SmsTemplateItem(
            key = "maintenance_alert",
            title = "রক্ষণাবেক্ষণ ও ডাউন নোটিশ",
            description = "ফাইবার বা আপস্ট্রিম মেইনটেন্যান্স চলাকালীন পাঠানো হয়",
            defaultText = "সম্মানিত গ্রাহক, আপস্ট্রিম ফাইবার রক্ষণাবেক্ষণের জন্য {DATE} তারিখে {TIME} পর্যন্ত ইন্টারনেট সেবা সাময়িকভাবে বিঘ্নিত হতে পারে। সাময়িক অসুবিধার জন্য আমরা আন্তরিকভাবে দুঃখিত। - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "festive_greeting",
            title = "ঈদ ও উৎসবের শুভেচ্ছা",
            description = "ঈদ, নববর্ষ বা বিশেষ দিবসের শুভেচ্ছা বার্তা",
            defaultText = "আপনাকে ও আপনার পরিবারের সবাইকে জানাই আন্তরিক শুভেচ্ছা ও ঈদ মোবারক! সবসময় আপনার পাশে - {COMPANY}।"
        ),
        SmsTemplateItem(
            key = "custom_notice",
            title = "সাধারণ নোটিশ বার্তা",
            description = "সকল বা নির্দিষ্ট গ্রাহকদের জন্য সাধারণ নোটিশ",
            defaultText = "সম্মানিত গ্রাহক, {MESSAGE}। যেকোনো প্রয়োজনে যোগাযোগ করুন: {PHONE}। - {COMPANY}"
        )
    )

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isAutoSmsEnabled = prefs.getBoolean(KEY_AUTO_SMS_ENABLED, false)
        isAutoBillSmsEnabled = prefs.getBoolean(KEY_AUTO_BILL_SMS, false)
        isAutoPaymentSmsEnabled = prefs.getBoolean(KEY_AUTO_PAYMENT_SMS, false)
        smsDelaySeconds = prefs.getInt(KEY_SMS_DELAY_SECONDS, 3)
    }

    fun setAutoSmsEnabled(enabled: Boolean, context: Context) {
        isAutoSmsEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SMS_ENABLED, enabled).apply()
    }

    fun setAutoBillSmsEnabled(enabled: Boolean, context: Context) {
        isAutoBillSmsEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_BILL_SMS, enabled).apply()
    }

    fun setAutoPaymentSmsEnabled(enabled: Boolean, context: Context) {
        isAutoPaymentSmsEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_PAYMENT_SMS, enabled).apply()
    }

    fun setSmsDelay(seconds: Int, context: Context) {
        smsDelaySeconds = seconds.coerceIn(1, 10)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SMS_DELAY_SECONDS, smsDelaySeconds).apply()
    }

    fun getTemplateText(key: String, context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultText = defaultTemplates.firstOrNull { it.key == key }?.defaultText ?: ""
        val savedText = prefs.getString(TEMPLATE_PREFIX + key, null) ?: return defaultText

        // Auto-migrate if the saved template is an old English template
        val isOldEnglish = savedText.startsWith("Dear {NAME}") ||
                           savedText.startsWith("Dear Customer") ||
                           savedText.startsWith("Welcome to {COMPANY}") ||
                           savedText.startsWith("Wishing you and your family")
        if (isOldEnglish) {
            prefs.edit().putString(TEMPLATE_PREFIX + key, defaultText).apply()
            return defaultText
        }
        return savedText
    }

    fun saveTemplateText(key: String, text: String, context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TEMPLATE_PREFIX + key, text).apply()
    }

    private fun getBanglaMonth(monthYear: String): String {
        return monthYear
            .replace("January", "জানুয়ারি", ignoreCase = true)
            .replace("February", "ফেব্রুয়ারি", ignoreCase = true)
            .replace("March", "মার্চ", ignoreCase = true)
            .replace("April", "এপ্রিল", ignoreCase = true)
            .replace("May", "মে", ignoreCase = true)
            .replace("June", "জুন", ignoreCase = true)
            .replace("July", "জুলাই", ignoreCase = true)
            .replace("August", "আগস্ট", ignoreCase = true)
            .replace("September", "সেপ্টেম্বর", ignoreCase = true)
            .replace("October", "অক্টোবর", ignoreCase = true)
            .replace("November", "নভেম্বর", ignoreCase = true)
            .replace("December", "ডিসেম্বর", ignoreCase = true)
    }

    fun formatMessage(
        template: String,
        customer: Customer,
        ispSettings: IspSettings?,
        extraMonth: String = "",
        extraAmount: Double = 0.0,
        extraMessage: String = ""
    ): String {
        val currentMonthRaw = extraMonth.ifBlank {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        }
        val currentMonth = getBanglaMonth(currentMonthRaw)
        val company = ispSettings?.companyName?.takeIf { it.isNotBlank() && !it.equals("U-turn Technologies", ignoreCase = true) } ?: "Internet Billing Manager"
        val phone = ispSettings?.phone?.takeIf { it.isNotBlank() && it != "01700000000" } ?: ""

        return template
            .replace("{NAME}", customer.name.ifBlank { customer.pppoeUsername })
            .replace("{BILL}", customer.monthlyBill.toInt().toString())
            .replace("{DUE}", customer.dueAmount.toInt().toString())
            .replace("{AMOUNT}", extraAmount.toInt().toString())
            .replace("{MONTH}", currentMonth)
            .replace("{COMPANY}", company)
            .replace("{PHONE}", phone)
            .replace("{PPPOE}", customer.pppoeUsername)
            .replace("{PACKAGE}", customer.packageName.ifBlank { "${customer.speedMbps} Mbps" })
            .replace("{DUE_DAY}", customer.billDueDay.toString())
            .replace("{LAST_DATE}", "${customer.billDueDay} ${currentMonth}")
            .replace("{DATE}", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
            .replace("{TIME}", SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
            .replace("{MESSAGE}", extraMessage)
    }

    fun sendSingleSms(context: Context, phone: String, text: String): Boolean {
        return try {
            val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
            if (cleanPhone.length < 5) return false

            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(text)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanPhone, null, text, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sendBatchSms(
        context: Context,
        customers: List<Customer>,
        template: String,
        ispSettings: IspSettings?,
        extraMessage: String = "",
        onProgress: (current: Int, total: Int, customerName: String, isWaiting: Boolean) -> Unit
    ): SmsSendSummary {
        var successCount = 0
        var failCount = 0
        val total = customers.size

        for (i in customers.indices) {
            val customer = customers[i]
            onProgress(i + 1, total, customer.name.ifBlank { customer.pppoeUsername }, false)

            val text = formatMessage(
                template = template,
                customer = customer,
                ispSettings = ispSettings,
                extraMessage = extraMessage
            )

            val ok = sendSingleSms(context, customer.phone, text)
            if (ok) {
                successCount++
            } else {
                failCount++
            }

            // Safe delay: Wait to prevent SIM spam throttling or carrier ban
            if (i < customers.size - 1) {
                onProgress(i + 1, total, customer.name.ifBlank { customer.pppoeUsername }, true)
                delay(smsDelaySeconds * 1000L)
            }
        }

        return SmsSendSummary(total = total, successful = successCount, failed = failCount)
    }
}
