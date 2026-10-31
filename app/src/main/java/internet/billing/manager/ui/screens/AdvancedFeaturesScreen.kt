package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.AutoSmsManager

@Composable
fun AdvancedFeaturesScreen(
    onBack: () -> Unit,
    onNavigateToAutoSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Top Header with Back button
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
                    text = appStrings.advancedFeatures,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBangla) "সিম অটোমেশন ও অ্যাডভান্সড সেটিংস" else "SIM automation & system settings",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Feature Rows List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Row 1: Auto SMS System (SIM) - Navigates to dedicated AutoSmsScreen!
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.Chat,
                    iconTint = AccentCyan,
                    title = if (isBangla) "অটো এসএমএস সিস্টেম (সিম)" else "Auto SMS System (SIM)",
                    subtitle = if (isBangla) "সিম রেট-লিমিট ডিসপ্যাচ, নোটিশ ও ৯টি টেমপ্লেট" else "SIM rate-limited dispatch, billing notices & 9 templates",
                    badgeText = if (AutoSmsManager.isAutoSmsEnabled) "ON" else "OFF",
                    badgeColor = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen else TextMuted,
                    showChevron = true,
                    onClick = onNavigateToAutoSms
                )
            }

            // Row 2: Billing Cycle & Schedule
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.CalendarMonth,
                    iconTint = AccentBlue,
                    title = if (isBangla) "বিলিং সাইকেল ও সময়সূচী" else "Billing Cycle & Schedule",
                    subtitle = if (isBangla) "প্রতি মাসের ১ তারিখে বিল তৈরি এবং ১০ তারিখে বকেয়া সময়সীমা" else "Monthly bill generation on 1st, payment due date 10th",
                    badgeText = "Auto",
                    badgeColor = AccentBlue,
                    showChevron = false,
                    onClick = {}
                )
            }

            // Row 3: Offline SQLite Storage
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.Storage,
                    iconTint = AccentOrange,
                    title = if (isBangla) "অফলাইন SQLite ডাটাবেজ" else "Offline SQLite Storage",
                    subtitle = if (isBangla) "ক্লাউডহীন নিরাপদ ডাটা স্টোরেজ, সম্পূর্ণ ফোনের ভেতরে সংরক্ষিত" else "Zero cloud dependency, all data stays securely on your phone",
                    badgeText = "Local",
                    badgeColor = AccentOrange,
                    showChevron = false,
                    onClick = {}
                )
            }

            // Row 4: Data Protection & Integrity
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.Security,
                    iconTint = AccentGreen,
                    title = if (isBangla) "ডাটা সুরক্ষা ও ট্রানজ্যাকশন" else "Data Safety & Protection",
                    subtitle = if (isBangla) "অটোমেটিক Room ডাটাবেজ ট্রানজ্যাকশন রোলব্যাক ও সেফটি" else "Automatic Room database transaction rollback & safe keeping",
                    badgeText = "Active",
                    badgeColor = AccentGreen,
                    showChevron = false,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun AdvancedFeatureRowCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    showChevron: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        borderColor = CardBorderSubtle,
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            if (showChevron) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
