package internet.billing.manager.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.IspSettings
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.ReceiptBitmapGenerator
import internet.billing.manager.util.ReceiptCustomizationManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentReceiptPopup(
    customer: Customer,
    amount: Double,
    discount: Double = 0.0,
    method: String,
    note: String,
    ispSettings: IspSettings?,
    dateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA
    val receiptSettings = ReceiptCustomizationManager.getSettings(context)
    val currentMonth = remember(dateMillis) { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(dateMillis)) }

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Success Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBangla) "পেমেন্ট মানি রসিদ" else "Payment Money Receipt",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "${customer.name.ifBlank { customer.pppoeUsername }} • ৳${amount.toInt()} ($method)",
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Receipt Preview Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Top Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val effectiveCompanyName = receiptSettings.companyName.ifBlank {
                                        ispSettings?.companyName?.takeIf { it.isNotBlank() && !it.equals("U-turn Technologies", ignoreCase = true) }
                                            ?: if (isBangla) "কোম্পানির নাম" else "Company Name"
                                    }
                                    val effectiveTagline = receiptSettings.companyTagline.ifBlank {
                                        ispSettings?.tagline?.ifBlank { "FAST • STABLE • CONNECTED" } ?: "FAST • STABLE • CONNECTED"
                                    }
                                    val logoAcronym = effectiveCompanyName.split(" ").filter { it.isNotBlank() }.take(3).map { it.first() }.joinToString("").uppercase().ifBlank { "ISP" }

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(logoAcronym.take(4), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = effectiveCompanyName,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = effectiveTagline,
                                            color = Color(0xFF0284C7),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 8.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row {
                                        Text("MONEY ", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("RECEIPT", color = Color(0xFF0D9488), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Text("Thank you for being with us!", color = Color(0xFF64748B), fontSize = 7.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Contact Capsule Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(BorderStroke(0.8.dp, Color(0xFFE2E8F0)), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("☎ Hotline: ${ispSettings?.phone?.ifBlank { "01XXXXXXXXX" } ?: "01XXXXXXXXX"}", color = Color(0xFF334155), fontSize = 8.sp, fontWeight = FontWeight.Medium)
                                    if (!ispSettings?.address.isNullOrBlank()) {
                                        Text("📍 ${ispSettings?.address}", color = Color(0xFF334155), fontSize = 8.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Voucher Cheque details
                            Text(
                                text = "Received with thanks from: ${customer.name.ifBlank { customer.pppoeUsername }}",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Amount: ৳ ${"%,.2f".format(amount)} (${method})",
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (discount > 0) {
                                Text(
                                    text = "Discount / ছাড়: ৳ ${"%,.2f".format(discount)}",
                                    color = Color(0xFF0D9488),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "In word: ${internet.billing.manager.util.NumberToWords.convertToWords(amount.toLong())}",
                                color = Color(0xFF475569),
                                fontSize = 9.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // ACCT, PAID, DUE row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ACCT: ${customer.customerCode.ifBlank { "U-${customer.id}" }}", color = Color(0xFF334155), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                Text("PAID: ৳ ${"%,.2f".format(amount)}", color = Color(0xFF0D9488), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                val dueColor = if (customer.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF0D9488)
                                Text("DUE: ৳ ${"%,.2f".format(customer.dueAmount)}", color = dueColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Action Buttons: WhatsApp Button (Prominent Green)
                    Button(
                        onClick = {
                            try {
                                val bitmap = ReceiptBitmapGenerator.generateReceiptBitmap(
                                    context = context,
                                    ispSettings = ispSettings,
                                    receiptSettings = receiptSettings,
                                    customer = customer,
                                    amount = amount,
                                    method = method,
                                    monthYear = currentMonth,
                                    totalDueRemaining = customer.dueAmount,
                                    discount = discount,
                                    dateMillis = dateMillis
                                )
                                val imageUri = ReceiptBitmapGenerator.saveBitmapToCache(context, bitmap)
                                ReceiptBitmapGenerator.sendReceiptViaWhatsApp(
                                    context = context,
                                    imageUri = imageUri,
                                    phone = customer.phone,
                                    customerName = customer.name.ifBlank { customer.pppoeUsername },
                                    amount = amount
                                )
                                Toast.makeText(
                                    context,
                                    if (isBangla) "হোয়াটসঅ্যাপে মানি রসিদ পাঠানো হচ্ছে..." else "Opening WhatsApp to send receipt...",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "WhatsApp এ মানি রসিদ পাঠান" else "Send Receipt on WhatsApp",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Action Button: Share Receipt (Any App / Share Sheet)
                    Button(
                        onClick = {
                            try {
                                val bitmap = ReceiptBitmapGenerator.generateReceiptBitmap(
                                    context = context,
                                    ispSettings = ispSettings,
                                    receiptSettings = receiptSettings,
                                    customer = customer,
                                    amount = amount,
                                    method = method,
                                    monthYear = currentMonth,
                                    totalDueRemaining = customer.dueAmount,
                                    discount = discount,
                                    dateMillis = dateMillis
                                )
                                val imageUri = ReceiptBitmapGenerator.saveBitmapToCache(context, bitmap)
                                ReceiptBitmapGenerator.shareReceipt(
                                    context = context,
                                    imageUri = imageUri,
                                    customerName = customer.name.ifBlank { customer.pppoeUsername },
                                    amount = amount
                                )
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), // Sky/Cyan Blue
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "যেকোনো মাধ্যমে শেয়ার করুন" else "Share Receipt",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Close Button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Text(
                            text = if (isBangla) "এখন নয় / বন্ধ করুন" else "Dismiss",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
