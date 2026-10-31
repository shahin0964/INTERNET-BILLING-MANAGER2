package internet.billing.manager.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.components.CustomerStatusBadge
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.customerSearchQuery.collectAsState()
    val statusFilter by viewModel.customerStatusFilter.collectAsState()

    val totalCount by viewModel.totalCustomerCount.collectAsState()
    val activeCount by viewModel.activeCustomerCount.collectAsState()
    val suspendedCount by viewModel.suspendedCustomerCount.collectAsState()
    val inactiveCount by viewModel.inactiveCustomerCount.collectAsState()

    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header with Add Customer button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Customers",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage your ISP customers",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Blue '+' button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue)
                    .clickable {
                        viewModel.customerToEdit = null
                        viewModel.showAddCustomerDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Customer",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setCustomerSearchQuery(it) },
            placeholder = {
                Text("Search name, phone, PPPoE user...", color = TextMuted, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setCustomerSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Status Filter Tabs (Pills)
        val filterTabs = listOf(
            Triple("All", totalCount, AccentPurple),
            Triple("Active", activeCount, AccentGreen),
            Triple("Suspended", suspendedCount, AccentOrange),
            Triple("Inactive", inactiveCount, AccentRed)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterTabs.forEach { (title, count, color) ->
                val isSelected = statusFilter.equals(title, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) color.copy(alpha = 0.25f) else BgCard)
                        .clickable { viewModel.setCustomerStatusFilter(title) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$title ($count)",
                        color = if (isSelected) color else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Customer Cards List
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching customers found" else "No customers added yet",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Click the '+' button above to add real customers",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onCall = { launchPhoneCall(context, customer.phone) },
                        onSms = { launchSms(context, customer.phone, customer.name, customer.dueAmount) },
                        onWhatsApp = { launchWhatsApp(context, customer.phone) },
                        onEdit = {
                            viewModel.customerToEdit = customer
                            viewModel.showAddCustomerDialog = true
                        },
                        onDelete = {
                            customerToDelete = customer
                        },
                        onCollect = {
                            viewModel.customerForPayment = customer
                            viewModel.showCollectPaymentDialog = true
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (customerToDelete != null) {
        val cust = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete ${cust.name.ifBlank { cust.pppoeUsername }} (${cust.customerCode})? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            containerColor = BgCard,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(cust)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { customerToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = remember(customer.name, customer.pppoeUsername) {
        val str = customer.name.ifBlank { customer.pppoeUsername }
        if (str.isNotBlank()) str.first().uppercaseChar().toString() else "U"
    }

    val avatarBg = when (customer.status) {
        Customer.STATUS_ACTIVE -> AccentBlue
        Customer.STATUS_SUSPENDED -> AccentOrange
        else -> AccentRed
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = CardBorderSubtle,
        onClick = onCollect
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.name.ifBlank { customer.pppoeUsername },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Code: ${customer.customerCode}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                CustomerStatusBadge(status = customer.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PPPoE Info & Package info
            Text(
                text = "PPPoE: ${customer.pppoeUsername}",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${customer.speedMbps} Mbps • ৳${customer.monthlyBill.toInt()}/mo",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Due: ৳${customer.dueAmount.toInt()}",
                    color = if (customer.dueAmount > 0) AccentRed else AccentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Phone Number + Quick Actions (Call, SMS, WhatsApp, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCall() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.phone.ifBlank { "No phone" },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call Button
                    ActionButtonCircle(
                        icon = Icons.Default.Phone,
                        tint = AccentBlue,
                        bgColor = AccentBlue.copy(alpha = 0.15f),
                        onClick = onCall
                    )
                    // SMS Button
                    ActionButtonCircle(
                        icon = Icons.Default.ChatBubble,
                        tint = AccentCyan,
                        bgColor = AccentCyan.copy(alpha = 0.15f),
                        onClick = onSms
                    )
                    // WhatsApp Button
                    ActionButtonCircle(
                        icon = Icons.Default.Forum,
                        tint = AccentGreen,
                        bgColor = AccentGreen.copy(alpha = 0.15f),
                        onClick = onWhatsApp
                    )
                    // Edit Button
                    ActionButtonCircle(
                        icon = Icons.Default.Edit,
                        tint = AccentOrange,
                        bgColor = AccentOrange.copy(alpha = 0.15f),
                        onClick = onEdit
                    )
                    // Delete Button
                    ActionButtonCircle(
                        icon = Icons.Default.Delete,
                        tint = AccentRed,
                        bgColor = AccentRed.copy(alpha = 0.15f),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtonCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Intent Launchers
private fun launchPhoneCall(context: Context, phone: String) {
    if (phone.isBlank()) {
        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun launchSms(context: Context, phone: String, name: String, due: Double) {
    if (phone.isBlank()) {
        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val msg = "Dear $name, your Internet bill due is ৳${due.toInt()}. Please pay soon. Thank you."
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
            putExtra("sms_body", msg)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show()
    }
}

private fun launchWhatsApp(context: Context, phone: String) {
    if (phone.isBlank()) {
        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val cleanPhone = phone.replace("+", "").replace("-", "").replace(" ", "")
        val formatted = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}
