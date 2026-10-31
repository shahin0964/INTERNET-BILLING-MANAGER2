package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
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
    val recentPayments by viewModel.recentPayments.collectAsState()
    val monthDisplay by viewModel.currentMonthYearDisplay.collectAsState()

    val shortTodayStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Billing Analytics Calculations
    val totalGenerated = monthlyBill + totalDue
    val collectedPercent = if (totalGenerated > 0) ((monthlyCollection / totalGenerated) * 100).toInt().coerceIn(0, 100) else 0
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
                totalGenerated = totalGenerated,
                monthlyCollection = monthlyCollection,
                totalDue = totalDue,
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
    totalGenerated: Double,
    monthlyCollection: Double,
    totalDue: Double,
    collectedPercent: Int,
    outstandingPercent: Int,
    onNavigateToBilling: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Smooth animation for Donut Chart progress
    val targetProgress = if (totalGenerated > 0) (collectedPercent / 100f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "DonutProgress"
    )

    // Infinite transition for live animations
    val infiniteTransition = rememberInfiniteTransition(label = "CardLiveAnimations")

    // 1. "wifi icone ache" -> Radiating & pulsing neon cyan Wi-Fi icon waves
    val wifiPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WifiGlowPulse"
    )

    // 2. "router er 3 ta bati ache" -> 3 cyan LEDs on front panel blinking with network data activity
    val ledBlinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RouterLedBlink"
    )

    // 3. "syning ache ekta" -> Shining laser shimmer sweep across the horizon wave
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveShimmerSweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0C172E),
                        Color(0xFF071122),
                        Color(0xFF040A15)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF1E3862),
                            Color(0xFF00E5FF).copy(alpha = 0.45f),
                            Color(0xFF102038)
                        )
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Top Header Row: BarChart Icon + Title & Subtitle + View All Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Column {
                        Text(
                            text = appStrings.billingAnalytics,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appStrings.monthlyCollectionOverview,
                            color = TextSecondary.copy(alpha = 0.75f),
                            fontSize = 10.5.sp
                        )
                    }
                }

                // View All > Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14233D).copy(alpha = 0.9f))
                        .border(BorderStroke(1.dp, Color(0xFF253E68)), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToBilling() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = appStrings.viewAll,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // 2. Middle Row: Total Generated + Live Animated 3D Router + Donut Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Total Generated
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF11213C))
                            .border(BorderStroke(1.dp, Color(0xFF223B64)), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .padding(end = 2.dp, bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = appStrings.totalGenerated,
                            color = TextSecondary.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "৳${formatAmount(totalGenerated)}",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Center: 3D Router with Animated LEDs, Wi-Fi Pulse, and Shining Wave
                val routerBoxWidth = 84.dp
                val routerBoxHeight = 58.dp
                Box(
                    modifier = Modifier
                        .size(routerBoxWidth, routerBoxHeight),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Clean 3D router background image
                    Image(
                        painter = painterResource(id = R.drawable.ic_clean_router),
                        contentDescription = "Wi-Fi Router",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Animated Wi-Fi radiating neon aura ("wifi icone ache")
                    // Positioned right over the Wi-Fi icon atop the router
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                            .size(22.dp)
                            .scale(0.85f + 0.25f * wifiPulse)
                            .alpha(wifiPulse * 0.85f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        AccentCyan.copy(alpha = 0.7f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // 3. Animated 3 LEDs on front panel ("router er 3 ta bati ache")
                    val ledPhaseInt = ledBlinkPhase.toInt() % 4
                    RouterLedDot(
                        x = routerBoxWidth * 0.24f,
                        y = routerBoxHeight * 0.71f,
                        isLit = ledPhaseInt == 0 || ledPhaseInt == 3
                    )
                    RouterLedDot(
                        x = routerBoxWidth * 0.30f,
                        y = routerBoxHeight * 0.71f,
                        isLit = ledPhaseInt == 1 || ledPhaseInt == 3
                    )
                    RouterLedDot(
                        x = routerBoxWidth * 0.36f,
                        y = routerBoxHeight * 0.72f,
                        isLit = ledPhaseInt == 2 || ledPhaseInt == 3
                    )

                    // 4. Animated Shining Wave flare ("syning ache ekta")
                    // Sweeping laser shimmer light flare along the curved horizon base
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        val flareX = shimmerProgress * size.width
                        val flareY = size.height * 0.5f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    AccentCyan.copy(alpha = 0.75f),
                                    Color(0xFF00B0FF).copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(flareX, flareY),
                                radius = 14.dp.toPx()
                            ),
                            center = Offset(flareX, flareY),
                            radius = 14.dp.toPx()
                        )
                    }
                }

                // Right: Dual-Color Donut Progress Chart
                Box(
                    modifier = Modifier.size(66.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()

                        // Base Outstanding ring (Orange)
                        drawCircle(
                            color = Color(0xFFFF9100),
                            style = Stroke(width = strokeWidth)
                        )

                        // Collected sweep arc (Emerald Green)
                        if (animatedProgress > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF00C853),
                                        Color(0xFF00E676)
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = if (animatedProgress in 0.02f..0.98f) StrokeCap.Round else StrokeCap.Butt
                                )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = appStrings.collected,
                            color = TextSecondary.copy(alpha = 0.8f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Bottom Row: Compact Status Cards (Collected & Outstanding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Pill: Collected
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF08261E), Color(0xFF041812))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.45f)),
                            RoundedCornerShape(11.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF00C853).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = appStrings.collected,
                            color = Color(0xFF69F0AE),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "৳${formatAmount(monthlyCollection)} ($collectedPercent%)",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Right Pill: Outstanding
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF331D08), Color(0xFF1E0F03))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.45f)),
                            RoundedCornerShape(11.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFFF9100).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = appStrings.outstanding,
                            color = Color(0xFFFFB74D),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "৳${formatAmount(totalDue)} ($outstandingPercent%)",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated Cyan/White LED light on router front panel
 */
@Composable
private fun BoxScope.RouterLedDot(
    x: Dp,
    y: Dp,
    isLit: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (isLit) 1.0f else 0.35f,
        animationSpec = tween(140),
        label = "LedBlinkAlpha"
    )

    // Outer aura
    Box(
        modifier = Modifier
            .offset(x = x - 3.5.dp, y = y - 3.5.dp)
            .size(7.dp)
            .alpha(alpha)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, AccentCyan, Color.Transparent)
                ),
                shape = CircleShape
            )
    )

    // Core light
    Box(
        modifier = Modifier
            .offset(x = x - 1.5.dp, y = y - 1.5.dp)
            .size(3.dp)
            .alpha(alpha)
            .background(
                color = if (isLit) Color.White else AccentCyan,
                shape = CircleShape
            )
    )
}

private fun formatAmount(amount: Double): String {
    return NumberFormat.getIntegerInstance(Locale.US).format(amount.toLong())
}

