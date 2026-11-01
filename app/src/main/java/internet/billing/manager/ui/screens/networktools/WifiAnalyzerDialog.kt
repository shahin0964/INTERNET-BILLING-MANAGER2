package internet.billing.manager.ui.screens.networktools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*
import kotlinx.coroutines.launch

@Composable
fun WifiAnalyzerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var audit by remember { mutableStateOf(WifiAudit()) }
    var isScanning by remember { mutableStateOf(false) }

    var isEditingName by remember { mutableStateOf(false) }
    var customNameInput by remember { mutableStateOf("") }

    fun scan() {
        coroutineScope.launch {
            isScanning = true
            audit = NetworkToolsManager.getWifiAudit(context)
            customNameInput = audit.customName
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
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wi-Fi Signal & Channels", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            Text("Please connect to a Wi-Fi network to analyze channel occupancy & signal.", color = AccentOrange, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        // Wi-Fi Header Info with Rename option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(audit.ssid, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    if (audit.customName.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(${audit.customName})", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("BSSID: ${audit.bssid}", color = TextMuted, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                            }

                            IconButton(onClick = { isEditingName = true }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = AccentCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Signal Quality Bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Signal: ${audit.rssiDbm} dBm (${audit.signalPercent}%)", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
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

                        // Channel Occupancy Bar Graph
                        ChannelOccupancyGraph(
                            currentChannel = audit.channel,
                            occupancyMap = audit.channelOccupancy
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technical Specs
                        IpInfoRow(title = "Frequency Band", value = "${audit.band} (${audit.frequencyMhz} MHz)", color = TextPrimary)
                        IpInfoRow(title = "Current Channel", value = "Channel ${audit.channel}", color = AccentCyan)
                        IpInfoRow(title = "Link Speed", value = "${audit.linkSpeedMbps} Mbps", color = AccentOrange)
                        IpInfoRow(title = "Assigned IP", value = audit.ipAddress, color = TextPrimary)
                        IpInfoRow(title = "Gateway Router", value = audit.gateway, color = TextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Channel Recommendation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgCardLight)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("💡 Channel Optimization Guide:", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (audit.band.contains("2.4")) {
                                        "For 2.4 GHz, use non-overlapping channels: 1, 6, or 11. Channels 1, 6, and 11 prevent cross-channel interference and ensure maximum ISP bandwidth. Your router is currently on Channel ${audit.channel}."
                                    } else {
                                        "5 GHz band offers up to 24 non-overlapping channels with minimal interference. Your router is on Channel ${audit.channel}."
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
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isScanning) "Scanning Wi-Fi..." else "Refresh Wi-Fi Scan", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }

    // Modal Dialog to Assign Custom Wi-Fi Name
    if (isEditingName) {
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            title = { Text("Assign Custom Wi-Fi Name", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text("Network: ${audit.ssid}", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customNameInput,
                        onValueChange = { customNameInput = it },
                        label = { Text("Friendly Name / Alias") },
                        placeholder = { Text("e.g. Home Office Wi-Fi, 2nd Floor AP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        NetworkToolsPrefs.setCustomWifiName(context, audit.ssid, customNameInput)
                        audit = audit.copy(customName = customNameInput)
                        isEditingName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Save", color = BgDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingName = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
