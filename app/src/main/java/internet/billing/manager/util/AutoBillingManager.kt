package internet.billing.manager.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import internet.billing.manager.InternetBillingApp
import internet.billing.manager.data.model.Customer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AutoBillingManager {
    private const val PREFS_NAME = "auto_billing_preferences"
    private const val KEY_LAST_BILLED_MONTH = "last_billed_month_key"
    private const val ALARM_REQUEST_CODE = 8801

    fun checkAndRunAutoBilling(context: Context) {
        val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastBilled = prefs.getString(KEY_LAST_BILLED_MONTH, null)

        if (lastBilled != currentMonthKey) {
            CoroutineScope(Dispatchers.IO).launch {
                runBillingForMonth(context, currentMonthKey)
                prefs.edit().putString(KEY_LAST_BILLED_MONTH, currentMonthKey).apply()
            }
        }

        // Ensure alarm is scheduled for next month 1st at 12:00 AM
        scheduleNextMonthAlarm(context)
    }

    suspend fun runBillingForMonth(context: Context, monthKey: String) {
        val repo = (context.applicationContext as InternetBillingApp).repository
        val active = repo.getCustomersByStatusList(Customer.STATUS_ACTIVE)
        if (active.isNotEmpty()) {
            repo.generateMonthlyBills(monthKey, active)

            // Trigger Auto SMS if enabled
            if (AutoSmsManager.isAutoSmsEnabled && AutoSmsManager.isAutoBillSmsEnabled) {
                val settings = (context.applicationContext as InternetBillingApp).database.settingsDao().getSettingsSync()
                val targets = active.filter { it.phone.isNotBlank() }
                if (targets.isNotEmpty()) {
                    val rawTpl = AutoSmsManager.getTemplateText("bill_generated", context)
                    AutoSmsManager.sendBatchSms(
                        context = context,
                        customers = targets,
                        template = rawTpl,
                        ispSettings = settings,
                        onProgress = { _, _, _, _ -> }
                    )
                }
            }
        }
    }

    fun scheduleNextMonthAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AutoBillingReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)

            // Target next month 1st day at 00:00:00 (12:00 AM)
            val nextMonthCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMonthCal.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMonthCal.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMonthCal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextMonthCal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class AutoBillingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        CoroutineScope(Dispatchers.IO).launch {
            AutoBillingManager.runBillingForMonth(context, currentMonthKey)
            AutoBillingManager.scheduleNextMonthAlarm(context)
        }
    }
}
