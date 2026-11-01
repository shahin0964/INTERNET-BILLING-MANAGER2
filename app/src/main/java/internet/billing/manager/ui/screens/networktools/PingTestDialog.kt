package internet.billing.manager.ui.screens.networktools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import kotlin.math.roundToInt

@Composable
fun PingTestDialog(
    defaultGateway: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var targetHost by remember { mutableStateOf("8.8.8.8") }
    var payloadSize by remember { mutableStateOf(56) }
    var intervalSec by remember { mutableStateOf(0.5f) }
    var pingReport by remember { mutableStateOf(PingReport()) }
    var activePingJob by remember { mutableStateOf<Job?>(null) }
    var isStarred by remember { mutableStateOf(false) }

    fun checkStarred(host: String) {
        isStarred = NetworkToolsPrefs.isStarred(context, "ping", host)
    }

    LaunchedEffect(targetHost) {
        checkStarred(targetHost)
    }

    fun startPing(host: String) {
        NetworkToolsPrefs.addHistory(context, "ping", host)
        activePingJob?.cancel()
        activePingJob = coroutineScope.launch {
            NetworkToolsManager.executePing(
                host = host,
                count = 12,
                intervalSec = intervalSec,
                payloadBytes = payloadSize
            ) { report ->
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
                            Icon(Icons.Default.Public, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ping Diagnostic", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = { activePingJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Host Input with Star button
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = targetHost,
                            onValueChange = {
                                targetHost = it
                                checkStarred(it)
                            },
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                isStarred = NetworkToolsPrefs.toggleStarred(context, "ping", targetHost)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgCardLight)
                        ) {
                            Icon(
                                imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star",
                                tint = if (isStarred) AccentOrange else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Configuration: Payload Size & Interval
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Payload Size Selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Payload:", color = TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            listOf(32, 56, 128, 512, 1472).forEach { sz ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (payloadSize == sz) AccentCyan.copy(alpha = 0.25f) else BgCardLight)
                                        .clickable { payloadSize = sz }
                                        .padding(horizontal = 5.dp, vertical = 3.dp)
                                ) {
                                    Text("${sz}B", color = if (payloadSize == sz) AccentCyan else TextSecondary, fontSize = 9.5.sp)
                                }
                            }
                        }

                        // Interval
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rate:", color = TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            listOf(0.2f to "0.2s", 0.5f to "0.5s", 1.0f to "1.0s").forEach { (sec, label) ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (intervalSec == sec) AccentCyan.copy(alpha = 0.25f) else BgCardLight)
                                        .clickable { intervalSec = sec }
                                        .padding(horizontal = 5.dp, vertical = 3.dp)
                                ) {
                                    Text(label, color = if (intervalSec == sec) AccentCyan else TextSecondary, fontSize = 9.5.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Host Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val history = NetworkToolsPrefs.getHistory(context, "ping").take(2)
                        val presets = (listOf("8.8.8.8", "1.1.1.1", defaultGateway.ifBlank { "192.168.1.1" }) + history).distinct().take(4)
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (targetHost == preset) AccentCyan.copy(alpha = 0.2f) else BgCardLight)
                                    .border(1.dp, if (targetHost == preset) AccentCyan else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { targetHost = preset }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(preset, color = if (targetHost == preset) AccentCyan else TextSecondary, fontSize = 10.5.sp)
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
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(imageVector = if (pingReport.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (pingReport.isRunning) "Stop Ping" else "Start Ping Test ($payloadSize B)", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatBox(modifier = Modifier.weight(1f), label = "LOSS", value = "${pingReport.packetLossPercent}%", valueColor = if (pingReport.packetLossPercent == 0) AccentGreen else AccentRed)
                        StatBox(modifier = Modifier.weight(1f), label = "MIN RTT", value = "${pingReport.minRttMs.roundToInt()}ms", valueColor = AccentCyan)
                        StatBox(modifier = Modifier.weight(1f), label = "AVG RTT", value = "${pingReport.avgRttMs.roundToInt()}ms", valueColor = TextPrimary)
                        StatBox(modifier = Modifier.weight(1f), label = "MAX RTT", value = "${pingReport.maxRttMs.roundToInt()}ms", valueColor = AccentOrange)
                        StatBox(modifier = Modifier.weight(1f), label = "JITTER", value = "${pingReport.jitterMs.roundToInt()}ms", valueColor = Color(0xFFC084FC))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Graphical Visualization of Probe Times
                    if (pingReport.probes.isNotEmpty()) {
                        RttBarGraph(probes = pingReport.probes)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

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
                                Text("Click 'Start Ping Test' to run ICMP probe with payload: $payloadSize bytes...", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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
