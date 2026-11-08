package internet.billing.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Business
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.Expense
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.localization.LanguageManager
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CollectPaymentDialog(
    customers: List<Customer>,
    initialCustomer: Customer? = null,
    onDismiss: () -> Unit,
    onCollect: (customerId: Long, amount: Double, method: String, note: String) -> Unit
) {
    var selectedCustomer by remember {
        mutableStateOf(initialCustomer ?: customers.firstOrNull())
    }
    var amountText by remember {
        mutableStateOf(selectedCustomer?.dueAmount?.takeIf { it > 0 }?.toInt()?.toString() ?: selectedCustomer?.monthlyBill?.toInt()?.toString() ?: "500")
    }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }

    val paymentMethods = listOf("Cash", "bKash", "Nagad", "Bank")

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.recordCollectionTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = appStrings.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(appStrings.selectCustomer, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Customer Selection Dropdown or List
                if (customers.isEmpty()) {
                    Text(appStrings.noCustomersFound, color = AccentRed, fontSize = 12.sp)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgCardLight)
                            .padding(6.dp)
                    ) {
                        customers.take(5).forEach { cust ->
                            val isSelected = selectedCustomer?.id == cust.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                    .clickable {
                                        selectedCustomer = cust
                                        amountText = if (cust.dueAmount > 0) cust.dueAmount.toInt().toString() else cust.monthlyBill.toInt().toString()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cust.name.ifBlank { cust.pppoeUsername },
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${cust.customerCode} • ${cust.packageName}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "${appStrings.dueLabel}: ৳${cust.dueAmount.toInt()}",
                                    color = if (cust.dueAmount > 0) AccentOrange else AccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("${appStrings.paymentAmount} (৳)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.5.sp),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                        focusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.55f else 0.85f),
                        unfocusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.35f else 0.65f),
                        cursorColor = AccentGreen
                    )
                )

                if (selectedCustomer != null && selectedCustomer!!.dueAmount > selectedCustomer!!.monthlyBill && selectedCustomer!!.monthlyBill > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .clickable {
                                    amountText = selectedCustomer!!.monthlyBill.toInt().toString()
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "১ মাসের বিল (৳${selectedCustomer!!.monthlyBill.toInt()})",
                                color = AccentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentOrange.copy(alpha = 0.15f))
                                .clickable {
                                    amountText = selectedCustomer!!.dueAmount.toInt().toString()
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "মোট বকেয়া (৳${selectedCustomer!!.dueAmount.toInt()})",
                                color = AccentOrange,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(appStrings.paymentMethod, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentGreen.copy(alpha = 0.25f) else BgCardLight)
                                .clickable { selectedMethod = method }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) AccentGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(appStrings.noteRemarks, color = TextSecondary) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.5.sp),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                        focusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.55f else 0.85f),
                        unfocusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.35f else 0.65f),
                        cursorColor = AccentGreen
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val cust = selectedCustomer ?: return@Button
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onCollect(cust.id, amount, selectedMethod, note)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled = selectedCustomer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        text = "${appStrings.confirmAndCollect} ৳${amountText.ifBlank { "0" }}",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
    }
}

