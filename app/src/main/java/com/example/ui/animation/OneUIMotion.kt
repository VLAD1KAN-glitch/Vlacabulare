package com.example.ui.animation

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Samsung One UI 9 Bouncy Press Effect.
 * Scales down to 0.96f on press and snaps back with a tactile spring.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    shape: Shape? = null,
    targetScaleOnPress: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) targetScaleOnPress else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "one_ui_bouncy_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (shape != null) Modifier.clip(shape) else Modifier
        )
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Shake animation modifier triggered when [shakeTrigger] increments.
 * Performs a natural horizontal oscillation with decaying amplitude.
 */
fun Modifier.shakeEffect(
    shakeTrigger: Int,
    amplitude: Dp = 12.dp
): Modifier = composed {
    val animatable = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
            )
            animatable.snapTo(0f)
        }
    }

    val amplitudePx = with(density) { amplitude.toPx() }
    val progress = animatable.value
    // Decaying sine wave: sin(progress * 4 * PI) * (1 - progress)
    val currentOffset = if (progress > 0f) {
        (sin(progress * Math.PI * 5) * amplitudePx * (1f - progress)).toFloat()
    } else 0f

    this.offset { IntOffset(x = currentOffset.roundToInt(), y = 0) }
}

/**
 * Dynamic Glassmorphic Blur for Android 12+ (RenderEffect) with graceful fallback.
 */
fun Modifier.oneUIBlur(
    blurRadius: Dp = 16.dp
): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val density = LocalDensity.current
        val radiusPx = with(density) { blurRadius.toPx() }
        this.graphicsLayer {
            if (radiusPx > 0f) {
                renderEffect = RenderEffect
                    .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
        }
    } else {
        this
    }
}
