package internet.billing.manager.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.AppUpdateManager
import internet.billing.manager.util.UpdateCheckState
import kotlinx.coroutines.launch

@Composable
fun AppUpdateDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Automatically check for updates when dialog opens
    LaunchedEffect(Unit) {
        if (AppUpdateManager.checkState !is UpdateCheckState.Success) {
            AppUpdateManager.checkForUpdates(context)
        }
    }

    Dialog(
        onDismissRequest = {
            if (!AppUpdateManager.isDownloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.86f)
                    .padding(vertical = 10.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appStrings.appUpdateTitle,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "GitHub Releases • OTA Update",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!AppUpdateManager.isDownloading) {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Version info summary badge
                    val currentVersion = remember { AppUpdateManager.getCurrentVersionName(context) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgCardLight)
                            .border(1.dp, CardBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = appStrings.currentVersion,
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "v$currentVersion",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Installed",
                                color = AccentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = AppUpdateManager.checkState) {
                        is UpdateCheckState.Checking -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = AccentCyan,
                                        modifier = Modifier.size(34.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = appStrings.checkingForUpdate,
                                        color = TextSecondary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        is UpdateCheckState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentRed.copy(alpha = 0.12f))
                                    .border(1.dp, AccentRed.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = AccentRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = appStrings.updateError,
                                            color = AccentRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = state.message,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/shahin0964/INTERNET-BILLING-MANAGER2/releases")
                                        )
                                        context.startActivity(browserIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = appStrings.openInBrowser,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            AppUpdateManager.checkForUpdates(context)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = appStrings.checkAgain,
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        is UpdateCheckState.Success -> {
                            val release = state.release

                            // Update Status Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (release.isUpdateAvailable) AccentGreen.copy(alpha = 0.15f)
                                        else BgCardLight
                                    )
                                    .border(
                                        1.dp,
                                        if (release.isUpdateAvailable) AccentGreen.copy(alpha = 0.45f) else CardBorderSubtle,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (release.isUpdateAvailable) Icons.Default.NewReleases else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (release.isUpdateAvailable) AccentGreen else AccentCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (release.isUpdateAvailable) appStrings.updateAvailable else appStrings.appUpToDate,
                                                color = if (release.isUpdateAvailable) AccentGreen else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = release.apkSizeFormatted,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${appStrings.latestVersion}: ${release.displayVersion}",
                                            color = if (release.isUpdateAvailable) AccentGreen else TextSecondary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (release.isUpdateAvailable) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentGreen)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "NEW",
                                                    color = BgDark,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "${appStrings.releaseDateLabel}: ${release.releaseDateFormatted}",
                                        color = TextMuted,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Release Notes Section
                            Text(
                                text = appStrings.releaseNotes,
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BgCardLight)
                                    .border(1.dp, CardBorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                val changelogCleaned = remember(release.changelog) {
                                    release.changelog
                                        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
                                        .replace("`", "")
                                        .trim()
                                }
                                Text(
                                    text = changelogCleaned.ifBlank { "Latest release build automatically generated and published via GitHub Actions." },
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Downloading Progress Card
                            if (AppUpdateManager.isDownloading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BgCardLight)
                                        .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = appStrings.downloading,
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${(AppUpdateManager.downloadProgress * 100).toInt()}%",
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { AppUpdateManager.downloadProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = AccentCyan,
                                            trackColor = CardBorder
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${AppUpdateManager.formatBytes(AppUpdateManager.downloadedBytes)} / ${AppUpdateManager.formatBytes(AppUpdateManager.totalBytes)}",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        AppUpdateManager.cancelDownload()
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Cancel",
                                                    color = AccentRed,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Download Completed Card
                            if (AppUpdateManager.downloadedApkFile != null && !AppUpdateManager.isDownloading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AccentGreen.copy(alpha = 0.15f))
                                        .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AccentGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = appStrings.downloadComplete,
                                            color = AccentGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                when (val state = AppUpdateManager.checkState) {
                    is UpdateCheckState.Success -> {
                        val release = state.release

                        if (AppUpdateManager.downloadedApkFile != null && !AppUpdateManager.isDownloading) {
                            // Ready to install
                            Button(
                                onClick = {
                                    AppUpdateManager.downloadedApkFile?.let { file ->
                                        AppUpdateManager.installApk(context, file)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.InstallMobile,
                                    contentDescription = null,
                                    tint = BgDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = appStrings.installNow,
                                    color = BgDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else if (!AppUpdateManager.isDownloading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            AppUpdateManager.checkForUpdates(context)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = appStrings.checkAgain,
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            AppUpdateManager.downloadAndPrepareApk(context, release) { apkFile ->
                                                AppUpdateManager.installApk(context, apkFile)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.35f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (release.isUpdateAvailable) AccentGreen else AccentCyan
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = BgDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (release.isUpdateAvailable) appStrings.downloadUpdate else "Re-download APK",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BgCardLight),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = appStrings.close,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}


