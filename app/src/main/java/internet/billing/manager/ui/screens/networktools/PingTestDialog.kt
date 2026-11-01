package internet.billing.manager.ui.screens.networktools

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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

private enum class PingViewMode {
    QUERY,
    RUNNING
}

@Composable
fun PingTestDialog(
    defaultGateway: String,
    onDismiss: () -> Unit,
    onSwitchToRoute: (String) -> Unit = {},
    onSwitchToPorts: (String) -> Unit = {},
    onSwitchToWhois: (String) -> Unit = {},
    onSwitchToDns: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var targetInput by remember { mutableStateOf("8.8.8.8") }
    var selectedTool by remember { mutableStateOf("Ping") }
    var preferIpv6 by remember { mutableStateOf(false) }

    var viewMode by remember { mutableStateOf(PingViewMode.QUERY) }
    var probesList by remember { mutableStateOf(listOf<PingResultItem>()) }
    var isRunning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    val accentGold = Color(0xFFE5A93C)
    val bgVideoDark = Color(0xFF13141D)
    val cardInputBg = Color(0xFF1B1C26)
    val textMutedGray = Color(0xFF94A3B8)
    val dividerColor = Color(0xFF222432)

    fun startPinging(host: String) {
        activeJob?.cancel()
        probesList = emptyList()
        errorMessage = null
        isRunning = true

        activeJob = coroutineScope.launch {
            NetworkToolsManager.executeStreamingPing(
                host = host,
                preferIpv6 = preferIpv6,
                onProbe = { probe ->
                    // Prepend newest probe at top (exactly matching video!)
                    probesList = listOf(probe) + probesList
                },
                onError = { err ->
                    errorMessage = err
                    isRunning = false
                }
            )
        }
    }

    fun handleStartClick() {
        when (selectedTool) {
            "Ping" -> {
                viewMode = PingViewMode.RUNNING
                startPinging(targetInput)
            }
            "Route" -> onSwitchToRoute(targetInput)
            "Ports" -> onSwitchToPorts(targetInput)
            "Whois" -> onSwitchToWhois(targetInput)
            "DNS" -> onSwitchToDns(targetInput)
        }
    }

    Dialog(
        onDismissRequest = {
            activeJob?.cancel()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgVideoDark,
            contentColor = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgVideoDark)
            ) {
                // =============================================================
                // 1. QUERY MODE (Matching Tools Query in User Video at 00:00-00:02)
                // =============================================================
                if (viewMode == PingViewMode.QUERY) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Top Bar: Back, "Tools Query", "Start"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tools Query",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Start",
                                color = if (targetInput.isNotBlank()) accentGold else textMutedGray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable(enabled = targetInput.isNotBlank()) {
                                        handleStartClick()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Input Field with Clear 'X' Button
                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { targetInput = it },
                            placeholder = { Text("Domain Name / IP Address", color = textMutedGray, fontSize = 14.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (targetInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { targetInput = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = textMutedGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = cardInputBg,
                                unfocusedContainerColor = cardInputBg,
                                cursorColor = accentGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tool Tabs: [Ping] [Route] [Ports] [Whois] [DNS] (Orange Border Box)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, accentGold, RoundedCornerShape(8.dp))
                        ) {
                            listOf("Ping", "Route", "Ports", "Whois", "DNS").forEachIndexed { idx, tab ->
                                val isSelected = (selectedTool == tab)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSelected) accentGold else Color.Transparent)
                                        .clickable { selectedTool = tab },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (isSelected) Color.Black else accentGold,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (idx < 4) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(accentGold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Prefer IPv6 Switch Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Prefer IPv6",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Switch(
                                checked = preferIpv6,
                                onCheckedChange = { preferIpv6 = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentGold,
                                    uncheckedThumbColor = textMutedGray,
                                    uncheckedTrackColor = cardInputBg
                                )
                            )
                        }
                    }
                }

                // =============================================================
                // 2. RUNNING / RESULT MODE (Matching Video at 00:03-00:14)
                // =============================================================
                else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Top Bar: Back, "Ping", "Stop" / "Start"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        activeJob?.cancel()
                                        isRunning = false
                                        viewMode = PingViewMode.QUERY
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ping",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (isRunning) "Stop" else "Start",
                                color = if (isRunning) Color(0xFFEF4444) else accentGold,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        if (isRunning) {
                                            activeJob?.cancel()
                                            isRunning = false
                                        } else {
                                            startPinging(targetInput)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subtitle: Target & Probe Count (e.g. "8.8.8.8 (10)")
                        if (errorMessage == null) {
                            Text(
                                text = "${targetInput.trim()} (${probesList.size})",
                                color = textMutedGray,
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Error state: "Failed to resolve IP address" + "What is this?"
                        if (errorMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage ?: "Failed to resolve IP address",
                                    color = Color.White,
                                    fontSize = 13.5.sp
                                )

                                Text(
                                    text = "What is this?",
                                    color = accentGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Probe Result List (Newest at top)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(probesList) { item ->
                                PingResultRow(item = item, dividerColor = dividerColor, textMutedGray = textMutedGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Result Row Component: Green Dot, Seq, IP, Hostname, Latency ms, Flag
@Composable
private fun PingResultRow(
    item: PingResultItem,
    dividerColor: Color,
    textMutedGray: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Green Indicator Dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (item.isSuccess) Color(0xFF22C55E) else Color(0xFFEF4444))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sequence Number (e.g. 1, 2, 3...)
                Text(
                    text = "${item.seq}",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(22.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // IP & Hostname
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.ip,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.hostName.isNotBlank() && item.hostName != item.ip) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.hostName,
                            color = textMutedGray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Latency & Country Flag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${"%.1f".format(item.latencyMs)} ms",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.countryFlag,
                    fontSize = 16.sp
                )
            }
        }

        // Row Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(dividerColor)
        )
    }
}
