package internet.billing.manager.ui.screens.networktools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
fun LanScannerDialog(
    localIpv4: String,
    onDismiss: () -> Unit,
    onOpenPing: (String) -> Unit,
    onOpenTraceroute: (String) -> Unit,
    onOpenPortScan: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultSubnet = remember(localIpv4) {
        val parts = localIpv4.split(".")
        if (parts.size >= 3) "${parts[0]}.${parts[1]}.${parts[2]}" else "192.168.1"
    }

    var subnetInput by remember { mutableStateOf(defaultSubnet) }
    var startIpInput by remember { mutableStateOf("1") }
    var endIpInput by remember { mutableStateOf("50") } // Default fast range 1-50, user can do 1-254
    var isFullScan by remember { mutableStateOf(false) }

    var deviceList by remember { mutableStateOf(listOf<LanDevice>()) }
    var isScanning by remember { mutableStateOf(false) }
    var progressScanned by remember { mutableStateOf(0) }
    var progressTotal by remember { mutableStateOf(0) }
    var activeJob by remember { mutableStateOf<Job?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog for renaming device
    var editingDevice by remember { mutableStateOf<LanDevice?>(null) }
    var customNameInput by remember { mutableStateOf("") }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Device Info", text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    fun startScan() {
        val s = startIpInput.toIntOrNull()?.coerceIn(1, 254) ?: 1
        val e = if (isFullScan) 254 else (endIpInput.toIntOrNull()?.coerceIn(s, 254) ?: 50)

        activeJob?.cancel()
        deviceList = emptyList()
        isScanning = true
        progressScanned = 0
        progressTotal = (e - s + 1)

        activeJob = coroutineScope.launch {
            NetworkToolsManager.scanLanSubnet(
                context = context,
                subnetBase = subnetInput.trim(),
                startIp = s,
                endIp = e,
                onProgress = { scanned, total ->
                    progressScanned = scanned
                    progressTotal = total
                },
                onDeviceFound = { dev ->
                    deviceList = (deviceList.filter { it.ip != dev.ip } + dev).sortedBy {
                        it.ip.substringAfterLast(".").toIntOrNull() ?: 0
                    }
                }
            )
            isScanning = false
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
                    .fillMaxHeight(0.92f)
                    .padding(vertical = 8.dp)
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
                            Icon(Icons.Default.Devices, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LAN Subnet Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${deviceList.size} Found", color = AccentGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { activeJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subnet & IP Range Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = subnetInput,
                            onValueChange = { subnetInput = it },
                            label = { Text("Subnet Base", color = TextSecondary, fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.8f)
                        )

                        OutlinedTextField(
                            value = startIpInput,
                            onValueChange = { startIpInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Start", color = TextSecondary, fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isFullScan,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (isFullScan) "254" else endIpInput,
                            onValueChange = { endIpInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("End", color = TextSecondary, fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isFullScan,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Full Subnet Toggle & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isFullScan,
                                onCheckedChange = { isFullScan = it },
                                colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                            )
                            Text("Full /24 Range (1-254)", color = TextSecondary, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                if (isScanning) {
                                    activeJob?.cancel()
                                    isScanning = false
                                } else {
                                    startScan()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) AccentRed else AccentGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isScanning) "Stop" else "Scan LAN", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Scan Progress Indicator
                    if (isScanning || progressTotal > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val fraction = if (progressTotal > 0) (progressScanned.toFloat() / progressTotal).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = AccentGreen,
                            trackColor = BgCardLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isScanning) "Scanning IPs: $progressScanned / $progressTotal" else "Scan Complete", color = TextMuted, fontSize = 9.5.sp)
                            Text("${(fraction * 100).toInt()}%", color = AccentGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search / Filter Input
                    if (deviceList.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter devices by IP, vendor or name...", color = TextMuted, fontSize = 11.5.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgCardLight.copy(alpha = 0.4f),
                                unfocusedContainerColor = BgCardLight.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Discovered Device Cards
                    val filteredDevices = deviceList.filter { dev ->
                        if (searchQuery.isBlank()) true
                        else dev.ip.contains(searchQuery, true) ||
                                dev.vendor.contains(searchQuery, true) ||
                                dev.customName.contains(searchQuery, true) ||
                                dev.mac.contains(searchQuery, true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (deviceList.isEmpty() && !isScanning) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 35.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Wifi, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Tap 'Scan LAN' to discover all active devices on $subnetInput.0/24", color = TextMuted, fontSize = 11.5.sp)
                                    }
                                }
                            }
                        } else {
                            items(filteredDevices) { device ->
                                LanDeviceCard(
                                    device = device,
                                    onEditName = {
                                        editingDevice = device
                                        customNameInput = device.customName
                                    },
                                    onCopy = { copyToClipboard("${device.ip} (${device.mac}) - ${device.vendor}") },
                                    onPing = { onOpenPing(device.ip) },
                                    onTraceroute = { onOpenTraceroute(device.ip) },
                                    onPortScan = { onOpenPortScan(device.ip) },
                                    onOpenWeb = {
                                        try {
                                            val webPort = if (device.openPorts.contains(8080)) 8080 else 80
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://${device.ip}:$webPort"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onToggleStar = {
                                        val newStarred = NetworkToolsPrefs.toggleStarred(context, "lan_device", device.ip)
                                        deviceList = deviceList.map {
                                            if (it.ip == device.ip) it.copy(isStarred = newStarred) else it
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Edit Device Custom Name
    if (editingDevice != null) {
        AlertDialog(
            onDismissRequest = { editingDevice = null },
            title = { Text("Assign Custom Device Name", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text("Device: ${editingDevice?.ip} (${editingDevice?.vendor})", color = TextSecondary, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customNameInput,
                        onValueChange = { customNameInput = it },
                        label = { Text("Custom Name / Alias") },
                        placeholder = { Text("e.g. Master Bedroom ONU, MikroTik CCR") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = editingDevice ?: return@Button
                        NetworkToolsPrefs.setCustomDeviceName(context, target.ip, customNameInput)
                        deviceList = deviceList.map {
                            if (it.ip == target.ip) it.copy(customName = customNameInput) else it
                        }
                        editingDevice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("Save Name", color = BgDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDevice = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun LanDeviceCard(
    device: LanDevice,
    onEditName: () -> Unit,
    onCopy: () -> Unit,
    onPing: () -> Unit,
    onTraceroute: () -> Unit,
    onPortScan: () -> Unit,
    onOpenWeb: () -> Unit,
    onToggleStar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgCardLight)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Row 1: Icon, IP & Custom Name / Vendor, Star
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (device.vendor.contains("MikroTik", true)) Icons.Default.Router else Icons.Default.Computer,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.ip,
                            color = AccentCyan,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (device.customName.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${device.customName}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${device.vendor} • ${device.mac}",
                        color = TextSecondary,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleStar, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (device.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (device.isStarred) AccentOrange else TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(onClick = onEditName, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Action Buttons (Ping, Traceroute, Port Scan, Open Web, Copy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Button(
                onClick = onPing,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Ping", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onTraceroute,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Trace", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onPortScan,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Ports", color = AccentPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (device.openPorts.contains(80) || device.openPorts.contains(8080) || device.openPorts.contains(443)) {
                Button(
                    onClick = onOpenWeb,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(28.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Web", color = AccentOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(BgCard)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(13.dp))
            }
        }
    }
}
