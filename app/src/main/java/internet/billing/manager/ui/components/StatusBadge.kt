package internet.billing.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.theme.*

@Composable
fun CustomerStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        "Active" -> Triple(StatusActiveBg, StatusActiveText, AccentGreen)
        "Suspended" -> Triple(StatusSuspendedBg, StatusSuspendedText, AccentOrange)
        else -> Triple(StatusInactiveBg, StatusInactiveText, AccentRed)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentStatusBadge(
    isPaid: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPaid) Color(0x2600E676) else Color(0x26FF5252)
    val textColor = if (isPaid) AccentGreen else AccentRed
    val text = if (isPaid) "PAID" else "DUE"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
