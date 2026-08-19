package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import kotlinx.coroutines.delay

@Composable
fun GameCategoryVisual(
    categoryId: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    when (categoryId) {
        "wer" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_thinking_harmony,
            accent = accent,
            animationLabel = "thinking_panda",
            modifier = modifier
        )

        "nie" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_never_harmony,
            accent = accent,
            animationLabel = "never_panda",
            modifier = modifier
        )

        "tot" -> AnimatedHarmonyCards(accent = accent, modifier = modifier)
        else -> HarmonyCategoryIcon(categoryId = categoryId, accent = accent, modifier = modifier)
    }
}

@Composable
private fun PandaArtworkIcon(
    @DrawableRes drawableRes: Int,
    accent: Color,
    animationLabel: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = animationLabel)
    val tilt by transition.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_tilt"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_breathe"
    )
    val glow by transition.animateFloat(
        initialValue = 0.44f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_glow"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                rotationZ = tilt
                scaleX = breathe
                scaleY = breathe
                shadowElevation = 8f
            }
            .clip(RoundedCornerShape(23.dp))
            .background(Color(0xFF15091E))
            .border(
                width = 1.4.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = glow),
                        Color.White.copy(alpha = glow * 0.72f),
                        HarmonyPink.copy(alpha = glow),
                        accent.copy(alpha = glow)
                    )
                ),
                shape = RoundedCornerShape(23.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(74.dp)
        )
    }
}

@Composable
private fun AnimatedHarmonyCards(
    accent: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val flip = remember { Animatable(0f) }
    var pairIndex by remember { mutableIntStateOf(0) }
    val pairs = remember {
        listOf(
            Icons.Default.Restaurant to Icons.Default.LocalCafe,
            Icons.Default.LocationCity to Icons.Default.Flight,
            Icons.Default.Movie to Icons.Default.Palette
        )
    }
    val floatTransition = rememberInfiniteTransition(label = "harmony_cards_float")
    val floatPhase by floatTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "harmony_cards_float_phase"
    )
    val glow by floatTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "harmony_cards_glow"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(3_400)
            flip.animateTo(90f, tween(520, easing = FastOutSlowInEasing))
            pairIndex = (pairIndex + 1) % pairs.size
            flip.snapTo(-90f)
            flip.animateTo(0f, tween(520, easing = FastOutSlowInEasing))
        }
    }

    val pair = pairs[pairIndex]
    Box(
        modifier = modifier
            .size(width = 84.dp, height = 76.dp)
            .graphicsLayer {
                translationY = floatPhase * 2.5f * density
                scaleX = 0.99f + glow * 0.015f
                scaleY = 0.99f + glow * 0.015f
            },
        contentAlignment = Alignment.Center
    ) {
        HarmonyFlipCard(
            icon = pair.first,
            accent = accent,
            rotationY = flip.value,
            rotationZ = -10f + floatPhase * 2f,
            glow = glow,
            modifier = Modifier.offset(x = (-15).dp, y = 3.dp)
        )
        HarmonyFlipCard(
            icon = pair.second,
            accent = HarmonyPink,
            rotationY = -flip.value,
            rotationZ = 10f - floatPhase * 2f,
            glow = glow,
            modifier = Modifier.offset(x = 15.dp, y = (-3).dp)
        )
    }
}

@Composable
private fun HarmonyFlipCard(
    icon: ImageVector,
    accent: Color,
    rotationY: Float,
    rotationZ: Float,
    glow: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(width = 42.dp, height = 57.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                cameraDistance = 14f * density
                shadowElevation = 9f * density
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        accent.copy(alpha = 0.88f),
                        HarmonyPurple.copy(alpha = 0.92f),
                        Color(0xFF170A21)
                    )
                )
            )
            .border(
                1.4.dp,
                Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f),
                        accent.copy(alpha = glow),
                        HarmonyPink.copy(alpha = glow),
                        Color.White.copy(alpha = 0.92f)
                    )
                ),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(23.dp)
        )
    }
}
