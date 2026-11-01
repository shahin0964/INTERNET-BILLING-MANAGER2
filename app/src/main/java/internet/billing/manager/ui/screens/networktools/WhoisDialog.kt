package internet.billing.manager.ui.screens.networktools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun WhoisDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var targetInput by remember { mutableStateOf("google.com") }
    var customServerInput by remember { mutableStateOf("") }
    var whoisResult by remember { mutableStateOf<WhoisResult?>(null) }
    var isQuerying by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $label", Toast.LENGTH_SHORT).show()
    }

    fun runQuery(target: String) {
        coroutineScope.launch {
            isQuerying = true
            whoisResult = NetworkToolsManager.queryWhois(target, customServerInput)
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
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
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
                            Icon(Icons.Default.Language, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WHOIS Domain & IP Lookup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Input
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = { Text("Domain Name or IP Address", color = TextSecondary) },
                        placeholder = { Text("e.g. google.com or 8.8.8.8") },
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Custom WHOIS Server (Optional)
                    OutlinedTextField(
                        value = customServerInput,
                        onValueChange = { customServerInput = it },
                        label = { Text("Custom WHOIS Server (Optional)", color = TextSecondary, fontSize = 10.sp) },
                        placeholder = { Text("Leave blank for auto-detected server") },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { runQuery(targetInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isQuerying && targetInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(if (isQuerying) "Querying WHOIS Database..." else "Lookup WHOIS Records", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result View
                    whoisResult?.let { res ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (res.isSuccess) {
                                IpInfoRow(title = "Queried Target", value = res.query, color = AccentCyan)
                                IpInfoRow(title = "WHOIS Server", value = res.whoisServer, color = TextPrimary)
                                IpInfoRow(title = "Registrar", value = res.registrar, color = AccentOrange)
                                IpInfoRow(title = "Organization", value = res.organization, color = TextPrimary)
                                IpInfoRow(title = "Creation Date", value = res.creationDate, color = AccentGreen)
                                IpInfoRow(title = "Expiry Date", value = res.expirationDate, color = AccentCyan)

                                if (res.nameServers.isNotEmpty()) {
                                    RecordCard(title = "Name Servers (DNS)", list = res.nameServers)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Raw WHOIS Console
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Raw WHOIS Record:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { copyToClipboard("Raw WHOIS", res.rawOutput) }, modifier = Modifier.size(26.dp)) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan, modifier = Modifier.size(15.dp))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgDark)
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = res.rawOutput.take(1500),
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentRed.copy(alpha = 0.15f))
                                        .padding(12.dp)
                                ) {
                                    Text(res.errorMessage ?: "WHOIS query failed", color = AccentRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
