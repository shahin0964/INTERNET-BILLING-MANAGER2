package internet.billing.manager.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.content.ContextCompat
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.components.BulkSmsSendDialog
import internet.billing.manager.ui.components.EditTemplateDialog
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import internet.billing.manager.util.AutoSmsManager
import internet.billing.manager.util.SmsTemplateItem

@Composable
fun AutoSmsScreen(
    viewModel: BillingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCustomers by viewModel.allCustomers.collectAsState()
    val ispSettings by viewModel.ispSettings.collectAsState()
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA

    var showBulkSmsDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<SmsTemplateItem?>(null) }

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, if (isBangla) "এসএমএস অনুমতি দেওয়া হয়েছে" else "SMS permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, if (isBangla) "এসএমএস পাঠানোর জন্য অনুমতি প্রয়োজন" else "SMS permission required to send auto SMS", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Top Bar with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BgCard)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = appStrings.autoSmsTitle,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = appStrings.autoSmsSubtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Main Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            // Permission Alert Banner if not granted
            if (!hasSmsPermission) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = AccentOrange.copy(alpha = 0.5f),
                        backgroundColor = AccentOrange.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "সিম থেকে এসএমএস পাঠানোর পারমিশন দিন" else "Grant SMS permission to send messages",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.SEND_SMS) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (isBangla) "অনুমতি দিন" else "Grant", color = BgDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 1: Master Auto SMS Switch Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    borderColor = if (AutoSmsManager.isAutoSmsEnabled) AccentCyan.copy(alpha = 0.5f) else CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (AutoSmsManager.isAutoSmsEnabled) AccentCyan.copy(alpha = 0.2f) else BgCardLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = if (AutoSmsManager.isAutoSmsEnabled) AccentCyan else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = appStrings.masterAutoSms,
                                        color = TextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (AutoSmsManager.isAutoSmsEnabled) "Active & Running ✓" else "Currently Disabled",
                                        color = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen else TextMuted,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Switch(
                                checked = AutoSmsManager.isAutoSmsEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked && !hasSmsPermission) {
                                        permissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    }
                                    AutoSmsManager.setAutoSmsEnabled(isChecked, context)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BgDark,
                                    checkedTrackColor = AccentCyan,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = BgCardLight
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = appStrings.masterAutoSmsDesc,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Section 2: Anti-Spam Rate Limit Interval (Delay) Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = appStrings.antiSpamInterval,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${AutoSmsManager.smsDelaySeconds}s delay between messages",
                                    color = AccentOrange,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = appStrings.antiSpamIntervalDesc,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interval Choice Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3, 5, 10).forEach { sec ->
                                val isSelected = AutoSmsManager.smsDelaySeconds == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentOrange.copy(alpha = 0.25f) else BgCardLight)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentOrange else CardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            AutoSmsManager.setSmsDelay(sec, context)
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${sec}s" + if (sec == 3) "★" else "",
                                        color = if (isSelected) AccentOrange else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Event Automation Triggers
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = appStrings.eventAutomation,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger 1: Auto SMS on Monthly Bill Generation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appStrings.autoSmsOnBill,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = appStrings.autoSmsOnBillDesc,
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                            Switch(
                                checked = AutoSmsManager.isAutoBillSmsEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked && !hasSmsPermission) {
                                        permissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    }
                                    AutoSmsManager.setAutoBillSmsEnabled(isChecked, context)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BgDark,
                                    checkedTrackColor = AccentBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = BgCardLight
                                )
                            )
                        }

                        Divider(
                            color = CardBorderSubtle,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Trigger 2: Auto SMS on Payment Collection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appStrings.autoSmsOnPayment,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = appStrings.autoSmsOnPaymentDesc,
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                            Switch(
                                checked = AutoSmsManager.isAutoPaymentSmsEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked && !hasSmsPermission) {
                                        permissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    }
                                    AutoSmsManager.setAutoPaymentSmsEnabled(isChecked, context)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BgDark,
                                    checkedTrackColor = AccentGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = BgCardLight
                                )
                            )
                        }
                    }
                }
            }

            // Section 4: Bulk SMS Action Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    borderColor = AccentCyan.copy(alpha = 0.35f),
                    backgroundColor = BgCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan.copy(alpha = 0.2f)),
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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBangla) "গ্রাহকদের নোটিশ বা বকেয়া রিমাইন্ডার পাঠান" else "Send notices or reminders to customers",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showBulkSmsDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (isBangla) "এসএমএস পাঠান" else "Compose",
                                color = BgDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 5: Template Library Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.smsTemplates,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "${AutoSmsManager.defaultTemplates.size}টি টেমপ্লেট" else "${AutoSmsManager.defaultTemplates.size} Templates",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Section 6: List of 9 SMS Templates
            items(AutoSmsManager.defaultTemplates, key = { it.key }) { tpl ->
                val currentText = remember(tpl.key) {
                    AutoSmsManager.getTemplateText(tpl.key, context)
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tpl.title,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tpl.description,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { templateToEdit = tpl },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBangla) "এডিট" else "Edit",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgCardLight)
                                .border(1.dp, CardBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(10.dp)
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
        }
    }

    // Edit Template Dialog
    if (templateToEdit != null) {
        val tpl = templateToEdit!!
        EditTemplateDialog(
            template = tpl,
            onDismiss = { templateToEdit = null },
            onSave = { newText: String ->
                AutoSmsManager.saveTemplateText(tpl.key, newText, context)
                templateToEdit = null
                Toast.makeText(context, if (isBangla) "টেমপ্লেট সংরক্ষণ করা হয়েছে" else "Template saved", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Bulk SMS Send Dialog
    if (showBulkSmsDialog) {
        BulkSmsSendDialog(
            allCustomers = allCustomers,
            ispSettings = ispSettings,
            onDismiss = { showBulkSmsDialog = false }
        )
    }
}
