package internet.billing.manager.ui.components

import android.content.Intent
import android.widget.Toast
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

    var backupInfo by remember { mutableStateOf(AutoBackupManager.getBackupInfo(context)) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    fun refreshInfo() {
        backupInfo = AutoBackupManager.getBackupInfo(context)
    }

    Dialog(
        onDismissRequest = {
            if (!isBackingUp && !isRestoring) {
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
                .fillMaxHeight(0.85f)
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
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isBangla) "ডাটা ব্যাকআপ ও রিস্টোর" else "Auto-Backup & Restore",
                                color = TextPrimary,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "সিঙ্গেল ফাইল অটো-ওভাররাইট ব্যাকআপ" else "Single-File Auto-Overwrite Backup",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
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

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status Badge Card
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
                            "প্রতিটি এডিট বা আপডেট হওয়ার সাথে সাথে স্বয়ংক্রিয়ভাবে একটি মাত্র ফাইলে (IBM_AutoBackup.db) ডাটা ওভাররাইট হয়ে ব্যাকআপ জমা হয়। এছাড়া প্রতিদিন রাত ২:৩০ মিনিটে ব্যাকগ্রাউন্ডে নিয়মিত স্ন্যাপশট নেওয়া হয়।"
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
                                    text = if (isBangla) "ব্যাকআপ ফাইল বিবরণ" else "BACKUP FILE DETAILS",
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

                    // Button 1: Backup Now (Manual Overwrite)
                    Button(
                        onClick = {
                            isBackingUp = true
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    AutoBackupManager.performBackup(context)
                                }
                                refreshInfo()
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
                        enabled = !isBackingUp && !isRestoring,
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
                        onClick = { showRestoreConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = backupInfo.exists && !isBackingUp && !isRestoring,
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

                    // Button 3: Share / Export Backup File
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
                        enabled = backupInfo.exists && !isBackingUp && !isRestoring,
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
                            text = if (isBangla) "ব্যাকআপ ফাইল শেয়ার / ড্রাইভ এ সেভ করুন" else "Share / Export Backup File",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation Alert for Restore
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = {
                Text(
                    text = if (isBangla) "ডাটা রিস্টোর নিশ্চিতকরণ" else "Confirm Data Restore",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনি কি নিশ্চিত যে সংরক্ষিত ব্যাকআপ থেকে ডাটা ফিরিয়ে আনতে চান? বর্তমান ডাটাবেজ ব্যাকআপ ফাইল (IBM_AutoBackup.db) দ্বারা প্রতিস্থাপিত হবে।"
                    else
                        "Are you sure you want to restore? Current app data will be replaced by the backup snapshot in IBM_AutoBackup.db.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirm = false
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
                                // Restart MainActivity to refresh all cached ViewModels & Room flows
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
                TextButton(onClick = { showRestoreConfirm = false }) {
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
