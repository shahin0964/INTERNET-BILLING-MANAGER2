package internet.billing.manager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import internet.billing.manager.R
import internet.billing.manager.ui.theme.AccentCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    // Dynamic app version name
    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.43"
        } catch (e: Exception) {
            "1.0.43"
        }
    }

    // Smooth cinematic entrance (fade and scale from 0.96f to exactly 1.0f)
    val entranceAnim = remember { Animatable(0f) }

    // Glowing aura pulse for central globe
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlowInfinite")
    val globeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlobeGlowPulse"
    )

    // Animated chaser light pulse across the 3 artwork dots
    val dotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ArtworkDotPhase"
    )

    fun dismissSplash() {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                entranceAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
                onAnimationFinished()
            }
        }
    }

    LaunchedEffect(Unit) {
        // Smooth cinematic entrance
        entranceAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = EaseOutCubic)
        )
        // Splash duration
        delay(2400)
        dismissSplash()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF021B3C))
            .alpha(entranceAnim.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dismissSplash()
            }
    ) {
        val entranceScale = 0.96f + (0.04f * entranceAnim.value)

        // 1. High-resolution full-bleed 20:9 artwork (1080x2400 native)
        // Completely fills the screen edge-to-edge without letterbox bars or cutoffs
        Image(
            painter = painterResource(id = R.drawable.app_splash),
            contentDescription = "Internet Billing Manager Splash",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(entranceScale)
        )

        // 2. Central Cyan Ambient Light Pulse behind the central globe
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.22f)
                .size(maxWidth * 0.72f)
                .alpha(globeGlowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.5f),
                            Color(0xFF0099FF).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 3. Cyber Animated Glow directly on the 3 Dots in the artwork:
        // Positioned using exact proportional coordinates:
        // Left Dot: x = 43.89%, y = 69.11%
        // Middle Dot: x = 49.95%, y = 69.11%
        // Right Dot: x = 56.00%, y = 69.11%
        val yOffset = maxHeight * 0.6911f
        val xOffsetDot1 = maxWidth * 0.4389f
        val xOffsetDot2 = maxWidth * 0.4995f
        val xOffsetDot3 = maxWidth * 0.5600f

        ArtworkDotGlow(
            xOffset = xOffsetDot1,
            yOffset = yOffset,
            intensity = calculateDotIntensity(dotPhase, 0)
        )

        ArtworkDotGlow(
            xOffset = xOffsetDot2,
            yOffset = yOffset,
            intensity = calculateDotIntensity(dotPhase, 1)
        )

        ArtworkDotGlow(
            xOffset = xOffsetDot3,
            yOffset = yOffset,
            intensity = calculateDotIntensity(dotPhase, 2)
        )

        // 4. Bottom Version & App Branding Tag
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚡ Internet Billing Manager",
                color = AccentCyan.copy(alpha = 0.92f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "v$appVersion",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Calculates a smooth, continuous wave intensity (0f to 1f) for a dot index.
 */
private fun calculateDotIntensity(phase: Float, dotIndex: Int): Float {
    val diff = (phase - dotIndex + 3f) % 3f
    return if (diff in 0f..1f) {
        sin(diff * Math.PI.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
}

/**
 * High-energy cyber neon lens flare & halo drawn precisely on top of a dot in the artwork.
 */
@Composable
private fun BoxScope.ArtworkDotGlow(
    xOffset: Dp,
    yOffset: Dp,
    intensity: Float
) {
    if (intensity > 0.04f) {
        // 1. Soft cyber halo aura (expanding and breathing with intensity)
        Box(
            modifier = Modifier
                .offset(
                    x = xOffset - 15.dp,
                    y = yOffset - 15.dp
                )
                .size(30.dp)
                .alpha(intensity * 0.95f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            AccentCyan.copy(alpha = 0.8f),
                            Color(0xFF0099FF).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 2. High-intensity brilliant white core flare
        val coreSize = 9.dp * (1f + 0.35f * intensity)
        Box(
            modifier = Modifier
                .offset(
                    x = xOffset - (coreSize / 2),
                    y = yOffset - (coreSize / 2)
                )
                .size(coreSize)
                .alpha(intensity)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
        )
    }
}
