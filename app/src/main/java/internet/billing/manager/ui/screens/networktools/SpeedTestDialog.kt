package internet.billing.manager.ui.screens.networktools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun SpeedTestDialog(onDismiss: () -> Unit) {
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

    Dialog(
        onDismissRequest = { activeJob?.cancel(); onDismiss() },
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
                    .fillMaxHeight(0.88f)
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Internet Speed Benchmark", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF4C1D95)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("Dual Stream", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { activeJob?.cancel(); onDismiss() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speedometer Gauge Circle
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
                                text = String.format(java.util.Locale.US, "%.1f", if (state.isTesting) state.currentSpeedMbps else state.downloadSpeedMbps),
                                color = TextPrimary,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Mbps", color = Color(0xFFF472B6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.phase, color = TextMuted, fontSize = 9.5.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = AccentCyan,
                        trackColor = BgCardLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Speed Readout: Download & Upload Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0C4A6E).copy(alpha = 0.35f))
                                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DOWNLOAD", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${"%.1f".format(state.downloadSpeedMbps)}",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("Mbps", color = TextMuted, fontSize = 9.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF4A044E).copy(alpha = 0.35f))
                                .border(1.dp, Color(0xFFC026D3).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("UPLOAD", color = Color(0xFFE879F9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${"%.1f".format(state.uploadSpeedMbps)}",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("Mbps", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ping, Jitter, Data transferred
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox(modifier = Modifier.weight(1f), label = "PING", value = "${state.latencyMs} ms", valueColor = AccentCyan)
                        StatBox(modifier = Modifier.weight(1f), label = "JITTER", value = "${state.jitterMs} ms", valueColor = AccentOrange)
                        StatBox(modifier = Modifier.weight(1f), label = "TRANSFERRED", value = "${(state.bytesDownloaded + state.bytesUploaded) / 1_000_000} MB", valueColor = AccentGreen)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(if (state.isTesting) "Stop Test" else "Start Live Speed Benchmark (DL & UL)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
