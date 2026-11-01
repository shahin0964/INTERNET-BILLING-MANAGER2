package internet.billing.manager.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import internet.billing.manager.InternetBillingApp
import internet.billing.manager.MainActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object AutoBackupManager {
    private const val PREFS_NAME = "auto_backup_preferences"
    private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
    private const val KEY_LAST_BACKUP_SIZE = "last_backup_size"
    private const val KEY_LAST_BACKUP_PATH = "last_backup_path"
    private const val KEY_LAST_CUSTOMERS_COUNT = "last_cust_count"
    private const val KEY_LAST_BILLS_COUNT = "last_bills_count"
    private const val KEY_LAST_PAYMENTS_COUNT = "last_pay_count"
    private const val ALARM_REQUEST_CODE = 9922
    private const val BACKUP_FILENAME = "IBM_AutoBackup.db"

    private val backupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var debounceJob: Job? = null

    data class BackupInfo(
        val exists: Boolean,
        val timestampMillis: Long,
        val formattedDate: String,
        val sizeBytes: Long,
        val formattedSize: String,
        val filePath: String,
        val customerCount: Int,
        val billCount: Int,
        val paymentCount: Int
    )

    fun initialize(context: Context) {
        scheduleDailyBackupAlarm(context)
        // If never backed up, run initial backup
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
        if (lastTime == 0L || System.currentTimeMillis() - lastTime > 24 * 60 * 60 * 1000L) {
            scheduleDebouncedBackup(context, delayMs = 3000L)
        }
    }

    /**
     * Call this whenever data is created, updated, or deleted.
     * Uses a debounce timer so batch operations (e.g., bulk billing) only write once.
     */
    fun scheduleDebouncedBackup(context: Context, delayMs: Long = 1500L) {
        debounceJob?.cancel()
        debounceJob = backupScope.launch {
            delay(delayMs)
            performBackup(context.applicationContext)
        }
    }

    /**
     * Performs a full atomic backup of the entire app data,
     * overwriting the single destination file (IBM_AutoBackup.db).
     */
    @Synchronized
    fun performBackup(context: Context): Boolean {
        return try {
            val app = context.applicationContext as InternetBillingApp
            val db = app.database

            // 1. Flush SQLite WAL to disk so main database file is 100% up-to-date
            try {
                val cursor = db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
                cursor.moveToFirst()
                cursor.close()
            } catch (_: Exception) {}

            // 2. Embed SharedPreferences into an SQLite table inside the database
            try {
                val sdb = db.openHelper.writableDatabase
                sdb.execSQL("CREATE TABLE IF NOT EXISTS app_preferences_backup (key_name TEXT PRIMARY KEY, value_json TEXT)")
                val prefsJson = JSONObject().apply {
                    put("auto_sms", getPrefsAsJson(context, "auto_sms_preferences"))
                    put("app_theme", getPrefsAsJson(context, "app_theme_preferences"))
                    put("language", getPrefsAsJson(context, "language_preferences"))
                    put("network_tools", getPrefsAsJson(context, "network_tools_prefs"))
                    put("auto_billing", getPrefsAsJson(context, "auto_billing_preferences"))
                }
                sdb.execSQL(
                    "INSERT OR REPLACE INTO app_preferences_backup (key_name, value_json) VALUES ('all_preferences', ?)",
                    arrayOf(prefsJson.toString())
                )
                val c2 = sdb.query("PRAGMA wal_checkpoint(FULL)")
                c2.moveToFirst()
                c2.close()
            } catch (_: Exception) {}

            // Count current records for summary
            var custCount = 0
            var billCount = 0
            var payCount = 0
            try {
                val sdb = db.openHelper.readableDatabase
                val cCust = sdb.query("SELECT COUNT(*) FROM customers")
                if (cCust.moveToFirst()) custCount = cCust.getInt(0)
                cCust.close()

                val cBill = sdb.query("SELECT COUNT(*) FROM bill_entries")
                if (cBill.moveToFirst()) billCount = cBill.getInt(0)
                cBill.close()

                val cPay = sdb.query("SELECT COUNT(*) FROM payment_transactions")
                if (cPay.moveToFirst()) payCount = cPay.getInt(0)
                cPay.close()
            } catch (_: Exception) {}

            // 3. Source DB file
            val sourceFile = context.getDatabasePath("internet_billing_manager.db")
            if (!sourceFile.exists()) return false

            // 4. Primary Destination: /sdcard/Download/IBM_Backup/IBM_AutoBackup.db
            var savedFile: File? = null
            try {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val backupDir = File(downloadDir, "IBM_Backup")
                if (!backupDir.exists()) backupDir.mkdirs()
                val targetFile = File(backupDir, BACKUP_FILENAME)
                copyFileAtomically(sourceFile, targetFile)
                savedFile = targetFile
            } catch (_: Exception) {}

            // 5. Internal Fallback: context.filesDir/backup/IBM_AutoBackup.db
            try {
                val internalDir = File(context.filesDir, "backup")
                if (!internalDir.exists()) internalDir.mkdirs()
                val internalTarget = File(internalDir, BACKUP_FILENAME)
                copyFileAtomically(sourceFile, internalTarget)
                if (savedFile == null) savedFile = internalTarget
            } catch (_: Exception) {}

            if (savedFile != null && savedFile.exists() && savedFile.length() > 0) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis())
                    .putLong(KEY_LAST_BACKUP_SIZE, savedFile.length())
                    .putString(KEY_LAST_BACKUP_PATH, savedFile.absolutePath)
                    .putInt(KEY_LAST_CUSTOMERS_COUNT, custCount)
                    .putInt(KEY_LAST_BILLS_COUNT, billCount)
                    .putInt(KEY_LAST_PAYMENTS_COUNT, payCount)
                    .apply()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Restores full app database from the single IBM_AutoBackup.db file.
     */
    @Synchronized
    fun restoreBackup(context: Context): Boolean {
        return try {
            val primaryFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "IBM_Backup/$BACKUP_FILENAME"
            )
            val fallbackFile = File(context.filesDir, "backup/$BACKUP_FILENAME")
            val backupFile = if (primaryFile.exists() && primaryFile.length() > 0) primaryFile else fallbackFile

            if (!backupFile.exists() || backupFile.length() == 0L) return false

            val currentDb = context.getDatabasePath("internet_billing_manager.db")
            val walDb = context.getDatabasePath("internet_billing_manager.db-wal")
            val shmDb = context.getDatabasePath("internet_billing_manager.db-shm")

            // Close DB
            try {
                (context.applicationContext as InternetBillingApp).database.close()
            } catch (_: Exception) {}

            // Overwrite main DB file
            copyFileAtomically(backupFile, currentDb)
            if (walDb.exists()) walDb.delete()
            if (shmDb.exists()) shmDb.delete()

            // Restore preferences if stored in DB
            try {
                val db = (context.applicationContext as InternetBillingApp).database
                val sdb = db.openHelper.readableDatabase
                val cursor = sdb.query("SELECT value_json FROM app_preferences_backup WHERE key_name = 'all_preferences'")
                if (cursor.moveToFirst()) {
                    val jsonStr = cursor.getString(0)
                    restorePrefsFromJson(context, jsonStr)
                }
                cursor.close()
            } catch (_: Exception) {}

            true
        } catch (_: Exception) {
            false
        }
    }

    fun getBackupInfo(context: Context): BackupInfo {
        val primaryFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "IBM_Backup/$BACKUP_FILENAME"
        )
        val fallbackFile = File(context.filesDir, "backup/$BACKUP_FILENAME")
        val backupFile = if (primaryFile.exists() && primaryFile.length() > 0) primaryFile else fallbackFile

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = if (backupFile.exists()) backupFile.lastModified() else prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
        val size = if (backupFile.exists()) backupFile.length() else prefs.getLong(KEY_LAST_BACKUP_SIZE, 0L)

        val formattedDate = if (timestamp > 0L) {
            val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            fmt.format(Date(timestamp))
        } else {
            "Never"
        }

        val formattedSize = formatBytes(size)
        val path = if (backupFile.exists()) backupFile.absolutePath else "Not generated yet"

        return BackupInfo(
            exists = backupFile.exists() && backupFile.length() > 0L,
            timestampMillis = timestamp,
            formattedDate = formattedDate,
            sizeBytes = size,
            formattedSize = formattedSize,
            filePath = path,
            customerCount = prefs.getInt(KEY_LAST_CUSTOMERS_COUNT, 0),
            billCount = prefs.getInt(KEY_LAST_BILLS_COUNT, 0),
            paymentCount = prefs.getInt(KEY_LAST_PAYMENTS_COUNT, 0)
        )
    }

    fun shareBackup(context: Context): Boolean {
        return try {
            val primaryFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "IBM_Backup/$BACKUP_FILENAME"
            )
            val fallbackFile = File(context.filesDir, "backup/$BACKUP_FILENAME")
            val backupFile = if (primaryFile.exists() && primaryFile.length() > 0) primaryFile else fallbackFile

            if (!backupFile.exists()) return false

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "IBM Full App Backup")
                putExtra(Intent.EXTRA_TEXT, "Internet Billing Manager Full Database Backup ($BACKUP_FILENAME)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Backup File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun scheduleDailyBackupAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AutoBackupReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)

            // Target 02:30 AM every day
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {}
    }

    private fun copyFileAtomically(source: File, destination: File) {
        val tmpFile = File(destination.parentFile, "${destination.name}.tmp")
        FileInputStream(source).use { input ->
            FileOutputStream(tmpFile).use { output ->
                input.copyTo(output)
            }
        }
        if (destination.exists()) {
            destination.delete()
        }
        tmpFile.renameTo(destination)
    }

    private fun getPrefsAsJson(context: Context, prefName: String): JSONObject {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val json = JSONObject()
        prefs.all.forEach { (k, v) ->
            json.put(k, v)
        }
        return json
    }

    private fun restorePrefsFromJson(context: Context, jsonString: String) {
        try {
            val root = JSONObject(jsonString)
            listOf(
                "auto_sms" to "auto_sms_preferences",
                "app_theme" to "app_theme_preferences",
                "language" to "language_preferences",
                "network_tools" to "network_tools_prefs",
                "auto_billing" to "auto_billing_preferences"
            ).forEach { (jsonKey, prefName) ->
                if (root.has(jsonKey)) {
                    val subObj = root.getJSONObject(jsonKey)
                    val p = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
                    val keys = subObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = subObj.get(k)
                        when (v) {
                            is Boolean -> p.putBoolean(k, v)
                            is Int -> p.putInt(k, v)
                            is Long -> p.putLong(k, v)
                            is Double -> p.putFloat(k, v.toFloat())
                            is String -> p.putString(k, v)
                        }
                    }
                    p.apply()
                }
            }
        } catch (_: Exception) {}
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes.toDouble() / 1024.0
        if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
