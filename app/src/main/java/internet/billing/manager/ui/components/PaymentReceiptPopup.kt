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
import androidx.compose.runtime.Composable
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
    method: String,
    note: String,
    ispSettings: IspSettings?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = LanguageManager.currentLanguage == internet.billing.manager.ui.localization.AppLanguage.BANGLA
    val receiptSettings = ReceiptCustomizationManager.getSettings(context)
    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

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
                        text = if (isBangla) "পেমেন্ট সফলভাবে সংরক্ষিত হয়েছে!" else "Payment Recorded Successfully!",
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
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ispSettings?.companyName?.ifBlank { "INTERNET SERVICE" } ?: "INTERNET SERVICE",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Hotline: ${ispSettings?.phone?.ifBlank { "01XXXXXXXXX" } ?: "01XXXXXXXXX"}",
                                        color = Color(0xFF475569),
                                        fontSize = 9.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF0F9FF))
                                        .border(BorderStroke(0.8.dp, Color(0xFF0284C7)), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MONEY RECEIPT",
                                        color = Color(0xFF0369A1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Received with thanks: ${customer.name.ifBlank { customer.pppoeUsername }}",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Amount: ৳ ${"%,.2f".format(amount)} (${method})",
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (customer.phone.isNotBlank()) {
                                Text(
                                    text = "Customer Phone: ${customer.phone}",
                                    color = Color(0xFF475569),
                                    fontSize = 9.5.sp
                                )
                            }
                            Text(
                                text = "Remaining Due: ৳ ${"%,.2f".format(customer.dueAmount)}",
                                color = if (customer.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    totalDueRemaining = customer.dueAmount
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
                            .height(46.dp)
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
                            fontSize = 13.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Close Button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(
                            text = if (isBangla) "এখন নয় / বন্ধ করুন" else "Dismiss",
                            color = TextSecondary,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}
