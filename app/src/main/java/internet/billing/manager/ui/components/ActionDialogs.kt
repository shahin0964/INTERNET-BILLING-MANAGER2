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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.Expense
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.theme.*
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
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collect Payment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Customer", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Customer Selection Dropdown or List
                if (customers.isEmpty()) {
                    Text("No customers found. Please add a customer first.", color = AccentRed, fontSize = 13.sp)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgCardLight)
                            .padding(8.dp)
                    ) {
                        customers.take(5).forEach { cust ->
                            val isSelected = selectedCustomer?.id == cust.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                    .clickable {
                                        selectedCustomer = cust
                                        amountText = if (cust.dueAmount > 0) cust.dueAmount.toInt().toString() else cust.monthlyBill.toInt().toString()
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cust.name.ifBlank { cust.pppoeUsername },
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${cust.customerCode} • ${cust.packageName}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Due: ৳${cust.dueAmount.toInt()}",
                                    color = if (cust.dueAmount > 0) AccentOrange else AccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Payment Method", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentGreen.copy(alpha = 0.25f) else BgCardLight)
                                .clickable { selectedMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) AccentGreen else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Transaction ID (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled = selectedCustomer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        text = "Confirm & Collect ৳${amountText.ifBlank { "0" }}",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Expense.CAT_MAINTENANCE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val currentMonthYear = remember { monthYearFormat.format(Date()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Business Expense",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Fiber Wire, Router)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Expense Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Expense.ALL_CATEGORIES.chunked(2).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCats.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentPurple.copy(alpha = 0.25f) else BgCardLight)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) AccentPurple else TextSecondary,
                                        fontSize = 11.sp,
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Vendor (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0) {
                            onSave(
                                Expense(
                                    title = title,
                                    category = selectedCategory,
                                    amount = amount,
                                    dateMillis = System.currentTimeMillis(),
                                    monthYear = currentMonthYear,
                                    note = note
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        text = "Save Expense",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GenerateBillDialog(
    activeCustomerCount: Int,
    totalMonthlyBill: Double,
    monthYear: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generate Monthly Bills",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Generate bill entries for $monthYear for all active customers. This will update customer due balances and create daily billing entries.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCardLight)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Customers to Bill:", color = TextSecondary, fontSize = 13.sp)
                        Text("$activeCustomerCount users", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Total Bill:", color = TextSecondary, fontSize = 13.sp)
                        Text("৳${totalMonthlyBill.toInt()}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.5f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Generate Now", color = Color.White, fontWeight = FontWeight.Bold)
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
    var companyName by remember { mutableStateOf(currentSettings?.companyName ?: "U-turn Technologies") }
    var tagline by remember { mutableStateOf(currentSettings?.tagline ?: "Connect People Better Tomorrow") }
    var phone by remember { mutableStateOf(currentSettings?.phone ?: "01700000000") }
    var address by remember { mutableStateOf(currentSettings?.address ?: "Dhaka, Bangladesh") }
    var bandwidthBillText by remember { mutableStateOf(currentSettings?.bandwidthBill?.toInt()?.toString() ?: "0") }
    var discountText by remember { mutableStateOf(currentSettings?.discount?.toInt()?.toString() ?: "0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ISP Business Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("ISP Business Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline / Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Helpline / Support Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Office Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bandwidthBillText,
                        onValueChange = { bandwidthBillText = it },
                        label = { Text("Upstream Bandwidth Bill (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Monthly Discount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val updated = (currentSettings ?: IspSettings()).copy(
                            companyName = companyName,
                            tagline = tagline,
                            phone = phone,
                            address = address,
                            bandwidthBill = bandwidthBillText.toDoubleOrNull() ?: 0.0,
                            discount = discountText.toDoubleOrNull() ?: 0.0
                        )
                        onSave(updated)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Save Business Settings", color = BgDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
