package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.OneUIBluePrimary
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    OneUIBluePrimary,            // Ultramarine
    Color(0xFF738AFF),           // Lavender
    OneUISuccess,                // Mint
    Color(0xFFFFB703),           // Warm Gold
    OneUIError,                  // Soft Coral
    Color(0xFFFF006E)            // Vivid Rose
)

enum class ParticleShape {
    RECTANGLE,
    CIRCLE,
    RIBBON
}

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    var vAngle: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val shape: ParticleShape,
    var alpha: Float = 1f,
    val swaySpeed: Float,
    val swayAmplitude: Float
)

/**
 * Pure Jetpack Compose Canvas Confetti Explosion.
 * Optimized for 120 FPS with real physics: gravity, wind resistance, and 2D rotation.
 */
@Composable
fun ConfettiCelebration(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 85,
    onAnimationEnd: () -> Unit = {}
) {
    if (!trigger) return

    val particles = remember(trigger) { mutableStateListOf<ConfettiParticle>() }
    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(trigger) {
        val rand = Random(System.currentTimeMillis())
        particles.clear()

        // Spawn particles burst originating from center-top
        for (i in 0 until particleCount) {
            val angle = rand.nextDouble(Math.PI * 0.15, Math.PI * 0.85).toFloat()
            val speed = rand.nextDouble(12.0, 32.0).toFloat()
            val vx = (cos(angle) * speed * (if (rand.nextBoolean()) 1 else -1)).toFloat()
            val vy = (-sin(angle) * speed * rand.nextDouble(0.7, 1.4)).toFloat()

            val shape = when (rand.nextInt(3)) {
                0 -> ParticleShape.RECTANGLE
                1 -> ParticleShape.CIRCLE
                else -> ParticleShape.RIBBON
            }

            particles.add(
                ConfettiParticle(
                    x = 0f, // will be centered when size is measured
                    y = 0f,
                    vx = vx,
                    vy = vy,
                    angle = rand.nextFloat() * 360f,
                    vAngle = rand.nextDouble(-12.0, 12.0).toFloat(),
                    color = ConfettiColors[rand.nextInt(ConfettiColors.size)],
                    width = if (shape == ParticleShape.RIBBON) rand.nextDouble(16.0, 24.0).toFloat() else rand.nextDouble(10.0, 18.0).toFloat(),
                    height = if (shape == ParticleShape.RIBBON) rand.nextDouble(6.0, 10.0).toFloat() else rand.nextDouble(10.0, 18.0).toFloat(),
                    shape = shape,
                    swaySpeed = rand.nextDouble(0.04, 0.09).toFloat(),
                    swayAmplitude = rand.nextDouble(1.5, 4.0).toFloat()
                )
            )
        }

        var isInitialized = false
        var startTime = 0L

        while (true) {
            withFrameNanos { nanos ->
                if (startTime == 0L) startTime = nanos
                frameTime = nanos
            }

            var allOffscreenOrFaded = true

            for (p in particles) {
                // Physics step
                p.vy += 0.48f // Gravity
                p.vx *= 0.985f // Drag
                p.vy *= 0.985f

                p.x += p.vx + sin(p.y * p.swaySpeed) * p.swayAmplitude
                p.y += p.vy
                p.angle += p.vAngle

                // Fade out as it descends
                if (p.y > 600f) {
                    p.alpha = (p.alpha - 0.012f).coerceAtLeast(0f)
                }

                if (p.alpha > 0.02f) {
                    allOffscreenOrFaded = false
                }
            }

            if (allOffscreenOrFaded && isInitialized) {
                onAnimationEnd()
                break
            }
            isInitialized = true
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.4f

        particles.forEach { p ->
            if (p.alpha > 0f) {
                val drawX = if (p.x == 0f && p.y == 0f) centerX else (centerX + p.x)
                val drawY = if (p.x == 0f && p.y == 0f) centerY else (centerY + p.y)

                rotate(p.angle, pivot = Offset(drawX, drawY)) {
                    val particlePaintColor = p.color.copy(alpha = p.alpha)
                    when (p.shape) {
                        ParticleShape.CIRCLE -> {
                            drawCircle(
                                color = particlePaintColor,
                                radius = p.width / 2f,
                                center = Offset(drawX, drawY)
                            )
                        }
                        ParticleShape.RECTANGLE -> {
                            drawRoundRect(
                                color = particlePaintColor,
                                topLeft = Offset(drawX - p.width / 2f, drawY - p.height / 2f),
                                size = Size(p.width, p.height),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                        }
                        ParticleShape.RIBBON -> {
                            drawRoundRect(
                                color = particlePaintColor,
                                topLeft = Offset(drawX - p.width / 2f, drawY - p.height / 2f),
                                size = Size(p.width, p.height),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
