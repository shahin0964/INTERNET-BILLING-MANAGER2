package internet.billing.manager.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.components.CustomerStatusBadge
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import internet.billing.manager.ui.viewmodel.CustomerBillingItem
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatBillMonth(monthYear: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val date = parser.parse(monthYear)
        if (date != null) formatter.format(date) else monthYear
    } catch (e: Exception) {
        monthYear
    }
}

@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customerBillingItems by viewModel.customerBillingItems.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Dues") }
    val filters = listOf("All Dues", "Previous Due", "Active Lines", "Suspended")

    // Filter logic based on CustomerBillingItem
    val filteredItems = remember(customerBillingItems, searchQuery, selectedFilter) {
        customerBillingItems.filter { item ->
            val customer = item.customer
            val name = customer.name.ifBlank { customer.pppoeUsername }
            val pppoe = customer.pppoeUsername
            val code = customer.customerCode
            val phone = customer.phone
            val ip = customer.staticIp

            val matchesSearch = searchQuery.isBlank() ||
                    name.contains(searchQuery, ignoreCase = true) ||
                    pppoe.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true) ||
                    phone.contains(searchQuery, ignoreCase = true) ||
                    ip.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Previous Due" -> item.previousDue > 0
                "Active Lines" -> customer.status.equals(Customer.STATUS_ACTIVE, ignoreCase = true)
                "Suspended" -> customer.status.equals(Customer.STATUS_SUSPENDED, ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Card: Monthly Billing Controls & Manual Add
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                borderColor = AccentCyan.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = appStrings.billingTitle,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${customerBillingItems.size} জন বকেয়া",
                                    color = AccentOrange,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "প্রতি মাসের ১ তারিখ রাত ১২টায় স্বয়ংক্রিয় বিল তৈরি হয়",
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.showGenerateBillDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = BgDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appStrings.generateBill.replace("\n", " "),
                            color = BgDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = appStrings.searchBillHint,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AccentCyan,
                        modifier = Modifier.size(17.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentCyan,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard
                )
            )
        }

        // 3. Filter Pills (All Dues, Previous Due, Active Lines, Suspended)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    val filterCount = when (filter) {
                        "All Dues" -> customerBillingItems.size
                        "Previous Due" -> customerBillingItems.count { it.previousDue > 0 }
                        "Active Lines" -> customerBillingItems.count { it.customer.status.equals(Customer.STATUS_ACTIVE, ignoreCase = true) }
                        "Suspended" -> customerBillingItems.count { it.customer.status.equals(Customer.STATUS_SUSPENDED, ignoreCase = true) }
                        else -> 0
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCard)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$filter ($filterCount)",
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Customer Billing Cards (Guaranteed 1 card per customer with dues)
        if (customerBillingItems.isEmpty()) {
            item {
                val activeCount = allCustomers.count { it.status == Customer.STATUS_ACTIVE }
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো বকেয়া বিল নেই / All Dues Cleared",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (activeCount > 0)
                                "$activeCount জন সক্রিয় গ্রাহকের বিল পরিশোধিত হয়েছে।"
                            else
                                "নতুন গ্রাহক যোগ করুন বা 'বিল তৈরি করুন' চাপুন।",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.showAddManualBillDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AccentCyan),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("+ ম্যানুয়াল বিল যোগ", color = AccentCyan, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.showGenerateBillDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Generate Bills Now", color = BgDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else if (filteredItems.isEmpty()) {
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"$selectedFilter\" ফিল্টারে কোনো গ্রাহক পাওয়া যায়নি",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.customer.id }) { item ->
                val customer = item.customer
                val displayName = customer.name.ifBlank { customer.pppoeUsername }
                val pppoeName = customer.pppoeUsername.ifBlank { customer.customerCode }
                val formattedCurrentMonth = remember(item.currentMonthKey) { formatBillMonth(item.currentMonthKey) }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectedCustomerForDetails = customer
                        },
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        // Card Top: Initial + Name + PPPoE + Status + Details Icon
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (customer.status) {
                                                Customer.STATUS_ACTIVE -> AccentBlue.copy(alpha = 0.2f)
                                                Customer.STATUS_SUSPENDED -> AccentOrange.copy(alpha = 0.2f)
                                                else -> AccentRed.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (customer.name.isNotBlank()) customer.name.first().uppercaseChar().toString() else "U",
                                        color = when (customer.status) {
                                            Customer.STATUS_ACTIVE -> AccentBlue
                                            Customer.STATUS_SUSPENDED -> AccentOrange
                                            else -> AccentRed
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = displayName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PPPoE: ",
                                            color = AccentCyan,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = pppoeName,
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                        if (customer.staticIp.isNotBlank()) {
                                            Text(
                                                text = " • IP: ${customer.staticIp}",
                                                color = TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CustomerStatusBadge(status = customer.status)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.selectedCustomerForDetails = customer
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Details",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Middle Breakdown Section: Current Month Bill + Previous Due
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            color = CardBorder.copy(alpha = 0.35f),
                            thickness = 0.8.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (item.previousDue > 0) {
                            // Row 1: Previous Due with exact months
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "পূর্বের বকেয়া বিল / Previous Due",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                    Text(
                                        text = "বকেয়া মাস: ${item.dueMonthsText}",
                                        color = AccentOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "৳${item.previousDue.toInt()}",
                                    color = AccentOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (item.currentMonthBill > 0) {
                                Spacer(modifier = Modifier.height(5.dp))

                                // Row 2: Current Month Bill with plus indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "(+) চলতি মাসের বিল / Current Bill",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                        Text(
                                            text = "মাস: $formattedCurrentMonth",
                                            color = TextMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                    Text(
                                        text = "৳${item.currentMonthBill.toInt()}",
                                        color = TextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(
                                color = CardBorder.copy(alpha = 0.35f),
                                thickness = 0.8.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            // Single Row: Current month bill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "চলতি মাসের বিল / Current Bill",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                    Text(
                                        text = "মাস: $formattedCurrentMonth",
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                }
                                Text(
                                    text = "৳${item.currentMonthBill.toInt()}",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(
                                color = CardBorder.copy(alpha = 0.35f),
                                thickness = 0.8.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Row 3: Total Payable (চলতি + পূর্বের বকেয়া = ২ টা প্লাস হবে বিল এ)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (item.previousDue > 0)
                                        "মোট প্রদেয় বিল (চলতি + বকেয়া)"
                                    else
                                        "চলতি মাসের মোট বিল",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Total Payable Amount",
                                    color = TextMuted,
                                    fontSize = 8.5.sp
                                )
                            }

                            Text(
                                text = "৳${item.totalPayable.toInt()}",
                                color = AccentRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(9.dp))

                        // Bottom Actions: Collect Payment + WhatsApp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button 1: Collect Payment
                            Button(
                                onClick = {
                                    viewModel.customerForPayment = customer
                                    viewModel.showCollectPaymentDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = BgDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Collect (৳${item.totalPayable.toInt()})",
                                    color = BgDark,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Button 2: WhatsApp / Invoice Reminder
                            OutlinedButton(
                                onClick = {
                                    val phone = customer.phone.replace("+", "").replace("-", "").replace(" ", "")
                                    val formattedPhone = if (phone.startsWith("0")) "88$phone" else phone
                                    val msg = "Dear $displayName, your internet bill for $formattedCurrentMonth is ৳${item.currentMonthBill.toInt()}." +
                                            (if (item.previousDue > 0) " Previous due (${item.dueMonthsText}): ৳${item.previousDue.toInt()}." else "") +
                                            " Total Payable: ৳${item.totalPayable.toInt()}. Please pay your bill on time. Thank you."
                                    if (phone.isNotBlank()) {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
                                                putExtra("sms_body", msg)
                                            }
                                            context.startActivity(smsIntent)
                                        }
                                    } else {
                                        Toast.makeText(context, "No phone number added for $displayName", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF25D366)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF25D366).copy(alpha = 0.12f)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WhatsApp",
                                    color = Color(0xFF25D366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
