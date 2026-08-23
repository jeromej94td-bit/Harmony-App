package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.contentText
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.ui.tr
import kotlinx.coroutines.delay

private data class HarmonyImageChoiceVisuals(
    val icon: ImageVector,
    val subtitle: String,
    @param:DrawableRes val images: List<Int>
)

private val eggChoiceImages = listOf(
    R.drawable.egg_choice_01, R.drawable.egg_choice_02, R.drawable.egg_choice_03,
    R.drawable.egg_choice_04, R.drawable.egg_choice_05, R.drawable.egg_choice_06,
    R.drawable.egg_choice_07, R.drawable.egg_choice_08, R.drawable.egg_choice_09,
    R.drawable.egg_choice_10, R.drawable.egg_choice_11, R.drawable.egg_choice_12
)

private val steakChoiceImages = listOf(
    R.drawable.steak_choice_01, R.drawable.steak_choice_02, R.drawable.steak_choice_03,
    R.drawable.steak_choice_04, R.drawable.steak_choice_05, R.drawable.steak_choice_06,
    R.drawable.steak_choice_07, R.drawable.steak_choice_08, R.drawable.steak_choice_09,
    R.drawable.steak_choice_10, R.drawable.steak_choice_11, R.drawable.steak_choice_12
)

private val travelChoiceImages = listOf(
    R.drawable.travel_choice_01, R.drawable.travel_choice_02, R.drawable.travel_choice_03,
    R.drawable.travel_choice_04, R.drawable.travel_choice_05, R.drawable.travel_choice_06,
    R.drawable.travel_choice_07, R.drawable.travel_choice_08, R.drawable.travel_choice_09,
    R.drawable.travel_choice_10, R.drawable.travel_choice_11, R.drawable.travel_choice_12
)

@Composable
private fun harmonyImageChoiceVisuals(kind: HarmonyImageChoiceKind): HarmonyImageChoiceVisuals =
    when (kind) {
        HarmonyImageChoiceKind.EGG -> HarmonyImageChoiceVisuals(
            icon = Icons.Filled.Restaurant,
            subtitle = tr(
                "Wähle die Garstufe, die dir am besten schmeckt.",
                "Choose the doneness you enjoy most."
            ),
            images = eggChoiceImages
        )

        HarmonyImageChoiceKind.STEAK -> HarmonyImageChoiceVisuals(
            icon = Icons.Filled.Restaurant,
            subtitle = tr(
                "Tippe auf die gewünschte Garstufe.",
                "Tap your preferred doneness."
            ),
            images = steakChoiceImages
        )

        HarmonyImageChoiceKind.TRAVEL -> HarmonyImageChoiceVisuals(
            icon = Icons.Filled.FlightTakeoff,
            subtitle = tr(
                "Welche Art zu reisen fühlt sich nach euch an?",
                "Which way of travelling feels most like you?"
            ),
            images = travelChoiceImages
        )
    }

@Composable
internal fun HarmonyImageChoiceQuestion(
    kind: HarmonyImageChoiceKind,
    question: String,
    options: List<String>,
    selectedAnswer: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = harmonyImageChoiceVisuals(kind)
    val containerShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        HarmonyPurple.copy(alpha = 0.48f),
                        HarmonyPink.copy(alpha = 0.17f),
                        HarmonySurface2.copy(alpha = 0.97f),
                        HarmonyBg
                    )
                )
            )
            .border(1.2.dp, HarmonyPink.copy(alpha = 0.46f), containerShape)
            .padding(horizontal = 11.dp, vertical = 15.dp)
            .testTag("harmony_image_choice_question")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(HarmonyPink.copy(alpha = 0.92f), HarmonyPurpleLight.copy(alpha = 0.94f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contentText(question),
                color = HarmonyText,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = visuals.subtitle,
                color = HarmonyMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(13.dp))

            options.take(12).chunked(3).forEachIndexed { row, rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    rowOptions.forEachIndexed { column, option ->
                        val index = row * 3 + column
                        HarmonyImageChoiceCard(
                            animationKey = "${kind.name}_$index",
                            index = index,
                            option = option,
                            imageRes = visuals.images[index],
                            selected = selectedAnswer == option,
                            onClick = { onPick(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (row < 3) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HarmonyImageChoiceCard(
    animationKey: Any,
    index: Int,
    option: String,
    @DrawableRes imageRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reveal = remember(animationKey) { Animatable(0f) }
    val density = LocalDensity.current.density
    val shape = RoundedCornerShape(17.dp)
    val localizedOption = contentText(option)
    val title = localizedOption.substringBefore(" – ")
    val detail = localizedOption.substringAfter(" – ", missingDelimiterValue = "")

    LaunchedEffect(animationKey) {
        reveal.snapTo(0f)
        delay(harmonyImageChoiceRevealDelayMillis(index))
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 430, easing = FastOutSlowInEasing)
        )
    }

    val progress = reveal.value.coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .aspectRatio(0.63f)
            .graphicsLayer {
                alpha = progress
                rotationY = -82f * (1f - progress)
                translationX = -18f * (1f - progress)
                scaleX = 0.94f + progress * 0.06f
                scaleY = 0.96f + progress * 0.04f
                transformOrigin = TransformOrigin(0f, 0.5f)
                cameraDistance = 26f * density
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        HarmonySurface.copy(alpha = 0.98f),
                        HarmonyPurple.copy(alpha = if (selected) 0.30f else 0.12f),
                        HarmonyBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HarmonyPink else Color.White.copy(alpha = 0.16f),
                shape = shape
            )
            .clickable(enabled = progress > 0.86f, onClick = onClick)
            .padding(bottom = 7.dp)
            .testTag(
                if (selected) {
                    "harmony_image_choice_option_${index}_selected"
                } else {
                    "harmony_image_choice_option_$index"
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, HarmonyBg.copy(alpha = 0.20f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = if (selected) Color.White else HarmonyText,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
        if (detail.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                color = HarmonyMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 9.5.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (selected) HarmonyPink else Color.White.copy(alpha = 0.50f),
            modifier = Modifier.size(17.dp)
        )
    }
}
