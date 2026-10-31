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
import internet.billing.manager.data.model.BillEntry
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
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
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allBills by viewModel.allBills.collectAsState()
    val monthlyBills by viewModel.monthlyBills.collectAsState()
    val billsToDisplay = if (allBills.isNotEmpty()) allBills else monthlyBills

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Bills") }
    val filters = listOf("All Bills", "Previous Due", "Active Lines", "Suspended")

    // Filter logic
    val filteredBills = remember(billsToDisplay, allCustomers, searchQuery, selectedFilter) {
        billsToDisplay.filter { bill ->
            val customer = allCustomers.firstOrNull { it.id == bill.customerId }
            val name = customer?.name ?: bill.customerName
            val pppoe = customer?.pppoeUsername ?: ""
            val code = customer?.customerCode ?: bill.customerCode
            val status = customer?.status ?: Customer.STATUS_ACTIVE
            val previousDue = if (customer != null) (customer.dueAmount - bill.dueAmount).coerceAtLeast(0.0) else 0.0

            val matchesSearch = searchQuery.isBlank() ||
                    name.contains(searchQuery, ignoreCase = true) ||
                    pppoe.contains(searchQuery, ignoreCase = true) ||
                    code.contains(searchQuery, ignoreCase = true) ||
                    bill.id.toString().contains(searchQuery)

            val matchesFilter = when (selectedFilter) {
                "Previous Due" -> previousDue > 0
                "Active Lines" -> status.equals(Customer.STATUS_ACTIVE, ignoreCase = true)
                "Suspended" -> status.equals(Customer.STATUS_SUSPENDED, ignoreCase = true)
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
        // 1. Top Card: Monthly Billing Control
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                borderColor = AccentCyan.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appStrings.billingTitle,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = appStrings.generateMonthlyBills,
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

        // 3. Filter Pills (All Bills, Previous Due, Active Lines, Suspended)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCard)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Customer Bills List
        if (billsToDisplay.isEmpty()) {
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No bills generated yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (activeCount > 0)
                                "$activeCount active subscribers ready to be billed."
                            else
                                "Add customers first, then tap 'Generate' to create monthly bills.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.showGenerateBillDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BgDark, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate Bills Now", color = BgDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (filteredBills.isEmpty()) {
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
                            text = "No bills match the filter \"$selectedFilter\"",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredBills, key = { it.id }) { bill ->
                val customer = remember(bill.customerId, allCustomers) {
                    allCustomers.firstOrNull { it.id == bill.customerId }
                }

                val displayName = customer?.name?.ifBlank { customer.pppoeUsername } ?: bill.customerName
                val pppoeName = customer?.pppoeUsername?.ifBlank { customer.customerCode } ?: bill.customerCode
                val isPaid = bill.isPaid || bill.dueAmount <= 0.0

                // Calculate Previous Due and Current Bill
                val currentBill = bill.billAmount
                val customerDue = customer?.dueAmount ?: bill.dueAmount
                val previousDue = (customerDue - bill.dueAmount).coerceAtLeast(0.0)
                val totalPayable = previousDue + currentBill
                val paidAmount = bill.paidAmount
                val dueAmount = (previousDue + bill.dueAmount).coerceAtLeast(0.0)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        // Card Top: Name + UNPAID/PAID badge + Edit icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = pppoeName,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status Pill (● UNPAID / ● PAID)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPaid) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isPaid) "● PAID" else "● UNPAID",
                                        color = if (isPaid) AccentGreen else AccentRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = {
                                        if (customer != null) {
                                            viewModel.customerToEdit = customer
                                            viewModel.showAddCustomerDialog = true
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Customer",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        val billMonth = remember(bill.monthYear) { formatBillMonth(bill.monthYear) }

                        // Bill Reference and Month tag
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BILL-${bill.monthYear.replace("-", "")}-${bill.id} • $billMonth",
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        // Middle Breakdown Section (if previous due exists)
                        if (previousDue > 0) {
                            Divider(
                                color = CardBorder.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Row 1: Previous Due
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Previous Due", color = TextSecondary, fontSize = 10.5.sp)
                                    Text("Past Outstanding", color = TextMuted, fontSize = 9.sp)
                                }
                                Text("৳${previousDue.toInt()}", color = AccentOrange, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Row 2: Current Bill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Current Bill", color = TextSecondary, fontSize = 10.5.sp)
                                    Text(billMonth, color = TextMuted, fontSize = 9.sp)
                                }
                                Text("৳${currentBill.toInt()}", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(
                            color = CardBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Amounts Row (Total Bill / Total Payable, Paid, Due)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (previousDue > 0) "Total Payable" else "Total Bill",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "৳${if (previousDue > 0) totalPayable.toInt() else currentBill.toInt()}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Paid Amount", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = "৳${paidAmount.toInt()}",
                                    color = AccentGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (previousDue > 0) "Total Due" else "Due Amount",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "৳${dueAmount.toInt()}",
                                    color = if (dueAmount > 0) AccentRed else AccentGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                                    if (customer != null) {
                                        viewModel.customerForPayment = customer
                                        viewModel.showCollectPaymentDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                enabled = dueAmount > 0,
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
                                    text = "Collect Payment (৳${dueAmount.toInt()})",
                                    color = BgDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Button 2: WhatsApp / Invoice
                            OutlinedButton(
                                onClick = {
                                    val phone = customer?.phone?.replace("+", "")?.replace("-", "")?.replace(" ", "") ?: ""
                                    val formattedPhone = if (phone.startsWith("0")) "88$phone" else phone
                                    val msg = "Dear $displayName, your ISP bill for $billMonth is ৳${dueAmount.toInt()}. Total Payable: ৳${totalPayable.toInt()}. Please pay your bill on time. Thank you."
                                    if (phone.isNotBlank()) {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback to SMS
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
