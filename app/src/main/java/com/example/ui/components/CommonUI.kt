package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.example.util.LanguageManager
import com.example.ui.LocalAppLanguage
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HarmonyBlue
import com.example.ui.theme.HarmonyGold
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyNavActive
import com.example.ui.theme.HarmonyNavInactive
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyTeal
import com.example.ui.theme.HarmonyText
import com.example.ui.theme.topicAccentColor
import com.example.data.model.QuestionPack
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File

@Composable
fun HarmonyTopBar(
    userName: String,
    partnerName: String,
    userAvatarPath: String? = null,
    partnerAvatarPath: String? = null,
    onProfileClick: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "HARMONY",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            style = MaterialTheme.typography.titleLarge.copy(
                brush = Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
            ),
            modifier = Modifier.testTag("brand_title")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(
                onClick = onRefresh,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = HarmonyText
                )
            }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick)
                    .padding(2.dp)
                    .testTag("avatars_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPinkSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatarPath != null) {
                    AsyncImage(model = File(userAvatarPath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text(userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = (-12).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(2.dp, HarmonySurface, CircleShape)
                    .background(Brush.linearGradient(listOf(HarmonyPurple, HarmonyPurpleLight))),
                contentAlignment = Alignment.Center
            ) {
                if (partnerAvatarPath != null) {
                    AsyncImage(model = File(partnerAvatarPath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text(partnerName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
        }
    }
}

private data class BottomNavGameVisualSpec(
    val categoryId: String,
    val accent: Color,
    val displayDurationMillis: Long
)

private val bottomNavGameVisuals = listOf(
    BottomNavGameVisualSpec("zeich", Color(0xFF9E59BD), 10_000L),
    BottomNavGameVisualSpec("tot", Color(0xFFFFC46B), 17_000L),
    BottomNavGameVisualSpec("zust", Color(0xFF9DB2FF), 10_000L),
    BottomNavGameVisualSpec("foto", Color(0xFF7BD8CB), 10_000L),
    BottomNavGameVisualSpec("tief", Color(0xFF9DB2FF), 10_000L),
    BottomNavGameVisualSpec("reden", Color(0xFFFFC46B), 10_000L),
    BottomNavGameVisualSpec("wer", Color(0xFFFF2E63), 10_000L),
    BottomNavGameVisualSpec("nie", Color(0xFFFF6B8F), 10_000L),
    BottomNavGameVisualSpec("lieber", Color(0xFFC89BE0), 10_000L)
)

@Composable
private fun BottomNavGameCarouselIcon(
    paused: Boolean,
    modifier: Modifier = Modifier
) {
    var visualIndex by remember { mutableIntStateOf(0) }
    val cardFlip = remember { Animatable(0f) }
    val density = LocalDensity.current.density

    LaunchedEffect(paused) {
        if (paused) {
            cardFlip.snapTo(0f)
            return@LaunchedEffect
        }

        while (true) {
            delay(bottomNavGameVisuals[visualIndex].displayDurationMillis)
            cardFlip.animateTo(88f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
            visualIndex = (visualIndex + 1) % bottomNavGameVisuals.size
            cardFlip.snapTo(-88f)
            cardFlip.animateTo(0f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
        }
    }

    val visual = bottomNavGameVisuals[visualIndex]
    Box(
        modifier = modifier
            .size(34.dp)
            .graphicsLayer {
                rotationY = cardFlip.value
                cameraDistance = 18f * density
                shadowElevation = if (paused) 5f else 8f
            }
            .testTag("nav_games_visual_${visual.categoryId}_${if (paused) "paused" else "running"}"),
        contentAlignment = Alignment.Center
    ) {
        GameCategoryVisual(
            categoryId = visual.categoryId,
            accent = visual.accent,
            animationEnabled = !paused,
            modifier = Modifier
                .requiredSize(84.dp)
                .graphicsLayer {
                    scaleX = 0.42f
                    scaleY = 0.42f
                }
        )
    }
}

@Composable
fun HarmonyBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    appLanguage: String = "de",
    modifier: Modifier = Modifier
) {
    val glowTransition = rememberInfiniteTransition(label = "bottom_nav_glow")
    val activeGlow by glowTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_nav_glow"
    )

    val navigationShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(navigationShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        HarmonySurface2.copy(alpha = 0.97f),
                        Color(0xFF12091B).copy(alpha = 0.985f),
                        Color(0xFF07020D).copy(alpha = 0.99f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        HarmonyPink.copy(alpha = 0.20f),
                        HarmonyPurpleLight.copy(alpha = 0.46f),
                        HarmonyPinkSoft.copy(alpha = 0.20f)
                    )
                ),
                shape = navigationShape
            )
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            Triple(0, LanguageManager.tr("Home", appLanguage), Icons.Default.Home),
            Triple(1, LanguageManager.tr("Spiele", appLanguage), Icons.Default.Psychology),
            Triple(2, LanguageManager.tr("Chat", appLanguage), Icons.Default.ChatBubble),
            Triple(3, LanguageManager.tr("Momente", appLanguage), Icons.Default.PhotoLibrary),
            Triple(4, LanguageManager.tr("Merken", appLanguage), Icons.Default.Bookmarks)
        )

        navItems.forEach { (index, label, icon) ->
            val isSelected = selectedTab == index
            val itemShape = RoundedCornerShape(15.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp)
                    .clip(itemShape)
                    .background(
                        if (isSelected) Brush.verticalGradient(
                            listOf(
                                HarmonyNavActive.copy(alpha = 0.17f),
                                HarmonyPurple.copy(alpha = 0.12f),
                                Color(0xFF110817).copy(alpha = 0.38f)
                            )
                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(
                        if (isSelected) 1.dp else 0.dp,
                        HarmonyNavActive.copy(alpha = if (isSelected) activeGlow else 0f),
                        itemShape
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 4.dp)
                    .testTag("nav_item_$index"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == 1) {
                        BottomNavGameCarouselIcon(paused = isSelected)
                    } else {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                HarmonyNavActive.copy(alpha = activeGlow * 0.25f),
                                                HarmonyPurple.copy(alpha = 0.10f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) HarmonyNavActive else HarmonyNavInactive,
                            modifier = Modifier.size(if (isSelected) 25.dp else 24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HarmonyNavActive else HarmonyNavInactive
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 22.dp else 4.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Brush.horizontalGradient(
                                listOf(Color.Transparent, HarmonyPinkSoft, HarmonyPurpleLight, Color.Transparent)
                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                )
            }
        }
    }
}

@Composable
fun CategoryTag(tag: String, modifier: Modifier = Modifier) {
    val appLanguage = LocalAppLanguage.current.code
    val category = com.example.data.model.HarmonyPacksData.CATEGORIES.find {
        it.id.equals(tag, ignoreCase = true) || it.name.equals(tag, ignoreCase = true)
    }

    val (bg, fg, label) = if (category != null) {
        val catColor = Color(category.tagColorHex)
        val localizedCategory = LanguageManager.translateCategory(category, appLanguage)
        Triple(catColor.copy(alpha = 0.22f), catColor, "${category.emoji} ${localizedCategory.name}")
    } else {
        val normalized = when (tag.lowercase()) {
            "unterhaltung", "entertainment" -> "Unterhaltung"
            "dasoderdas", "tot", "oder" -> "Das oder das"
            "hochzeit" -> "Hochzeit"
            "kinder" -> "Kinder"
            "reden" -> "Reden vor..."
            "tiere" -> "Tiere"
            "fürpaare", "fuerpaare" -> "Für Paare"
            "party" -> "Party"
            "wer", "werwuerde" -> "Wer würde eher?"
            "ichhabenochnie" -> "Ich habe noch nie"
            "essen" -> "Essen & Genuss"
            "zuhause" -> "Zuhause & Alltag"
            "games" -> "Spiele"
            else -> tag.replaceFirstChar { it.uppercase() }
        }
        val localized = LanguageManager.tr(normalized, appLanguage)
        when (tag.lowercase()) {
            "unterhaltung", "entertainment" -> Triple(HarmonyPink.copy(alpha = 0.16f), HarmonyPinkSoft, localized)
            "dasoderdas", "tot", "oder" -> Triple(HarmonyPurple.copy(alpha = 0.18f), HarmonyPurpleLight, localized)
            "hochzeit" -> Triple(HarmonyGold.copy(alpha = 0.16f), HarmonyGold, localized)
            "kinder" -> Triple(HarmonyTeal.copy(alpha = 0.16f), HarmonyTeal, localized)
            "reden" -> Triple(HarmonyBlue.copy(alpha = 0.16f), HarmonyBlue, localized)
            else -> Triple(Color.White.copy(alpha = 0.12f), HarmonyText, localized)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun TimerPill(modifier: Modifier = Modifier) {
    var timerText by remember { mutableStateOf("--:--:--") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            val endOfDay = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
            }.timeInMillis

            val diff = (endOfDay - now).coerceAtLeast(0)
            val hours = diff / 3600000
            val minutes = (diff / 60000) % 60
            val seconds = (diff / 1000) % 60
            timerText = String.format(Locale.GERMAN, "%02d:%02d:%02d", hours, minutes, seconds)
            delay(1000)
        }
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(HarmonyPink.copy(alpha = 0.12f))
            .border(1.dp, HarmonyPink.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(HarmonyPink)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = timerText,
            color = HarmonyPinkSoft,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun HarmonyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color = HarmonyPurple,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            HarmonySurface2.copy(alpha = 0.76f),
                            HarmonySurface.copy(alpha = 0.92f)
                        )
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.78f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, accent.copy(alpha = 0.72f), Color.White.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )
            )
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun AuroraGlassSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = HarmonyText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.2.sp
    )
}

@Composable
fun HarmonyTopicIcon(
    topicId: String,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val glowTransition = rememberInfiniteTransition(label = "topic_icon_$topicId")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.74f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "topic_icon_glow_$topicId"
    )
    val breathe by glowTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "topic_icon_breathe_$topicId"
    )
    val icon = when (topicId) {
        "moral" -> Icons.Default.AccountBalance
        "geld" -> Icons.Default.Savings
        "sex" -> Icons.Default.LocalFireDepartment
        "reisen" -> Icons.Default.Flight
        "essen" -> Icons.Default.Restaurant
        "filme_serien" -> Icons.Default.Movie
        "familie" -> Icons.Default.People
        "hobbys" -> Icons.Default.Palette
        "kennen" -> Icons.Default.People
        "aufwaermen", "beziehung" -> Icons.Default.Favorite
        else -> Icons.Default.Psychology
    }
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = glowAlpha * 0.18f),
                        accent.copy(alpha = glowAlpha * 0.72f),
                        accent.copy(alpha = 0.10f)
                    )
                )
            )
            .border(if (size > 50.dp) 2.dp else 1.dp, accent.copy(alpha = glowAlpha), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (topicId) {
            "sex" -> Text(
                text = "18+",
                color = Color.White,
                fontSize = if (size > 50.dp) 15.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp
            )
            "beziehung" -> Box(modifier = Modifier.size(size * 0.66f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = accent.copy(alpha = 0.65f),
                    modifier = Modifier.size(size * 0.46f).offset(x = (-6).dp, y = 3.dp)
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.46f).offset(x = 6.dp, y = (-3).dp)
                )
            }
            else -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

/**
 * A compact icon for an individual question pack. This is intentionally separate from
 * [HarmonyTopicIcon], because the large topic cards keep their established symbols while
 * pack cards can communicate their actual subject (for example a controller for gaming).
 */
@Composable
fun HarmonyPackIcon(
    pack: QuestionPack,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val searchable = remember(pack.id, pack.title, pack.tags, pack.cat, pack.topic) {
        listOf(pack.id, pack.title, pack.cat, pack.topic, *pack.tags.toTypedArray())
            .joinToString(" ")
            .lowercase(Locale.GERMAN)
    }
    val icon = when {
        listOf("videospiel", "video game", "gaming", "gamer").any(searchable::contains) -> Icons.Default.SportsEsports
        listOf("eis", "gourmet").any(searchable::contains) -> Icons.Default.Icecream
        listOf("hogwarts", "schloss", "haus stolz").any(searchable::contains) -> Icons.Default.Castle
        listOf("disney", "feenstaub").any(searchable::contains) -> Icons.Default.AutoAwesome
        listOf("entertainment", "universal", "film", "serie", "kino").any(searchable::contains) -> Icons.Default.Movie
        listOf("party", "hochzeit").any(searchable::contains) -> Icons.Default.Celebration
        listOf("ring", "antrag", "verlob").any(searchable::contains) -> Icons.Default.Diamond
        listOf("haustier", "tier").any(searchable::contains) -> Icons.Default.Pets
        listOf("kind", "familie").any(searchable::contains) -> Icons.Default.ChildCare
        listOf("reise", "urlaub", "stadt").any(searchable::contains) -> Icons.Default.Flight
        listOf("essen", "genuss", "gericht", "restaurant").any(searchable::contains) -> Icons.Default.Restaurant
        listOf("foto", "schnapp", "bild").any(searchable::contains) || pack.cat == "foto" -> Icons.Default.CameraAlt
        listOf("geld", "finanz", "kauf").any(searchable::contains) -> Icons.Default.Savings
        listOf("haus", "zuhause", "alltag").any(searchable::contains) -> Icons.Default.Home
        pack.cat == "zeich" -> Icons.Default.Palette
        pack.cat == "zust" -> Icons.Default.CheckCircle
        pack.cat == "tief" || listOf("gespräch", "reden", "meinung").any(searchable::contains) -> Icons.Default.QuestionAnswer
        listOf("liebe", "nähe", "intim", "ehepaar").any(searchable::contains) -> Icons.Default.Favorite
        else -> when (pack.topic) {
            "moral" -> Icons.Default.AccountBalance
            "geld" -> Icons.Default.Savings
            "sex" -> Icons.Default.LocalFireDepartment
            "reisen" -> Icons.Default.Flight
            "essen" -> Icons.Default.Restaurant
            "filme_serien" -> Icons.Default.Movie
            "familie" -> Icons.Default.People
            "hobbys" -> Icons.Default.Palette
            else -> Icons.Default.Psychology
        }
    }
    val motion = rememberInfiniteTransition(label = "pack_icon_${pack.id}")
    val glow by motion.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pack_icon_glow_${pack.id}"
    )
    val tilt by motion.animateFloat(
        initialValue = -2.4f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(tween(3100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pack_icon_tilt_${pack.id}"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = tilt
                scaleX = 0.97f + glow * 0.045f
                scaleY = 0.97f + glow * 0.045f
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = glow * 0.18f), accent.copy(alpha = glow * 0.68f), accent.copy(alpha = 0.10f))
                )
            )
            .border(
                1.2.dp,
                Brush.sweepGradient(listOf(accent, Color.White.copy(alpha = glow), HarmonyPink, accent)),
                CircleShape
            )
            .testTag("pack_icon_${pack.id}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.52f))
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White.copy(alpha = glow * 0.72f),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp).size(size * 0.22f)
        )
    }
}

