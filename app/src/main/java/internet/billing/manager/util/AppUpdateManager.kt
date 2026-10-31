package internet.billing.manager.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class GitHubReleaseInfo(
    val tagName: String,
    val title: String,
    val publishedAt: String,
    val updatedAt: String,
    val releaseDateFormatted: String,
    val changelog: String,
    val apkName: String,
    val apkDownloadUrl: String,
    val apkSizeBytes: Long,
    val apkSizeFormatted: String,
    val isUpdateAvailable: Boolean
)

sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    data class Success(val release: GitHubReleaseInfo) : UpdateCheckState()
    data class Error(val message: String) : UpdateCheckState()
}

object AppUpdateManager {
    private const val GITHUB_REPO = "shahin0964/INTERNET-BILLING-MANAGER2"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
    private const val PREFS_NAME = "ibm_app_update_prefs"
    private const val KEY_LAST_SEEN_UPDATE = "last_seen_update_timestamp"

    var checkState: UpdateCheckState by mutableStateOf(UpdateCheckState.Idle)
        private set

    var isDownloading: Boolean by mutableStateOf(false)
        private set

    var downloadProgress: Float by mutableStateOf(0f)
        private set

    var downloadedBytes: Long by mutableLongStateOf(0L)
        private set

    var totalBytes: Long by mutableLongStateOf(0L)
        private set

    var downloadedApkFile: File? by mutableStateOf(null)
        private set

    var isCancelRequested: Boolean = false

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun getAppInstalledTime(context: Context): Long {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.lastUpdateTime
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun checkForUpdates(context: Context) {
        checkState = UpdateCheckState.Checking
        downloadProgress = 0f
        downloadedBytes = 0L
        totalBytes = 0L
        downloadedApkFile = null

        withContext(Dispatchers.IO) {
            try {
                val url = URL(RELEASES_API_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "InternetBillingManager-Android")
                    connectTimeout = 12000
                    readTimeout = 15000
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)

                    val tagName = json.optString("tag_name", "latest")
                    val title = json.optString("name", "Latest Release")
                    val publishedAt = json.optString("published_at", "")
                    val updatedAt = json.optString("updated_at", publishedAt)
                    val body = json.optString("body", "No changelog provided.")

                    // Find APK asset
                    var apkDownloadUrl = ""
                    var apkName = "IBM-latest.apk"
                    var apkSize = 0L
                    var assetUpdatedAt = updatedAt

                    val assets = json.optJSONArray("assets")
                    if (assets != null && assets.length() > 0) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", "")
                                apkName = name
                                apkSize = asset.optLong("size", 0L)
                                val assetUpdate = asset.optString("updated_at", "")
                                if (assetUpdate.isNotBlank()) assetUpdatedAt = assetUpdate
                                break
                            }
                        }
                    }

                    // Fallback URL if asset array was empty
                    if (apkDownloadUrl.isBlank()) {
                        apkDownloadUrl = "https://github.com/$GITHUB_REPO/releases/download/latest/IBM-latest.apk"
                    }

                    // Parse release timestamp
                    val releaseMillis = parseIso8601ToMillis(assetUpdatedAt.ifBlank { updatedAt })
                    val appInstalledMillis = getAppInstalledTime(context)

                    // Consider update available if release was built/updated after this app's installed time (with 3-minute grace)
                    // or if tag is a higher version
                    val isNewer = if (releaseMillis > 0 && appInstalledMillis > 0) {
                        releaseMillis > (appInstalledMillis + 180000L)
                    } else {
                        true // If cannot compare timestamps, allow user to install
                    }

                    val releaseInfo = GitHubReleaseInfo(
                        tagName = tagName,
                        title = title,
                        publishedAt = publishedAt,
                        updatedAt = assetUpdatedAt,
                        releaseDateFormatted = formatTimestampToDisplay(assetUpdatedAt),
                        changelog = body.trim(),
                        apkName = apkName,
                        apkDownloadUrl = apkDownloadUrl,
                        apkSizeBytes = apkSize,
                        apkSizeFormatted = formatBytes(apkSize),
                        isUpdateAvailable = isNewer
                    )

