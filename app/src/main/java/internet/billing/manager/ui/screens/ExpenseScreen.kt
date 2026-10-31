package internet.billing.manager.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.Expense
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseScreen(
    viewModel: BillingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()
    val outflow by viewModel.expenseOutflow.collectAsState()
    val vouchersCount by viewModel.vouchersCount.collectAsState()
    val highestExpense by viewModel.highestExpense.collectAsState()
    val avgExpense by viewModel.averageExpense.collectAsState()
    val expenses by viewModel.monthlyExpenses.collectAsState()
    val categorySums by viewModel.categorySums.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") }
    val tabs = listOf("Dashboard", "Details", "Analytics")

    val categoryColors = mapOf(
        Expense.CAT_OFFICE_RENT to AccentOrange,
        Expense.CAT_MAINTENANCE to AccentBlue,
        Expense.CAT_EQUIPMENT to AccentPurple,
        Expense.CAT_INTERNET to AccentGreen,
        Expense.CAT_OTHER to Color(0xFF90A4AE)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Expense Management",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track office, maintenance & other costs",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // "+ Add" Button
                Button(
                    onClick = { viewModel.showAddExpenseDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Sub-tabs: Dashboard, Details, Analytics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCard)
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Stats Rows 1 & 2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // This Month Outflow
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = CardBorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("This Month Outflow", color = TextSecondary, fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AccentRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "৳${outflow.toInt()}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Vouchers Logged
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = CardBorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vouchers Logged", color = TextSecondary, fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$vouchersCount",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Highest Expense
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = CardBorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Highest Expense", color = TextSecondary, fontSize = 11.sp)
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "৳${highestExpense.toInt()}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Single Largest Cost", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    // Average Expense
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = CardBorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Average Expense", color = TextSecondary, fontSize = 11.sp)
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "৳${avgExpense.toInt()}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Per Transaction Avg", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. Expense Breakdown Card with Donut Chart
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
                            text = "Expense Breakdown",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.nextMonth() }
                        ) {
                            Text(
                                text = monthDisplay,
                                color = AccentCyan,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut Chart Graphic
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                val strokeWidth = 18.dp.toPx()
                                if (outflow <= 0 || categorySums.isEmpty()) {
                                    // Empty state ring
                                    drawCircle(
                                        color = Color(0xFF1E293B),
                                        style = Stroke(width = strokeWidth)
                                    )
                                } else {
                                    var startAngle = -90f
                                    categorySums.forEach { item ->
                                        val sweep = ((item.total / outflow) * 360f).toFloat()
                                        val color = categoryColors[item.category] ?: Color.Gray
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                        startAngle += sweep
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "৳${outflow.toInt()}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Total", color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        // Category Legends List
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Expense.ALL_CATEGORIES.forEach { category ->
                                val sumItem = categorySums.firstOrNull { it.category == category }
                                val percent = if (outflow > 0 && sumItem != null) {
                                    ((sumItem.total / outflow) * 100).toInt()
                                } else 0
                                val color = categoryColors[category] ?: Color.Gray

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(140.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = category,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "$percent%",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Expenses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Expenses",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.showAddExpenseDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BgDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", color = BgDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No expenses recorded for $monthDisplay",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add' to record office, fiber, or router expenses",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                val dateStr = remember(expense.dateMillis) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.dateMillis))
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AccentPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = expense.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${expense.category} • $dateStr",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳${expense.amount.toInt()}",
                                color = AccentRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
