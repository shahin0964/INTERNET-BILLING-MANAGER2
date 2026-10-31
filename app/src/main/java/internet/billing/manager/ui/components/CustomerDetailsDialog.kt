package internet.billing.manager.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerDetailsDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onCollectPayment: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val initial = remember(customer.name, customer.pppoeUsername) {
        val str = customer.name.ifBlank { customer.pppoeUsername }
        if (str.isNotBlank()) str.first().uppercaseChar().toString() else "U"
    }

    val avatarBg = when (customer.status) {
        Customer.STATUS_ACTIVE -> AccentBlue
        Customer.STATUS_SUSPENDED -> AccentOrange
        else -> AccentRed
    }

    val joinDateStr = remember(customer.createdDateMillis) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(customer.createdDateMillis))
    }

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
                // Top Bar with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Profile & Details",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Header Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle,
                    backgroundColor = BgCardLight.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(avatarBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name.ifBlank { customer.pppoeUsername },
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Code: ${customer.customerCode}",
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }

                            CustomerStatusBadge(status = customer.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Status Switcher (Active / Suspended / Inactive)
                        Text(
                            text = "Customer Status (ট্যাপ করে পরিবর্তন করুন):",
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(Customer.STATUS_ACTIVE, appStrings.statusActive, AccentGreen),
                                Triple(Customer.STATUS_SUSPENDED, appStrings.statusSuspended, AccentOrange),
                                Triple(Customer.STATUS_INACTIVE, appStrings.statusInactive, AccentRed)
                            ).forEach { (st, label, color) ->
                                val isSelected = customer.status.equals(st, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) color.copy(alpha = 0.25f) else BgCard)
                                        .border(1.dp, if (isSelected) color else CardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (!isSelected) {
                                                onStatusChange(st)
                                                Toast.makeText(context, "Status updated to $label", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) color else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Billing & Dues Overview Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Monthly Bill", color = TextSecondary, fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳${customer.monthlyBill.toInt()}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            color = CardBorder.copy(alpha = 0.6f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Current Due", color = TextSecondary, fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳${customer.dueAmount.toInt()}",
                                color = if (customer.dueAmount > 0) AccentRed else AccentGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Info Rows
                Text(
                    text = "Subscription & Network Info",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoDetailRow(
                            icon = Icons.Default.AccountCircle,
                            label = "PPPoE Username",
                            value = customer.pppoeUsername.ifBlank { "Not set" }
                        )

                        if (customer.staticIp.isNotBlank()) {
                            InfoDetailRow(
                                icon = Icons.Default.Dns,
                                label = "Static IP Address",
                                value = customer.staticIp
                            )
                        }

                        InfoDetailRow(
                            icon = Icons.Default.Speed,
                            label = "Internet Package",
                            value = "${customer.packageName.ifBlank { "Standard Package" }} • ${customer.speedMbps} Mbps"
                        )

                        InfoDetailRow(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = customer.phone.ifBlank { "No phone registered" },
                            action = {
                                if (customer.phone.isNotBlank()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = AccentGreen, modifier = Modifier.size(15.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "SMS", tint = AccentCyan, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        )

                        if (customer.address.isNotBlank()) {
                            InfoDetailRow(
                                icon = Icons.Default.Home,
                                label = "Installation Address",
                                value = customer.address
                            )
                        }

                        if (customer.notes.isNotBlank()) {
                            InfoDetailRow(
                                icon = Icons.Default.Notes,
                                label = "Notes / Remarks",
                                value = customer.notes
                            )
                        }

                        InfoDetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Connection Date",
                            value = joinDateStr
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Collect Bill Button
                    Button(
                        onClick = {
                            onDismiss()
                            onCollectPayment()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = BgDark, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Collect Bill", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Edit Details Button
                    Button(
                        onClick = {
                            onDismiss()
                            onEdit()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = BgDark, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = appStrings.editCustomerTitle, color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delete Customer Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AccentRed, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = appStrings.deleteBtn, color = AccentRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InfoDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = label, color = TextMuted, fontSize = 10.sp)
                Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (action != null) {
            action()
        }
    }
}
