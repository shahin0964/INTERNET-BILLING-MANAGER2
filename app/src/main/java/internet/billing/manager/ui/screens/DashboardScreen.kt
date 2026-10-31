package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.components.MetricStatCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: BillingViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ispSettings by viewModel.ispSettings.collectAsState()
    val activeCount by viewModel.activeCustomerCount.collectAsState()
    val totalCount by viewModel.totalCustomerCount.collectAsState()
    val monthlyBill by viewModel.totalActiveMonthlyBill.collectAsState()
    val monthlyCollection by viewModel.totalCollectedThisMonth.collectAsState()
    val todayCollection by viewModel.totalCollectedToday.collectAsState()
    val totalDue by viewModel.totalOutstandingDue.collectAsState()
    val recentPayments by viewModel.recentPayments.collectAsState()
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()

    val todayStr = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
    val shortTodayStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Billing Analytics Calculations
    val totalGenerated = monthlyBill + totalDue
    val collectedPercent = if (totalGenerated > 0) ((monthlyCollection / totalGenerated) * 100).toInt().coerceIn(0, 100) else 0
    val outstandingPercent = (100 - collectedPercent).coerceIn(0, 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: Greeting, Company Name & Date & Notification Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning 👋",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ispSettings?.companyName ?: "U-turn Technologies",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todayStr,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Notification Bell with Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                        .clickable { /* Notification clicked */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                    )
                }
            }
        }

        // 2. Brand Wi-Fi Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                borderColor = AccentCyan.copy(alpha = 0.3f),
                onClick = onNavigateToMore
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ispSettings?.companyName ?: "U-turn Technologies",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Internet Billing Management",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Settings",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Quick Actions (3 Rounded Gradient Action Buttons)
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Action 1: Add Customer (Teal/Cyan)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = AccentCyan.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentCyan.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showAddCustomerDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Customer",
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add\nCustomer",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Action 2: Collect Payment (Green)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = AccentGreen.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentGreen.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showCollectPaymentDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Collect Payment",
                                tint = AccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Collect\nPayment",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Action 3: Generate Bill (Purple)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = AccentPurple.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentPurple.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showGenerateBillDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Generate Bill",
                                tint = AccentPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate\nBill",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Business Overview (2x2 Metrics Grid)
        item {
            Column {
                Text(
                    text = "Business Overview",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Active Users",
                            value = "$activeCount",
                            subtitle = "Total: $totalCount",
                            icon = Icons.Default.CheckCircle,
                            accentColor = AccentGreen,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCustomers
                        )
                        MetricStatCard(
                            title = "Monthly Bill",
                            value = "৳${monthlyBill.toInt()}",
                            subtitle = monthDisplay,
                            icon = Icons.Default.Receipt,
                            accentColor = AccentBlue,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToBilling
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Monthly Collection",
                            value = "৳${monthlyCollection.toInt()}",
                            subtitle = monthDisplay,
                            icon = Icons.Default.Payments,
                            accentColor = AccentPurple,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCollection
                        )
                        MetricStatCard(
                            title = "Today Collection",
                            value = "৳${todayCollection.toInt()}",
                            subtitle = shortTodayStr,
                            icon = Icons.Default.AccessTime,
                            accentColor = AccentOrange,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCollection
                        )
                    }
                }
            }
        }

        // 5. Billing Analytics Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                borderColor = CardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Billing Analytics",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToBilling() }
                        ) {
                            Text(
                                text = "View Details",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total Generated: ৳${totalGenerated.toInt()}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two-Tone Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(AccentOrange.copy(alpha = 0.8f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (collectedPercent > 0) collectedPercent / 100f else 0.01f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(AccentGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Collected ৳${monthlyCollection.toInt()} ($collectedPercent%)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Outstanding ৳${totalDue.toInt()} ($outstandingPercent%)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Collections Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Collections",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToCollection() }
                ) {
                    Text(
                        text = "View All",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recent payments recorded",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Collect Payment' above to record collections",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(recentPayments.take(5)) { payment ->
                val dateStr = remember(payment.dateMillis) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.dateMillis))
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = payment.customerName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$dateStr • ${payment.paymentMethod}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "+৳${payment.amount.toInt()}",
                            color = AccentGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
