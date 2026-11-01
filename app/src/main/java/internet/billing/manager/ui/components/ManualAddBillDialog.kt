package internet.billing.manager.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.theme.*

@Composable
fun ManualAddBillDialog(
    allCustomers: List<Customer>,
    currentMonthKey: String,
    onDismiss: () -> Unit,
    onAddBill: (customerId: Long, monthYear: String, amount: Double, notes: String) -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember { mutableStateOf(allCustomers.firstOrNull()) }
    var monthYearInput by remember { mutableStateOf(currentMonthKey) }
    var amountInput by remember(selectedCustomer) {
        mutableStateOf(selectedCustomer?.monthlyBill?.toInt()?.toString() ?: "500")
    }
    var notesInput by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

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

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Customer to Billing",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Customer Dropdown
                Text(text = "Select Customer *", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.name.ifBlank { it.pppoeUsername }} (${it.customerCode})" } ?: "No customers available",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentCyan)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogTextFieldColors,
                        textStyle = dialogTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { customerDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false },
                        modifier = Modifier
                            .background(BgCard)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                            .widthIn(min = 260.dp)
                    ) {
                        allCustomers.forEach { cust ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = cust.name.ifBlank { cust.pppoeUsername },
                                            color = if (cust.id == selectedCustomer?.id) AccentCyan else TextPrimary,
                                            fontWeight = if (cust.id == selectedCustomer?.id) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "PPPoE: ${cust.pppoeUsername} • ৳${cust.monthlyBill.toInt()}",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCustomer = cust
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Billing Month (yyyy-MM)
                Text(text = "Billing Month (বছর-মাস) *", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = monthYearInput,
                    onValueChange = { monthYearInput = it },
                    placeholder = { Text("e.g. 2026-10", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bill Amount (৳)
                Text(text = "Bill Amount (টাকার পরিমাণ) *", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                Text(text = "Notes / Remarks (ঐচ্ছিক)", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    placeholder = { Text("Manual bill entry, adjustment, etc.", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogTextFieldColors,
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val cust = selectedCustomer
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (cust == null) {
                            Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amt <= 0.0) {
                            Toast.makeText(context, "Please enter a valid bill amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onAddBill(cust.id, monthYearInput.trim(), amt, notesInput.trim())
                        Toast.makeText(context, "Customer added to Billing with ৳${amt.toInt()} bill", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(
                        text = "Add to Billing Screen",
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
