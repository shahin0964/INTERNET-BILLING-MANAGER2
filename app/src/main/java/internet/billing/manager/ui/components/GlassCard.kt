package internet.billing.manager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import internet.billing.manager.ui.theme.BgCard
import internet.billing.manager.ui.theme.BgCardLight
import internet.billing.manager.ui.theme.CardBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = BgCard,
    borderColor: Color = CardBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    gradient: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        val bgModifier = if (gradient != null) {
            Modifier.background(gradient)
        } else {
            Modifier.background(backgroundColor)
        }

        Box(
            modifier = Modifier
                .clip(shape)
                .then(bgModifier),
            content = content
        )
    }
}
