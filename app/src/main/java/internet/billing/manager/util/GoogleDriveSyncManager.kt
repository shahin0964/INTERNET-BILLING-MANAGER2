package internet.billing.manager.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

object GoogleDriveSyncManager {
    const val PREFS_NAME = "google_drive_sync_preferences"
    const val KEY_GOOGLE_AUTO_BACKUP_ENABLED = "google_auto_backup_enabled"
    const val KEY_LAST_DRIVE_BACKUP_TIME = "last_drive_backup_time"
    const val KEY_LAST_DRIVE_BACKUP_SIZE = "last_drive_backup_size"
    const val KEY_LAST_DRIVE_FILE_ID = "last_drive_file_id"
    const val KEY_LAST_ACCOUNT_EMAIL = "last_account_email"

    const val ALARM_REQUEST_CODE_CLOUD = 9933
    const val ACTION_CLOUD_BACKUP_1130PM = "internet.billing.manager.ACTION_CLOUD_BACKUP_1130PM"
    const val CLIENT_ID = "590405464921-q32pqrivpba6pec5n9ksba8up3jces60.apps.googleusercontent.com"
    private const val DRIVE_FILE_NAME = "IBM_AutoBackup.db"
    private const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"

    data class GoogleAccountInfo(
        val isSignedIn: Boolean,
        val displayName: String = "",
        val email: String = "",
        val photoUrl: String? = null
    )

    data class GoogleDriveBackupStatus(
        val isCloudFilePresent: Boolean,
        val fileId: String? = null,
        val timestampMillis: Long = 0L,
        val formattedDate: String = "Never",
        val sizeBytes: Long = 0L,
        val formattedSize: String = "0 B",
        val isAutoBackupEnabled: Boolean = true,
        val backupTimeText: String = "11:30 PM"
    )

    fun initialize(context: Context) {
        scheduleDaily1130PmDriveBackupAlarm(context)
    }

