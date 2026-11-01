package internet.billing.manager.ui.screens.networktools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun PortScannerDialog(
    defaultHost: String,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var hostInput by remember { mutableStateOf(defaultHost.ifBlank { "192.168.1.1" }) }
    var selectedPreset by remember { mutableStateOf("ISP/Router") }
    var speedPreset by remember { mutableStateOf("Fast (300ms)") }
    var customPortsInput by remember { mutableStateOf("80, 443, 8291, 8080, 22") }

    var resultsList by remember { mutableStateOf(listOf<PortCheckResult>()) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    fun getPortsToScan(): List<Int> {
        return when (selectedPreset) {
            "ISP/Router" -> listOf(80, 443, 8291, 8080, 8728, 8729, 22, 23, 53, 21)
            "Web & Mail" -> listOf(80, 443, 8080, 8443, 25, 110, 143, 993, 995, 587)
            "Remote Mgmt" -> listOf(22, 23, 3389, 5900, 8291, 80, 443)
            else -> {
                customPortsInput.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..65535 }
            }
        }
    }

    fun getTimeout(): Int {
        return when (speedPreset) {
            "Fast (300ms)" -> 300
            "Normal (800ms)" -> 800
            else -> 1500
        }
    }

    fun startScan() {
        val ports = getPortsToScan()
        if (ports.isEmpty()) return

        activeJob?.cancel()
        resultsList = emptyList()
        isScanning = true
        scannedCount = 0
        totalCount = ports.size

        activeJob = coroutineScope.launch {
            NetworkToolsManager.scanPorts(
                host = hostInput,
                ports = ports,
                timeoutMs = getTimeout(),
                concurrency = 8,
                onPortResult = { res ->
                    resultsList = resultsList + res
                },
                onProgress = { s, t ->
                    scannedCount = s
                    totalCount = t
                }
            )
            isScanning = false
        }
    }

    Dialog(onDismissRequest = { activeJob?.cancel(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
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
                            Icon(Icons.Default.SettingsEthernet, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Multi-Port Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = { activeJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Host Input
                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Target Host / IP", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Port Presets Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("ISP/Router", "Web & Mail", "Remote Mgmt", "Custom").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedPreset == preset) AccentPurple.copy(alpha = 0.25f) else BgCardLight)
                                    .border(1.dp, if (selectedPreset == preset) AccentPurple else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedPreset = preset }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(preset, color = if (selectedPreset == preset) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (selectedPreset == "Custom") {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customPortsInput,
                            onValueChange = { customPortsInput = it },
                            label = { Text("Custom Ports (comma separated)", color = TextSecondary, fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scanner Speed Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speed:", color = TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Fast (300ms)", "Normal (800ms)", "Deep (1500ms)").forEach { spd ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (speedPreset == spd) Color(0xFF6B21A8) else BgCardLight)
                                        .clickable { speedPreset = spd }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(spd.substringBefore(" "), color = if (speedPreset == spd) Color.White else TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (isScanning) {
                                activeJob?.cancel()
                                isScanning = false
                            } else {
                                startScan()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) AccentRed else AccentPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isScanning) "Stop Port Scan" else "Scan Ports (${getPortsToScan().size} ports)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    if (isScanning || totalCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val frac = if (totalCount > 0) (scannedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { frac },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = AccentPurple,
                            trackColor = BgCardLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Results List
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (resultsList.isEmpty() && !isScanning) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                                    Text("Click 'Scan Ports' to audit ports on $hostInput", color = TextMuted, fontSize = 11.5.sp)
                                }
                            }
                        } else {
                            items(resultsList) { portRes ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (portRes.isOpen) AccentGreen.copy(alpha = 0.12f) else BgCardLight)
                                        .border(1.dp, if (portRes.isOpen) AccentGreen.copy(alpha = 0.4f) else CardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Port ${portRes.port}",
                                            color = if (portRes.isOpen) AccentGreen else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = portRes.serviceName,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (portRes.isOpen) AccentGreen else if (portRes.isTimeout) AccentOrange.copy(alpha = 0.8f) else AccentRed.copy(alpha = 0.8f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (portRes.isOpen) "OPEN (${portRes.latencyMs}ms)" else if (portRes.isTimeout) "TIMEOUT" else "CLOSED",
                                            color = BgDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
