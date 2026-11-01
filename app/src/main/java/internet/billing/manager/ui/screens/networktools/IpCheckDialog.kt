package internet.billing.manager.ui.screens.networktools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.*

@Composable
fun IpCheckDialog(overview: NetworkOverview, onDismiss: () -> Unit) {
    val context = LocalContext.current

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.88f).padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IP Address & Interface", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    IpInfoRow(title = "Connection Status", value = if (overview.isConnected) "ONLINE" else "OFFLINE", color = if (overview.isConnected) AccentGreen else AccentRed)
                    IpInfoRow(title = "Network Interface", value = overview.connectionType, color = TextPrimary)
                    IpInfoRow(title = "Local IPv4 Address", value = overview.localIpv4, color = AccentCyan, onCopy = { copyToClipboard("Local IP", overview.localIpv4) })
                    IpInfoRow(title = "Default Gateway", value = overview.defaultGateway, color = TextPrimary, onCopy = { copyToClipboard("Gateway", overview.defaultGateway) })
                    IpInfoRow(title = "Primary DNS", value = overview.dns1, color = TextSecondary, onCopy = { copyToClipboard("DNS 1", overview.dns1) })
                    IpInfoRow(title = "Secondary DNS", value = overview.dns2, color = TextSecondary, onCopy = { copyToClipboard("DNS 2", overview.dns2) })
                    IpInfoRow(title = "Public ISP IP", value = overview.publicIp, color = AccentOrange, onCopy = { copyToClipboard("Public IP", overview.publicIp) })
                    IpInfoRow(title = "ISP Provider / ASN", value = "${overview.ispName} (${overview.asNumber})", color = TextPrimary)
                    IpInfoRow(title = "Location", value = "${overview.geoCity}, ${overview.geoCountry}", color = TextSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val fullSummary = "Local IP: ${overview.localIpv4}\nGateway: ${overview.defaultGateway}\nDNS: ${overview.dns1}, ${overview.dns2}\nPublic IP: ${overview.publicIp}\nISP: ${overview.ispName} (${overview.asNumber})\nLocation: ${overview.geoCity}, ${overview.geoCountry}"
                            copyToClipboard("Full Diagnostic", fullSummary)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Full Network Diagnostic", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