    /**
     * Builds and returns GoogleSignInClient configured with DRIVE_FILE scope.
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getAccountInfo(context: Context): GoogleAccountInfo {
        val account = getSignedInAccount(context)
        return if (account != null) {
            GoogleAccountInfo(
                isSignedIn = true,
                displayName = account.displayName ?: account.givenName ?: "Google User",
                email = account.email ?: "",
                photoUrl = account.photoUrl?.toString()
            )
        } else {
            GoogleAccountInfo(isSignedIn = false)
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        try {
            getGoogleSignInClient(context).signOut().addOnCompleteListener {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_LAST_ACCOUNT_EMAIL).apply()
                onComplete()
            }
        } catch (_: Exception) {
            onComplete()
        }
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_GOOGLE_AUTO_BACKUP_ENABLED, true)
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GOOGLE_AUTO_BACKUP_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDaily1130PmDriveBackupAlarm(context)
        }
    }

    fun getDriveBackupStatus(context: Context): GoogleDriveBackupStatus {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(KEY_LAST_DRIVE_BACKUP_TIME, 0L)
        val size = prefs.getLong(KEY_LAST_DRIVE_BACKUP_SIZE, 0L)
        val fileId = prefs.getString(KEY_LAST_DRIVE_FILE_ID, null)

        val formattedDate = if (timestamp > 0L) {
            val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            fmt.format(Date(timestamp))
        } else {
            "Never synced yet"
        }

        return GoogleDriveBackupStatus(
            isCloudFilePresent = timestamp > 0L && fileId != null,
            fileId = fileId,
            timestampMillis = timestamp,
            formattedDate = formattedDate,
            sizeBytes = size,
            formattedSize = AutoBackupManager.formatBytes(size),
            isAutoBackupEnabled = isAutoBackupEnabled(context),
            backupTimeText = "11:30 PM (Daily)"
        )
    }

    /**
     * Obtains valid OAuth 2.0 Bearer Token on a background thread.
     */
    suspend fun getAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
        val account = getSignedInAccount(context) ?: return@withContext null
        val androidAccount = account.account ?: return@withContext null
        try {
            GoogleAuthUtil.getToken(context, androidAccount, DRIVE_SCOPE)
        } catch (e: Exception) {
            try {
                // Clear expired/cached token and retry
                val oldToken = GoogleAuthUtil.getToken(context, androidAccount, DRIVE_SCOPE)
                GoogleAuthUtil.clearToken(context, oldToken)
                GoogleAuthUtil.getToken(context, androidAccount, DRIVE_SCOPE)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Locates existing IBM_AutoBackup.db file on Google Drive.
     */
    suspend fun findBackupFileOnDrive(token: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode("name = '$DRIVE_FILE_NAME' and trashed = false", "UTF-8")
            val url = URL("https://www.googleapis.com/drive/v3/files?q=$query&fields=files(id,name,size,modifiedTime)&spaces=drive")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val json = JSONObject(response)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    return@withContext files.getJSONObject(0)
                }
            } else {
                conn.disconnect()
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Uploads the latest database to Google Drive (overwriting previous file if exists).
     * Also updates the local 24/7 backup file on phone storage.
     */
    suspend fun uploadToDrive(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken(context)
                ?: return@withContext Result.failure(Exception("Google Sign-In authorization failed. Please sign in again."))

            // Flush WAL and write fresh local backup
            val backupFile = AutoBackupManager.prepareFreshBackupFile(context)
                ?: return@withContext Result.failure(Exception("Could not prepare local database snapshot."))

            val fileBytes = backupFile.readBytes()
            val existingFile = findBackupFileOnDrive(token)

            if (existingFile != null) {
                // PATCH existing file content using standard X-HTTP-Method-Override
                val fileId = existingFile.getString("id")
                val uploadUrl = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
                val conn = uploadUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/octet-stream")
                conn.setRequestProperty("Content-Length", fileBytes.size.toString())
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 30000

                conn.outputStream.use { os ->
                    os.write(fileBytes)
                    os.flush()
                }

                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) {
                    saveDriveBackupMetadata(context, fileId, System.currentTimeMillis(), fileBytes.size.toLong())
                    Result.success("Google Drive backup updated successfully (Overwrote $DRIVE_FILE_NAME).")
                } else {
                    Result.failure(Exception("Google Drive upload failed with HTTP status $code"))
                }
            } else {
                // Create new file via Multipart POST
                val boundary = "==IBM_BOUNDARY_${System.currentTimeMillis()}=="
                val uploadUrl = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                val conn = uploadUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 30000

                val metadata = JSONObject().apply {
                    put("name", DRIVE_FILE_NAME)
                    put("description", "Internet Billing Manager 24/7 Cloud Auto-Backup Snapshot")
                }.toString()

                conn.outputStream.use { os ->
                    // Part 1: Metadata
                    os.write("--$boundary\r\n".toByteArray())
                    os.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
                    os.write(metadata.toByteArray())
                    os.write("\r\n".toByteArray())

                    // Part 2: Media
                    os.write("--$boundary\r\n".toByteArray())
                    os.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
                    os.write(fileBytes)
                    os.write("\r\n".toByteArray())
                    os.write("--$boundary--\r\n".toByteArray())
                    os.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resStr = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    val resJson = JSONObject(resStr)
                    val fileId = resJson.getString("id")
                    saveDriveBackupMetadata(context, fileId, System.currentTimeMillis(), fileBytes.size.toLong())
                    Result.success("Google Drive backup created successfully ($DRIVE_FILE_NAME).")
                } else {
                    conn.disconnect()
                    Result.failure(Exception("Google Drive create failed with HTTP status $code"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads IBM_AutoBackup.db from Google Drive and verifies SQLite integrity & record counts.
     */
    suspend fun downloadAndValidateFromDrive(context: Context): AutoBackupManager.BackupValidationResult = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken(context)
                ?: return@withContext AutoBackupManager.BackupValidationResult(
                    isValid = false,
                    errorMessage = "Please sign in with Google to access your cloud backup."
                )

            val fileObj = findBackupFileOnDrive(token)
                ?: return@withContext AutoBackupManager.BackupValidationResult(
                    isValid = false,
                    errorMessage = "No IBM_AutoBackup.db file found in your Google Drive."
                )

            val fileId = fileObj.getString("id")
            val downloadUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            val conn = downloadUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            if (conn.responseCode !in 200..299) {
                val code = conn.responseCode
                conn.disconnect()
                return@withContext AutoBackupManager.BackupValidationResult(
                    isValid = false,
                    errorMessage = "Failed to download backup from Drive (HTTP $code)"
                )
            }

            val tempDir = File(context.cacheDir, "google_drive_download")
            if (!tempDir.exists()) tempDir.mkdirs()
            val tempFile = File(tempDir, "temp_drive_restore_${System.currentTimeMillis()}.db")

            conn.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            conn.disconnect()

            if (!tempFile.exists() || tempFile.length() < 100L) {
                tempFile.delete()
                return@withContext AutoBackupManager.BackupValidationResult(
                    isValid = false,
                    errorMessage = "Downloaded Drive backup file is empty or corrupted."
                )
            }

            // Validate SQLite integrity
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
                    return@withContext AutoBackupManager.BackupValidationResult(
                        isValid = false,
                        errorMessage = "Drive SQLite database integrity check failed."
                    )
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
                    return@withContext AutoBackupManager.BackupValidationResult(
                        isValid = false,
                        errorMessage = "Invalid database schema (missing 'customers' table)."
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

            AutoBackupManager.BackupValidationResult(
                isValid = true,
                customerCount = custCount,
                billCount = billCount,
                paymentCount = payCount,
                tempFile = tempFile
            )
        } catch (e: Exception) {
            AutoBackupManager.BackupValidationResult(
                isValid = false,
                errorMessage = e.localizedMessage ?: "Drive restore validation error"
            )
        }
    }

    /**
     * Schedules the daily 11:30 PM (23:30) Cloud Auto-Backup Alarm.
     */
    fun scheduleDaily1130PmDriveBackupAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AutoBackupReceiver::class.java).apply {
                action = ACTION_CLOUD_BACKUP_1130PM
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE_CLOUD, intent, flags)

            // Target 11:30 PM (23:30) every day
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
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

    /**
     * Called when the 11:30 PM alarm fires in AutoBackupReceiver.
     */
    suspend fun checkAndRunScheduled1130PmBackup(context: Context) = withContext(Dispatchers.IO) {
        try {
            if (getSignedInAccount(context) != null && isAutoBackupEnabled(context)) {
                uploadToDrive(context)
            }
        } finally {
            scheduleDaily1130PmDriveBackupAlarm(context)
        }
    }

    private fun saveDriveBackupMetadata(context: Context, fileId: String, timestamp: Long, size: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_DRIVE_FILE_ID, fileId)
            .putLong(KEY_LAST_DRIVE_BACKUP_TIME, timestamp)
            .putLong(KEY_LAST_DRIVE_BACKUP_SIZE, size)
            .apply()
    }
}
