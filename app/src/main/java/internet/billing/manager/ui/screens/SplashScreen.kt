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

@Composable
fun AnimatedSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    // Smooth cinematic entrance (fade and scale from 0.94f to exactly 1.0f)
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF01589B),
                        Color(0xFF003770),
                        Color(0xFF001F47),
                        Color(0xFF001F47),
                        Color(0xFF003770),
                        Color(0xFF02569E)
                    )
                )
            )
            .alpha(entranceAnim.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dismissSplash()
            },
        contentAlignment = Alignment.Center
    ) {
        // Image aspect ratio: 941px width by 1672px height (~0.5628)
        val imageAspectRatio = 941f / 1672f

        // Fit image perfectly so it NEVER extends or clips outside screen bounds
        val posterWidth: Dp
        val posterHeight: Dp

        if (maxWidth / maxHeight > imageAspectRatio) {
            // Screen is wider than poster (e.g. tablet or landscape)
            posterHeight = maxHeight
            posterWidth = maxHeight * imageAspectRatio
        } else {
            // Screen is taller than poster (standard phone 20:9 or 19.5:9)
            posterWidth = maxWidth
            posterHeight = maxWidth / imageAspectRatio
        }

        val entranceScale = 0.94f + (0.06f * entranceAnim.value)

        // Poster Box container matching exact dimensions of the artwork
        Box(
            modifier = Modifier
                .size(posterWidth, posterHeight)
                .scale(entranceScale)
        ) {
            // 1. High-resolution poster image (fitted 100% inside container, no text clipped)
            Image(
                painter = painterResource(id = R.drawable.app_splash),
                contentDescription = "Internet Billing Manager Splash",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Central Cyan Ambient Light Pulse behind the central globe
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = posterHeight * 0.21f)
                    .size(posterWidth * 0.68f)
                    .alpha(globeGlowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentCyan.copy(alpha = 0.5f),
                                Color(0xFF0099FF).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 3. Cyber Animated Glow directly on the 3 Dots in the artwork:
            // Measured exact pixel coordinates in 941x1672 image:
            // Left Dot: x = 413, y = 1236 (x = 43.89%, y = 73.92%)
            // Middle Dot: x = 470, y = 1236 (x = 49.95%, y = 73.92%)
            // Right Dot: x = 527, y = 1236 (x = 56.00%, y = 73.92%)
            val yOffset = posterHeight * (1236f / 1672f)
            val xOffsetDot1 = posterWidth * (413f / 941f)
            val xOffsetDot2 = posterWidth * (470f / 941f)
            val xOffsetDot3 = posterWidth * (527f / 941f)

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
