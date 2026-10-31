package internet.billing.manager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.Expense
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
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
    val isBangla = LanguageManager.isBengali()
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()
    val outflow by viewModel.expenseOutflow.collectAsState()
    val vouchersCount by viewModel.vouchersCount.collectAsState()
    val highestExpense by viewModel.highestExpense.collectAsState()
    val avgExpense by viewModel.averageExpense.collectAsState()
    val expenses by viewModel.monthlyExpenses.collectAsState()
    val categorySums by viewModel.categorySums.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") }
    val tabs = listOf("Dashboard", "Details", "Analytics")

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categoryColors = mapOf(
        Expense.CAT_BANDWIDTH_BILL to AccentBlue,
        Expense.CAT_OFFICE_RENT to AccentOrange,
        Expense.CAT_MAINTENANCE to AccentCyan,
        Expense.CAT_EQUIPMENT to AccentPurple,
        Expense.CAT_INTERNET to AccentGreen,
        Expense.CAT_OTHER to Color(0xFF90A4AE)
    )

    fun getCategoryIcon(category: String): ImageVector = when (category) {
        Expense.CAT_BANDWIDTH_BILL -> Icons.Default.Speed
        Expense.CAT_OFFICE_RENT -> Icons.Default.Business
        Expense.CAT_MAINTENANCE -> Icons.Default.Tune
        Expense.CAT_EQUIPMENT -> Icons.Default.Dns
        Expense.CAT_INTERNET -> Icons.Default.Wifi
        else -> Icons.Default.Receipt
    }

    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter) {
        expenses.filter { exp ->
            val matchesCategory = if (selectedCategoryFilter == "All") true else exp.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                exp.title.contains(searchQuery, ignoreCase = true) ||
                exp.note.contains(searchQuery, ignoreCase = true) ||
                exp.amount.toString().contains(searchQuery)
            }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = appStrings.expenseTitle,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBangla) "অফিস, ব্যান্ডউইথ ও রক্ষণাবেক্ষণ খরচ" else "Track office, bandwidth & maintenance costs",
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Month Switcher & "+ Add" Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = monthDisplay,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { viewModel.showAddExpenseDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isBangla) "যোগ" else "Add", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Sub-tabs: Dashboard, Details, Analytics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    val tabText = when (tab) {
                        "Dashboard" -> if (isBangla) "ড্যাশবোর্ড" else "Dashboard"
                        "Details" -> if (isBangla) "ভাউচার তালিকা" else "Details"
                        "Analytics" -> if (isBangla) "অ্যানালিটিক্স" else "Analytics"
                        else -> tab
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AccentCyan else CardBorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabText,
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Tab Contents based on activeTab
        when (activeTab) {
            "Dashboard" -> {
                // Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // This Month Outflow
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBangla) "চলতি মাসের খরচ" else "This Month Outflow", color = TextSecondary, fontSize = 10.5.sp)
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(AccentRed.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = AccentRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "৳${outflow.toInt()}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Vouchers Logged
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBangla) "মোট ভাউচার" else "Vouchers Logged", color = TextSecondary, fontSize = 10.5.sp)
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(AccentBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$vouchersCount",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Highest Expense
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBangla) "সর্বোচ্চ খরচ" else "Highest Expense", color = TextSecondary, fontSize = 10.5.sp)
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AccentGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳${highestExpense.toInt()}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(if (isBangla) "একক সর্বোচ্চ বিল" else "Single Largest Cost", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }

                            // Average Expense
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBangla) "গড় খরচ" else "Average Expense", color = TextSecondary, fontSize = 10.5.sp)
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = null,
                                            tint = AccentGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳${avgExpense.toInt()}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(if (isBangla) "ভাউচার প্রতি গড়" else "Per Voucher Avg", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }
                        }
                    }
                }

                // Donut Chart Breakdown
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        borderColor = CardBorder
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "খরচের খাত ভিত্তিক বণ্টন" else "Expense Breakdown",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = monthDisplay,
                                    color = AccentCyan,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                // Donut Chart Graphic
                                Box(
                                    modifier = Modifier.size(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(90.dp)) {
                                        val strokeWidth = 14.dp.toPx()
                                        if (outflow <= 0 || categorySums.isEmpty()) {
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
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(if (isBangla) "মোট" else "Total", color = TextMuted, fontSize = 9.sp)
                                    }
                                }

                                // Category Legends List
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Expense.ALL_CATEGORIES.forEach { category ->
                                        val sumItem = categorySums.firstOrNull { it.category == category }
                                        val percent = if (outflow > 0 && sumItem != null) {
                                            ((sumItem.total / outflow) * 100).toInt()
                                        } else 0
                                        val color = categoryColors[category] ?: Color.Gray

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.width(130.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = category,
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Text(
                                                text = "$percent%",
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Expenses Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "সাম্প্রতিক খরচসমূহ" else "Recent Expenses",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (expenses.size > 5) {
                            Text(
                                text = if (isBangla) "সবগুলো দেখুন (${expenses.size}) →" else "View All (${expenses.size}) →",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { activeTab = "Details" }
                            )
                        }
                    }
                }

                if (expenses.isEmpty()) {
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
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBangla) "$monthDisplay মাসে কোনো খরচ রেকর্ড নেই" else "No expenses recorded for $monthDisplay",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isBangla) "অফিস, ফাইবার বা ব্যান্ডউইথ খরচ যোগ করতে '+ যোগ' চাপুন" else "Tap '+ Add' to record office, bandwidth or maintenance expenses",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    val displayExpenses = expenses.take(5)
                    items(displayExpenses, key = { it.id }) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            categoryColors = categoryColors,
                            getCategoryIcon = ::getCategoryIcon,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }

            "Details" -> {
                // Search field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = if (isBangla) "খরচের নাম বা নোট দিয়ে খুঁজুন..." else "Search expense title or note...",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(15.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentCyan
                        )
                    )
                }

                // Category Filter Chips
                item {
                    val filterOptions = listOf("All") + Expense.ALL_CATEGORIES
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(filterOptions) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            val catColor = if (cat == "All") AccentCyan else (categoryColors[cat] ?: AccentCyan)
                            val catLabel = when (cat) {
                                "All" -> if (isBangla) "সকল" else "All"
                                Expense.CAT_BANDWIDTH_BILL -> if (isBangla) "ব্যান্ডউইথ" else "Bandwidth"
                                Expense.CAT_OFFICE_RENT -> if (isBangla) "অফিস ভাড়া" else "Rent"
                                Expense.CAT_MAINTENANCE -> if (isBangla) "মেরামত" else "Maintenance"
                                Expense.CAT_EQUIPMENT -> if (isBangla) "ইকুইপমেন্ট" else "Equipment"
                                Expense.CAT_INTERNET -> if (isBangla) "ইন্টারনেট" else "Internet"
                                Expense.CAT_OTHER -> if (isBangla) "অন্যান্য" else "Other"
                                else -> cat
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.22f) else BgCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) catColor else CardBorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catLabel,
                                    color = if (isSelected) catColor else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Summary Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla)
                                "${filteredExpenses.size} টি রেকর্ড • মোট ৳${filteredExpenses.sumOf { it.amount }.toInt()}"
                            else
                                "${filteredExpenses.size} records • Total ৳${filteredExpenses.sumOf { it.amount }.toInt()}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(
                            onClick = { viewModel.showAddExpenseDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BgDark, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isBangla) "নতুন খরচ" else "New", color = BgDark, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Filtered List items
                if (filteredExpenses.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterListOff,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBangla) "কোনো খরচের ভাউচার পাওয়া যায়নি" else "No matching expense records",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isBangla) "সার্চ বা ফিল্টার পরিবর্তন করে আবার চেষ্টা করুন" else "Try changing search keywords or category filter",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            categoryColors = categoryColors,
                            getCategoryIcon = ::getCategoryIcon,
                            onDelete = { viewModel.deleteExpense(expense) },
                            showNotes = true
                        )
                    }
                }
            }

            "Analytics" -> {
                // Period Card with Month Navigator
                item {
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
                            Column {
                                Text(
                                    text = if (isBangla) "মাসিক আর্থিক বিশ্লেষণ" else "Monthly Expense Analytics",
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBangla) "$monthDisplay এর বিস্তারিত ব্যয় বিবরণী" else "Overview of spending for $monthDisplay",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.previousMonth() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = AccentCyan, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = monthDisplay,
                                    color = AccentCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.nextMonth() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = AccentCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Analytics Core Metric Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isBangla) "মোট ব্যয়ের পরিমাণ" else "Total Outflow", color = TextSecondary, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("৳${outflow.toInt()}", color = AccentRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$vouchersCount ${if (isBangla) "টি ভাউচার" else "vouchers"}", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }

                            val bandwidthSum = categorySums.firstOrNull { it.category == Expense.CAT_BANDWIDTH_BILL }?.total ?: 0.0
                            val bwSharePct = if (outflow > 0) ((bandwidthSum / outflow) * 100).toInt() else 0
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isBangla) "ব্যান্ডউইথ শেয়ার" else "Bandwidth Share", color = TextSecondary, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("৳${bandwidthSum.toInt()}", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$bwSharePct% ${if (isBangla) "মোট খরচের" else "of total outflow"}", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isBangla) "সর্বোচ্চ একক খরচ" else "Highest Cost", color = TextSecondary, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("৳${highestExpense.toInt()}", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(if (isBangla) "একক বৃহত্তম বিল" else "Single invoice", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }

                            GlassCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                borderColor = CardBorderSubtle
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isBangla) "গড় ভাউচার মূল্য" else "Avg Voucher", color = TextSecondary, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("৳${avgExpense.toInt()}", color = AccentGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(if (isBangla) "খরচ প্রতি গড়" else "Average per bill", color = TextMuted, fontSize = 9.5.sp)
                                }
                            }
                        }
                    }
                }

                // Category Spending Distribution with Progress Bars
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = CardBorder
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isBangla) "খাত অনুযায়ী খরচের হার ও পরিমাণ" else "Category Spending & Percentages",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Expense.ALL_CATEGORIES.forEach { category ->
                                val sumItem = categorySums.firstOrNull { it.category == category }
                                val amount = sumItem?.total ?: 0.0
                                val fraction = if (outflow > 0 && amount > 0) (amount / outflow).toFloat() else 0f
                                val percent = (fraction * 100).toInt()
                                val catColor = categoryColors[category] ?: Color.Gray

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = category,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = "৳${amount.toInt()} ($percent%)",
                                            color = if (amount > 0) TextPrimary else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Progress bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E293B))
                                    ) {
                                        if (fraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction.coerceIn(0.01f, 1f))
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(catColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cost Breakdown Insight: Bandwidth vs Operational
                item {
                    val bandwidthSum = categorySums.firstOrNull { it.category == Expense.CAT_BANDWIDTH_BILL }?.total ?: 0.0
                    val opSum = (outflow - bandwidthSum).coerceAtLeast(0.0)
                    val bwPct = if (outflow > 0) ((bandwidthSum / outflow) * 100).toInt() else 0
                    val opPct = if (outflow > 0) 100 - bwPct else 0

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = CardBorderSubtle,
                        backgroundColor = BgCardLight.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "আইএসপি ব্যয় বিশ্লেষণ কাঠামো" else "ISP Expense Structure",
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isBangla) "কোর ব্যান্ডউইথ খরচ" else "Core Bandwidth Cost", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("৳${bandwidthSum.toInt()} ($bwPct%)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isBangla) "অপারেশনাল ও অন্যান্য" else "Operational & Misc", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("৳${opSum.toInt()} ($opPct%)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    expense: Expense,
    categoryColors: Map<String, Color>,
    getCategoryIcon: (String) -> ImageVector,
    onDelete: () -> Unit,
    showNotes: Boolean = false
) {
    val dateStr = remember(expense.dateMillis) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.dateMillis))
    }
    val catColor = categoryColors[expense.category] ?: Color.Gray

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        borderColor = CardBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(expense.category),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = expense.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(catColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = expense.category,
                                    color = catColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateStr,
                                color = TextMuted,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳${expense.amount.toInt()}",
                        color = AccentRed,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (showNotes && expense.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BgDark.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Note: ${expense.note}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
