package internet.billing.manager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.R
import internet.billing.manager.ui.theme.AccentCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    // Overall entrance and exit alpha
    val contentAlpha = remember { Animatable(0f) }

    // Breathing scale for the background artwork
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgBreathingScale"
    )

    // Center glow pulsing aura
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    // 3 Dots staggered glowing animation
    val dotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotsPhase"
    )

    fun dismissSplash() {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                contentAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
                onAnimationFinished()
            }
        }
    }

    LaunchedEffect(Unit) {
        // Fast, smooth entrance
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = EaseOutCubic)
        )
        // Splash duration
        delay(2000)
        dismissSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030A1A))
            .alpha(contentAlpha.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dismissSplash()
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. High-resolution poster background with cinematic breathing zoom
        Image(
            painter = painterResource(id = R.drawable.app_splash),
            contentDescription = "Internet Billing Manager Splash",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(bgScale)
        )

        // 2. Central Cyan Ambient Light Pulse Aura
        Box(
            modifier = Modifier
                .size(320.dp)
                .alpha(glowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.45f),
                            Color(0xFF0099FF).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Bottom Animated Loader & Version Tag
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Cyber Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndex = dotPhase.toInt() % 3
                CyberDot(isActive = activeIndex == 0)
                CyberDot(isActive = activeIndex == 1)
                CyberDot(isActive = activeIndex == 2)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "⚡ Internet Billing Manager",
                color = AccentCyan.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Smart ISP Billing & Management • v1.0.38",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 9.5.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun CyberDot(isActive: Boolean) {
    val dotScale by animateFloatAsState(
        targetValue = if (isActive) 1.35f else 0.85f,
        animationSpec = tween(200),
        label = "DotScale"
    )
    val dotAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.4f,
        animationSpec = tween(200),
        label = "DotAlpha"
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .scale(dotScale)
            .alpha(dotAlpha)
            .clip(CircleShape)
            .background(if (isActive) Color.White else AccentCyan)
    )
}

