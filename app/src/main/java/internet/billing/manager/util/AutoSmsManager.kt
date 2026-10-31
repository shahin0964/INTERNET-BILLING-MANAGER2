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
            title = "Monthly Bill Notice",
            description = "Sent when monthly bill is generated",
            defaultText = "Dear {NAME}, your internet bill for {MONTH} is ৳{BILL}. Please pay by the 10th to enjoy uninterrupted service. Helpline: {PHONE}. - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "payment_received",
            title = "Payment Received Receipt",
            description = "Sent after payment is collected",
            defaultText = "Dear {NAME}, received payment of ৳{AMOUNT} for {MONTH}. Remaining due: ৳{DUE}. Thank you for staying with {COMPANY}."
        ),
        SmsTemplateItem(
            key = "due_reminder",
            title = "Due Warning / Reminder",
            description = "Sent to remind customers with unpaid due",
            defaultText = "Dear {NAME}, your unpaid due is ৳{DUE}. Please clear your bill within 24 hours to avoid line disconnection. Helpline: {PHONE}. - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "line_suspended",
            title = "Line Disconnection Notice",
            description = "Sent when customer line is suspended",
            defaultText = "Dear {NAME}, your internet line has been temporarily suspended due to unpaid bill ৳{DUE}. Please pay to restore line immediately. Helpline: {PHONE}. - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "line_reconnected",
            title = "Reconnection Success Alert",
            description = "Sent when line is restored/active",
            defaultText = "Dear {NAME}, your internet connection has been successfully re-activated. Thank you for your payment. Happy browsing! - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "welcome_customer",
            title = "New Customer Welcome",
            description = "Sent when a new customer is registered",
            defaultText = "Welcome to {COMPANY}! Your PPPoE ID: {PPPOE}, Package: {PACKAGE}. Helpline: {PHONE}. Thank you for choosing us!"
        ),
        SmsTemplateItem(
            key = "maintenance_alert",
            title = "Maintenance / Down Alert",
            description = "Sent during fiber / upstream maintenance",
            defaultText = "Dear Customer, internet service will be temporarily interrupted on {DATE} from {TIME} due to upstream fiber maintenance. - {COMPANY}"
        ),
        SmsTemplateItem(
            key = "festive_greeting",
            title = "Festive & Eid Greetings",
            description = "Holiday / Eid / New Year wishes",
            defaultText = "Wishing you and your family a joyful Eid Mubarak and best wishes from {COMPANY}!"
        ),
        SmsTemplateItem(
            key = "custom_notice",
            title = "General Custom Notice",
            description = "Custom message for all or selected users",
            defaultText = "Dear Customer, {MESSAGE}. Helpline: {PHONE}. - {COMPANY}"
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
        return prefs.getString(TEMPLATE_PREFIX + key, defaultText) ?: defaultText
    }

    fun saveTemplateText(key: String, text: String, context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TEMPLATE_PREFIX + key, text).apply()
    }

    fun formatMessage(
        template: String,
        customer: Customer,
        ispSettings: IspSettings?,
        extraMonth: String = "",
        extraAmount: Double = 0.0,
        extraMessage: String = ""
    ): String {
        val currentMonth = extraMonth.ifBlank {
            SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
        }
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
