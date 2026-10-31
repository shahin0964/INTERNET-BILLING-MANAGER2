package internet.billing.manager.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.data.model.PreviousDueItem
import internet.billing.manager.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    packages: List<InternetPackage>,
    onDismiss: () -> Unit,
    onSave: (Customer, List<PreviousDueItem>) -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var customerCode by remember {
        mutableStateOf(customerToEdit?.customerCode ?: "U${(10..99).random()}")
    }
    var pppoeUsername by remember { mutableStateOf(customerToEdit?.pppoeUsername ?: "") }
    var staticIp by remember { mutableStateOf(customerToEdit?.staticIp ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }

    var selectedPackage by remember {
        mutableStateOf(packages.firstOrNull { it.id == customerToEdit?.packageId } ?: packages.firstOrNull())
    }
    var monthlyBillText by remember {
        mutableStateOf(
            customerToEdit?.monthlyBill?.takeIf { it > 0 }?.toInt()?.toString()
                ?: selectedPackage?.monthlyPrice?.toInt()?.toString()
                ?: "500"
        )
    }

    var status by remember { mutableStateOf(customerToEdit?.status ?: Customer.STATUS_ACTIVE) }
    val connectionType by remember { mutableStateOf(customerToEdit?.connectionType ?: "Fiber") }

    // Previous Due / Opening Due States
    var hasPreviousDue by remember {
        mutableStateOf((customerToEdit?.dueAmount ?: 0.0) > 0.0)
    }

    val monthsList = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }
    val currentCal = remember { Calendar.getInstance() }
    val currentMonthName = remember { SimpleDateFormat("MMMM", Locale.ENGLISH).format(Date()) }
    val currentYearStr = remember { currentCal.get(Calendar.YEAR).toString() }
    val yearsList = remember {
        val y = currentCal.get(Calendar.YEAR)
        (y - 2..y + 2).map { it.toString() }
    }

    var selectedMonth by remember { mutableStateOf(currentMonthName) }
    var selectedYear by remember { mutableStateOf(currentYearStr) }
    var dueInputText by remember {
        mutableStateOf(
            if ((customerToEdit?.dueAmount ?: 0.0) > 0.0) customerToEdit!!.dueAmount.toInt().toString() else ""
        )
    }
    var previousDuesList by remember { mutableStateOf<List<PreviousDueItem>>(emptyList()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
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
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (customerToEdit == null) "Add New Customer" else "Edit Customer",
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
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Customer Name *
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    placeholder = { Text("Full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Phone Number * and Code Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("01XXXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1.8f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan
                        )
                    )
                    OutlinedTextField(
                        value = customerCode,
                        onValueChange = { customerCode = it },
                        label = { Text("Code") },
                        placeholder = { Text("U001") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. PPPoE Username / ID *
                OutlinedTextField(
                    value = pppoeUsername,
                    onValueChange = { pppoeUsername = it },
                    label = { Text("PPPoE Username / ID *") },
                    placeholder = { Text("Username or user ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Static IP (Optional)
                OutlinedTextField(
                    value = staticIp,
                    onValueChange = { staticIp = it },
                    label = { Text("Static IP (Optional)") },
                    placeholder = { Text("e.g. 192.168.1.100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Address / Location
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    placeholder = { Text("Area, Road, House or Fiber Box") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Notes (Optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Remarks, ONU MAC, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Internet Package * (Dropdown Selector)
                var packageMenuExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when {
                            selectedPackage != null -> "${selectedPackage!!.name} • ৳${selectedPackage!!.monthlyPrice.toInt()}/mo"
                            packages.isNotEmpty() -> "Select Internet Package"
                            else -> "Custom Package (Manual Price)"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Internet Package *") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Package",
                                tint = AccentCyan
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            focusedContainerColor = BgCardLight.copy(alpha = 0.35f),
                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Overlay to capture clicks across the whole input box
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { packageMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = packageMenuExpanded,
                        onDismissRequest = { packageMenuExpanded = false },
                        modifier = Modifier
                            .background(BgCard)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                    ) {
                        if (packages.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "No preset packages (Set fee below)",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    selectedPackage = null
                                    packageMenuExpanded = false
                                }
                            )
                        } else {
                            packages.forEach { pkg ->
                                val isSelected = selectedPackage?.id == pkg.id
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = pkg.name,
                                                    color = if (isSelected) AccentCyan else TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${pkg.speedMbps} Mbps Speed",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "৳${pkg.monthlyPrice.toInt()}/mo",
                                                color = if (isSelected) AccentCyan else AccentGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPackage = pkg
                                        monthlyBillText = pkg.monthlyPrice.toInt().toString()
                                        packageMenuExpanded = false
                                    },
                                    modifier = Modifier.background(
                                        if (isSelected) AccentCyan.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                )
                            }
                            Divider(color = CardBorder, thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Custom Package (Manual Fee)",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    selectedPackage = null
                                    packageMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 8. Monthly Fee (৳) *
                OutlinedTextField(
                    value = monthlyBillText,
                    onValueChange = { monthlyBillText = it },
                    label = { Text("Monthly Fee (৳) *") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 9. Connection Status
                Text("Connection Status", color = TextSecondary, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusList = listOf(Customer.STATUS_ACTIVE, Customer.STATUS_SUSPENDED, Customer.STATUS_INACTIVE)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else BgCardLight)
                                .clickable { status = item }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) activeColor else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 10. Previous Due / Opening Due Section
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = if (hasPreviousDue) AccentCyan.copy(alpha = 0.5f) else CardBorder,
                    backgroundColor = BgCardLight.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { hasPreviousDue = !hasPreviousDue },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = hasPreviousDue,
                                onCheckedChange = { hasPreviousDue = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentCyan,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = BgDark
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Previous Due / Opening Due",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Add past unpaid monthly bills or opening balance",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        if (hasPreviousDue) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = CardBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Month & Year Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Month Selector
                                var monthMenuExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1.3f)) {
                                    OutlinedTextField(
                                        value = selectedMonth,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Month", fontSize = 11.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = AccentCyan
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentCyan,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = AccentCyan,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedContainerColor = BgCardLight.copy(alpha = 0.3f),
                                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { monthMenuExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = monthMenuExpanded,
                                        onDismissRequest = { monthMenuExpanded = false },
                                        modifier = Modifier
                                            .background(BgCard)
                                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                                    ) {
                                        monthsList.forEach { m ->
                                            val isSelected = m == selectedMonth
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = m,
                                                        color = if (isSelected) AccentCyan else TextPrimary,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                onClick = {
                                                    selectedMonth = m
                                                    monthMenuExpanded = false
                                                },
                                                modifier = Modifier.background(
                                                    if (isSelected) AccentCyan.copy(alpha = 0.15f) else Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }

                                // Year Selector
                                var yearMenuExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = selectedYear,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Year", fontSize = 11.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = AccentCyan
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentCyan,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = AccentCyan,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedContainerColor = BgCardLight.copy(alpha = 0.3f),
                                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { yearMenuExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = yearMenuExpanded,
                                        onDismissRequest = { yearMenuExpanded = false },
                                        modifier = Modifier
                                            .background(BgCard)
                                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                                    ) {
                                        yearsList.forEach { y ->
                                            val isSelected = y == selectedYear
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = y,
                                                        color = if (isSelected) AccentCyan else TextPrimary,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                onClick = {
                                                    selectedYear = y
                                                    yearMenuExpanded = false
                                                },
                                                modifier = Modifier.background(
                                                    if (isSelected) AccentCyan.copy(alpha = 0.15f) else Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Due Amount Input and "+ Add" Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = dueInputText,
                                    onValueChange = { dueInputText = it },
                                    label = { Text("Due Amount (৳)") },
                                    placeholder = { Text("e.g. 500") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = AccentCyan,
                                        unfocusedLabelColor = TextSecondary,
                                        cursorColor = AccentCyan
                                    )
                                )

                                Button(
                                    onClick = {
                                        val amount = dueInputText.toDoubleOrNull() ?: 0.0
                                        if (amount > 0) {
                                            val existingIndex = previousDuesList.indexOfFirst {
                                                it.month == selectedMonth && it.year == selectedYear
                                            }
                                            if (existingIndex >= 0) {
                                                previousDuesList = previousDuesList.toMutableList().also {
                                                    it[existingIndex] = it[existingIndex].copy(amount = amount)
                                                }
                                            } else {
                                                previousDuesList = previousDuesList + PreviousDueItem(
                                                    month = selectedMonth,
                                                    year = selectedYear,
                                                    amount = amount
                                                )
                                            }
                                            dueInputText = ""
                                        }
                                    },
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = BgDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }

                            // Added Previous Dues Breakdown List
                            if (previousDuesList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    previousDuesList.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BgCard)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${item.month} ${item.year}:",
                                                    color = TextSecondary,
                                                    fontSize = 11.5.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "৳${item.amount.toInt()}",
                                                    color = AccentRed,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    previousDuesList = previousDuesList.toMutableList().also {
                                                        it.removeAt(index)
                                                    }
                                                },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    val totalPreviousDue = previousDuesList.sumOf { it.amount }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Due Recorded:", color = TextSecondary, fontSize = 11.5.sp)
                                        Text(
                                            text = "৳${totalPreviousDue.toInt()}",
                                            color = AccentRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 11. Bottom Action Buttons: Cancel & Save Customer
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
                        Text("Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val monthlyPrice = monthlyBillText.toDoubleOrNull() ?: 0.0
                            val enteredAmount = dueInputText.toDoubleOrNull() ?: 0.0
                            val duesToSave = if (previousDuesList.isEmpty() && enteredAmount > 0) {
                                listOf(PreviousDueItem(month = selectedMonth, year = selectedYear, amount = enteredAmount))
                            } else {
                                previousDuesList
                            }
                            val totalDue = if (hasPreviousDue) {
                                if (duesToSave.isNotEmpty()) duesToSave.sumOf { it.amount } else enteredAmount
                            } else {
                                0.0
                            }

                            val customer = (customerToEdit ?: Customer()).copy(
                                name = name,
                                customerCode = customerCode,
                                pppoeUsername = pppoeUsername.ifBlank { name.lowercase().replace(" ", "") },
                                phone = phone,
                                packageId = selectedPackage?.id ?: 0,
                                packageName = selectedPackage?.name ?: if (monthlyPrice > 0) "Custom Plan" else "",
                                speedMbps = selectedPackage?.speedMbps ?: 0,
                                monthlyBill = monthlyPrice,
                                dueAmount = totalDue,
                                status = status,
                                connectionType = connectionType,
                                address = address,
                                staticIp = staticIp,
                                notes = notes
                            )
                            onSave(customer, duesToSave)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text(
                            text = if (customerToEdit == null) "Save Customer" else "Update Customer",
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
