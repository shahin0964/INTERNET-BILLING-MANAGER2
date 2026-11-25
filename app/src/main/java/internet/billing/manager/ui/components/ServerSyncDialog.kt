package internet.billing.manager.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.window.Dialog
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.ServerSyncManager
import kotlinx.coroutines.launch

@Composable
fun ServerSyncDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var serverUrlText by remember { mutableStateOf(ServerSyncManager.serverUrl) }
    var isEnabled by remember { mutableStateOf(ServerSyncManager.isSyncEnabled) }
    var testStatusText by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("ক্লাউড সার্ভার সিঙ্ক", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("ওয়েব সফটওয়্যার ও মোবাইল কানেকশন", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgCardLight)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("সার্ভার সিঙ্ক সক্রিয় রাখুন", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(if (isEnabled) "লাইভ ক্লাউডের সাথে সংযুক্ত" else "অফলাইন লোকাল মোড", fontSize = 11.sp, color = if (isEnabled) AccentGreen else TextMuted)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                ServerSyncManager.updateConfig(context, serverUrlText, isEnabled)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = AccentCyan.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // URL Input
                    Text("সেন্ট্রাল API সার্ভার URL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = serverUrlText,
                        onValueChange = {
                            serverUrlText = it
                            ServerSyncManager.updateConfig(context, serverUrlText, isEnabled)
                        },
                        placeholder = { Text("https://your-domain.com/api", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = AccentCyan) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Connection Button
                    Button(
                        onClick = {
                            isTesting = true
                            testStatusText = "কানেকশন টেস্ট করা হচ্ছে..."
                            coroutineScope.launch {
                                val (ok, msg) = ServerSyncManager.testConnection(serverUrlText)
                                isTesting = false
                                testStatusText = msg
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BgCardLight),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                    ) {
                        Text(if (isTesting) "টেস্ট হচ্ছে..." else "📡 টেস্ট কানেকশন (Ping Server)", color = AccentCyan, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (testStatusText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(testStatusText, fontSize = 11.5.sp, color = if (testStatusText.contains("সফল")) AccentGreen else AccentOrange)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sync Now Button
                    Button(
                        onClick = {
                            ServerSyncManager.updateConfig(context, serverUrlText, true)
                            isEnabled = true
                            coroutineScope.launch {
                                val (ok, msg) = ServerSyncManager.performSync(context)
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled = !ServerSyncManager.isSyncing
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = BgDark, modifier = Modifier.size(18.dp))
                            Text(if (ServerSyncManager.isSyncing) "সিঙ্ক হচ্ছে..." else "⚡ এখনই সিঙ্ক করুন (Sync Now)", color = BgDark, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (ServerSyncManager.lastSyncStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(ServerSyncManager.lastSyncStatus, fontSize = 11.5.sp, color = AccentCyan)
                    }
                }
            }
        }
    }
}
