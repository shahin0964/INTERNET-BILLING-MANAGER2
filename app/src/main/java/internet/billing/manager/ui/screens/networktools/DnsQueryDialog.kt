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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
fun DnsQueryDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var domainInput by remember { mutableStateOf("google.com") }
    var selectedType by remember { mutableStateOf("A") }
    var dnssecEnabled by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<DetailedDnsRecord?>(null) }
    var isQuerying by remember { mutableStateOf(false) }
    var isStarred by remember { mutableStateOf(false) }

    fun checkStarred(domain: String) {
        isStarred = NetworkToolsPrefs.isStarred(context, "dns", domain)
    }

    LaunchedEffect(domainInput) {
        checkStarred(domainInput)
    }

    fun runLookup(domain: String, type: String) {
        NetworkToolsPrefs.addHistory(context, "dns", domain)
        coroutineScope.launch {
            isQuerying = true
            result = NetworkToolsManager.resolveDetailedDns(domain, type, dnssecEnabled)
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
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DNS Query & DNSSEC Analyzer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Domain Input + Star button
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = domainInput,
                            onValueChange = {
                                domainInput = it
                                checkStarred(it)
                            },
                            label = { Text("Domain Name", color = TextSecondary) },
                            placeholder = { Text("e.g. google.com or cloudflare.com") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgCardLight.copy(alpha = 0.5f),
                                unfocusedContainerColor = BgCardLight.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                isStarred = NetworkToolsPrefs.toggleStarred(context, "dns", domainInput)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
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

                    // Record Type Selector (A, AAAA, MX, TXT, NS, CNAME)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("A", "AAAA", "MX", "TXT", "NS", "CNAME").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedType == type) AccentCyan.copy(alpha = 0.25f) else BgCardLight)
                                    .border(1.dp, if (selectedType == type) AccentCyan else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedType = type }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type, color = if (selectedType == type) AccentCyan else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // DNSSEC Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = dnssecEnabled,
                                onCheckedChange = { dnssecEnabled = it },
                                colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
                            )
                            Icon(Icons.Default.Security, contentDescription = null, tint = if (dnssecEnabled) AccentCyan else TextMuted, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request DNSSEC Validation", color = TextSecondary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { runLookup(domainInput, selectedType) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isQuerying && domainInput.isNotBlank(),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Text(if (isQuerying) "Querying..." else "Query $selectedType", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Result View (Answer, Authority, Additional sections)
                    result?.let { res ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Status bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (res.isSuccess) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Status: ${res.statusText}", color = if (res.isSuccess) AccentGreen else AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("DNSSEC Validated: ${if (res.isDnssecValidated) "SECURE (AUTHENTIC DATA)" else "INSECURE / UNSIGNED"}", color = if (res.isDnssecValidated) AccentCyan else TextMuted, fontSize = 10.5.sp)
                                }
                                Text("${res.responseTimeMs} ms", color = AccentCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            val answers = res.records.filter { it.section == "Answer" }
                            val authorities = res.records.filter { it.section == "Authority" }
                            val additionals = res.records.filter { it.section == "Additional" }

                            if (answers.isNotEmpty()) {
                                SectionRecordBox(sectionTitle = "ANSWER SECTION (${answers.size} records)", records = answers, headerColor = AccentGreen)
                            }

                            if (authorities.isNotEmpty()) {
                                SectionRecordBox(sectionTitle = "AUTHORITY SECTION (${authorities.size} records)", records = authorities, headerColor = AccentCyan)
                            }

                            if (additionals.isNotEmpty()) {
                                SectionRecordBox(sectionTitle = "ADDITIONAL SECTION (${additionals.size} records)", records = additionals, headerColor = AccentOrange)
                            }

                            if (!res.isSuccess && res.errorMessage != null) {
                                Text(res.errorMessage, color = AccentRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRecordBox(sectionTitle: String, records: List<DnsSectionRecord>, headerColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgCardLight)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(sectionTitle, color = headerColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        records.forEach { rec ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rec.data, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text("Name: ${rec.name}", color = TextMuted, fontSize = 9.5.sp)
                }
                Text("TTL ${rec.ttl}s • ${rec.type}", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