@Composable
fun AddExpenseDialog(
    initialCategory: String = Expense.CAT_BANDWIDTH_BILL,
    initialTitle: String = "",
    currentMonthKey: String = "",
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle.ifBlank { if (initialCategory == Expense.CAT_BANDWIDTH_BILL) "Bandwidth Bill" else "" }) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val currentMonthYear = remember { monthYearFormat.format(Date()) }

    val expenseTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentPurple,
        unfocusedBorderColor = CardBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = AccentPurple,
        unfocusedLabelColor = TextSecondary,
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.55f else 0.85f),
        unfocusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.35f else 0.65f),
        cursorColor = AccentPurple
    )
    val expenseTextStyle = androidx.compose.ui.text.TextStyle(
        color = TextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium
    )

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.addExpense,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = appStrings.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(appStrings.expenseName, color = TextSecondary) },
                    singleLine = true,
                    textStyle = expenseTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = expenseTextFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(appStrings.expenseCategory, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(5.dp))
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Expense.ALL_CATEGORIES.chunked(2).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AccentPurple.copy(alpha = 0.25f) else BgCardLight)
                                        .clickable {
                                            selectedCategory = cat
                                            if (cat == Expense.CAT_BANDWIDTH_BILL && (title.isBlank() || Expense.ALL_CATEGORIES.contains(title))) {
                                                title = "Bandwidth Bill"
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) AccentPurple else TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            if (rowCats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(appStrings.expenseAmount, color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = expenseTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = expenseTextFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(appStrings.noteRemarks, color = TextSecondary) },
                    singleLine = true,
                    textStyle = expenseTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = expenseTextFieldColors
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.ifBlank { selectedCategory }
                        if (finalTitle.isNotBlank() && amount > 0) {
                            onSave(
                                Expense(
                                    title = finalTitle.trim(),
                                    category = selectedCategory,
                                    amount = amount,
                                    dateMillis = System.currentTimeMillis(),
                                    monthYear = if (currentMonthKey.isNotBlank()) currentMonthKey else currentMonthYear,
                                    note = note.trim()
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    enabled = (title.isNotBlank() || selectedCategory.isNotBlank()) && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        text = appStrings.saveExpense,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
    }
}

@Composable
fun GenerateBillDialog(
    allCustomers: List<Customer> = emptyList(),
    activeCustomerCount: Int,
    totalMonthlyBill: Double,
    monthYear: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onConfirmSingle: ((customerId: Long, amount: Double, notes: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA
    var selectedTab by remember { mutableStateOf(0) } // 0 = Single User, 1 = All Users

    var customerSearchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var billAmountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // When a customer is picked, initialize their monthly bill
    LaunchedEffect(selectedCustomer) {
        if (selectedCustomer != null) {
            billAmountText = selectedCustomer!!.monthlyBill.takeIf { it > 0 }?.toInt()?.toString() ?: "500"
            if (notesText.isBlank()) {
                notesText = if (isBangla) "$monthYear মাসের বিল" else "Bill for $monthYear"
            }
        }
    }

    val filteredCustomers = remember(allCustomers, customerSearchQuery) {
        if (customerSearchQuery.isBlank()) {
            allCustomers.sortedBy { it.name.ifBlank { it.pppoeUsername } }
        } else {
            val q = customerSearchQuery.trim()
            allCustomers.filter { cust ->
                cust.name.contains(q, ignoreCase = true) ||
                cust.pppoeUsername.contains(q, ignoreCase = true) ||
                cust.customerCode.contains(q, ignoreCase = true) ||
                cust.phone.contains(q, ignoreCase = true)
            }.sortedBy { it.name.ifBlank { it.pppoeUsername } }
        }
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

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header with Icon & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "বিল জেনারেট / তৈরি" else "Generate Bills",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Month: $monthYear",
                                    fontSize = 11.sp,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Mode Selector Tabs: [নির্দিষ্ট গ্রাহক (Single)] / [সকল গ্রাহক (All Users)]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgCardLight)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 0) AccentCyan else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBangla) "নির্দিষ্ট গ্রাহক" else "Single User",
                                color = if (selectedTab == 0) BgDark else TextSecondary,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) AccentCyan else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBangla) "সকল সক্রিয় গ্রাহক" else "All Active Users",
                                color = if (selectedTab == 1) BgDark else TextSecondary,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedTab == 0) {
                        // --- TAB 0: Single User Mode ---
                        if (selectedCustomer == null) {
                            // Instruction & Search Bar
                            Text(
                                text = if (isBangla) "যে গ্রাহকের বিল তৈরি করতে চান তাকে নির্বাচন করুন:"
                                else "Select the customer you want to generate a bill for:",
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customerSearchQuery,
                                onValueChange = { customerSearchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (isBangla) "নাম, ইউজারনেম বা কোড খুঁজুন..." else "Search name, username, code...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (customerSearchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { customerSearchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Customer List
                            if (filteredCustomers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (allCustomers.isEmpty()) {
                                            if (isBangla) "কোনো গ্রাহক পাওয়া যায়নি। আগে গ্রাহক যোগ করুন।" else "No customers found. Add customers first."
                                        } else {
                                            if (isBangla) "কোনো গ্রাহকের মিল পাওয়া যায়নি" else "No matching customers found"
                                        },
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(10.dp))
                                        .background(BgCardLight.copy(alpha = 0.3f))
                                        .verticalScroll(rememberScrollState())
                                        .padding(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    filteredCustomers.forEach { cust ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedCustomer = cust }
                                                .background(BgCard)
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = cust.name.ifBlank { cust.pppoeUsername },
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.5.sp
                                                )
                                                Text(
                                                    text = "PPPoE: ${cust.pppoeUsername} • Code: ${cust.customerCode}",
                                                    color = TextMuted,
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "৳${cust.monthlyBill.toInt()}",
                                                    color = AccentCyan,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp
                                                )
                                                Text(
                                                    text = cust.status,
                                                    color = if (cust.status == Customer.STATUS_ACTIVE) AccentGreen else AccentOrange,
                                                    fontSize = 9.5.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Customer is Selected
                            val cust = selectedCustomer!!
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                    .background(AccentCyan.copy(alpha = 0.08f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.name.ifBlank { cust.pppoeUsername },
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = "Username: ${cust.pppoeUsername} • Code: ${cust.customerCode}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        if (cust.phone.isNotBlank()) {
                                            Text(
                                                text = "Phone: ${cust.phone}",
                                                color = TextMuted,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { selectedCustomer = null },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Text(
                                            text = if (isBangla) "পরিবর্তন" else "Change",
                                            fontSize = 11.sp,
                                            color = AccentCyan
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bill Amount Input
                            Text(
                                text = if (isBangla) "বিলের পরিমাণ (৳) *" else "Bill Amount (৳) *",
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = billAmountText,
                                onValueChange = { billAmountText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Notes Input
                            Text(
                                text = if (isBangla) "বিবরণ / নোট (ঐচ্ছিক)" else "Notes / Remarks (Optional)",
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = dialogTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons for Single User
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Text(appStrings.cancel, color = TextSecondary, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val cust = selectedCustomer
                                    val amt = billAmountText.toDoubleOrNull() ?: 0.0
                                    if (cust == null) {
                                        Toast.makeText(context, if (isBangla) "অনুগ্রহ করে একজন গ্রাহক নির্বাচন করুন" else "Please select a customer", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, if (isBangla) "সঠিক বিলের পরিমাণ লিখুন" else "Please enter a valid bill amount", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onConfirmSingle?.invoke(cust.id, amt, notesText.trim())
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "${cust.name.ifBlank { cust.pppoeUsername }} এর জন্য ৳${amt.toInt()} বিল তৈরি হয়েছে"
                                        else "Bill of ৳${amt.toInt()} generated for ${cust.name.ifBlank { cust.pppoeUsername }}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                enabled = selectedCustomer != null,
                                modifier = Modifier.weight(1.5f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    disabledContainerColor = AccentCyan.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = if (isBangla) "বিল তৈরি করুন" else "Generate Bill",
                                    color = if (selectedCustomer != null) BgDark else TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                    } else {
                        // --- TAB 1: All Active Users Mode ---
                        Text(
                            text = if (isBangla)
                                "সকল সক্রিয় গ্রাহকদের জন্য $monthYear মাসের বিল তৈরি হবে। এর ফলে গ্রাহকদের বকেয়া আপডেট হবে এবং দৈনিক বিলিং এন্ট্রি তৈরি হবে।"
                            else
                                "Generate bill entries for $monthYear for all active customers. This will update customer due balances and create daily billing entries.",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgCardLight)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${appStrings.activeCustomersToBill}:", color = TextSecondary, fontSize = 11.5.sp)
                                Text("$activeCustomerCount ${appStrings.usersCountSuffix}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${appStrings.estimatedTotalBill}:", color = TextSecondary, fontSize = 11.5.sp)
                                Text("৳${totalMonthlyBill.toInt()}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Text(appStrings.cancel, color = TextSecondary, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    onConfirm()
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "সকল সক্রিয় গ্রাহকের বিল তৈরি করা হয়েছে" else "Bills generated for all active subscribers",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1.5f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                            ) {
                                Text(appStrings.generateNow, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditBusinessInfoDialog(
    currentSettings: IspSettings?,
    onDismiss: () -> Unit,
    onSave: (IspSettings) -> Unit
) {
    val initialName = currentSettings?.companyName?.takeIf { !it.equals("U-turn Technologies", ignoreCase = true) } ?: ""
    val initialPhone = currentSettings?.phone?.takeIf { it != "01700000000" } ?: ""
    val initialCurrency = currentSettings?.currencySymbol?.ifBlank { "৳" } ?: "৳"
    val initialEmail = currentSettings?.email ?: ""
    val initialAddress = currentSettings?.address?.takeIf { !it.equals("Dhaka, Bangladesh", ignoreCase = true) } ?: ""
    val initialNetworkStatus = currentSettings?.networkStatus?.ifBlank { "Operational" } ?: "Operational"
    val initialLogoUri = currentSettings?.logoUri ?: ""

    var companyName by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var currencySymbol by remember { mutableStateOf(initialCurrency) }
    var email by remember { mutableStateOf(initialEmail) }
    var address by remember { mutableStateOf(initialAddress) }
    var networkStatus by remember { mutableStateOf(initialNetworkStatus) }
    var logoUri by remember { mutableStateOf(initialLogoUri) }

    val context = LocalContext.current
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if persistable permission is not supported
            }
            logoUri = uri.toString()
        }
    }

    val logoBitmap = remember(logoUri) {
        if (logoUri.isNotBlank()) {
            try {
                val uri = Uri.parse(logoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        } else null
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
        focusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.55f else 0.85f),
        unfocusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.35f else 0.65f),
        cursorColor = AccentCyan
    )
    val dialogTextStyle = androidx.compose.ui.text.TextStyle(
        color = TextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.90f)
                    .padding(vertical = 8.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.ispInfoTitle,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = appStrings.close,
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Company Logo
                Text(
                    text = appStrings.companyLogo,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(BgCardLight)
                            .border(1.dp, CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = appStrings.companyLogo,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { logoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = appStrings.changeLogo,
                                color = BgDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }

                        if (logoUri.isNotBlank()) {
                            TextButton(
                                onClick = { logoUri = "" },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = appStrings.removeLogo,
                                    color = AccentRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. ISP Name / Reseller Brand
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text(appStrings.ispNameLabel, color = TextSecondary) },
                    placeholder = { Text(appStrings.ispNameLabel, color = TextMuted) },
                    singleLine = true,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Support Hotline & Currency ($)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(appStrings.supportHotline, color = TextSecondary) },
                        placeholder = { Text("01XXXXXXXXX", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        textStyle = dialogTextStyle,
                        modifier = Modifier.weight(1.7f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogTextFieldColors
                    )

                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text(appStrings.currencySymbol, color = TextSecondary) },
                        placeholder = { Text("৳", color = TextMuted) },
                        singleLine = true,
                        textStyle = dialogTextStyle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogTextFieldColors
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Company Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(appStrings.companyEmail, color = TextSecondary) },
                    placeholder = { Text("info@isp.com", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Office Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(appStrings.officeAddress, color = TextSecondary) },
                    placeholder = { Text(appStrings.officeAddress, color = TextMuted) },
                    singleLine = true,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Network Status
                Text(
                    text = appStrings.networkStatusLabel,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf(
                        "Operational" to appStrings.statusOperational,
                        "Maintenance" to appStrings.statusMaintenance,
                        "Degraded" to appStrings.statusDegraded
                    )
                    statusOptions.forEach { (rawVal, localizedLabel) ->
                        val isSelected = networkStatus.equals(rawVal, ignoreCase = true)
                        val activeColor = when (rawVal) {
                            "Operational" -> AccentGreen
                            "Maintenance" -> AccentOrange
                            else -> AccentRed
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else BgCardLight)
                                .clickable { networkStatus = rawVal }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = localizedLabel,
                                color = if (isSelected) activeColor else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 7. Bottom Buttons: Cancel & Save Business Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(appStrings.cancel, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val updated = (currentSettings ?: IspSettings()).copy(
                                companyName = companyName,
                                phone = phone,
                                currencySymbol = currencySymbol.ifBlank { "৳" },
                                email = email,
                                address = address,
                                networkStatus = networkStatus,
                                logoUri = logoUri
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text(
                            text = appStrings.saveBusinessInfo,
                            color = BgDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
    }
}
