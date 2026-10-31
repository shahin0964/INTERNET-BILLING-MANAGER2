package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.components.GlowStatCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel

@Composable
fun BillingScreen(
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

    val bandwidthBill = ispSettings?.bandwidthBill ?: 0.0
    val discount = ispSettings?.discount ?: 0.0
    val previousDue = (totalDue - monthlyBill).coerceAtLeast(0.0)
    val monthlyProfit = (totalCollected - bandwidthBill - outflow).coerceAtLeast(0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header: Billing & Collection + Calendar icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Billing & Collection",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgCard)
                    .clickable { /* Date filter */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Month Selector (< October 2026 >)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            borderColor = CardBorderSubtle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = monthDisplay,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 4 Primary Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlowStatCard(
                title = "Total Bill",
                value = "৳${monthlyBill.toInt()}",
                accentColor = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            GlowStatCard(
                title = "Total Collected",
                value = "৳${totalCollected.toInt()}",
                accentColor = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlowStatCard(
                title = "Previous Due",
                value = "৳${previousDue.toInt()}",
                accentColor = AccentPurple,
                modifier = Modifier.weight(1f)
            )
            GlowStatCard(
                title = "Total Due",
                value = "৳${totalDue.toInt()}",
                accentColor = AccentRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Secondary Metrics (Bandwidth Bill, Monthly Profit, Discount)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlowStatCard(
                title = "Bandwidth Bill",
                value = "৳${bandwidthBill.toInt()}",
                accentColor = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            GlowStatCard(
                title = "Monthly Profit",
                value = "৳${monthlyProfit.toInt()}",
                accentColor = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlowStatCard(
            title = "Discount",
            value = "৳${discount.toInt()}",
            accentColor = AccentOrange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Navigation Action Cards: Daily Bill Entry & Expense Management
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            borderColor = CardBorder,
            onClick = onNavigateToDailyEntry
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Bill Entry",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generate daily customer bills",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            borderColor = CardBorder,
            onClick = onNavigateToExpense
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Expense Management",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track and manage expenses",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Record Customer Payment Bottom Box
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            borderColor = AccentGreen.copy(alpha = 0.3f),
            backgroundColor = BgCardLight.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Record Customer Payment",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Outstanding Balance: ৳${totalDue.toInt()}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.showCollectPaymentDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = BgDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collect",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
