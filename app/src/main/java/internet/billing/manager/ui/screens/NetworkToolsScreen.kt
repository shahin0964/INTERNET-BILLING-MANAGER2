package internet.billing.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.screens.networktools.*
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*
import kotlinx.coroutines.launch

private enum class ActiveToolSheet {
    NONE,
    SPEED_TEST,
    PING,
    TRACEROUTE,
    DNS,
    WIFI_ANALYZER,
    LAN_SCANNER,
    PORT_SCANNER,
    WAKE_ON_LAN,
    UPNP_BROWSER,
    WHOIS,
    GATEWAY,
    IP_CHECK
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

    // Inter-tool navigation state (e.g. from LAN scanner or Traceroute to Ping/Port check)
    var sharedTargetHost by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 2. Wi-Fi / Active Network Card
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

                        Spacer(modifier = Modifier.height(14.dp))

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

            // 3. Section Title: DIAGNOSTIC & PERFORMANCE SUITE
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DIAGNOSTIC & PERFORMANCE SUITE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            // Tool 1: Speed Test (with Live ISP badge)
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Speed,
                    iconContainerColor = Color(0xFF831843).copy(alpha = 0.5f),
                    iconTint = Color(0xFFF472B6),
                    title = "⚡ Speed Test",
                    badgeText = "DL & UL",
                    badgeColor = Color(0xFF4C1D95),
                    subtitle = "Benchmark download & upload speed, latency, and jitter",
                    onClick = { activeSheet = ActiveToolSheet.SPEED_TEST }
                )
            }

            // Tool 2: Ping Test (with RTT graph, payload, interval)
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Public,
                    iconContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF60A5FA),
                    title = "Ping Diagnostic",
                    badgeText = "Graph & Geo",
                    badgeColor = Color(0xFF1E40AF),
                    subtitle = "Configurable payload (32-1472B), interval, AS number, and RTT graph",
                    onClick = {
                        sharedTargetHost = "8.8.8.8"
                        activeSheet = ActiveToolSheet.PING
                    }
                )
            }

            // Tool 3: Traceroute Visualizer
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Route,
                    iconContainerColor = Color(0xFF0369A1).copy(alpha = 0.5f),
                    iconTint = Color(0xFF38BDF8),
                    title = "🗺️ Traceroute Route Visualizer",
                    badgeText = "ICMP / UDP",
                    badgeColor = Color(0xFF0284C7),
                    subtitle = "Hop-by-hop latency route map with quick ping & port inspection",
                    onClick = { activeSheet = ActiveToolSheet.TRACEROUTE }
                )
            }

            // Tool 4: DNS Query & DNSSEC Analyzer
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Search,
                    iconContainerColor = Color(0xFF0E7490).copy(alpha = 0.5f),
                    iconTint = Color(0xFF38BDF8),
                    title = "DNS Query & DNSSEC",
                    badgeText = "All Sections",
                    badgeColor = Color(0xFF0891B2),
                    subtitle = "A, AAAA, MX, TXT, NS records with DNSSEC validation & sections",
                    onClick = { activeSheet = ActiveToolSheet.DNS }
                )
            }

            // Tool 5: Wi-Fi Signal & Channels
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.WifiTethering,
                    iconContainerColor = Color(0xFF134E4A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF2DD4BF),
                    title = "📡 Wi-Fi Signal & Channels",
                    badgeText = "Channel Map",
                    badgeColor = Color(0xFF312E81),
                    subtitle = "2.4 GHz channel occupancy graph, optimal channels & custom name",
                    onClick = { activeSheet = ActiveToolSheet.WIFI_ANALYZER }
                )
            }

            // 4. Section Title: INFRASTRUCTURE & LAN TOOLS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "INFRASTRUCTURE & LAN TOOLS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            // Tool 6: LAN Subnet Scanner
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Devices,
                    iconContainerColor = Color(0xFF065F46).copy(alpha = 0.5f),
                    iconTint = Color(0xFF34D399),
                    title = "🔎 LAN Subnet Scanner",
                    badgeText = "OUI & MAC",
                    badgeColor = Color(0xFF047857),
                    subtitle = "Scan /24 subnet or custom range, detect vendors, rename, and star",
                    onClick = { activeSheet = ActiveToolSheet.LAN_SCANNER }
                )
            }

            // Tool 7: Multi-Port Scanner
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.SettingsEthernet,
                    iconContainerColor = Color(0xFF581C87).copy(alpha = 0.5f),
                    iconTint = Color(0xFFC084FC),
                    title = "🔌 Multi-Port Scanner",
                    badgeText = "Presets",
                    badgeColor = Color(0xFF6B21A8),
                    subtitle = "MikroTik, Web, Mail, or custom port range with speed presets",
                    onClick = {
                        sharedTargetHost = overview.defaultGateway
                        activeSheet = ActiveToolSheet.PORT_SCANNER
                    }
                )
            }

            // Tool 8: Wake on LAN (WOL)
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.PowerSettingsNew,
                    iconContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF60A5FA),
                    title = "⚡ Wake on LAN (WOL)",
                    badgeText = "Remote WOL",
                    badgeColor = Color(0xFF1D4ED8),
                    subtitle = "Wake remote PCs, NAS, and MikroTik via UDP magic packet",
                    onClick = { activeSheet = ActiveToolSheet.WAKE_ON_LAN }
                )
            }

            // Tool 9: UPnP & Bonjour Browser
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Router,
                    iconContainerColor = Color(0xFF0F766E).copy(alpha = 0.5f),
                    iconTint = Color(0xFF2DD4BF),
                    title = "📡 UPnP & Bonjour Browser",
                    badgeText = "SSDP Discovery",
                    badgeColor = Color(0xFF0D9488),
                    subtitle = "Discover local routers, ONUs, media servers, and open web consoles",
                    onClick = { activeSheet = ActiveToolSheet.UPNP_BROWSER }
                )
            }

            // Tool 10: WHOIS Lookup
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Language,
                    iconContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                    iconTint = Color(0xFF38BDF8),
                    title = "🏢 WHOIS Domain & IP Lookup",
                    badgeText = "Custom Server",
                    badgeColor = Color(0xFF1E40AF),
                    subtitle = "Registrar, creation & expiry dates, name servers, and raw records",
                    onClick = { activeSheet = ActiveToolSheet.WHOIS }
                )
            }

            // Tool 11: Gateway Reachability
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Router,
                    iconContainerColor = Color(0xFF78350F).copy(alpha = 0.5f),
                    iconTint = Color(0xFFFBBF24),
                    title = "🛡️ Gateway Reachability",
                    subtitle = "Audit router default gateway, ports (80, 8291), and open web GUI",
                    onClick = { activeSheet = ActiveToolSheet.GATEWAY }
                )
            }

            // Tool 12: IP Address & Interface Check
            item {
                DiagnosticToolCard(
                    icon = Icons.Default.Wifi,
                    iconContainerColor = Color(0xFF065F46).copy(alpha = 0.5f),
                    iconTint = Color(0xFF34D399),
                    title = "ℹ️ IP Address & Interface",
                    subtitle = "Inspect interfaces, gateway, DNS servers, public IP, ASN & Geo",
                    onClick = { activeSheet = ActiveToolSheet.IP_CHECK }
                )
            }
        }
    }

    // Modal Interactive Tool Dialogs
    when (activeSheet) {
        ActiveToolSheet.SPEED_TEST -> SpeedTestDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.PING -> PingTestDialog(
            defaultGateway = sharedTargetHost.ifBlank { overview.defaultGateway },
            onDismiss = { activeSheet = ActiveToolSheet.NONE }
        )
        ActiveToolSheet.TRACEROUTE -> TracerouteDialog(
            defaultGateway = overview.defaultGateway,
            onDismiss = { activeSheet = ActiveToolSheet.NONE },
            onOpenPing = { hopIp ->
                sharedTargetHost = hopIp
                activeSheet = ActiveToolSheet.PING
            },
            onOpenPortCheck = { hopIp ->
                sharedTargetHost = hopIp
                activeSheet = ActiveToolSheet.PORT_SCANNER
            }
        )
        ActiveToolSheet.DNS -> DnsQueryDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.WIFI_ANALYZER -> WifiAnalyzerDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.LAN_SCANNER -> LanScannerDialog(
            localIpv4 = overview.localIpv4,
            onDismiss = { activeSheet = ActiveToolSheet.NONE },
            onOpenPing = { ip ->
                sharedTargetHost = ip
                activeSheet = ActiveToolSheet.PING
            },
            onOpenTraceroute = { ip ->
                activeSheet = ActiveToolSheet.TRACEROUTE
            },
            onOpenPortScan = { ip ->
                sharedTargetHost = ip
                activeSheet = ActiveToolSheet.PORT_SCANNER
            }
        )
        ActiveToolSheet.PORT_SCANNER -> PortScannerDialog(
            defaultHost = sharedTargetHost.ifBlank { overview.defaultGateway },
            onDismiss = { activeSheet = ActiveToolSheet.NONE }
        )
        ActiveToolSheet.WAKE_ON_LAN -> WakeOnLanDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.UPNP_BROWSER -> UpnpBrowserDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.WHOIS -> WhoisDialog(onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.GATEWAY -> GatewayDialog(gatewayIp = overview.defaultGateway, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.IP_CHECK -> IpCheckDialog(overview = overview, onDismiss = { activeSheet = ActiveToolSheet.NONE })
        ActiveToolSheet.NONE -> {}
    }
}
