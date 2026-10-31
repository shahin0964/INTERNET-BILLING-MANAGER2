package internet.billing.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.theme.*

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector? = null,
    accentColor: Color = AccentCyan,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = CardBorderSubtle,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun GlowStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        borderColor = accentColor.copy(alpha = 0.4f),
        backgroundColor = BgCardLight.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
