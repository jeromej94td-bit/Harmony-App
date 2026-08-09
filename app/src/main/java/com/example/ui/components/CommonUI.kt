package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.contentText
import com.example.ui.tr
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HarmonyTopBar(
    userName: String,
    partnerName: String,
    onProfileClick: () -> Unit,
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
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
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
                Text(
                    text = partnerName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun HarmonyBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HarmonySurface.copy(alpha = 0.95f))
            .border(1.dp, HarmonyLine, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            Triple(0, "Home", Icons.Default.Home),
            Triple(1, tr("Fragen", "Games"), Icons.Default.Psychology),
            Triple(2, "Chat", Icons.Default.ChatBubble),
            Triple(3, tr("Momente", "Moments"), Icons.Default.PhotoLibrary),
            Triple(4, tr("Wir", "Profile"), Icons.Default.Favorite),
            Triple(5, "Dev", Icons.Default.Build)
        )

        navItems.forEach { (index, label, icon) ->
            val isSelected = selectedTab == index
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("nav_item_$index"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) HarmonyNavActive else HarmonyNavInactive,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HarmonyNavActive else HarmonyNavInactive
                )
            }
        }
    }
}

@Composable
fun CategoryTag(tag: String, modifier: Modifier = Modifier) {
    val category = com.example.data.model.HarmonyPacksData.CATEGORIES.find { 
        it.id.equals(tag, ignoreCase = true) || it.name.equals(tag, ignoreCase = true) 
    }
    
    val (bg, fg, label) = if (category != null) {
        val catColor = Color(category.tagColorHex)
        Triple(catColor.copy(alpha = 0.22f), catColor, "${category.emoji} ${contentText(category.name)}")
    } else {
        when (tag.lowercase()) {
            "unterhaltung", "entertainment" -> Triple(HarmonyPink.copy(alpha = 0.16f), HarmonyPinkSoft, tr("Unterhaltung", "Entertainment"))
            "dasoderdas", "tot", "this or that" -> Triple(HarmonyPurple.copy(alpha = 0.18f), HarmonyPurpleLight, tr("Das oder das", "This or That"))
            "hochzeit", "wedding" -> Triple(HarmonyGold.copy(alpha = 0.16f), HarmonyGold, tr("Hochzeit", "Wedding"))
            "kinder", "children" -> Triple(HarmonyTeal.copy(alpha = 0.16f), HarmonyTeal, tr("Kinder", "Children"))
            "reden", "discussion" -> Triple(HarmonyBlue.copy(alpha = 0.16f), HarmonyBlue, tr("Reden vor...", "Talk Before..."))
            else -> Triple(Color.White.copy(alpha = 0.12f), HarmonyText, tag.replaceFirstChar { it.uppercase() })
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
            text = label.uppercase(Locale.GERMAN),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
            letterSpacing = 0.8.sp
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
                        colors = listOf(HarmonySurface2, HarmonySurface)
                    )
                )
                .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            content()
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
