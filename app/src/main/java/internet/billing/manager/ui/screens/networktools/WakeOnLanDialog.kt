package internet.billing.manager.ui.screens.networktools

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.launch

@Composable
fun WakeOnLanDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var deviceNameInput by remember { mutableStateOf("Server PC") }
    var macInput by remember { mutableStateOf("") }
    var broadcastIpInput by remember { mutableStateOf("255.255.255.255") }
    var portInput by remember { mutableStateOf("9") }
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    var savedTargets by remember { mutableStateOf(listOf<WolTarget>()) }

    fun refreshSaved() {
        savedTargets = NetworkToolsPrefs.getSavedWolTargets(context)
    }

    LaunchedEffect(Unit) {
        refreshSaved()
    }

    fun triggerWol(mac: String, ip: String, port: Int, name: String) {
        coroutineScope.launch {
            isSending = true
            val result = NetworkToolsManager.sendWakeOnLan(mac, ip, port)
            isSending = false
            result.onSuccess { msg ->
                isSuccess = true
                statusMessage = msg
                Toast.makeText(context, "Magic Packet Sent!", Toast.LENGTH_SHORT).show()
                // Save target
                NetworkToolsPrefs.saveWolTarget(
                    context,
                    WolTarget(
                        name = name.ifBlank { "Device" },
                        mac = mac,
                        broadcastIp = ip,
                        port = port,
                        lastWoken = System.currentTimeMillis()
                    )
                )
                refreshSaved()
            }.onFailure { err ->
                isSuccess = false
                statusMessage = "Error: ${err.localizedMessage ?: "Failed to transmit WOL"}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wake on LAN (WOL)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentCyan.copy(alpha = 0.2f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                Text("Local & Remote", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Form: Device Name & MAC Address
                    OutlinedTextField(
                        value = deviceNameInput,
                        onValueChange = { deviceNameInput = it },
                        label = { Text("Device Label", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("e.g. Office PC, MikroTik, NAS Server") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = macInput,
                        onValueChange = { macInput = it.uppercase() },
                        label = { Text("MAC Address (12 Hex)", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("AA:BB:CC:DD:EE:FF") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Broadcast IP & Port (Local or Remote WOL)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = broadcastIpInput,
                            onValueChange = { broadcastIpInput = it },
                            label = { Text("Broadcast / Remote IP", color = TextSecondary, fontSize = 11.sp) },
                            placeholder = { Text("255.255.255.255 or DDNS") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Port", color = TextSecondary, fontSize = 11.sp) },
                            placeholder = { Text("9") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Send Button
                    Button(
                        onClick = {
                            val p = portInput.toIntOrNull() ?: 9
                            triggerWol(macInput, broadcastIpInput, p, deviceNameInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSending && macInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSending) "Broadcasting Packet..." else "Send Wake-on-LAN Magic Packet", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    // Status Message
                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSuccess) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f))
                                .border(1.dp, if (isSuccess) AccentGreen else AccentRed, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(statusMessage ?: "", color = if (isSuccess) AccentGreen else AccentRed, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Saved Devices List
                    Text("Saved / Recent WOL Devices:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (savedTargets.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No saved WOL devices yet. Enter a MAC to send & save.", color = TextMuted, fontSize = 11.5.sp)
                                }
                            }
                        } else {
                            items(savedTargets) { target ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgCardLight)
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(30.dp).clip(CircleShape).background(AccentCyan.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Computer, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(target.name, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                            Text("${target.mac} • ${target.broadcastIp}:${target.port}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                macInput = target.mac
                                                deviceNameInput = target.name
                                                broadcastIpInput = target.broadcastIp
                                                portInput = target.port.toString()
                                                triggerWol(target.mac, target.broadcastIp, target.port, target.name)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp)
                                        ) {
                                            Text("WAKE", color = BgDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = {
                                                NetworkToolsPrefs.deleteWolTarget(context, target.mac)
                                                refreshSaved()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
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
}
