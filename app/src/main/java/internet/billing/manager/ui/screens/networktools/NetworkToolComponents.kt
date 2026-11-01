package internet.billing.manager.ui.screens.networktools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.components.GlassCard
import internet.billing.manager.ui.theme.*
import internet.billing.manager.util.PingProbe
import kotlin.math.max

@Composable
fun DiagnosticToolCard(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = AccentCyan,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        borderColor = CardBorder,
        backgroundColor = BgCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (!badgeText.isNullOrBlank()) Modifier.weight(1f, fill = false) else Modifier
                    )

                    if (!badgeText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgCardLight)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IpInfoRow(
    title: String,
    value: String,
    color: Color,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = color, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onCopy() }
                )
            }
        }
    }
}

@Composable
fun RecordCard(title: String, list: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgCardLight)
            .padding(10.dp)
    ) {
        Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        list.forEach { item ->
            Text(item, color = AccentCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// Graphical Visualization of Ping Probe Times
@Composable
fun RttBarGraph(
    probes: List<PingProbe>,
    modifier: Modifier = Modifier
) {
    if (probes.isEmpty()) return

    val maxRtt = max(50f, probes.map { it.rttMs }.maxOrNull() ?: 50f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PROBE LATENCY HISTORY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("MAX: ${maxRtt.toInt()}ms", color = AccentOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            probes.takeLast(25).forEach { probe ->
                val barFraction = if (probe.isSuccess) (probe.rttMs / maxRtt).coerceIn(0.1f, 1f) else 1f
                val barColor = when {
                    !probe.isSuccess -> AccentRed
                    probe.rttMs < 40f -> AccentGreen
                    probe.rttMs < 90f -> AccentCyan
                    probe.rttMs < 160f -> AccentOrange
                    else -> AccentRed
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(barFraction)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(barColor)
                )
            }
        }
    }
}

// Wi-Fi Channel Occupancy Bar Graph
@Composable
fun ChannelOccupancyGraph(
    currentChannel: Int,
    occupancyMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgCardLight)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("2.4 GHz Channel Occupancy", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text("Clean: 1, 6, 11", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (ch in 1..14) {
                val occ = occupancyMap[ch] ?: 25
                val heightFraction = (occ / 100f).coerceIn(0.15f, 1f)
                val isCurrent = (ch == currentChannel)
                val isOptimal = ch in listOf(1, 6, 11)

                val barColor = when {
                    isCurrent -> AccentCyan
                    isOptimal -> AccentGreen.copy(alpha = 0.8f)
                    occ > 60 -> AccentRed.copy(alpha = 0.7f)
                    else -> AccentOrange.copy(alpha = 0.7f)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(heightFraction)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "$ch",
                        color = if (isCurrent) AccentCyan else TextMuted,
                        fontSize = 8.5.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
