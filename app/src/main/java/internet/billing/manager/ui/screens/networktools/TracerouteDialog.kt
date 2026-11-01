package internet.billing.manager.ui.screens.networktools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun TracerouteDialog(
    defaultGateway: String,
    onDismiss: () -> Unit,
    onOpenPing: (String) -> Unit,
    onOpenPortCheck: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var targetHost by remember { mutableStateOf("1.1.1.1") }
    var maxHops by remember { mutableStateOf(20) }
    var probeType by remember { mutableStateOf("ICMP") }
    var hopsList by remember { mutableStateOf(listOf<TracerouteHop>()) }
    var logsList by remember { mutableStateOf(listOf<String>()) }
    var isRunning by remember { mutableStateOf(false) }
    var activeJob by remember { mutableStateOf<Job?>(null) }
    var selectedHop by remember { mutableStateOf<TracerouteHop?>(null) }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Traceroute Hop", text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    fun startTraceroute(host: String) {
        NetworkToolsPrefs.addHistory(context, "traceroute", host)
        activeJob?.cancel()
        hopsList = emptyList()
        logsList = emptyList()
        isRunning = true

        activeJob = coroutineScope.launch {
            NetworkToolsManager.runTraceroute(
                host = host,
                maxHops = maxHops,
                probeType = probeType,
                onHop = { hop ->
                    hopsList = hopsList + hop
                },
                onLog = { log ->
                    logsList = logsList + log
                }
            )
            isRunning = false
        }
    }

    Dialog(
        onDismissRequest = {
            activeJob?.cancel()
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
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.90f)
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
                            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Traceroute Route Visualizer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = { activeJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Host Input
                    OutlinedTextField(
                        value = targetHost,
                        onValueChange = { targetHost = it },
                        label = { Text("Destination Host / IP", color = TextSecondary) },
                        placeholder = { Text("e.g. google.com or 1.1.1.1", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgCardLight.copy(alpha = 0.5f),
                            unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Configuration: Max Hops & Probe Type (ICMP / UDP)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Probe Type
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Probe:", color = TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            listOf("ICMP", "UDP").forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (probeType == type) Color(0xFF0369A1) else BgCardLight)
                                        .clickable { probeType = type }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(type, color = if (probeType == type) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Max Hops
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Max Hops:", color = TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            listOf(15, 20, 30).forEach { hops ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (maxHops == hops) Color(0xFF0369A1) else BgCardLight)
                                        .clickable { maxHops = hops }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("$hops", color = if (maxHops == hops) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (isRunning) {
                                activeJob?.cancel()
                                isRunning = false
                            } else {
                                startTraceroute(targetHost)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) AccentRed else Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) "Stop Traceroute" else "Start Traceroute ($probeType, $maxHops hops)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hop List View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (hopsList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Press Start to trace route hops to $targetHost...", color = TextMuted, fontSize = 11.5.sp)
                                }
                            }
                        } else {
                            items(hopsList) { hop ->
                                HopCard(
                                    hop = hop,
                                    isSelected = selectedHop?.hop == hop.hop,
                                    onSelect = { selectedHop = hop },
                                    onCopy = { copyToClipboard(hop.ip) },
                                    onPing = { onOpenPing(hop.ip) },
                                    onPortCheck = { onOpenPortCheck(hop.ip) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HopCard(
    hop: TracerouteHop,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    onPing: () -> Unit,
    onPortCheck: () -> Unit
) {
    val rttColor = when {
        hop.isTimeout -> TextMuted
        hop.rttMs < 40f -> AccentGreen
        hop.rttMs < 100f -> AccentCyan
        hop.rttMs < 200f -> AccentOrange
        else -> AccentRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BgCardLight else BgCardLight.copy(alpha = 0.5f))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else CardBorder, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hop Badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (hop.isReached) AccentGreen.copy(alpha = 0.2f) else BgCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${hop.hop}",
                        color = if (hop.isReached) AccentGreen else Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (hop.isTimeout) "* * * (Timeout)" else hop.ip,
                            color = if (hop.isTimeout) TextMuted else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (hop.isReached) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentGreen).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                Text("DESTINATION", color = BgDark, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (hop.hostName.isNotBlank()) {
                        Text(hop.hostName, color = TextSecondary, fontSize = 10.5.sp)
                    }
                }
            }

            // Latency
            Text(
                text = if (hop.isTimeout) "Timeout" else "${"%.1f".format(hop.rttMs)} ms",
                color = rttColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Quick Hop Action Bar when selected
        if (isSelected && !hop.isTimeout && hop.ip != "*") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onPing,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ping Hop", color = AccentCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPortCheck,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.SettingsEthernet, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Port Scan", color = AccentPurple, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(BgCard)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
