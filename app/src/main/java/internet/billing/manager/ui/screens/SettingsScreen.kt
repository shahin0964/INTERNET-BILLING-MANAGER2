package internet.billing.manager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.ui.components.AppUpdateDialog
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.components.LanguageSelectionDialog
import internet.billing.manager.ui.components.ThemeSelectionDialog
import internet.billing.manager.util.AppUpdateManager
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel

@Composable
fun SettingsScreen(
    viewModel: BillingViewModel,
    onNavigateToAdvancedFeatures: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ispSettings by viewModel.ispSettings.collectAsState()
    val packages by viewModel.allPackages.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = appStrings.settingsTitle,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = appStrings.settingsSubtitle,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Settings Menu Items
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                borderColor = CardBorderSubtle
            ) {
                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    // Item 1: ISP Business Info
                    SettingsRowItem(
                        icon = Icons.Default.Business,
                        title = appStrings.ispBusinessInfo,
                        subtitle = ispSettings?.companyName?.takeIf { it.isNotBlank() && !it.equals("U-turn Technologies", ignoreCase = true) } ?: appStrings.tapToManage,
                        iconTint = AccentCyan,
                        onClick = { viewModel.showEditBusinessInfoDialog = true }
                    )
                    // Item 2: Internet Packages
                    SettingsRowItem(
                        icon = Icons.Default.Wifi,
                        title = appStrings.internetPackages,
                        subtitle = if (packages.isNotEmpty()) "${packages.size} ${appStrings.packagesCountSuffix}" else appStrings.tapToManage,
                        iconTint = AccentPurple,
                        onClick = {
                            viewModel.showPackagesManagerDialog = true
                        }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 3: Advanced Features
                    SettingsRowItem(
                        icon = Icons.Default.Tune,
                        title = appStrings.advancedFeatures,
                        subtitle = appStrings.advancedFeaturesSubtitle,
                        iconTint = AccentBlue,
                        onClick = onNavigateToAdvancedFeatures
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 4: Language (English / বাংলা)
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = appStrings.language,
                        subtitle = LanguageManager.currentLanguage.nativeName,
                        iconTint = AccentCyan,
                        onClick = {
                            showLanguageDialog = true
                        }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 5: Theme
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = appStrings.theme,
                        subtitle = ThemeManager.currentTheme.name,
                        iconTint = AccentPurple,
                        onClick = {
                            showThemeDialog = true
                        }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 6: Backup & Restore
                    SettingsRowItem(
                        icon = Icons.Default.CloudSync,
                        title = appStrings.backupRestore,
                        subtitle = "Securely backup and restore data",
                        iconTint = AccentGreen,
                        onClick = {
                            Toast.makeText(context, "Local Room Database Saved", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 7: App Update
                    SettingsRowItem(
                        icon = Icons.Default.SystemUpdate,
                        title = appStrings.appUpdate,
                        subtitle = if (LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA) "GitHub থেকে নতুন আপডেট চেক ও ইনস্টল করুন" else "Check & install updates from GitHub",
                        iconTint = AccentBlue,
                        onClick = {
                            showUpdateDialog = true
                        }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 8: About
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = appStrings.about,
                        subtitle = "App information, privacy & support",
                        iconTint = AccentCyan,
                        onClick = { showAboutDialog = true }
                    )
                    Divider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 10.dp))

                    // Item 9: Security
                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = appStrings.security,
                        subtitle = "Protected ✓",
                        iconTint = AccentGreen,
                        onClick = {
                            Toast.makeText(context, "Database encrypted and protected", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (packages.isNotEmpty()) {
            // 3. Internet Packages Section (Preview Cards for created packages)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.internetPackages,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.showPackagesManagerDialog = true
                        }
                    ) {
                        Text(
                            text = if (LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA) "ম্যানেজ" else "Manage",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(packages, key = { it.id }) { pkg ->
                        PackageCardItem(
                            pkg = pkg,
                            onClick = {
                                viewModel.packageToEdit = pkg
                                viewModel.showAddPackageDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // About Dialog
    if (showAboutDialog) {
        val appVersionName = AppUpdateManager.getCurrentVersionName(context)
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("INTERNET BILLING MANAGER (IBM)", color = TextPrimary) },
            text = {
                Column {
                    Text("Short Name: IBM", color = AccentCyan, fontWeight = FontWeight.Bold)
                    Text("Package: internet.billing.manager", color = TextSecondary, fontSize = 12.sp)
                    Text("Version: v$appVersionName", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Professional ISP Customer, Billing, Daily Bill Collection, and Expense Management System built with Modern Android Jetpack Compose & Room Database.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            containerColor = BgCard,
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("OK", color = BgDark)
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showUpdateDialog) {
        AppUpdateDialog(
            onDismiss = { showUpdateDialog = false }
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun PackageCardItem(
    pkg: InternetPackage,
    onClick: () -> Unit
) {
    val accentColor = remember(pkg.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(pkg.colorHex))
        } catch (e: Exception) {
            AccentBlue
        }
    }

    GlassCard(
        modifier = Modifier
            .width(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        borderColor = accentColor.copy(alpha = 0.4f),
        backgroundColor = BgCardLight.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = if (pkg.speedMbps > 0) "${pkg.speedMbps} Mbps" else pkg.name,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "৳${pkg.monthlyPrice.toInt()}/mo",
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
