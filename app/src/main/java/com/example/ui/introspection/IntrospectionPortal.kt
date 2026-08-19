package com.example.ui.introspection

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object IntrospectionColors {
    val BaseBackground = Color(0xFF09020E)
    val TopViolet = Color(0xFF13031B)
    val SurfaceDark = Color(0xAA201029)
    val SurfaceHighlighted = Color(0xFF2E1138)
    val PrimaryPink = Color(0xFFFF387A)
    val Magenta = Color(0xFFD92ED1)
    val PortalViolet = Color(0xFF8F33ED)
    val DeepViolet = Color(0xFF4721C7)
    val PeachLight = Color(0xFFFF787A)
    val CoralAccent = Color(0xFFFF6E6A)
    val PrimaryText = Color(0xFFFAF6FF)
    val SecondaryText = Color(0xFFC9B3D4)
}

private data class PortalParticle(
    val initialAngle: Float,
    val distanceFraction: Float,
    val size: Float,
    val speedFactor: Float,
    val phaseOffset: Float,
    val durationMs: Int
)

@Composable
fun IntrospectionPortal(
    modifier: Modifier = Modifier,
    isRevelation: Boolean = false,
    reducedMotion: Boolean = false,
    size: Dp = 230.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portalMotion")

    // 1. Portal Breathing: Scale 0.965 -> 1.035 -> 0.965 in 3200ms (Reduced: 0.99 -> 1.01)
    val minScale = if (reducedMotion) 0.99f else 0.965f
    val maxScale = if (reducedMotion) 1.01f else 1.035f
    val portalScale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalScale"
    )

    // 2. Center Star Breathing: Scale 0.90 -> 1.10 -> 0.90 in 2250ms
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    // 3. Energy Arcs Rotation: Outer 0° -> 360° in 11000ms, Inner 360° -> 0° in 15000ms
    val outerRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingRotation"
    )

    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = if (reducedMotion) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRingRotation"
    )

    // 4. Three Organic Nebula / Fog Layers (Alpha 0.36 -> 0.84, Durations 2350 - 2700ms)
    val nebula1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.36f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nebula1Alpha"
    )

    val nebula2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(2650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nebula2Alpha"
    )

    val nebula3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nebula3Alpha"
    )

    // 5. Floor Reflection: ScaleX 0.92 -> 1.09, Alpha 0.22 -> 0.52 in 2800ms
    val floorScaleX by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floorScaleX"
    )

    val floorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floorAlpha"
    )

    // 6. Particles time progression (18% faster during revelation)
    val baseParticleDuration = if (isRevelation) 4650 else 5500
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseParticleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleTime"
    )

    // 16 stably remembered particles with durations between 4500 and 7200ms
    val particles = remember {
        val random = Random(1337)
        List(16) {
            PortalParticle(
                initialAngle = random.nextFloat() * 360f,
                distanceFraction = 0.72f + random.nextFloat() * 0.48f,
                size = 2.2f + random.nextFloat() * 3.6f,
                speedFactor = 0.75f + random.nextFloat() * 0.5f,
                phaseOffset = random.nextFloat(),
                durationMs = 4500 + random.nextInt(2700)
            )
        }
    }

    // Glow intensity is boosted by 25% during revelation
    val glowMultiplier = if (isRevelation) 1.25f else 1.0f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f - 6.dp.toPx())
            val baseRadius = this.size.minDimension * 0.33f

            // Layer 0: Elliptical Floor Reflection (Phase-delayed breath)
            val floorCenter = Offset(this.size.width / 2f, this.size.height - 14.dp.toPx())
            val floorRadiusX = baseRadius * 1.38f * floorScaleX
            val floorRadiusY = baseRadius * 0.26f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        IntrospectionColors.Magenta.copy(alpha = (floorAlpha * 0.85f * glowMultiplier).coerceIn(0f, 1f)),
                        IntrospectionColors.PortalViolet.copy(alpha = (floorAlpha * 0.45f * glowMultiplier).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = floorCenter,
                    radius = floorRadiusX
                ),
                topLeft = Offset(floorCenter.x - floorRadiusX, floorCenter.y - floorRadiusY),
                size = Size(floorRadiusX * 2f, floorRadiusY * 2f)
            )

            // Scaled Portal Elements
            scale(scale = portalScale, pivot = center) {

                // Layer 1: Wide Violet-Magenta Atmosphere Nebulae
                val atmosphereRadius = baseRadius * 1.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IntrospectionColors.Magenta.copy(alpha = (0.26f * nebula1Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.PortalViolet.copy(alpha = (0.18f * nebula2Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.DeepViolet.copy(alpha = (0.08f * nebula3Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = center,
                        radius = atmosphereRadius
                    ),
                    radius = atmosphereRadius,
                    center = center
                )

                // Layer 2: Warm Coral Light Accent (Top-Left quadrant glow)
                val coralCenter = Offset(center.x - baseRadius * 0.42f, center.y - baseRadius * 0.42f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IntrospectionColors.CoralAccent.copy(alpha = (0.35f * nebula1Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.PeachLight.copy(alpha = (0.18f * nebula2Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = coralCenter,
                        radius = baseRadius * 0.95f
                    ),
                    radius = baseRadius * 0.95f,
                    center = coralCenter
                )

                // Layer 3: Secondary Magenta Fog Layer (Bottom-Right glow)
                val magentaCenter = Offset(center.x + baseRadius * 0.35f, center.y + baseRadius * 0.35f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IntrospectionColors.Magenta.copy(alpha = (0.30f * nebula3Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.PortalViolet.copy(alpha = (0.14f * nebula3Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = magentaCenter,
                        radius = baseRadius * 0.9f
                    ),
                    radius = baseRadius * 0.9f,
                    center = magentaCenter
                )

                // Layer 4: Deep Dark Inner Portal Core
                val coreRadius = baseRadius * 0.88f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IntrospectionColors.BaseBackground,
                            IntrospectionColors.TopViolet,
                            IntrospectionColors.SurfaceHighlighted.copy(alpha = 0.85f)
                        ),
                        center = center,
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = center
                )

                // Layer 5: Outer Violet-Magenta Energy Arc (Rotating sweep gradient)
                rotate(degrees = outerRingRotation, pivot = center) {
                    val outerSweep = Brush.sweepGradient(
                        colors = listOf(
                            IntrospectionColors.PortalViolet.copy(alpha = 0.92f),
                            IntrospectionColors.DeepViolet.copy(alpha = 0.25f),
                            IntrospectionColors.Magenta.copy(alpha = 0.95f),
                            IntrospectionColors.CoralAccent.copy(alpha = 0.75f),
                            IntrospectionColors.PortalViolet.copy(alpha = 0.92f)
                        ),
                        center = center
                    )
                    drawCircle(
                        brush = outerSweep,
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Layer 6: Inner Pink-Peach Energy Arc (Counter-rotating sweep gradient)
                rotate(degrees = innerRingRotation, pivot = center) {
                    val innerSweep = Brush.sweepGradient(
                        colors = listOf(
                            IntrospectionColors.PrimaryPink.copy(alpha = 0.95f),
                            IntrospectionColors.PeachLight.copy(alpha = 0.35f),
                            IntrospectionColors.Magenta.copy(alpha = 0.92f),
                            IntrospectionColors.CoralAccent.copy(alpha = 0.6f),
                            IntrospectionColors.PrimaryPink.copy(alpha = 0.95f)
                        ),
                        center = center
                    )
                    drawCircle(
                        brush = innerSweep,
                        radius = baseRadius * 0.92f,
                        center = center,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Layer 7: Irregular Organic Radiant Arcs (Non-uniform paths)
                val arcRadius = baseRadius * 1.07f
                val arcRect = Size(arcRadius * 2f, arcRadius * 2f)
                val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)

                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            IntrospectionColors.Magenta.copy(alpha = (nebula1Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.PrimaryPink.copy(alpha = (nebula1Alpha * 0.45f * glowMultiplier).coerceIn(0f, 1f))
                        )
                    ),
                    startAngle = (outerRingRotation * 1.25f) % 360f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcRect,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            IntrospectionColors.CoralAccent.copy(alpha = (nebula2Alpha * glowMultiplier).coerceIn(0f, 1f)),
                            IntrospectionColors.PortalViolet.copy(alpha = (nebula2Alpha * 0.3f * glowMultiplier).coerceIn(0f, 1f))
                        )
                    ),
                    startAngle = ((innerRingRotation * 1.35f) + 170f) % 360f,
                    sweepAngle = 95f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcRect,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Layer 8: Breathing 8-Point Celestial Star at Portal Center
                scale(scale = starScale, pivot = center) {
                    drawCelestialStar(
                        center = center,
                        radius = baseRadius * 0.28f,
                        alpha = (0.90f * nebula1Alpha * glowMultiplier).coerceIn(0f, 1f)
                    )
                }
            }

            // Layer 9: 16 Floating Light Particles (Constrained to portal area)
            particles.forEach { p ->
                // The particle is transparent at both ends, so its off-screen reset cannot pop.
                val progress = (particleTime + p.phaseOffset) % 1f
                val angleRad = Math.toRadians((p.initialAngle + progress * 40f).toDouble())
                val particleDist = baseRadius * p.distanceFraction
                val yDrift = -progress * 18.dp.toPx()
                val xDrift = sin(progress * 2 * Math.PI.toFloat()) * 3.5.dp.toPx()

                val px = center.x + (particleDist * cos(angleRad)).toFloat() + xDrift
                val py = center.y + (particleDist * sin(angleRad)).toFloat() + yDrift

                val particleAlpha = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f) * (0.88f * glowMultiplier).coerceIn(0f, 1f)

                drawCircle(
                    color = if (p.initialAngle % 2 == 0f) IntrospectionColors.CoralAccent.copy(alpha = particleAlpha)
                    else IntrospectionColors.PrimaryText.copy(alpha = particleAlpha),
                    radius = p.size.dp.toPx() * 0.5f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private fun DrawScope.drawCelestialStar(
    center: Offset,
    radius: Float,
    alpha: Float
) {
    // Soft outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                IntrospectionColors.PeachLight.copy(alpha = alpha * 0.55f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.55f
        ),
        radius = radius * 1.55f,
        center = center
    )

    val starPath = Path()
    val points = 8
    val innerRadius = radius * 0.32f
    val outerRadius = radius

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * Math.PI / points) - (Math.PI / 2)
        val x = (center.x + r * cos(angle)).toFloat()
        val y = (center.y + r * sin(angle)).toFloat()
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()

    drawPath(
        path = starPath,
        color = Color.White.copy(alpha = alpha)
    )
}
