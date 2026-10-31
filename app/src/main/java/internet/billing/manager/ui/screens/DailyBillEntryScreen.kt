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
import internet.billing.manager.ui.components.PaymentStatusBadge
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyBillEntryScreen(
    viewModel: BillingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyEntries by viewModel.dailyBillEntries.collectAsState()
    val activeFilter by viewModel.dailyBillFilter.collectAsState()

    val totalAmount = remember(dailyEntries) {
        dailyEntries.sumOf { it.billAmount }
    }

    val todayHeaderStr = remember {
        SimpleDateFormat("dd MMMM yyyy – EEEE", Locale.getDefault()).format(Date())
    }

    val filters = listOf("All", "Today", "Yesterday", "This Month")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Bar: Back button + Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
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
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Daily Bill Entry",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Filter Pills (All, Today, Yesterday, This Month)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = activeFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCard)
                        .clickable { viewModel.setDailyBillFilter(filter) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) AccentCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Date Grouping Header Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            borderColor = CardBorderSubtle,
            backgroundColor = BgCardLight.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = todayHeaderStr,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${dailyEntries.size} Bill Entries • Total: ৳${totalAmount.toInt()}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Daily Entries List
        if (dailyEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No bill entries for $activeFilter",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Generate Bill' or 'Collect Payment' to record entries",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dailyEntries, key = { it.id }) { entry ->
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.15f)),
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
                                        text = entry.customerName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Code: ${entry.customerCode}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "৳${entry.billAmount.toInt()}",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                PaymentStatusBadge(isPaid = entry.isPaid)
                            }
                        }
                    }
                }
            }
        }
    }
}
