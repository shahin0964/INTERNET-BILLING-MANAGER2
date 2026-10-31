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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.components.GlowStatCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CollectionScreen(
    viewModel: BillingViewModel,
    onNavigateToDailyEntry: () -> Unit,
    onNavigateToExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()
    val monthlyBill by viewModel.totalActiveMonthlyBill.collectAsState()
    val totalCollected by viewModel.totalCollectedThisMonth.collectAsState()
    val totalDue by viewModel.totalOutstandingDue.collectAsState()
    val outflow by viewModel.expenseOutflow.collectAsState()
    val ispSettings by viewModel.ispSettings.collectAsState()
    val recentPayments by viewModel.recentPayments.collectAsState()

    val bandwidthBill = ispSettings?.bandwidthBill ?: 0.0
    val discount = ispSettings?.discount ?: 0.0
    val previousDue = (totalDue - monthlyBill).coerceAtLeast(0.0)
    val monthlyProfit = (totalCollected - bandwidthBill - outflow).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header: Billing & Collection Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Collection Overview",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track payments, dues & financial metrics",
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgCard)
                        .clickable { viewModel.showCollectPaymentDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Collect",
                        tint = AccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Month Selector (< October 2026 >)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                borderColor = CardBorderSubtle
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = monthDisplay,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. Financial Metrics Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: Total Bill & Total Collected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowStatCard(
                        title = "Total Bill",
                        amount = "৳${monthlyBill.toInt()}",
                        accentColor = AccentCyan,
                        icon = Icons.Default.Receipt,
                        subtitle = "Billed this month",
                        modifier = Modifier.weight(1f)
                    )
                    GlowStatCard(
                        title = "Total Collected",
                        amount = "৳${totalCollected.toInt()}",
                        accentColor = AccentGreen,
                        icon = Icons.Default.CheckCircle,
                        subtitle = "Payments received",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Previous Due & Total Due
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowStatCard(
                        title = "Previous Due",
                        amount = "৳${previousDue.toInt()}",
                        accentColor = AccentPurple,
                        icon = Icons.Default.History,
                        subtitle = "Unpaid past balance",
                        modifier = Modifier.weight(1f)
                    )
                    GlowStatCard(
                        title = "Total Due",
                        amount = "৳${totalDue.toInt()}",
                        accentColor = AccentOrange,
                        icon = Icons.Default.Warning,
                        subtitle = "Total pending",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Bandwidth Bill & Monthly Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowStatCard(
                        title = "Bandwidth Bill",
                        amount = "৳${bandwidthBill.toInt()}",
                        accentColor = AccentBlue,
                        icon = Icons.Default.Speed,
                        subtitle = "Upstream cost",
                        modifier = Modifier.weight(1f)
                    )
                    GlowStatCard(
                        title = "Monthly Profit",
                        amount = "৳${monthlyProfit.toInt()}",
                        accentColor = AccentGold,
                        icon = Icons.Default.TrendingUp,
                        subtitle = "Est. net profit",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: Discount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowStatCard(
                        title = "Discount",
                        amount = "৳${discount.toInt()}",
                        accentColor = Color(0xFFE0E0E0),
                        icon = Icons.Default.Discount,
                        subtitle = "Customer rebates",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 4. Quick Action Cards: Daily Bill Entry & Expense Management
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Card 1: Daily Bill Entry
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDailyEntry() },
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Bill Entry",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "View daily collection entries by date",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Card 2: Expense Management
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToExpense() },
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Expense Management",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Track and manage office & maintenance costs",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
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

        // 5. Button: Record Customer Payment
        item {
            Button(
                onClick = { viewModel.showCollectPaymentDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BgDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Record Customer Payment",
                    color = BgDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // 6. Recent Collections List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Collections",
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recentPayments.size} entries",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No collection records yet",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap 'Record Customer Payment' to collect bill",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            items(recentPayments, key = { it.id }) { tx ->
                val dateStr = remember(tx.dateMillis) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.dateMillis))
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tx.customerName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${tx.customerCode} • ${tx.paymentMethod} • $dateStr",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "+৳${tx.amount.toInt()}",
                            color = AccentGreen,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
