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
    private const val KEY_LAST_DRIVE_BACKUP_TIME = "last_drive_backup_time"
    private const val KEY_LAST_DRIVE_BACKUP_SIZE = "last_drive_backup_size"
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

    data class DriveBackupInfo(
        val exists: Boolean,
        val timestampMillis: Long,
        val formattedDate: String,
        val sizeBytes: Long,
        val formattedSize: String
    )

    data class BackupValidationResult(
        val isValid: Boolean,
        val customerCount: Int = 0,
        val billCount: Int = 0,
        val paymentCount: Int = 0,
        val errorMessage: String? = null,
        val tempFile: File? = null
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
                    put("auto_sms", getPrefsAsJson(context, "ibm_auto_sms_prefs"))
                    put("app_theme", getPrefsAsJson(context, "ibm_theme_prefs"))
                    put("language", getPrefsAsJson(context, "ibm_language_prefs"))
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

                val cPay = sdb.query("SELECT COUNT(*) FROM payments")
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

            // Reset & Close DB
            try {
                internet.billing.manager.data.local.AppDatabase.resetDatabase()
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

    fun getDriveBackupInfo(context: Context): DriveBackupInfo {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(KEY_LAST_DRIVE_BACKUP_TIME, 0L)
        val size = prefs.getLong(KEY_LAST_DRIVE_BACKUP_SIZE, 0L)

        val formattedDate = if (timestamp > 0L) {
            val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            fmt.format(Date(timestamp))
        } else {
            "Never"
        }

        val formattedSize = formatBytes(size)
        return DriveBackupInfo(
            exists = timestamp > 0L,
            timestampMillis = timestamp,
            formattedDate = formattedDate,
            sizeBytes = size,
            formattedSize = formattedSize
        )
    }

    /**
     * Prepares a 100% up-to-date backup snapshot by forcing a WAL checkpoint,
     * writing app preferences into the DB, and copying it to a file.
     * Also keeps the 24/7 local auto-backup file in sync.
     */
    @Synchronized
    fun prepareFreshBackupFile(context: Context): File? {
        performBackup(context)
        val primaryFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "IBM_Backup/$BACKUP_FILENAME"
        )
        val fallbackFile = File(context.filesDir, "backup/$BACKUP_FILENAME")
        return if (primaryFile.exists() && primaryFile.length() > 0) primaryFile else if (fallbackFile.exists()) fallbackFile else null
    }

    /**
     * Saves the current database backup directly into a destination URI provided by SAF (e.g. Google Drive).
     */
    fun saveBackupToUri(context: Context, destinationUri: Uri): Boolean {
        return try {
            val backupFile = prepareFreshBackupFile(context) ?: return false
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(backupFile).use { input ->
                    input.copyTo(output)
                }
                output.flush()
            } ?: return false

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_DRIVE_BACKUP_TIME, System.currentTimeMillis())
                .putLong(KEY_LAST_DRIVE_BACKUP_SIZE, backupFile.length())
                .apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Direct upload trigger targeting the official Google Drive Android app.
     */
    fun openGoogleDriveDirectUpload(context: Context): Boolean {
        return try {
            val backupFile = prepareFreshBackupFile(context) ?: return false
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val driveIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "IBM Cloud Backup ($BACKUP_FILENAME)")
                putExtra(Intent.EXTRA_TEXT, "Internet Billing Manager Full Database Backup ($BACKUP_FILENAME)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.google.android.apps.docs")
            }

            val resolveInfos = context.packageManager.queryIntentActivities(driveIntent, 0)
            val launchIntent = if (resolveInfos.isNotEmpty()) {
                driveIntent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            } else {
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "IBM Cloud Backup ($BACKUP_FILENAME)")
                    putExtra(Intent.EXTRA_TEXT, "Internet Billing Manager Full Database Backup ($BACKUP_FILENAME)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                Intent.createChooser(genericIntent, "Save to Google Drive").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            context.startActivity(launchIntent)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_DRIVE_BACKUP_TIME, System.currentTimeMillis())
                .putLong(KEY_LAST_DRIVE_BACKUP_SIZE, backupFile.length())
                .apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Validates a backup file from a selected content Uri (Google Drive or file picker),
     * verifying SQLite integrity and table presence (customers, bill_entries).
     */
    fun validateBackupUri(context: Context, sourceUri: Uri): BackupValidationResult {
        return try {
            val tempDir = File(context.cacheDir, "drive_restore")
            if (!tempDir.exists()) tempDir.mkdirs()
            val tempFile = File(tempDir, "temp_restore_${System.currentTimeMillis()}.db")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return BackupValidationResult(isValid = false, errorMessage = "Cannot open selected file stream")

            if (!tempFile.exists() || tempFile.length() < 100L) {
                tempFile.delete()
                return BackupValidationResult(isValid = false, errorMessage = "Selected file is empty or corrupted")
            }

            var custCount = 0
            var billCount = 0
            var payCount = 0
            var integrityPassed = false

            val sqliteDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                tempFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )

            try {
                val checkCursor = sqliteDb.rawQuery("PRAGMA integrity_check", null)
                if (checkCursor.moveToFirst()) {
                    val status = checkCursor.getString(0)
                    integrityPassed = status.equals("ok", ignoreCase = true)
                }
                checkCursor.close()

                if (!integrityPassed) {
                    sqliteDb.close()
                    tempFile.delete()
                    return BackupValidationResult(isValid = false, errorMessage = "SQLite integrity check failed")
                }

                val masterCursor = sqliteDb.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='customers'",
                    null
                )
                val hasCustomers = masterCursor.count > 0
                masterCursor.close()

                if (!hasCustomers) {
                    sqliteDb.close()
                    tempFile.delete()
                    return BackupValidationResult(
                        isValid = false,
                        errorMessage = "Invalid database schema: missing 'customers' table"
                    )
                }

                val cCust = sqliteDb.rawQuery("SELECT COUNT(*) FROM customers", null)
                if (cCust.moveToFirst()) custCount = cCust.getInt(0)
                cCust.close()

                val cBill = sqliteDb.rawQuery("SELECT COUNT(*) FROM bill_entries", null)
                if (cBill.moveToFirst()) billCount = cBill.getInt(0)
                cBill.close()

                try {
                    val cPay = sqliteDb.rawQuery("SELECT COUNT(*) FROM payments", null)
                    if (cPay.moveToFirst()) payCount = cPay.getInt(0)
                    cPay.close()
                } catch (_: Exception) {}

            } finally {
                try { sqliteDb.close() } catch (_: Exception) {}
            }

            BackupValidationResult(
                isValid = true,
                customerCount = custCount,
                billCount = billCount,
                paymentCount = payCount,
                tempFile = tempFile
            )
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                errorMessage = e.localizedMessage ?: "Failed to validate backup file"
            )
        }
    }

    /**
     * Atomically restores the application database from a pre-validated database file.
     * Replaces main database, resets Room connection, restores preferences,
     * and immediately updates the local 24/7 backup so local protection continues seamlessly.
     */
    @Synchronized
    fun restoreFromValidatedFile(context: Context, validatedTempFile: File): Boolean {
        return try {
            if (!validatedTempFile.exists() || validatedTempFile.length() == 0L) return false

            val currentDb = context.getDatabasePath("internet_billing_manager.db")
            val walDb = context.getDatabasePath("internet_billing_manager.db-wal")
            val shmDb = context.getDatabasePath("internet_billing_manager.db-shm")

            // 1. Close active database connections
            try {
                internet.billing.manager.data.local.AppDatabase.resetDatabase()
            } catch (_: Exception) {}

            // 2. Overwrite main DB file
            copyFileAtomically(validatedTempFile, currentDb)
            if (walDb.exists()) walDb.delete()
            if (shmDb.exists()) shmDb.delete()

            // 3. Restore preferences if stored in DB
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

            // 4. Immediately sync local 24/7 auto-backup file with the restored data
            performBackup(context)

            // 5. Delete temp file
            try {
                validatedTempFile.delete()
            } catch (_: Exception) {}

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            if (v is Set<*>) {
                val array = org.json.JSONArray()
                v.forEach { array.put(it) }
                json.put(k, array)
            } else {
                json.put(k, v)
            }
        }
        return json
    }

    private fun restorePrefsFromJson(context: Context, jsonString: String) {
        try {
            val root = JSONObject(jsonString)
            listOf(
                "auto_sms" to "ibm_auto_sms_prefs",
                "app_theme" to "ibm_theme_prefs",
                "language" to "ibm_language_prefs",
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
                            is org.json.JSONArray -> {
                                val set = mutableSetOf<String>()
                                for (i in 0 until v.length()) {
                                    set.add(v.optString(i))
                                }
                                p.putStringSet(k, set)
                            }
                        }
                    }
                    p.apply()
                }
            }
            // Re-initialize live app states so changes take effect immediately
            internet.billing.manager.ui.theme.ThemeManager.initialize(context)
            internet.billing.manager.ui.localization.LanguageManager.initialize(context)
            AutoSmsManager.initialize(context)
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
