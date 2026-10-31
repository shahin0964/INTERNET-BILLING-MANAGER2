package internet.billing.manager.ui.components

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.util.AutoSmsManager
import internet.billing.manager.util.SmsSendSummary
import internet.billing.manager.util.SmsTemplateItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AdvancedFeaturesDialog(
    allCustomers: List<Customer>,
    ispSettings: IspSettings?,
    onDismiss: () -> Unit
) {
    var showAutoSmsHub by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showAutoSmsHub) {
        AutoSmsHubDialog(
            allCustomers = allCustomers,
            ispSettings = ispSettings,
            onDismiss = { showAutoSmsHub = false }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = appStrings.advancedFeatures,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = appStrings.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Feature 1: Auto SMS System (SIM Based)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAutoSmsHub = true },
                    shape = RoundedCornerShape(12.dp),
                    borderColor = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen.copy(alpha = 0.5f) else CardBorder,
                    backgroundColor = BgCardLight.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (AutoSmsManager.isAutoSmsEnabled) AccentGreen.copy(alpha = 0.2f)
                                             else AccentCyan.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen else AccentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appStrings.autoSmsTitle,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                if (AutoSmsManager.isAutoSmsEnabled) AccentGreen.copy(alpha = 0.2f)
                                                else CardBorder
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (AutoSmsManager.isAutoSmsEnabled) "ACTIVE" else "OFF",
                                            color = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen else TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = appStrings.autoSmsSubtitle,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                }
                            }

                            Switch(
                                checked = AutoSmsManager.isAutoSmsEnabled,
                                onCheckedChange = { AutoSmsManager.setAutoSmsEnabled(it, context) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BgDark,
                                    checkedTrackColor = AccentGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = BgCard
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = CardBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "9 Ready-made Templates • Delay: ${AutoSmsManager.smsDelaySeconds}s",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Configure",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature 2: Billing Cycle Rules
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorder,
                    backgroundColor = BgCardLight.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Billing Cycle & Last Date",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Monthly bill generation on 1st, payment due date 10th",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature 3: Database & Export
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "All records stored in local SQLite database", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorder,
                    backgroundColor = BgCardLight.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline SQLite Storage",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Zero cloud dependency, all data stays securely on your phone",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(
                        text = appStrings.done,
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditTemplateDialog(
    template: SmsTemplateItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    var currentText by remember(template.key) {
        mutableStateOf(AutoSmsManager.getTemplateText(template.key, context))
    }

    val isBangla = LanguageManager.isBengali()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BgCard,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isBangla) "টেমপ্লেট এডিট: ${template.title}" else "Edit Template: ${template.title}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBangla) "ভেরিয়েবলসমূহ: {NAME}, {BILL}, {DUE}, {MONTH}, {COMPANY}, {PHONE}, {PPPOE}, {PACKAGE}"
                           else "Placeholders: {NAME}, {BILL}, {DUE}, {MONTH}, {COMPANY}, {PHONE}, {PPPOE}, {PACKAGE}",
                    color = AccentCyan,
                    fontSize = 10.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            currentText = template.defaultText
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(if (isBangla) "ডিফল্ট রিসেট" else "Reset Default", color = TextSecondary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            onSave(currentText)
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text(if (isBangla) "সংরক্ষণ করুন" else "Save", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BulkSmsSendDialog(
    allCustomers: List<Customer>,
    ispSettings: IspSettings?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTemplateKey by remember { mutableStateOf(AutoSmsManager.defaultTemplates.first().key) }
    var targetGroup by remember { mutableStateOf("All") } // "All", "Active", "Due"
    var isSendingInProgress by remember { mutableStateOf(false) }
    var sendProgressCurrent by remember { mutableStateOf(0) }
    var sendProgressTotal by remember { mutableStateOf(0) }
    var currentRecipientName by remember { mutableStateOf("") }
    var isWaitingDelay by remember { mutableStateOf(false) }
    var sendSummaryResult by remember { mutableStateOf<SmsSendSummary?>(null) }
    var sendJob by remember { mutableStateOf<Job?>(null) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "SMS Permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "SMS Permission required to send via SIM", Toast.LENGTH_LONG).show()
        }
    }

    // Sending Progress Dialog
    if (isSendingInProgress) {
        Dialog(
            onDismissRequest = { /* Prevent dismiss while sending */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sending Bulk SMS via SIM",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = {
                            if (sendProgressTotal > 0) sendProgressCurrent.toFloat() / sendProgressTotal.toFloat() else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentCyan,
                        trackColor = BgCardLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sent: $sendProgressCurrent of $sendProgressTotal",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current: $currentRecipientName",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isWaitingDelay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = AccentOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pausing ${AutoSmsManager.smsDelaySeconds}s to protect SIM from carrier spam block...",
                                color = AccentOrange,
                                fontSize = 10.5.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Dispatching next message...",
                            color = AccentGreen,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            sendJob?.cancel()
                            isSendingInProgress = false
                            Toast.makeText(context, "SMS Queue Stopped", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                    ) {
                        Text("Stop Queue", color = AccentRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Summary Result Dialog
    if (sendSummaryResult != null) {
        val summary = sendSummaryResult!!
        AlertDialog(
            onDismissRequest = {
                sendSummaryResult = null
                onDismiss()
            },
            title = { Text("Bulk SMS Complete", color = TextPrimary) },
            text = {
                Column {
                    Text("Total Processed: ${summary.total}", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Successfully Sent: ${summary.successful}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (summary.failed > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Failed / No phone: ${summary.failed}", color = AccentRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sendSummaryResult = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("OK", color = BgDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BgCard
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appStrings.bulkSmsSend,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Queue SIM messages with safe anti-spam delay",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Choose Template Dropdown
                var templateExpanded by remember { mutableStateOf(false) }
                val activeTemplateItem = AutoSmsManager.defaultTemplates.firstOrNull { it.key == selectedTemplateKey }
                    ?: AutoSmsManager.defaultTemplates.first()

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = activeTemplateItem.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(appStrings.selectTemplate) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AccentCyan
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { templateExpanded = true }
                    )

                    DropdownMenu(
                        expanded = templateExpanded,
                        onDismissRequest = { templateExpanded = false },
                        modifier = Modifier
                            .background(BgCard)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                    ) {
                        AutoSmsManager.defaultTemplates.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = item.title,
                                            color = if (item.key == selectedTemplateKey) AccentCyan else TextPrimary,
                                            fontWeight = if (item.key == selectedTemplateKey) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = item.description,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedTemplateKey = item.key
                                    templateExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Target Recipients Filter
                Text(appStrings.targetAudience, color = TextSecondary, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val allWithPhone = allCustomers.filter { it.phone.isNotBlank() }
                val activeWithPhone = allCustomers.filter { it.phone.isNotBlank() && it.status == Customer.STATUS_ACTIVE }
                val dueWithPhone = allCustomers.filter { it.phone.isNotBlank() && it.dueAmount > 0 }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("All", "${appStrings.targetAll} (${allWithPhone.size})", allWithPhone),
                        Triple("Active", "${appStrings.targetActiveOnly} (${activeWithPhone.size})", activeWithPhone),
                        Triple("Due", "${appStrings.targetDueOnly} (${dueWithPhone.size})", dueWithPhone)
                    ).forEach { (key, label, _) ->
                        val isSelected = targetGroup == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                .border(1.dp, if (isSelected) AccentCyan else CardBorder, RoundedCornerShape(8.dp))
                                .clickable { targetGroup = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) AccentCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                val targetRecipients = when (targetGroup) {
                    "Active" -> activeWithPhone
                    "Due" -> dueWithPhone
                    else -> allWithPhone
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message Preview Box
                val rawTemplateText = AutoSmsManager.getTemplateText(selectedTemplateKey, context)
                val sampleCustomer = targetRecipients.firstOrNull() ?: Customer(
                    name = "Sample Customer",
                    phone = "017XXXXXXXX",
                    monthlyBill = 500.0,
                    dueAmount = 500.0,
                    pppoeUsername = "sample_user",
                    packageName = "5 Mbps"
                )
                val previewFormatted = AutoSmsManager.formatMessage(
                    template = rawTemplateText,
                    customer = sampleCustomer,
                    ispSettings = ispSettings
                )

                Text(appStrings.messagePreview, color = TextSecondary, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgCardLight)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = previewFormatted,
                        color = TextPrimary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Sending Button
                Button(
                    onClick = {
                        if (targetRecipients.isEmpty()) {
                            Toast.makeText(context, "No customers with valid phone numbers in this group", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            return@Button
                        }

                        isSendingInProgress = true
                        sendProgressCurrent = 0
                        sendProgressTotal = targetRecipients.size

                        sendJob = coroutineScope.launch {
                            val summary = AutoSmsManager.sendBatchSms(
                                context = context,
                                customers = targetRecipients,
                                template = rawTemplateText,
                                ispSettings = ispSettings,
                                onProgress = { current, total, name, isWaiting ->
                                    sendProgressCurrent = current
                                    sendProgressTotal = total
                                    currentRecipientName = name
                                    isWaitingDelay = isWaiting
                                }
                            )
                            isSendingInProgress = false
                            sendSummaryResult = summary
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    enabled = targetRecipients.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = BgDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${appStrings.sendSmsBtn} (${targetRecipients.size})",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AutoSmsHubDialog(
    allCustomers: List<Customer>,
    ispSettings: IspSettings?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBangla = LanguageManager.isBengali()

    var activeTab by remember { mutableStateOf(0) } // 0: Templates & Automation, 1: Send Bulk SMS
    var editingTemplateItem by remember { mutableStateOf<SmsTemplateItem?>(null) }

    // Bulk SMS states
    var selectedTemplateKey by remember { mutableStateOf(AutoSmsManager.defaultTemplates.first().key) }
    var targetGroup by remember { mutableStateOf("All") } // "All", "Active", "Due"
    var isSendingInProgress by remember { mutableStateOf(false) }
    var sendProgressCurrent by remember { mutableStateOf(0) }
    var sendProgressTotal by remember { mutableStateOf(0) }
    var currentRecipientName by remember { mutableStateOf("") }
    var isWaitingDelay by remember { mutableStateOf(false) }
    var sendSummaryResult by remember { mutableStateOf<SmsSendSummary?>(null) }
    var sendJob by remember { mutableStateOf<Job?>(null) }

    // SMS Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, if (isBangla) "এসএমএস অনুমতি দেওয়া হয়েছে!" else "SMS Permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, if (isBangla) "সিম থেকে এসএমএস পাঠানোর জন্য অনুমতি প্রয়োজন" else "SMS Permission required to send via SIM", Toast.LENGTH_LONG).show()
        }
    }

    // Template Edit Dialog
    if (editingTemplateItem != null) {
        val item = editingTemplateItem!!
        EditTemplateDialog(
            template = item,
            onDismiss = { editingTemplateItem = null },
            onSave = { newText ->
                AutoSmsManager.saveTemplateText(item.key, newText, context)
                Toast.makeText(context, if (isBangla) "টেমপ্লেট সফলভাবে সংরক্ষিত হয়েছে!" else "Template saved!", Toast.LENGTH_SHORT).show()
                editingTemplateItem = null
            }
        )
    }

    // Sending Progress Dialog
    if (isSendingInProgress) {
        Dialog(
            onDismissRequest = { /* Prevent dismiss while sending */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sending Bulk SMS via SIM",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = {
                            if (sendProgressTotal > 0) sendProgressCurrent.toFloat() / sendProgressTotal.toFloat() else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentCyan,
                        trackColor = BgCardLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sent: $sendProgressCurrent of $sendProgressTotal",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current: $currentRecipientName",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isWaitingDelay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = AccentOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pausing ${AutoSmsManager.smsDelaySeconds}s to protect SIM from carrier spam block...",
                                color = AccentOrange,
                                fontSize = 10.5.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Dispatching next message...",
                            color = AccentGreen,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            sendJob?.cancel()
                            isSendingInProgress = false
                            Toast.makeText(context, "SMS Queue Stopped", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                    ) {
                        Text("Stop Queue", color = AccentRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Summary Result Dialog
    if (sendSummaryResult != null) {
        val summary = sendSummaryResult!!
        AlertDialog(
            onDismissRequest = { sendSummaryResult = null },
            title = { Text("Bulk SMS Complete", color = TextPrimary) },
            text = {
                Column {
                    Text("Total Processed: ${summary.total}", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Successfully Sent: ${summary.successful}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (summary.failed > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Failed / No phone: ${summary.failed}", color = AccentRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { sendSummaryResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("OK", color = BgDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BgCard
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = appStrings.autoSmsTitle,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = appStrings.autoSmsSubtitle,
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = appStrings.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: 1. Templates & Settings | 2. Send Bulk SMS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgCardLight)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == 0) AccentCyan else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appStrings.smsTemplates,
                            color = if (activeTab == 0) BgDark else TextSecondary,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == 1) AccentCyan else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appStrings.bulkSmsSend,
                            color = if (activeTab == 1) BgDark else TextSecondary,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeTab == 0) {
                        // TAB 1: Automation Settings & Templates
                        // Master Auto SMS Switch
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            borderColor = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen.copy(alpha = 0.5f) else CardBorder,
                            backgroundColor = BgCardLight.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appStrings.masterAutoSms,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = appStrings.masterAutoSmsDesc,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Switch(
                                        checked = AutoSmsManager.isAutoSmsEnabled,
                                        onCheckedChange = { AutoSmsManager.setAutoSmsEnabled(it, context) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BgDark,
                                            checkedTrackColor = AccentGreen
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Event Triggers
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            borderColor = CardBorder,
                            backgroundColor = BgCardLight.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = appStrings.eventAutomation,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(appStrings.autoSmsOnBill, color = TextPrimary, fontSize = 12.sp)
                                        Text(appStrings.autoSmsOnBillDesc, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Checkbox(
                                        checked = AutoSmsManager.isAutoBillSmsEnabled,
                                        onCheckedChange = { AutoSmsManager.setAutoBillSmsEnabled(it, context) },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
                                    )
                                }

                                Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(appStrings.autoSmsOnPayment, color = TextPrimary, fontSize = 12.sp)
                                        Text(appStrings.autoSmsOnPaymentDesc, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Checkbox(
                                        checked = AutoSmsManager.isAutoPaymentSmsEnabled,
                                        onCheckedChange = { AutoSmsManager.setAutoPaymentSmsEnabled(it, context) },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rate-Limiting Anti-Spam Safe Delay
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            borderColor = AccentOrange.copy(alpha = 0.4f),
                            backgroundColor = BgCardLight.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SIM Anti-Spam Interval Delay",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pauses between consecutive SMS to protect SIM from carrier rate limits and spam bans.",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(2, 3, 5, 8).forEach { sec ->
                                        val isSelected = AutoSmsManager.smsDelaySeconds == sec
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) AccentOrange.copy(alpha = 0.25f) else BgCard)
                                                .border(1.dp, if (isSelected) AccentOrange else CardBorder, RoundedCornerShape(6.dp))
                                                .clickable { AutoSmsManager.setSmsDelay(sec, context) }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${sec}s${if (sec == 3) " ★" else ""}",
                                                color = if (isSelected) AccentOrange else TextSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Template Library
                        Text(
                            text = if (isBangla) "আইএসপি এসএমএস টেমপ্লেটসমূহ (${AutoSmsManager.defaultTemplates.size}টি)" else "Ready-made ISP SMS Templates (${AutoSmsManager.defaultTemplates.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        AutoSmsManager.defaultTemplates.forEach { tpl ->
                            val currentText = AutoSmsManager.getTemplateText(tpl.key, context)
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                borderColor = CardBorder,
                                backgroundColor = BgCardLight.copy(alpha = 0.25f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tpl.title,
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = tpl.description,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { editingTemplateItem = tpl },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = AccentCyan,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BgCard)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = currentText,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                    } else {
                        // TAB 2: Send Bulk SMS Tool
                        Text(
                            text = appStrings.bulkSmsSend,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBangla) "টেমপ্লেট ও প্রাপক নির্বাচন করুন। প্রতি এসএমএসের মাঝে ${AutoSmsManager.smsDelaySeconds} সেকেন্ড বিরতি দিয়ে পাঠানো হবে।"
                                   else "Select template and recipients. SMS will be dispatched safely with ${AutoSmsManager.smsDelaySeconds}s delay.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Choose Template Dropdown
                        var templateExpanded by remember { mutableStateOf(false) }
                        val activeTemplateItem = AutoSmsManager.defaultTemplates.firstOrNull { it.key == selectedTemplateKey }
                            ?: AutoSmsManager.defaultTemplates.first()

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = activeTemplateItem.title,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(appStrings.selectTemplate) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = AccentCyan
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { templateExpanded = true }
                            )

                            DropdownMenu(
                                expanded = templateExpanded,
                                onDismissRequest = { templateExpanded = false },
                                modifier = Modifier
                                    .background(BgCard)
                                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                            ) {
                                AutoSmsManager.defaultTemplates.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    color = if (item.key == selectedTemplateKey) AccentCyan else TextPrimary,
                                                    fontWeight = if (item.key == selectedTemplateKey) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.5.sp
                                                )
                                                Text(
                                                    text = item.description,
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedTemplateKey = item.key
                                            templateExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Target Recipients Filter
                        Text(appStrings.targetAudience, color = TextSecondary, fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val allWithPhone = allCustomers.filter { it.phone.isNotBlank() }
                        val activeWithPhone = allCustomers.filter { it.phone.isNotBlank() && it.status == Customer.STATUS_ACTIVE }
                        val dueWithPhone = allCustomers.filter { it.phone.isNotBlank() && it.dueAmount > 0 }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("All", "${appStrings.targetAll} (${allWithPhone.size})", allWithPhone),
                                Triple("Active", "${appStrings.targetActiveOnly} (${activeWithPhone.size})", activeWithPhone),
                                Triple("Due", "${appStrings.targetDueOnly} (${dueWithPhone.size})", dueWithPhone)
                            ).forEach { (key, label, _) ->
                                val isSelected = targetGroup == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                        .border(1.dp, if (isSelected) AccentCyan else CardBorder, RoundedCornerShape(8.dp))
                                        .clickable { targetGroup = key }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) AccentCyan else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        val targetRecipients = when (targetGroup) {
                            "Active" -> activeWithPhone
                            "Due" -> dueWithPhone
                            else -> allWithPhone
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Message Preview Box
                        val rawTemplateText = AutoSmsManager.getTemplateText(selectedTemplateKey, context)
                        val sampleCustomer = targetRecipients.firstOrNull() ?: Customer(
                            name = "Sample Customer",
                            phone = "017XXXXXXXX",
                            monthlyBill = 500.0,
                            dueAmount = 500.0,
                            pppoeUsername = "sample_user",
                            packageName = "5 Mbps"
                        )
                        val previewFormatted = AutoSmsManager.formatMessage(
                            template = rawTemplateText,
                            customer = sampleCustomer,
                            ispSettings = ispSettings
                        )

                        Text(appStrings.messagePreview, color = TextSecondary, fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgCardLight)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = previewFormatted,
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Start Sending Button
                        Button(
                            onClick = {
                                if (targetRecipients.isEmpty()) {
                                    Toast.makeText(context, "No customers with valid phone numbers in this group", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.SEND_SMS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasPermission) {
                                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    return@Button
                                }

                                isSendingInProgress = true
                                sendProgressCurrent = 0
                                sendProgressTotal = targetRecipients.size

                                sendJob = coroutineScope.launch {
                                    val summary = AutoSmsManager.sendBatchSms(
                                        context = context,
                                        customers = targetRecipients,
                                        template = rawTemplateText,
                                        ispSettings = ispSettings,
                                        onProgress = { current, total, name, isWaiting ->
                                            sendProgressCurrent = current
                                            sendProgressTotal = total
                                            currentRecipientName = name
                                            isWaitingDelay = isWaiting
                                        }
                                    )
                                    isSendingInProgress = false
                                    sendSummaryResult = summary
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = targetRecipients.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = BgDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${appStrings.sendSmsBtn} (${targetRecipients.size})",
                                color = BgDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
