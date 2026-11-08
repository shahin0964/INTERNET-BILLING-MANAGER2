package internet.billing.manager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.ReceiptCustomizationManager
import internet.billing.manager.util.ReceiptCustomizationSettings

@Composable
fun ReceiptCustomizationScreen(
    ispSettings: IspSettings?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA

    var settings by remember {
        mutableStateOf(ReceiptCustomizationManager.getSettings(context))
    }

    val dialogTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentCyan,
        unfocusedBorderColor = CardBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = AccentCyan,
        unfocusedLabelColor = TextSecondary,
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedContainerColor = BgCardLight.copy(alpha = 0.5f),
        unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f),
        cursorColor = AccentCyan
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isBangla) "রসিদ কাস্টমাইজেশন" else "Receipt Customization",
                        color = TextPrimary,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "মানি রসিদ ও হোয়াটসঅ্যাপ শেয়ার" else "Money receipt & WhatsApp sharing",
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Reset Button
                IconButton(
                    onClick = {
                        settings = ReceiptCustomizationManager.resetToDefaults(context)
                        Toast.makeText(context, if (isBangla) "ডিফল্ট সেটিংস রিস্টোর করা হয়েছে" else "Reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        tint = AccentOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save Button
                IconButton(
                    onClick = {
                        ReceiptCustomizationManager.saveSettings(context, settings)
                        Toast.makeText(context, if (isBangla) "রসিদ সেটিংস সংরক্ষিত হয়েছে!" else "Receipt settings saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = BgDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Body
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            // Section 1: Auto Receipt Popup
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "অটো রসিদ পপআপ (Auto Popup)" else "Auto Receipt Popup",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBangla) "বিল পেমেন্ট কালেকশন রেকর্ড করার সাথে সাথে হোয়াটসঅ্যাপে রসিদ পাঠানোর পপআপ আসবে"
                                else "Prompt to send Money Receipt on WhatsApp immediately after payment is collected",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.autoReceiptPopupEnabled,
                            onCheckedChange = { settings = settings.copy(autoReceiptPopupEnabled = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BgDark,
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BgCardLight
                            )
                        )
                    }
                }
            }

            // Section 2: Format (Money Receipt)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "রসিদের ফরম্যাট (Receipt Format)" else "Receipt Format",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.5.dp, AccentCyan), RoundedCornerShape(10.dp))
                                .background(AccentCyan.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isBangla) "মানি রসিদ (Money Receipt)" else "Money Receipt Voucher",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (isBangla) "স্টাইলিশ গ্রাহক স্লিপ • সরাসরি হোয়াটসঅ্যাপে শেয়ারযোগ্য" else "Stylish compact voucher • Directly shareable to WhatsApp",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentCyan)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Header & Notes Customization
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "শিরোনাম ও নোট কাস্টমাইজেশন" else "Header & Notes Customization",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Receipt Title
                        Text(
                            text = if (isBangla) "রসিদের শিরোনাম (Receipt Title / Header)" else "Receipt Title / Header",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = settings.receiptTitle,
                            onValueChange = { settings = settings.copy(receiptTitle = it) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = dialogTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Footer Note
                        Text(
                            text = if (isBangla) "ধন্যবাদ নোট (Footer / Thank You Note)" else "Footer / Thank You Note",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = settings.footerNote,
                            onValueChange = { settings = settings.copy(footerNote = it) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = dialogTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Special Notes
                        Text(
                            text = if (isBangla) "বিশেষ শর্ত বা হেল্পলাইন নোট (Special Notes / Terms)" else "Special Notes / Terms",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = settings.specialNotes,
                            onValueChange = { settings = settings.copy(specialNotes = it) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = dialogTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 4: Mobile Banking & Signature
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "মোবাইল ব্যাংকিং ও স্বাক্ষর" else "Mobile Banking & Signature",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = if (isBangla) "বিকাশ/নগদ নম্বর ও অনুমোদিত স্বাক্ষর" else "bKash/Nagad numbers & signature",
                                        color = TextMuted,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Switch(
                                checked = settings.showMobileBanking,
                                onCheckedChange = { settings = settings.copy(showMobileBanking = it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BgDark,
                                    checkedTrackColor = AccentOrange
                                )
                            )
                        }

                        if (settings.showMobileBanking) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // bKash Number
                            Text(
                                text = if (isBangla) "বিকাশ নম্বর (bKash Number)" else "bKash Number",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = settings.bkashNumber,
                                onValueChange = { settings = settings.copy(bkashNumber = it) },
                                placeholder = { Text("017XXXXXXXX", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Nagad Number
                            Text(
                                text = if (isBangla) "নগদ নম্বর (Nagad Number - ঐচ্ছিক)" else "Nagad Number (Optional)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = settings.nagadNumber,
                                onValueChange = { settings = settings.copy(nagadNumber = it) },
                                placeholder = { Text("018XXXXXXXX", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Signature Name
                            Text(
                                text = if (isBangla) "স্বাক্ষরকারীর নাম বা পদবি (Signature Name)" else "Signature Name / Title",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = settings.signatureName,
                                onValueChange = { settings = settings.copy(signatureName = it) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Corporate Motto
                            Text(
                                text = if (isBangla) "কোম্পানির স্লোগান (Corporate Motto)" else "Corporate Motto / Slogan",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = settings.corporateMotto,
                                onValueChange = { settings = settings.copy(corporateMotto = it) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section 5: Visible Information Fields
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "রসিদে প্রদর্শিত ফিল্ডসমূহ" else "Visible Information Fields",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (isBangla) "যে তথ্যগুলো মানি রসিদে দেখাতে চান" else "Information to show on the receipt",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FieldToggleRow(
                            title = if (isBangla) "গ্রাহকের ফোন নম্বর (Customer Phone)" else "Customer Phone Number",
                            checked = settings.showPhone,
                            onCheckedChange = { settings = settings.copy(showPhone = it) }
                        )

                        FieldToggleRow(
                            title = if (isBangla) "পিপিপিওই ইউজারনেম (PPPoE Username)" else "PPPoE Username",
                            checked = settings.showUsername,
                            onCheckedChange = { settings = settings.copy(showUsername = it) }
                        )

                        FieldToggleRow(
                            title = if (isBangla) "ইন্টারনেট প্যাকেজের নাম (Package Name)" else "Package Name",
                            checked = settings.showPackage,
                            onCheckedChange = { settings = settings.copy(showPackage = it) }
                        )

                        FieldToggleRow(
                            title = if (isBangla) "গ্রাহকের ঠিকানা (Customer Address)" else "Customer Address",
                            checked = settings.showAddress,
                            onCheckedChange = { settings = settings.copy(showAddress = it) }
                        )

                        FieldToggleRow(
                            title = if (isBangla) "অবশিষ্ট বকেয়া টাকা (Remaining Due Amount)" else "Remaining Due Amount",
                            checked = settings.showRemainingDue,
                            onCheckedChange = { settings = settings.copy(showRemainingDue = it) }
                        )

                        FieldToggleRow(
                            title = if (isBangla) "পরিশোধের মাধ্যম (Payment Method - Cash/bKash)" else "Payment Method",
                            checked = settings.showPaymentMethod,
                            onCheckedChange = { settings = settings.copy(showPaymentMethod = it) }
                        )
                    }
                }
            }

            // Section 6: Live Money Receipt Preview
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Preview,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "লাইভ রসিদ প্রিভিউ (Live Preview)" else "Live Receipt Preview",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Money Receipt",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Realistic Money Receipt Preview Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(2.dp, AccentCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header: ISP Details + Mobile Banking Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ispSettings?.companyName?.ifBlank { "HR_INTERNET" } ?: "HR_INTERNET",
                                        color = Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Hotline: ${ispSettings?.phone?.ifBlank { "01725208629" } ?: "01725208629"}",
                                        color = Color(0xFF475569),
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "Address: ${ispSettings?.address?.ifBlank { "Mirpur, Dhaka" } ?: "Mirpur, Dhaka"}",
                                        color = Color(0xFF64748B),
                                        fontSize = 9.sp
                                    )
                                }

                                if (settings.showMobileBanking) {
                                    val num = settings.bkashNumber.ifBlank { settings.nagadNumber.ifBlank { "01725208629" } }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.2.dp, Color(0xFFE11D48)), RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFF1F2))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "bKash / Nagad",
                                                color = Color(0xFFBE123C),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = num,
                                                color = Color(0xFF881337),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Pill Title: MONEY RECEIPT
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF0284C7)), RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF0F9FF))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = settings.receiptTitle.ifBlank { "MONEY RECEIPT (মানি রসিদ)" },
                                    color = Color(0xFF0369A1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Divider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Demo Content Details
                            Text(
                                text = "Received with thanks from: ariful_net (Md. Ariful Islam)",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Amount: ৳ 1,500.00",
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "In word: One Thousand Five Hundred Taka Only",
                                color = Color(0xFF475569),
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Grid Info Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(BorderStroke(0.8.dp, Color(0xFFE2E8F0)), RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    if (settings.showUsername) Text("PPPoE: ariful_net", color = Color(0xFF334155), fontSize = 9.5.sp)
                                    if (settings.showPackage) Text("Package: 20 Mbps Fast", color = Color(0xFF334155), fontSize = 9.5.sp)
                                    if (settings.showPhone) Text("Phone: 01712345678", color = Color(0xFF334155), fontSize = 9.5.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (settings.showPaymentMethod) Text("Method: Cash", color = Color(0xFF334155), fontSize = 9.5.sp)
                                    Text("Paid: ৳ 1,500.00", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                                    if (settings.showRemainingDue) Text("Due: ৳ 0.00", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (settings.footerNote.isNotBlank()) {
                                Text(
                                    text = settings.footerNote,
                                    color = Color(0xFF047857),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Signature Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Divider(color = Color(0xFF94A3B8), thickness = 1.dp, modifier = Modifier.width(90.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Received by", color = Color(0xFF64748B), fontSize = 8.5.sp)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Divider(color = Color(0xFF94A3B8), thickness = 1.dp, modifier = Modifier.width(110.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Authorized Signature", color = Color(0xFF64748B), fontSize = 8.5.sp)
                                    Text(settings.signatureName, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Corporate Motto Ribbon
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = settings.corporateMotto,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Save Settings Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        ReceiptCustomizationManager.saveSettings(context, settings)
                        Toast.makeText(context, if (isBangla) "রসিদ কাস্টমাইজেশন সেটিংস সংরক্ষিত হয়েছে!" else "Receipt settings successfully saved!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = BgDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "সেটিংস সংরক্ষণ করুন" else "Save Receipt Settings",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 11.5.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgDark,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BgCardLight
            )
        )
    }
}
