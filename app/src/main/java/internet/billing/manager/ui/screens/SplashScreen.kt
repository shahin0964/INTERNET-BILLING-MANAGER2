package internet.billing.manager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.R
import internet.billing.manager.ui.theme.AccentCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

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
            pInfo.versionName ?: "1.0.48"
        } catch (e: Exception) {
            "1.0.48"
        }
    }

    // Smooth cinematic entrance (fade and scale from 0.96f to exactly 1.0f)
    val entranceAnim = remember { Animatable(0f) }

    // Loading progress animation from 0f to 1f
    val loadingProgress = remember { Animatable(0f) }
    var loadingStatusText by remember { mutableStateOf("Initializing...") }

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
        // 1. Cinematic entrance fade in
        launch {
            entranceAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = EaseOutCubic)
            )
        }

        // 2. Realistic multi-stage loading animation
        launch {
            delay(150)
            loadingStatusText = "Connecting..."
            loadingProgress.animateTo(
                targetValue = 0.35f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )

            loadingStatusText = "Loading Database..."
            loadingProgress.animateTo(
                targetValue = 0.72f,
                animationSpec = tween(durationMillis = 650, easing = LinearEasing)
            )

            loadingStatusText = "Synchronizing..."
            loadingProgress.animateTo(
                targetValue = 0.94f,
                animationSpec = tween(durationMillis = 500, easing = EaseOutQuad)
            )

            loadingStatusText = "Ready!"
            loadingProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
            )

            delay(250)
            dismissSplash()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF021636))
            .alpha(entranceAnim.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dismissSplash()
            }
    ) {
        val entranceScale = 0.96f + (0.04f * entranceAnim.value)

        // 1. Full-bleed artwork (941x1672 native, fills phone screen edge-to-edge)
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
                            AccentCyan.copy(alpha = 0.45f),
                            Color(0xFF0099FF).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 3. Cyber Animated Loading System
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progressPercent = (loadingProgress.value * 100).toInt().coerceIn(0, 100)

            // Status message + Progress percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = loadingStatusText,
                    color = AccentCyan,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$progressPercent%",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing Capsule Progress Bar
            val barWidth = 210.dp
            val barHeight = 6.dp
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(Color(0xFF041B3B))
                    .border(
                        BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                        CircleShape
                    )
            ) {
                // Active Fill Bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loadingProgress.value)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0091EA),
                                    Color(0xFF00E5FF),
                                    Color.White
                                )
                            )
                        )
                )

                // Shimmer sweep flare running through the bar
                val shimmerX by infiniteTransition.animateFloat(
                    initialValue = -0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "LoadingBarShimmer"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(45.dp)
                        .offset(x = barWidth * shimmerX)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.85f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            // 3 Animated Cyber Dancing Light Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotAnim by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "DancingDots"
                )

                for (i in 0..2) {
                    val intensity = calculateDotIntensity(dotAnim, i)
                    val dotScale = 0.75f + (0.45f * intensity)
                    val dotAlpha = 0.35f + (0.65f * intensity)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(dotScale)
                            .alpha(dotAlpha)
                            .background(
                                if (i == 1) Color.White else AccentCyan,
                                CircleShape
                            )
                    )
                }
            }
        }

        // 4. Bottom Version & App Branding Tag
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚡ Internet Billing Manager",
                color = AccentCyan.copy(alpha = 0.92f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "v$appVersion",
                color = Color.White.copy(alpha = 0.75f),
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
