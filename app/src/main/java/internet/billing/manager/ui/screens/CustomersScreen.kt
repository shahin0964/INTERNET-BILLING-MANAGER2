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
import internet.billing.manager.ui.components.CustomerDetailsDialog
import internet.billing.manager.ui.components.CustomerStatusBadge
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Header with Add Customer button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = appStrings.customersTitle,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = appStrings.customersSubtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Blue '+' button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBlue)
                    .clickable {
                        viewModel.customerToEdit = null
                        viewModel.showAddCustomerDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = appStrings.addCustomerBtn,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setCustomerSearchQuery(it) },
            placeholder = {
                Text(appStrings.searchCustomerHint, color = TextMuted, fontSize = 12.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setCustomerSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Status Filter Tabs (Pills)
        data class FilterTabItem(val filterKey: String, val label: String, val count: Int, val color: Color)
        val filterTabs = listOf(
            FilterTabItem("All", appStrings.filterAll, totalCount, AccentPurple),
            FilterTabItem("Active", appStrings.filterActive, activeCount, AccentGreen),
            FilterTabItem("Suspended", appStrings.filterSuspended, suspendedCount, AccentOrange),
            FilterTabItem("Inactive", appStrings.filterInactive, inactiveCount, AccentRed)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterTabs.forEach { (filterKey, label, count, color) ->
                val isSelected = statusFilter.equals(filterKey, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.25f) else BgCard)
                        .clickable { viewModel.setCustomerStatusFilter(filterKey) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$label ($count)",
                        color = if (isSelected) color else TextSecondary,
                        fontSize = 11.sp,
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
                        text = appStrings.noCustomersFound,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onOpenDetails = {
                            viewModel.selectedCustomerForDetails = customer
                        },
                        onCall = { launchPhoneCall(context, customer.phone) },
                        onSms = { launchSms(context, customer.phone, customer.name, customer.dueAmount) },
                        onWhatsApp = { launchWhatsApp(context, customer.phone) },
                        onEdit = {
                            viewModel.customerToEdit = customer
                            viewModel.showAddCustomerDialog = true
                        },
                        onDelete = {
                            viewModel.customerToDelete = customer
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
}

@Composable
fun CustomerCard(
    customer: Customer,
    onOpenDetails: () -> Unit,
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
        shape = RoundedCornerShape(12.dp),
        borderColor = CardBorderSubtle,
        onClick = onOpenDetails
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Row: Avatar + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = customer.name.ifBlank { customer.pppoeUsername },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Code: ${customer.customerCode}",
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                CustomerStatusBadge(status = customer.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // PPPoE Info & Package info
            val pppoeLine = buildString {
                append("PPPoE: ${customer.pppoeUsername}")
                if (customer.staticIp.isNotBlank()) {
                    append(" • IP: ${customer.staticIp}")
                }
            }
            Text(
                text = pppoeLine,
                color = TextSecondary,
                fontSize = 11.sp
            )
            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = customer.address,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Note: ${customer.notes}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val planText = if (customer.speedMbps > 0) {
                    "${customer.speedMbps} Mbps • ৳${customer.monthlyBill.toInt()}/mo"
                } else if (customer.packageName.isNotBlank()) {
                    "${customer.packageName} • ৳${customer.monthlyBill.toInt()}/mo"
                } else {
                    "৳${customer.monthlyBill.toInt()}/mo"
                }
                Text(
                    text = planText,
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${appStrings.dueLabel}: ৳${customer.dueAmount.toInt()}",
                    color = if (customer.dueAmount > 0) AccentRed else AccentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (LanguageManager.isBengali()) "বিল শেষ তারিখ: প্রতি মাসের ${customer.billDueDay} তারিখ" else "Bill Due: ${customer.billDueDay}th of month",
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

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
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.phone.ifBlank { "No phone" },
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            .size(26.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
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
