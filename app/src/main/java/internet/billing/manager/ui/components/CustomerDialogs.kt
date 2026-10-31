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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    packages: List<InternetPackage>,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var customerCode by remember {
        mutableStateOf(customerToEdit?.customerCode ?: "U${(10..99).random()}")
    }
    var pppoeUsername by remember { mutableStateOf(customerToEdit?.pppoeUsername ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var selectedPackage by remember {
        mutableStateOf(packages.firstOrNull { it.id == customerToEdit?.packageId } ?: packages.firstOrNull())
    }
    var monthlyBillText by remember {
        mutableStateOf(customerToEdit?.monthlyBill?.toInt()?.toString() ?: selectedPackage?.monthlyPrice?.toInt()?.toString() ?: "500")
    }
    var dueAmountText by remember {
        mutableStateOf(customerToEdit?.dueAmount?.toInt()?.toString() ?: "0")
    }
    var status by remember { mutableStateOf(customerToEdit?.status ?: Customer.STATUS_ACTIVE) }
    var connectionType by remember { mutableStateOf(customerToEdit?.connectionType ?: "Fiber") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }

    val statusList = listOf(Customer.STATUS_ACTIVE, Customer.STATUS_SUSPENDED, Customer.STATUS_INACTIVE)
    val connTypes = listOf("Fiber", "Cat6", "Wireless")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
                        text = if (customerToEdit == null) "Add New Customer" else "Edit Customer",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Full Name") },
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
                        value = customerCode,
                        onValueChange = { customerCode = it },
                        label = { Text("Code (e.g. U001)") },
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
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pppoeUsername,
                    onValueChange = { pppoeUsername = it },
                    label = { Text("PPPoE Username / ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Internet Package", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    packages.take(4).forEach { pkg ->
                        val isSelected = selectedPackage?.id == pkg.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else BgCardLight)
                                .clickable {
                                    selectedPackage = pkg
                                    monthlyBillText = pkg.monthlyPrice.toInt().toString()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pkg.name,
                                color = if (isSelected) AccentCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlyBillText,
                        onValueChange = { monthlyBillText = it },
                        label = { Text("Monthly Bill (৳)") },
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
                        value = dueAmountText,
                        onValueChange = { dueAmountText = it },
                        label = { Text("Current Due (৳)") },
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

                Spacer(modifier = Modifier.height(14.dp))

                Text("Customer Status", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusList.forEach { item ->
                        val isSelected = status == item
                        val activeColor = when (item) {
                            Customer.STATUS_ACTIVE -> AccentGreen
                            Customer.STATUS_SUSPENDED -> AccentOrange
                            else -> AccentRed
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else BgCardLight)
                                .clickable { status = item }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) activeColor else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Area / Fiber Box") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val monthlyPrice = monthlyBillText.toDoubleOrNull() ?: 500.0
                        val due = dueAmountText.toDoubleOrNull() ?: 0.0
                        val customer = (customerToEdit ?: Customer()).copy(
                            name = name,
                            customerCode = customerCode,
                            pppoeUsername = pppoeUsername.ifBlank { name.lowercase().replace(" ", "") },
                            phone = phone,
                            packageId = selectedPackage?.id ?: 0,
                            packageName = selectedPackage?.name ?: "10 Mbps",
                            speedMbps = selectedPackage?.speedMbps ?: 10,
                            monthlyBill = monthlyPrice,
                            dueAmount = due,
                            status = status,
                            connectionType = connectionType,
                            address = address
                        )
                        onSave(customer)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(
                        text = if (customerToEdit == null) "Add Customer" else "Update Customer",
                        color = BgDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