@Composable
fun HarmonyCategoryIcon(
    categoryId: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val icon = when (categoryId) {
        "unterbewusstsein" -> Icons.Default.AutoAwesome
        "wer" -> Icons.Default.People
        "zeich" -> Icons.Default.Palette
        "tot" -> Icons.AutoMirrored.Filled.CompareArrows
        "zust" -> Icons.Default.CheckCircle
        "nie" -> Icons.Default.VisibilityOff
        "lieber" -> Icons.Default.Favorite
        "foto" -> Icons.Default.PhotoLibrary
        "tief" -> Icons.Default.Psychology
        "reden" -> Icons.Default.ChatBubble
        else -> Icons.Default.AutoAwesome
    }
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.28f), Color.Transparent)))
            .border(1.dp, accent.copy(alpha = 0.58f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
fun AuroraProgressBar(
    progress: Float,
    accent: Color = HarmonyPink,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "aurora_progress")
    val shimmerPhase by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_progress_shimmer"
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.5.dp, accent.copy(alpha = 0.22f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.72f), accent)))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = shimmerPhase * size.width
                val halfWidth = size.width * 0.34f
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.86f), Color.Transparent),
                        startX = center - halfWidth,
                        endX = center + halfWidth
                    )
                )
            }
        }
    }
}

@Composable
fun HarmonyToast(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 90.dp)
                .clip(CircleShape)
                .background(HarmonySurface2.copy(alpha = 0.95f))
                .border(1.dp, HarmonyLine, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("toast_message"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message ?: "",
                color = HarmonyText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    return sdf.format(Date(ts))
}

fun formatTimeOnly(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.GERMAN)
    return sdf.format(Date(ts))
}
