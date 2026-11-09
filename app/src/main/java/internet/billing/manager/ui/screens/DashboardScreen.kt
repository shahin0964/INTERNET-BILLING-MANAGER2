package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import internet.billing.manager.R
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.components.MetricStatCard
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: BillingViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ispSettings by viewModel.ispSettings.collectAsState()
    val activeCount by viewModel.activeCustomerCount.collectAsState()
    val totalCount by viewModel.totalCustomerCount.collectAsState()
    val monthlyBill by viewModel.totalActiveMonthlyBill.collectAsState()
    val monthlyCollection by viewModel.totalCollectedThisMonth.collectAsState()
    val todayCollection by viewModel.totalCollectedToday.collectAsState()
    val totalDue by viewModel.totalOutstandingDue.collectAsState()
    val monthlyDiscount by viewModel.totalDiscountThisMonth.collectAsState()
    val recentPayments by viewModel.recentPayments.collectAsState()
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()

    val shortTodayStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Billing Analytics Calculations - Total month demand does not subtract when collected
    val totalMonthDue = (totalDue + monthlyCollection + monthlyDiscount).coerceAtLeast(monthlyBill)
    val collectedPercent = if (totalMonthDue > 0) ((monthlyCollection / totalMonthDue) * 100).toInt().coerceIn(0, 100) else 0
    val outstandingPercent = (100 - collectedPercent).coerceIn(0, 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Top Brand Header Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                borderColor = AccentCyan.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    val logoUri = ispSettings?.logoUri.orEmpty()
                    val logoBitmap = remember(logoUri) {
                        if (logoUri.isNotBlank()) {
                            try {
                                val uri = Uri.parse(logoUri)
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                }
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = ispSettings?.companyName?.takeIf { it.isNotBlank() && !it.equals("U-turn Technologies", ignoreCase = true) } ?: "Internet Billing Manager",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        val rawStatus = ispSettings?.networkStatus?.ifBlank { "Operational" } ?: "Operational"
                        val netStatus = when (rawStatus) {
                            "Maintenance" -> appStrings.statusMaintenance
                            "Degraded" -> appStrings.statusDegraded
                            else -> appStrings.statusOperational
                        }
                        val statusColor = when (rawStatus) {
                            "Maintenance" -> AccentOrange
                            "Degraded" -> AccentRed
                            else -> AccentGreen
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = netStatus,
                                color = statusColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val phone = ispSettings?.phone
                            if (!phone.isNullOrBlank() && phone != "01700000000") {
                                Text(
                                    text = " • $phone",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Actions (3 Rounded Gradient Action Buttons)
        item {
            Column {
                Text(
                    text = appStrings.quickActions,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Add Customer (Teal/Cyan)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = AccentCyan.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentCyan.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showAddCustomerDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Customer",
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appStrings.addCustomer,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 13.5.sp
                            )
                        }
                    }

                    // Action 2: Collect Payment (Green)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = AccentGreen.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentGreen.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showCollectPaymentDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Collect Payment",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appStrings.collectPayment,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 13.5.sp
                            )
                        }
                    }

                    // Action 3: Generate Bill (Purple)
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        borderColor = AccentPurple.copy(alpha = 0.35f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(AccentPurple.copy(alpha = 0.2f), BgCard)
                        ),
                        onClick = { viewModel.showGenerateBillDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Generate Bill",
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appStrings.generateBill,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 13.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Business Overview (2x2 Metrics Grid)
        item {
            Column {
                Text(
                    text = appStrings.businessOverview,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = appStrings.activeUsers,
                            value = "$activeCount",
                            subtitle = "${appStrings.totalLabel}: $totalCount",
                            icon = Icons.Default.CheckCircle,
                            accentColor = AccentGreen,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCustomers
                        )
                        MetricStatCard(
                            title = appStrings.monthlyBill,
                            value = "৳${monthlyBill.toInt()}",
                            subtitle = monthDisplay,
                            icon = Icons.Default.Receipt,
                            accentColor = AccentBlue,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToBilling
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = appStrings.monthlyCollection,
                            value = "৳${monthlyCollection.toInt()}",
                            subtitle = monthDisplay,
                            icon = Icons.Default.Payments,
                            accentColor = AccentPurple,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCollection
                        )
                        MetricStatCard(
                            title = appStrings.todayCollection,
                            value = "৳${todayCollection.toInt()}",
                            subtitle = shortTodayStr,
                            icon = Icons.Default.AccessTime,
                            accentColor = AccentOrange,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCollection
                        )
                    }
                }
            }
        }

        // 5. Modern Animated Billing Analytics Card
        item {
            ModernBillingAnalyticsCard(
                totalDueMonth = totalMonthDue,
                monthlyCollection = monthlyCollection,
                totalOutstanding = totalDue,
                collectedPercent = collectedPercent,
                outstandingPercent = outstandingPercent,
                onNavigateToBilling = onNavigateToBilling
            )
        }

        // 6. Recent Collections Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appStrings.recentPayments,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToCollection() }
                ) {
                    Text(
                        text = appStrings.viewAll,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = appStrings.noPaymentsRecorded,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(recentPayments.take(5)) { payment ->
                val dateStr = remember(payment.dateMillis) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.dateMillis))
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    borderColor = CardBorderSubtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = payment.customerName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "$dateStr • ${payment.paymentMethod}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = "+৳${payment.amount.toInt()}",
                            color = AccentGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernBillingAnalyticsCard(
    totalDueMonth: Double,
    monthlyCollection: Double,
    totalOutstanding: Double,
    collectedPercent: Int,
    outstandingPercent: Int,
    onNavigateToBilling: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (collectedPercent / 100f).coerceIn(0f, 1f)

    // Outer Card with Cyber Neon Glow Border and Deep Space Navy Glass Background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF040A18),
                        Color(0xFF06142E),
                        Color(0xFF020712)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.4.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF00B0FF),
                            Color(0xFF1E88E5),
                            Color(0xFFA855F7),
                            Color(0xFFE879F9),
                            Color(0xFF00E5FF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top Header Row: 3D BarChart Icon + Title & Subtitle + View All Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // 3D Glossy Blue BarChart Icon Box
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF29B6F6),
                                        Color(0xFF0288D1),
                                        Color(0xFF01579B)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(19.dp)) {
                            val barW = 3.6.dp.toPx()
                            val rad = CornerRadius(1.8.dp.toPx(), 1.8.dp.toPx())
                            // Bar 1
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(1.5.dp.toPx(), size.height * 0.52f),
                                size = Size(barW, size.height * 0.48f),
                                cornerRadius = rad
                            )
                            // Bar 2
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(7.2.dp.toPx(), size.height * 0.28f),
                                size = Size(barW, size.height * 0.72f),
                                cornerRadius = rad
                            )
                            // Bar 3
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(13.dp.toPx(), 0f),
                                size = Size(barW, size.height),
                                cornerRadius = rad
                            )
                        }
                    }

                    Column {
                        Text(
                            text = appStrings.billingAnalytics,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = appStrings.monthlyCollectionOverview,
                            color = Color(0xFFB0C4DE),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // View All > Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF15386C), Color(0xFF0B2042))
                            )
                        )
                        .border(
                            BorderStroke(1.2.dp, Color(0xFF2979FF).copy(alpha = 0.85f)),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onNavigateToBilling() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = appStrings.viewAll,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 2. Middle Row: Total Generated + Live Animated 3D Router + 3D Donut Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Total Generated Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.15f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF143058), Color(0xFF0A1C36))
                                )
                            )
                            .border(BorderStroke(1.2.dp, Color(0xFF1E88E5)), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF90CAF9),
                            modifier = Modifier.size(22.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .padding(end = 2.dp, bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(9.dp))

                    Column {
                        Text(
                            text = appStrings.totalDueAmount,
                            color = Color(0xFF90A4AE),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "৳${formatAmount(totalDueMonth)}",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                // Center: 3D Router Illustration (Clean, static, crisp artwork)
                Box(
                    modifier = Modifier.size(92.dp, 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_clean_router),
                        contentDescription = "Wi-Fi Router",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Right: 3D Donut Progress Chart (Static, crisp)
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 9.dp.toPx()

                        // Base Outstanding ring (Vibrant Warm Orange)
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFF9100),
                                    Color(0xFFFF6D00),
                                    Color(0xFFFF9100)
                                )
                            ),
                            style = Stroke(width = strokeWidth)
                        )

                        // Collected sweep arc (Vibrant Emerald Green)
                        if (progressFraction > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF00C853),
                                        Color(0xFF00E676)
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = progressFraction * 360f,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = if (progressFraction in 0.02f..0.98f) StrokeCap.Round else StrokeCap.Butt
                                )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${collectedPercent}%",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = appStrings.collected,
                            color = Color(0xFF90A4AE),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Bottom Row: Status Cards (Collected & Outstanding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Left: Collected Status Card
                val collectedAnnotated = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                        append("৳${formatAmount(monthlyCollection)} ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)) {
                        append("($collectedPercent%)")
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF052B1E), Color(0xFF021710))
                            )
                        )
                        .border(
                            BorderStroke(1.2.dp, Color(0xFF00E676).copy(alpha = 0.75f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF00E676), Color(0xFF00897B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 3 Stacked coin discs icon
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val discH = 2.6.dp.toPx()
                            val discW = 13.dp.toPx()
                            val startX = (size.width - discW) / 2f
                            val rad = CornerRadius(1.3.dp.toPx(), 1.3.dp.toPx())
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(startX, 1.5.dp.toPx()),
                                size = Size(discW, discH),
                                cornerRadius = rad
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.95f),
                                topLeft = Offset(startX, 6.2.dp.toPx()),
                                size = Size(discW, discH),
                                cornerRadius = rad
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.9f),
                                topLeft = Offset(startX, 11.dp.toPx()),
                                size = Size(discW, discH),
                                cornerRadius = rad
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Column {
                        Text(
                            text = appStrings.collected,
                            color = Color(0xFF69F0AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = collectedAnnotated,
                            maxLines = 1
                        )
                    }
                }

                // Right: Outstanding Status Card
                val outstandingAnnotated = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                        append("৳${formatAmount(totalOutstanding)} ")
                    }
                    withStyle(SpanStyle(color = Color(0xFFFF9100), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)) {
                        append("($outstandingPercent%)")
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF381B05), Color(0xFF1E0E02))
                            )
                        )
                        .border(
                            BorderStroke(1.2.dp, Color(0xFFFF9100).copy(alpha = 0.75f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF9100), Color(0xFFE65100))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Column {
                        Text(
                            text = appStrings.outstanding,
                            color = Color(0xFFFFB74D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = outstandingAnnotated,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}


private fun formatAmount(amount: Double): String {
    return NumberFormat.getIntegerInstance(Locale.US).format(amount.toLong())
}

