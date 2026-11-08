package internet.billing.manager.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import internet.billing.manager.MainActivity
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.AutoBackupManager
import internet.billing.manager.util.GoogleDriveSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBangla = LanguageManager.isBengali()

    // 0 = 24/7 Local Auto-Backup, 1 = Google Drive Cloud
    var selectedTab by remember { mutableStateOf(0) }

    var backupInfo by remember { mutableStateOf(AutoBackupManager.getBackupInfo(context)) }
    var accountInfo by remember { mutableStateOf(GoogleDriveSyncManager.getAccountInfo(context)) }
    var driveStatus by remember { mutableStateOf(GoogleDriveSyncManager.getDriveBackupStatus(context)) }
    var isAutoBackup1130Pm by remember { mutableStateOf(GoogleDriveSyncManager.isAutoBackupEnabled(context)) }

    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isSyncingDrive by remember { mutableStateOf(false) }
    var isDownloadingDrive by remember { mutableStateOf(false) }

    var showLocalRestoreConfirm by remember { mutableStateOf(false) }
    var showDriveRestoreConfirm by remember { mutableStateOf(false) }
    var driveValidationResult by remember { mutableStateOf<AutoBackupManager.BackupValidationResult?>(null) }

    fun refreshAll() {
        backupInfo = AutoBackupManager.getBackupInfo(context)
        accountInfo = GoogleDriveSyncManager.getAccountInfo(context)
        driveStatus = GoogleDriveSyncManager.getDriveBackupStatus(context)
        isAutoBackup1130Pm = GoogleDriveSyncManager.isAutoBackupEnabled(context)
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            accountInfo = GoogleDriveSyncManager.getAccountInfo(context)
            driveStatus = GoogleDriveSyncManager.getDriveBackupStatus(context)
            Toast.makeText(
                context,
                if (isBangla) "গুগল সাইন-ইন সফল: ${account.email}" else "Google Sign-In successful: ${account.email}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                10 -> if (isBangla) "গুগল ক্লাউড কনসোলে SHA-1/SHA-256 ফিঙ্গারপ্রিন্ট দেওয়া আবশ্যক (Error 10)।"
                else "Developer error (Code 10): Ensure SHA-1/SHA-256 fingerprint is registered in Google Cloud Console."
                12500 -> if (isBangla) "গুগল সাইন-ইন বাতিল বা সংযোগ ব্যর্থ হয়েছে (Error 12500)।"
                else "Google Sign-In failed or was cancelled (Code 12500)."
                else -> "Google Sign-In error (${e.statusCode}): ${e.localizedMessage ?: ""}"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Sign-In error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val isBusy = isBackingUp || isRestoring || isSyncingDrive || isDownloadingDrive

    Dialog(
        onDismissRequest = {
            if (!isBusy) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
            contentColor = TextPrimary,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
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
                                .background(
                                    if (selectedTab == 0) AccentGreen.copy(alpha = 0.15f)
                                    else AccentCyan.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.CloudSync else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (selectedTab == 0) AccentGreen else AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isBangla) "ডাটা ব্যাকআপ ও রিস্টোর" else "Backup & Restore",
                                color = TextPrimary,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedTab == 0) {
                                    if (isBangla) "সিঙ্গেল ফাইল অটো-ওভাররাইট ব্যাকআপ" else "Single-File Auto-Overwrite Backup"
                                } else {
                                    if (isBangla) "গুগল ড্রাইভ ক্লাউড ব্যাকআপ (রাত ১১:৩০)" else "Google Drive Cloud Backup (11:30 PM)"
                                },
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isBusy,
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

                // Segmented Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgCardLight)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    // Tab 0: 24/7 Local Auto-Backup
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) AccentGreen else Color.Transparent)
                            .clickable(enabled = !isBusy) { selectedTab = 0 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (selectedTab == 0) BgDark else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "২৪/৭ লোকাল ব্যাকআপ" else "24/7 Local Backup",
                                color = if (selectedTab == 0) BgDark else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Tab 1: Google Drive Cloud
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) AccentCyan else Color.Transparent)
                            .clickable(enabled = !isBusy) { selectedTab = 1 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (selectedTab == 1) BgDark else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "গুগল ড্রাইভ ক্লাউড" else "Google Drive Cloud",
                                color = if (selectedTab == 1) BgDark else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        // -------------------------------------------------------------
                        // TAB 0: 24/7 LOCAL AUTO-BACKUP
                        // -------------------------------------------------------------
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGreen.copy(alpha = 0.08f))
                                .border(1.dp, AccentGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "অটো-ব্যাকআপ সক্রিয় • ২৪/৭ সুরক্ষিত" else "Auto-Backup Active • 24/7 Protected",
                                    color = AccentGreen,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isBangla)
                                "প্রতিটি এডিট বা পরিবর্তন হওয়ার সাথে সাথে স্বয়ংক্রিয়ভাবে একটি মাত্র ফাইলে (IBM_AutoBackup.db) ডাটা ওভাররাইট হয়ে ব্যাকআপ জমা হয়। এছাড়া প্রতিদিন রাত ২:৩০ মিনিটে ব্যাকগ্রাউন্ডে নিয়মিত স্ন্যাপশট নেওয়া হয়।"
                            else
                                "Whenever you edit or update any data, the entire app database is automatically backed up and overwritten to a single secure file (IBM_AutoBackup.db). It also runs daily in the background at 02:30 AM.",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Backup Details Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BgCardLight)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBangla) "লোকাল ব্যাকআপ বিবরণ" else "LOCAL BACKUP DETAILS",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = if (backupInfo.exists) "READY ✓" else "PENDING",
                                        color = if (backupInfo.exists) AccentGreen else AccentOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                InfoRow(
                                    label = if (isBangla) "ফাইল নাম" else "File Name",
                                    value = "IBM_AutoBackup.db",
                                    valueColor = AccentCyan
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "লোকেশন" else "Location",
                                    value = "Download/IBM_Backup/",
                                    valueColor = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "সর্বশেষ ব্যাকআপ" else "Last Backup",
                                    value = backupInfo.formattedDate,
                                    valueColor = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "ফাইল সাইজ" else "File Size",
                                    value = backupInfo.formattedSize,
                                    valueColor = AccentOrange
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "সংরক্ষিত রেকর্ড" else "Records Saved",
                                    value = "${backupInfo.customerCount} Customers • ${backupInfo.billCount} Bills",
                                    valueColor = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button 1: Backup Now
                        Button(
                            onClick = {
                                isBackingUp = true
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        AutoBackupManager.performBackup(context)
                                    }
                                    refreshAll()
                                    isBackingUp = false
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "ব্যাকআপ সফল হয়েছে! IBM_AutoBackup.db আপডেট করা হয়েছে।" else "Backup successful! Overwrote IBM_AutoBackup.db",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            if (isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = BgDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "ব্যাকআপ নেওয়া হচ্ছে..." else "Backing up...",
                                    color = BgDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = BgDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "এখনই ব্যাকআপ আপডেট করুন (Backup Now)" else "Backup Now (Overwrite File)",
                                    color = BgDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: Restore Data
                        OutlinedButton(
                            onClick = { showLocalRestoreConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = backupInfo.exists && !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = AccentCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "রিস্টোর করা হচ্ছে..." else "Restoring...",
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SettingsBackupRestore,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "ডাটা রিস্টোর করুন (Restore Backup)" else "Restore Data from Backup",
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 3: Share
                        OutlinedButton(
                            onClick = {
                                val ok = AutoBackupManager.shareBackup(context)
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "ব্যাকআপ ফাইল পাওয়া যায়নি" else "Backup file not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(10.dp),
                            enabled = backupInfo.exists && !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "ব্যাকআপ ফাইল শেয়ার করুন" else "Share Backup File",
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            )
                        }

                    } else {
                        // -------------------------------------------------------------
                        // TAB 1: GOOGLE DRIVE CLOUD (GOOGLE SIGN-IN & 11:30 PM AUTO-SYNC)
                        // -------------------------------------------------------------
                        if (!accountInfo.isSignedIn) {
                            // SIGN-IN REQUIRED VIEW
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentCyan.copy(alpha = 0.08f))
                                    .border(1.dp, AccentCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "গুগল ড্রাইভ ক্লাউড ব্যাকআপ • সাইন-ইন প্রয়োজন" else "Google Drive Cloud • Sign-In Required",
                                        color = AccentCyan,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (isBangla)
                                    "আপনার গুগল অ্যাকাউন্ট দিয়ে একবার সাইন-ইন করে রাখলে প্রতিদিন রাত ১১:৩০ মিনিটে সম্পূর্ণ ডাটাবেজ (IBM_AutoBackup.db) স্বয়ংক্রিয়ভাবে গুগল ড্রাইভে ব্যাকআপ ওভাররাইট হয়ে যাবে। ফোন হারালে বা পরিবর্তন করলেও যেকোনো ডিভাইস থেকে এক ক্লিকে রিস্টোর করতে পারবেন।"
                                else
                                    "Sign in with your Google Account once to enable automatic daily cloud backups at 11:30 PM directly to your Google Drive. Overwrites a single file (IBM_AutoBackup.db) automatically.",
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Local Protection Guarantee Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentGreen.copy(alpha = 0.07f))
                                    .border(1.dp, AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "ফোনের ২৪/৭ লোকাল অটো-ব্যাকআপ সর্বদা ব্যাকগ্রাউন্ডে সক্রিয় ও সুরক্ষিত থাকবে।" else "24/7 phone local auto-backup remains fully protected & active.",
                                        color = AccentGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Button: Sign In with Google
                            Button(
                                onClick = {
                                    val client = GoogleDriveSyncManager.getGoogleSignInClient(context)
                                    googleSignInLauncher.launch(client.signInIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = BgDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "গুগল দিয়ে সাইন-ইন করুন (Sign in with Google)" else "Sign in with Google",
                                color = BgDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        } else {
                            // SIGNED IN VIEW
                            // Connected Account Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgCardLight)
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(AccentCyan.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = accountInfo.displayName.take(1).uppercase(),
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = accountInfo.displayName,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(AccentGreen.copy(alpha = 0.15f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "CONNECTED ✓",
                                                        color = AccentGreen,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = accountInfo.email,
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            GoogleDriveSyncManager.signOut(context) {
                                                refreshAll()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isBangla) "সাইন আউট" else "Sign Out",
                                            color = AccentRed,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 11:30 PM Auto-Backup Switch Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentCyan.copy(alpha = 0.06f))
                                    .border(1.dp, AccentCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AccentCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = AccentCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = if (isBangla) "রাত ১১:৩০ মিনিটে অটো-ব্যাকআপ" else "Daily 11:30 PM Auto-Backup",
                                                color = TextPrimary,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isBangla) "প্রতিদিন রাত ১১:৩০ মিনিটে ব্যাকগ্রাউন্ডে স্বয়ংক্রিয় ক্লাউড সিঙ্ক" else "Silent background sync to Google Drive every night at 11:30 PM",
                                                color = TextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isAutoBackup1130Pm,
                                        onCheckedChange = { checked ->
                                            isAutoBackup1130Pm = checked
                                            GoogleDriveSyncManager.setAutoBackupEnabled(context, checked)
                                            Toast.makeText(
                                                context,
                                                if (checked) {
                                                    if (isBangla) "রাত ১১:৩০ মিনিটের ক্লাউড অটো-ব্যাকআপ সক্রিয় করা হয়েছে!" else "11:30 PM Cloud Auto-Backup enabled!"
                                                } else {
                                                    if (isBangla) "রাত ১১:৩০ মিনিটের ক্লাউড অটো-ব্যাকআপ নিষ্ক্রিয় করা হয়েছে।" else "11:30 PM Cloud Auto-Backup disabled."
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BgDark,
                                            checkedTrackColor = AccentCyan,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = BgCardLight
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Local Protection Guarantee Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentGreen.copy(alpha = 0.07f))
                                    .border(1.dp, AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "ফোনের ২৪/৭ লোকাল অটো-ব্যাকআপ সর্বদা ব্যাকগ্রাউন্ডে সক্রিয় ও সুরক্ষিত থাকবে।" else "24/7 phone local auto-backup remains fully protected & active.",
                                        color = AccentGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cloud Details Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgCardLight)
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBangla) "গুগল ড্রাইভ বিবরণ" else "GOOGLE DRIVE DETAILS",
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = if (driveStatus.isCloudFilePresent) "SYNCED ✓" else "NOT SYNCED",
                                            color = if (driveStatus.isCloudFilePresent) AccentCyan else AccentOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    InfoRow(
                                        label = if (isBangla) "ক্লাউড সার্ভিস" else "Cloud Service",
                                        value = "Google Drive REST API",
                                        valueColor = AccentCyan
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    InfoRow(
                                        label = if (isBangla) "টার্গেট ফাইল" else "Target File",
                                        value = "IBM_AutoBackup.db",
                                        valueColor = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    InfoRow(
                                        label = if (isBangla) "সর্বশেষ ক্লাউড ব্যাকআপ" else "Last Cloud Sync",
                                        value = driveStatus.formattedDate,
                                        valueColor = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    InfoRow(
                                        label = if (isBangla) "ক্লাউড ফাইল সাইজ" else "Cloud File Size",
                                        value = driveStatus.formattedSize,
                                        valueColor = AccentOrange
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    InfoRow(
                                        label = if (isBangla) "অটো-ব্যাকআপ শিডিউল" else "Auto-Backup Schedule",
                                        value = driveStatus.backupTimeText,
                                        valueColor = AccentGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action 1: Backup Now to Google Drive
                            Button(
                                onClick = {
                                    isSyncingDrive = true
                                    coroutineScope.launch {
                                        val result = GoogleDriveSyncManager.uploadToDrive(context)
                                        refreshAll()
                                        isSyncingDrive = false
                                        result.fold(
                                            onSuccess = { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { err ->
                                                Toast.makeText(context, "Upload failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                if (isSyncingDrive) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = BgDark,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "গুগল ড্রাইভে আপলোড হচ্ছে..." else "Uploading to Google Drive...",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = BgDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "এখনই গুগল ড্রাইভে ব্যাকআপ নিন (Backup Now)" else "Backup to Google Drive Now",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action 2: Restore from Google Drive
                            OutlinedButton(
                                onClick = {
                                    isDownloadingDrive = true
                                    coroutineScope.launch {
                                        val validation = GoogleDriveSyncManager.downloadAndValidateFromDrive(context)
                                        isDownloadingDrive = false
                                        if (validation.isValid) {
                                            driveValidationResult = validation
                                            showDriveRestoreConfirm = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isBangla) "ড্রাইভ ব্যাকআপ যাচাই ব্যর্থ: ${validation.errorMessage ?: ""}"
                                                else "Drive validation failed: ${validation.errorMessage ?: ""}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                if (isDownloadingDrive || isRestoring) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AccentGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isDownloadingDrive) {
                                            if (isBangla) "ড্রাইভ থেকে ডাউনলোড ও যাচাই হচ্ছে..." else "Downloading from Drive..."
                                        } else {
                                            if (isBangla) "রিস্টোর করা হচ্ছে..." else "Restoring..."
                                        },
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBangla) "গুগল ড্রাইভ থেকে রিস্টোর করুন" else "Restore from Google Drive",
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert for Local Restore
    if (showLocalRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showLocalRestoreConfirm = false },
            title = {
                Text(
                    text = if (isBangla) "লোকাল ডাটা রিস্টোর নিশ্চিতকরণ" else "Confirm Local Restore",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনি কি নিশ্চিত যে লোকাল সংরক্ষিত ব্যাকআপ থেকে ডাটা ফিরিয়ে আনতে চান? বর্তমান ডাটাবেজ ব্যাকআপ ফাইল (IBM_AutoBackup.db) দ্বারা প্রতিস্থাপিত হবে।"
                    else
                        "Are you sure you want to restore? Current app data will be replaced by the backup snapshot in IBM_AutoBackup.db.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocalRestoreConfirm = false
                        isRestoring = true
                        coroutineScope.launch {
                            val success = withContext(Dispatchers.IO) {
                                AutoBackupManager.restoreBackup(context)
                            }
                            isRestoring = false
                            if (success) {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "ডাটা সফলভাবে রিস্টোর হয়েছে! অ্যাপ রিলোড হচ্ছে..." else "Data restored successfully! Reloading...",
                                    Toast.LENGTH_LONG
                                ).show()
                                onDismiss()
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "রিস্টোর ব্যর্থ হয়েছে! ব্যাকআপ ফাইলটি ক্ষতিগ্রস্ত হতে পারে।" else "Restore failed! Backup file might be damaged.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(
                        text = if (isBangla) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore",
                        color = BgDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocalRestoreConfirm = false }) {
                    Text(
                        text = if (isBangla) "বাতিল" else "Cancel",
                        color = TextMuted
                    )
                }
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // Confirmation Alert for Google Drive Restore
    if (showDriveRestoreConfirm && driveValidationResult != null) {
        val result = driveValidationResult!!
        AlertDialog(
            onDismissRequest = {
                result.tempFile?.delete()
                showDriveRestoreConfirm = false
                driveValidationResult = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "গুগল ড্রাইভ ব্যাকআপ রিস্টোর" else "Restore from Google Drive",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBangla) "গুগল ড্রাইভ ব্যাকআপ ফাইল সফলভাবে ডাউনলোড ও যাচাই করা হয়েছে। এই ফাইলে নিম্নলিখিত রেকর্ড পাওয়া গেছে:"
                        else "Google Drive backup snapshot downloaded and verified successfully. Found following records in file:",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgCardLight)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            InfoRow(
                                label = if (isBangla) "কাস্টমার সংখ্যা" else "Customers",
                                value = "${result.customerCount} জন",
                                valueColor = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            InfoRow(
                                label = if (isBangla) "বিল এন্ট্রি সংখ্যা" else "Bills",
                                value = "${result.billCount} টি",
                                valueColor = AccentOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            InfoRow(
                                label = if (isBangla) "পেমেন্ট লেনদেন" else "Payments",
                                value = "${result.paymentCount} টি",
                                valueColor = AccentGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBangla) "সতর্কতা: রিস্টোর করলে বর্তমান অ্যাপের ডাটা এই ব্যাকআপ ডাটা দিয়ে প্রতিস্থাপিত হবে এবং ফোনের ২৪/৭ লোকাল ব্যাকআপও স্বয়ংক্রিয়ভাবে আপডেট হয়ে সুরক্ষিত থাকবে।"
                        else "Warning: Restoring will overwrite current app data with this backup snapshot, and the phone's 24/7 auto-backup will also immediately sync.",
                        color = AccentOrange,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val temp = result.tempFile
                        showDriveRestoreConfirm = false
                        driveValidationResult = null
                        if (temp != null && temp.exists()) {
                            isRestoring = true
                            coroutineScope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    AutoBackupManager.restoreFromValidatedFile(context, temp)
                                }
                                isRestoring = false
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "গুগল ড্রাইভ ডাটা সফলভাবে রিস্টোর হয়েছে! অ্যাপ রিলোড হচ্ছে..."
                                        else "Google Drive backup restored successfully! Reloading...",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismiss()
                                    val intent = Intent(context, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "রিস্টোর ব্যর্থ হয়েছে! ডাটাবেজ ফরম্যাট সঠিক নয়।"
                                        else "Restore failed! Invalid database file.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text(
                        text = if (isBangla) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore Now",
                        color = BgDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        result.tempFile?.delete()
                        showDriveRestoreConfirm = false
                        driveValidationResult = null
                    }
                ) {
                    Text(
                        text = if (isBangla) "বাতিল" else "Cancel",
                        color = TextMuted
                    )
                }
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
