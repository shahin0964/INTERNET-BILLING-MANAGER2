package internet.billing.manager.ui.components

import android.content.Intent
import android.net.Uri
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
import internet.billing.manager.MainActivity
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.AutoBackupManager
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
    var driveBackupInfo by remember { mutableStateOf(AutoBackupManager.getDriveBackupInfo(context)) }

    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isSavingToDrive by remember { mutableStateOf(false) }
    var isValidatingDriveFile by remember { mutableStateOf(false) }

    var showLocalRestoreConfirm by remember { mutableStateOf(false) }
    var showDriveRestoreConfirm by remember { mutableStateOf(false) }
    var driveValidationResult by remember { mutableStateOf<AutoBackupManager.BackupValidationResult?>(null) }

    fun refreshAllInfo() {
        backupInfo = AutoBackupManager.getBackupInfo(context)
        driveBackupInfo = AutoBackupManager.getDriveBackupInfo(context)
    }

    // Google Drive SAF File Saver
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            isSavingToDrive = true
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    AutoBackupManager.saveBackupToUri(context, uri)
                }
                refreshAllInfo()
                isSavingToDrive = false
                if (success) {
                    Toast.makeText(
                        context,
                        if (isBangla) "গুগল ড্রাইভে ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে!" else "Backup saved to Google Drive successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        if (isBangla) "গুগল ড্রাইভে সেভ ব্যর্থ হয়েছে!" else "Failed to save backup to Google Drive!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Google Drive SAF File Picker for Restore
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isValidatingDriveFile = true
            coroutineScope.launch {
                val validation = withContext(Dispatchers.IO) {
                    AutoBackupManager.validateBackupUri(context, uri)
                }
                isValidatingDriveFile = false
                if (validation.isValid) {
                    driveValidationResult = validation
                    showDriveRestoreConfirm = true
                } else {
                    Toast.makeText(
                        context,
                        if (isBangla) "ব্যাকআপ ফাইল সঠিক নয়: ${validation.errorMessage ?: ""}"
                        else "Invalid backup file: ${validation.errorMessage ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val isBusy = isBackingUp || isRestoring || isSavingToDrive || isValidatingDriveFile

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
                                    if (isBangla) "গুগল ড্রাইভ ক্লাউড ব্যাকআপ" else "Google Drive Cloud Backup"
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
                                text = if (isBangla) "২৪/৭ অটো-ব্যাকআপ" else "24/7 Local Backup",
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

                        // Action 1: Backup Now
                        Button(
                            onClick = {
                                isBackingUp = true
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        AutoBackupManager.performBackup(context)
                                    }
                                    refreshAllInfo()
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

                        // Action 2: Restore from Local File
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

                        // Action 3: Share / Export File
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
                        // TAB 1: GOOGLE DRIVE CLOUD BACKUP & RESTORE
                        // -------------------------------------------------------------
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCyan.copy(alpha = 0.08f))
                                .border(1.dp, AccentCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AccentCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "গুগল ড্রাইভ ক্লাউড • স্বতন্ত্র ও সুরক্ষিত" else "Google Drive Cloud • Independent & Secure",
                                    color = AccentCyan,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 24/7 Protection Guarantee Pill
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
                                    text = if (isBangla) "ফোনের ২৪/৭ অটো-ব্যাকআপ সর্বদা ব্যাকগ্রাউন্ডে চালু ও সুরক্ষিত থাকবে।" else "24/7 phone auto-backup remains fully protected & uninterrupted.",
                                    color = AccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isBangla)
                                "আপনার ইন্টারনেট বিলিং ম্যানেজারের সম্পূর্ণ ডাটাবেজ গুগল ড্রাইভে ক্লাউড ফাইল হিসেবে সুরক্ষিত রাখুন। ফোন পরিবর্তন করলে বা ডাটা হারালে সহজেই গুগল ড্রাইভ থেকে এক ক্লিকে সম্পূর্ণ হিসাব রিস্টোর করতে পারবেন।"
                            else
                                "Save a secure cloud copy in your Google Drive. You can restore your complete business data anytime on any phone directly by picking your backup file from Google Drive.",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Drive Info Card
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
                                        text = if (isBangla) "গুগল ড্রাইভ ক্লাউড বিবরণ" else "GOOGLE DRIVE CLOUD DETAILS",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = if (driveBackupInfo.exists) "SYNCED ✓" else "NOT SYNCED",
                                        color = if (driveBackupInfo.exists) AccentCyan else AccentOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                InfoRow(
                                    label = if (isBangla) "ক্লাউড সার্ভিস" else "Cloud Service",
                                    value = "Google Drive",
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
                                    label = if (isBangla) "সর্বশেষ ক্লাউড ব্যাকআপ" else "Last Cloud Backup",
                                    value = driveBackupInfo.formattedDate,
                                    valueColor = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "ক্লাউড ফাইল সাইজ" else "Cloud File Size",
                                    value = driveBackupInfo.formattedSize,
                                    valueColor = AccentOrange
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                InfoRow(
                                    label = if (isBangla) "লোকাল ২৪/৭ প্রটেকশন" else "Local 24/7 Protection",
                                    value = if (isBangla) "সক্রিয় ও সুরক্ষিত ✓" else "Active & Protected ✓",
                                    valueColor = AccentGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button 1: Save to Google Drive via SAF
                        Button(
                            onClick = {
                                createDocumentLauncher.launch("IBM_AutoBackup.db")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            if (isSavingToDrive) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = BgDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "গুগল ড্রাইভে সেভ হচ্ছে..." else "Saving to Drive...",
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
                                    text = if (isBangla) "গুগল ড্রাইভে সেভ করুন (Save to Drive)" else "Save / Upload to Google Drive",
                                    color = BgDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: Restore from Google Drive
                        OutlinedButton(
                            onClick = {
                                openDocumentLauncher.launch(
                                    arrayOf("*/*", "application/octet-stream", "application/x-sqlite3")
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            if (isValidatingDriveFile || isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = AccentGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isValidatingDriveFile) {
                                        if (isBangla) "ফাইল যাচাই করা হচ্ছে..." else "Verifying file..."
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 3: Send directly to Google Drive App
                        OutlinedButton(
                            onClick = {
                                val ok = AutoBackupManager.openGoogleDriveDirectUpload(context)
                                refreshAllInfo()
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "গুগল ড্রাইভ অ্যাপ চালু করা যায়নি" else "Could not open Google Drive app",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DriveFolderUpload,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "সরাসরি গুগল ড্রাইভ অ্যাপে আপলোড" else "Open in Google Drive App",
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            )
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
                        text = if (isBangla) "গুগল ড্রাইভ ব্যাকআপ ফাইল সফলভাবে যাচাই করা হয়েছে। এই ফাইলে নিম্নলিখিত রেকর্ড পাওয়া গেছে:"
                        else "Google Drive backup snapshot verified successfully. Found following records in file:",
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