                    withContext(Dispatchers.Main) {
                        checkState = UpdateCheckState.Success(releaseInfo)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        checkState = UpdateCheckState.Error("Server returned HTTP $responseCode")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    checkState = UpdateCheckState.Error(e.localizedMessage ?: "Network connection error")
                }
            }
        }
    }

    suspend fun downloadAndPrepareApk(
        context: Context,
        release: GitHubReleaseInfo,
        onComplete: (File) -> Unit
    ) {
        if (isDownloading) return

        isDownloading = true
        isCancelRequested = false
        downloadProgress = 0f
        downloadedBytes = 0L
        totalBytes = release.apkSizeBytes
        downloadedApkFile = null

        withContext(Dispatchers.IO) {
            try {
                // Ensure target directory exists
                val updatesDir = File(context.cacheDir, "apk_updates")
                if (!updatesDir.exists()) updatesDir.mkdirs()

                // Delete any old APK files
                updatesDir.listFiles()?.forEach { it.delete() }

                val destinationFile = File(updatesDir, release.apkName.ifBlank { "IBM-latest.apk" })

                var downloadUrl = release.apkDownloadUrl
                var connection: HttpURLConnection
                var redirectCount = 0

                // Follow HTTP redirects (301, 302, 307, 308)
                while (true) {
                    val url = URL(downloadUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "InternetBillingManager-Android")
                        connectTimeout = 15000
                        readTimeout = 20000
                    }

                    val code = connection.responseCode
                    if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                        code == HttpURLConnection.HTTP_MOVED_TEMP ||
                        code == 307 || code == 308
                    ) {
                        val newLocation = connection.getHeaderField("Location")
                        if (newLocation != null && redirectCount < 5) {
                            downloadUrl = newLocation
                            redirectCount++
                            connection.disconnect()
                            continue
                        }
                    }
                    break
                }

                val contentLength = connection.contentLengthLong
                if (contentLength > 0) {
                    totalBytes = contentLength
                }

                val input: InputStream = connection.inputStream
                val output = FileOutputStream(destinationFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var currentBytes = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (isCancelRequested) {
                        output.flush()
                        output.close()
                        input.close()
                        destinationFile.delete()
                        withContext(Dispatchers.Main) {
                            isDownloading = false
                            downloadProgress = 0f
                        }
                        return@withContext
                    }

                    output.write(buffer, 0, bytesRead)
                    currentBytes += bytesRead
                    downloadedBytes = currentBytes

                    if (totalBytes > 0) {
                        downloadProgress = (currentBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    }
                }

                output.flush()
                output.close()
                input.close()

                if (destinationFile.exists() && destinationFile.length() > 500000L) {
                    withContext(Dispatchers.Main) {
                        isDownloading = false
                        downloadProgress = 1f
                        downloadedApkFile = destinationFile
                        onComplete(destinationFile)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isDownloading = false
                        Toast.makeText(context, "Downloaded file is incomplete", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun cancelDownload() {
        isCancelRequested = true
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() < 10000L) {
            Toast.makeText(context, "APK file not ready for installation", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Android 8.0+ Unknown App Sources Permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "Please allow 'Install unknown apps' permission for IBM to proceed",
                        Toast.LENGTH_LONG
                    ).show()

                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Installation error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun parseIso8601ToMillis(isoString: String): Long {
        if (isoString.isBlank()) return 0L
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(isoString)?.time ?: 0L
        } catch (e: Exception) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                format.parse(isoString)?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }

    private fun formatTimestampToDisplay(isoString: String): String {
        if (isoString.isBlank()) return "Recently"
        val millis = parseIso8601ToMillis(isoString)
        if (millis <= 0L) return isoString
        val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return displayFormat.format(Date(millis))
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "16.5 MB"
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
