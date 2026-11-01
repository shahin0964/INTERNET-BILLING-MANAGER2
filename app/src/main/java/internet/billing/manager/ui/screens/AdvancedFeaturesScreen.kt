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
    onNavigateToNetworkTools: () -> Unit,
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
                    text = if (isBangla) "সিম অটোমেশন ও নেটওয়ার্ক টুলস" else "SIM automation & network tools",
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
            // Row 1: Network Tools (নেটওয়ার্ক টুলস)
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.Build,
                    iconTint = AccentCyan,
                    title = if (isBangla) "নেটওয়ার্ক টুলস (Network Tools)" else "Network Tools",
                    subtitle = if (isBangla) "পিং টেস্ট, ডিএনএস লুকআপ, আইপি ও গেটওয়ে, পোর্ট চেকার, স্পিড টেস্ট" else "Ping test, DNS lookup, IP & gateway, Port checker, Speed test, Wi-Fi analyzer",
                    badgeText = "7 Tools",
                    badgeColor = AccentCyan,
                    showChevron = true,
                    onClick = onNavigateToNetworkTools
                )
            }

            // Row 2: Auto SMS System (SIM) - Navigates to dedicated AutoSmsScreen!
            item {
                AdvancedFeatureRowCard(
                    icon = Icons.Default.Chat,
                    iconTint = AccentOrange,
                    title = if (isBangla) "অটো এসএমএস সিস্টেম (সিম)" else "Auto SMS System (SIM)",
                    subtitle = if (isBangla) "সিম রেট-লিমিট ডিসপ্যাচ, নোটিশ ও ৯টি বাংলা টেমপ্লেট" else "SIM rate-limited dispatch, billing notices & 9 templates",
                    badgeText = if (AutoSmsManager.isAutoSmsEnabled) "ON" else "OFF",
                    badgeColor = if (AutoSmsManager.isAutoSmsEnabled) AccentGreen else TextMuted,
                    showChevron = true,
                    onClick = onNavigateToAutoSms
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
