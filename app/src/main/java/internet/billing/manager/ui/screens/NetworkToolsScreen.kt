package internet.billing.manager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private enum class ActiveToolSheet {
    NONE,
    PING,
    DNS,
    IP_CHECK,
    GATEWAY,
    PORT_CHECKER,
    SPEED_TEST,
    WIFI_ANALYZER
}

@Composable
fun NetworkToolsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBangla = LanguageManager.isBengali()

    var overview by remember { mutableStateOf(NetworkOverview()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf(ActiveToolSheet.NONE) }

    fun refreshNetwork() {
        coroutineScope.launch {
            isRefreshing = true
            overview = NetworkToolsManager.getNetworkOverview(context)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refreshNetwork()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Bar: Back, Title, Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isBangla) "🛠️ নেটওয়ার্ক টুলস" else "🛠️ Network Tools",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { refreshNetwork() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BgCard)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) AccentCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 2. Wi-Fi / Active Network Card (Matching reference image)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    borderColor = CardBorder,
                    backgroundColor = BgCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Card Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (overview.isConnected) AccentGreen.copy(alpha = 0.2f) else AccentRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (overview.isConnected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (overview.isConnected) AccentGreen else AccentRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = overview.connectionType,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (overview.isConnected) "Internet Connected • Online" else "Offline • No Internet",
                                        color = if (overview.isConnected) AccentGreen else AccentRed,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = { refreshNetwork() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Card Bottom Row: Local IPv4 & Default Gateway
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "LOCAL IPv4",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = overview.localIpv4,
                                    color = AccentCyan,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "DEFAULT GATEWAY",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = overview.defaultGateway,
                                    color = TextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Title: DIAGNOSTIC & TESTING SUITE
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DIAGNOSTIC & TESTING SUITE",
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            // Tool 1: Ping Test
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Public,
                    iconContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF60A5FA),
                    title = "Ping Test",
                    subtitle = "Real-time ICMP ping, packet loss & round-trip time (RTT)",
                    onClick = { activeSheet = ActiveToolSheet.PING }
                )
            }

            // Tool 2: DNS Lookup
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Search,
                    iconContainerColor = Color(0xFF0E7490).copy(alpha = 0.5f),
                    iconTint = Color(0xFF38BDF8),
                    title = "DNS Lookup",
                    subtitle = "Resolve domain names to IPv4 & IPv6 records with response time",
                    onClick = { activeSheet = ActiveToolSheet.DNS }
                )
            }

            // Tool 3: IP Address Check
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Wifi,
                    iconContainerColor = Color(0xFF065F46).copy(alpha = 0.5f),
                    iconTint = Color(0xFF34D399),
                    title = "IP Address Check",
                    subtitle = "Inspect local network interfaces, gateway, and public ISP IP",
                    onClick = { activeSheet = ActiveToolSheet.IP_CHECK }
                )
            }

            // Tool 4: Gateway/Server Reachability
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Router,
                    iconContainerColor = Color(0xFF78350F).copy(alpha = 0.5f),
                    iconTint = Color(0xFFFBBF24),
                    title = "Gateway/Server Reachability",
                    subtitle = "Auto-detect router default gateway and test socket reachability",
                    onClick = { activeSheet = ActiveToolSheet.GATEWAY }
                )
            }

            // Tool 5: TCP Port Checker
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.SettingsEthernet,
                    iconContainerColor = Color(0xFF581C87).copy(alpha = 0.5f),
                    iconTint = Color(0xFFC084FC),
                    title = "TCP Port Checker",
                    subtitle = "Test open ports (80, 443, 8080, 22) on local or remote servers",
                    onClick = { activeSheet = ActiveToolSheet.PORT_CHECKER }
                )
            }

            // Tool 6: Speed Test (with Live ISP badge)
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Speed,
                    iconContainerColor = Color(0xFF831843).copy(alpha = 0.5f),
                    iconTint = Color(0xFFF472B6),
                    title = "⚡ Speed Test",
                    badgeText = "Live ISP",
                    badgeColor = Color(0xFF4C1D95),
                    subtitle = "Benchmark download, upload speed, latency, and jitter",
                    onClick = { activeSheet = ActiveToolSheet.SPEED_TEST }
                )
            }

            // Tool 7: Wi-Fi Analyzer (with Real Scan badge)
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.WifiTethering,
                    iconContainerColor = Color(0xFF134E4A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF2DD4BF),
                    title = "📡 Wi-Fi Analyzer",
                    badgeText = "Real Scan",
                    badgeColor = Color(0xFF312E81),
                    subtitle = "Real-time 2.4/5/6 GHz channels, signal graph, and ratings",
                    onClick = { activeSheet = ActiveToolSheet.WIFI_ANALYZER }
                )
            }
        }
    }

    // Modal Tool Dialogs
    when (activeSheet) {
        ActiveToolSheet.PING -> PingTestDialog(defaultGateway = overview.defaultGateway, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.DNS -> DnsLookupDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.IP_CHECK -> IpAddressCheckDialog(overview = overview, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.GATEWAY -> GatewayReachabilityDialog(gatewayIp = overview.defaultGateway, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.PORT_CHECKER -> TcpPortCheckerDialog(defaultHost = overview.defaultGateway, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.SPEED_TEST -> SpeedTestDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.WIFI_ANALYZER -> WifiAnalyzerDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.NONE -> {}
    }
}

// -------------------------------------------------------------
// Diagnostic Tool Card Component (Matches User Screenshots)
// -------------------------------------------------------------
@Composable
private fun DiagnosticToolCard(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = AccentCyan,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        borderColor = CardBorder,
        backgroundColor = BgCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!badgeText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(7.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 1. Ping Test Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun PingTestDialog(
    defaultGateway: String,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var targetHost by remember { mutableStateOf("8.8.8.8") }
    var pingReport by remember { mutableStateOf(PingReport()) }
    var activePingJob by remember { mutableStateOf<Job?>(null) }

    fun startPing(host: String) {
        activePingJob?.cancel()
        activePingJob = coroutineScope.launch {
            NetworkToolsManager.executePing(host, count = 6) { report ->
                pingReport = report
            }
        }
    }

    Dialog(
        onDismissRequest = {
            activePingJob?.cancel()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.88f)
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ping Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = { activePingJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Host Input
                    OutlinedTextField(
                        value = targetHost,
                        onValueChange = { targetHost = it },
                        label = { Text("Target Host / IP", color = TextSecondary) },
                        placeholder = { Text("8.8.8.8", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgCardLight.copy(alpha = 0.5f),
                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f),
                            cursorColor = AccentCyan
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Host Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("8.8.8.8", "1.1.1.1", defaultGateway.ifBlank { "192.168.1.1" }).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (targetHost == preset) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                    .border(1.dp, if (targetHost == preset) AccentCyan else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { targetHost = preset }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(preset, color = if (targetHost == preset) AccentCyan else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (pingReport.isRunning) {
                                activePingJob?.cancel()
                                pingReport = pingReport.copy(isRunning = false)
                            } else {
                                startPing(targetHost)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (pingReport.isRunning) AccentRed else AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(imageVector = if (pingReport.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = BgDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (pingReport.isRunning) "Stop Ping" else "Start Ping Test", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(modifier = Modifier.weight(1f), label = "LOSS", value = "${pingReport.packetLossPercent}%", valueColor = if (pingReport.packetLossPercent == 0) AccentGreen else AccentRed)
                        StatBox(modifier = Modifier.weight(1f), label = "MIN RTT", value = "${pingReport.minRttMs.roundToInt()}ms", valueColor = AccentCyan)
                        StatBox(modifier = Modifier.weight(1f), label = "AVG RTT", value = "${pingReport.avgRttMs.roundToInt()}ms", valueColor = TextPrimary)
                        StatBox(modifier = Modifier.weight(1f), label = "MAX RTT", value = "${pingReport.maxRttMs.roundToInt()}ms", valueColor = AccentOrange)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terminal Console Log
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                            if (pingReport.logs.isEmpty()) {
                                Text("Click 'Start Ping Test' to run ICMP probe...", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            } else {
                                pingReport.logs.forEach { line ->
                                    Text(line, color = Color(0xFF6EE7B7), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. DNS Lookup Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun DnsLookupDialog(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var domainInput by remember { mutableStateOf("google.com") }
    var dnsResult by remember { mutableStateOf<DnsRecord?>(null) }
    var isQuerying by remember { mutableStateOf(false) }

    fun runLookup(domain: String) {
        coroutineScope.launch {
            isQuerying = true
            dnsResult = NetworkToolsManager.resolveDns(domain)
            isQuerying = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DNS Lookup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = domainInput,
                        onValueChange = { domainInput = it },
                        label = { Text("Domain Name", color = TextSecondary) },
                        placeholder = { Text("e.g. google.com", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgCardLight.copy(alpha = 0.5f),
                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("google.com", "cloudflare.com", "bdix.net", "facebook.com").forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (domainInput == p) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                    .border(1.dp, if (domainInput == p) AccentCyan else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { domainInput = p }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(p, color = if (domainInput == p) AccentCyan else TextSecondary, fontSize = 10.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { runLookup(domainInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isQuerying && domainInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(if (isQuerying) "Resolving DNS..." else "Resolve DNS Records", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    dnsResult?.let { res ->
                        if (res.isSuccess) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status: Resolved", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Response: ${res.responseTimeMs} ms", color = AccentCyan, fontSize = 11.5.sp)
                                }

                                RecordCard(title = "Canonical Host", list = listOf(res.canonicalName))
                                RecordCard(title = "IPv4 Addresses (A Records)", list = res.ipv4List)
                                if (res.ipv6List.isNotEmpty()) {
                                    RecordCard(title = "IPv6 Addresses (AAAA Records)", list = res.ipv6List)
                                }
                            }
                        } else {
                            Text(res.errorMessage ?: "DNS resolution failed", color = AccentRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. IP Address Check Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun IpAddressCheckDialog(overview: NetworkOverview, onDismiss: () -> Unit) {
    val context = LocalContext.current

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IP Address & Interface", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    IpInfoRow(title = "Connection Status", value = if (overview.isConnected) "ONLINE" else "OFFLINE", color = if (overview.isConnected) AccentGreen else AccentRed)
                    IpInfoRow(title = "Network Interface", value = overview.connectionType, color = TextPrimary)
                    IpInfoRow(title = "Local IPv4 Address", value = overview.localIpv4, color = AccentCyan, onCopy = { copyToClipboard("Local IP", overview.localIpv4) })
                    IpInfoRow(title = "Default Gateway", value = overview.defaultGateway, color = TextPrimary, onCopy = { copyToClipboard("Gateway", overview.defaultGateway) })
                    IpInfoRow(title = "Primary DNS", value = overview.dns1, color = TextSecondary, onCopy = { copyToClipboard("DNS 1", overview.dns1) })
                    IpInfoRow(title = "Secondary DNS", value = overview.dns2, color = TextSecondary, onCopy = { copyToClipboard("DNS 2", overview.dns2) })
                    IpInfoRow(title = "Public ISP IP", value = overview.publicIp, color = AccentOrange, onCopy = { copyToClipboard("Public IP", overview.publicIp) })
                    IpInfoRow(title = "ISP Provider", value = overview.ispName, color = TextPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val fullSummary = "Local IP: ${overview.localIpv4}\nGateway: ${overview.defaultGateway}\nDNS: ${overview.dns1}, ${overview.dns2}\nPublic IP: ${overview.publicIp}"
                            copyToClipboard("Network Info", fullSummary)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Full Network Diagnostic", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Gateway Reachability Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun GatewayReachabilityDialog(gatewayIp: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var targetGateway by remember { mutableStateOf(gatewayIp.ifBlank { "192.168.1.1" }) }
    var auditResult by remember { mutableStateOf<GatewayAudit?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    fun runAudit() {
        coroutineScope.launch {
            isChecking = true
            auditResult = NetworkToolsManager.auditGateway(targetGateway)
            isChecking = false
        }
    }

    LaunchedEffect(Unit) {
        runAudit()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Router, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gateway Reachability", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = targetGateway,
                            onValueChange = { targetGateway = it },
                            label = { Text("Gateway IP", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { runAudit() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isChecking,
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Test", tint = BgDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    auditResult?.let { res ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (res.isPingable) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Router Status: ${if (res.isPingable) "REACHABLE (ONLINE)" else "UNREACHABLE"}", color = if (res.isPingable) AccentGreen else AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (res.isPingable) Text("${res.pingLatencyMs} ms", color = AccentCyan, fontSize = 11.5.sp)
                            }

                            Text("Common Service Ports:", color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)

                            res.ports.forEach { portRes ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgCardLight)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Port ${portRes.port} • ${portRes.serviceName}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(if (portRes.isOpen) "Open (${portRes.latencyMs} ms)" else if (portRes.isTimeout) "Filtered / Timeout" else "Closed", color = if (portRes.isOpen) AccentGreen else if (portRes.isTimeout) AccentOrange else AccentRed, fontSize = 10.5.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (portRes.isOpen) AccentGreen else if (portRes.isTimeout) AccentOrange else AccentRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (portRes.isOpen) "OPEN" else "OFF", color = BgDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Open in Browser Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$targetGateway"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Router Webpage ($targetGateway)", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. TCP Port Checker Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun TcpPortCheckerDialog(defaultHost: String, onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var hostInput by remember { mutableStateOf(defaultHost.ifBlank { "192.168.1.1" }) }
    var portInput by remember { mutableStateOf("8291") }
    var result by remember { mutableStateOf<PortCheckResult?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    fun scan(h: String, pStr: String) {
        val p = pStr.toIntOrNull() ?: 80
        coroutineScope.launch {
            isChecking = true
            result = NetworkToolsManager.checkPort(h, p)
            isChecking = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SettingsEthernet, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TCP Port Checker", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hostInput,
                            onValueChange = { hostInput = it },
                            label = { Text("Target IP", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Port", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Quick Port Presets:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("8291" to "Winbox", "80" to "HTTP", "443" to "HTTPS", "8080" to "ONU", "22" to "SSH").forEach { (p, l) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (portInput == p) AccentPurple.copy(alpha = 0.25f) else BgCardLight)
                                    .border(1.dp, if (portInput == p) AccentPurple else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { portInput = p }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("$p ($l)", color = if (portInput == p) AccentPurple else TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { scan(hostInput, portInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isChecking && hostInput.isNotBlank() && portInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(if (isChecking) "Testing TCP Port..." else "Check Port Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    result?.let { res ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (res.isOpen) AccentGreen.copy(alpha = 0.15f) else if (res.isTimeout) AccentOrange.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f))
                                .border(1.dp, if (res.isOpen) AccentGreen else if (res.isTimeout) AccentOrange else AccentRed, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (res.isOpen) "PORT OPEN" else if (res.isTimeout) "TIMEOUT / FILTERED" else "PORT CLOSED",
                                        color = if (res.isOpen) AccentGreen else if (res.isTimeout) AccentOrange else AccentRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (res.isOpen) {
                                        Text("${res.latencyMs} ms", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(res.message, color = TextPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Service: ${res.serviceName}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Speed Test Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun SpeedTestDialog(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var state by remember { mutableStateOf(SpeedTestState()) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    fun startSpeedTest() {
        activeJob?.cancel()
        activeJob = coroutineScope.launch {
            NetworkToolsManager.runSpeedTest { newState ->
                state = newState
            }
        }
    }

    Dialog(onDismissRequest = { activeJob?.cancel(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speed Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF4C1D95)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("Live ISP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { activeJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Speedometer / Circle Meter
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF831843).copy(alpha = 0.15f))
                            .border(3.dp, if (state.isTesting) AccentCyan else Color(0xFFF472B6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f".format(if (state.isTesting) state.currentSpeedMbps else state.finalDownloadMbps),
                                color = TextPrimary,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Mbps", color = Color(0xFFF472B6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.phase, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = AccentCyan,
                        trackColor = BgCardLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ping & Jitter badges
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(modifier = Modifier.weight(1f), label = "PING", value = "${state.latencyMs} ms", valueColor = AccentCyan)
                        StatBox(modifier = Modifier.weight(1f), label = "JITTER", value = "${state.jitterMs} ms", valueColor = AccentOrange)
                        StatBox(modifier = Modifier.weight(1f), label = "DATA", value = "${state.bytesDownloaded / 1_000_000} MB", valueColor = AccentGreen)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (state.isTesting) {
                                activeJob?.cancel()
                                state = state.copy(isTesting = false, phase = "Stopped")
                            } else {
                                startSpeedTest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (state.isTesting) AccentRed else Color(0xFFE11D48)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(if (state.isTesting) "Stop Test" else "Start Live Speed Benchmark", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Wi-Fi Analyzer Interactive Dialog
// -------------------------------------------------------------
@Composable
private fun WifiAnalyzerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var audit by remember { mutableStateOf(WifiAudit()) }
    var isScanning by remember { mutableStateOf(false) }

    fun scan() {
        coroutineScope.launch {
            isScanning = true
            audit = NetworkToolsManager.getWifiAudit(context)
            isScanning = false
        }
    }

    LaunchedEffect(Unit) {
        scan()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.85f).padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wi-Fi Analyzer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF312E81)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("Real Scan", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!audit.isConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentOrange.copy(alpha = 0.15f))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Please connect to a Wi-Fi network to analyze channels and signal.", color = AccentOrange, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        // Wi-Fi Header Info
                        Text(audit.ssid, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("BSSID: ${audit.bssid}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Signal Quality Bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Signal: ${audit.rssiDbm} dBm", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(audit.rating, color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { audit.signalPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = AccentGreen,
                            trackColor = BgCardLight
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technical Specs
                        IpInfoRow(title = "Frequency Band", value = "${audit.band} (${audit.frequencyMhz} MHz)", color = TextPrimary)
                        IpInfoRow(title = "Wi-Fi Channel", value = "Channel ${audit.channel}", color = AccentCyan)
                        IpInfoRow(title = "Link Speed", value = "${audit.linkSpeedMbps} Mbps", color = AccentOrange)
                        IpInfoRow(title = "Assigned IP", value = audit.ipAddress, color = TextPrimary)
                        IpInfoRow(title = "Gateway Router", value = audit.gateway, color = TextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Channel Recommendation Tip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgCardLight)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("💡 ISP Technician Channel Recommendation:", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (audit.band.contains("2.4")) {
                                        "For 2.4 GHz, recommended non-overlapping channels are 1, 6, and 11. Your router is currently on Channel ${audit.channel}."
                                    } else {
                                        "5 GHz band provides minimal interference and superior speeds. Your router is on Channel ${audit.channel}."
                                    },
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { scan() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isScanning) "Scanning Wi-Fi..." else "Refresh Wi-Fi Scan", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Small Helper Components
// -------------------------------------------------------------
@Composable
private fun StatBox(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgCardLight)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IpInfoRow(title: String, value: String, color: Color, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = color, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp).clickable { onCopy() }
                )
            }
        }
    }
}

@Composable
private fun RecordCard(title: String, list: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgCardLight)
            .padding(10.dp)
    ) {
        Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        list.forEach { item ->
            Text(item, color = AccentCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
