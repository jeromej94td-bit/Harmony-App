package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryKind
import com.example.ui.memory.MemoryEntryUi
import com.example.ui.theme.HarmonyBlue
import com.example.ui.theme.HarmonyFood
import com.example.ui.theme.HarmonyGold
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyTeal
import com.example.ui.theme.HarmonyText
import com.example.util.LanguageManager
import java.net.URI

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MemoryEntryCard(
    item: MemoryEntryUi,
    category: MemoryCategoryEntity?,
    appLanguage: String,
    previewFailed: Boolean,
    onComplete: (String) -> Unit,
    onRestore: (String) -> Unit,
    onRetryPreview: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    onLongPress: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entity
    val accent = memoryCategoryColor(category?.colorKey)
    val categoryLabel = memoryCategoryLabel(category, appLanguage)
    val isCompleted = item.bucket != MemoryBucket.CURRENT_OPEN
    val shape = RoundedCornerShape(24.dp)
    var menuExpanded by remember(entry.id) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelection() else onOpen() },
                onLongClick = { if (selectionMode) onToggleSelection() else onLongPress() }
            )
            .testTag("memory_entry_${entry.id}")
            .then(
                if (isCompleted) {
                    Modifier.semantics { stateDescription = "completed" }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.22f),
                            HarmonySurface2.copy(alpha = 0.88f),
                            HarmonySurface.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.48f), shape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent, RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
            )

            if (entry.kind == MemoryEntryKind.LINK) {
                MemoryLinkCardContent(
                    item = item,
                    category = category,
                    categoryLabel = categoryLabel,
                    accent = accent,
                    appLanguage = appLanguage,
                    previewFailed = previewFailed,
                    onComplete = onComplete,
                    onRestore = onRestore,
                    onRetryPreview = onRetryPreview,
                    onOpenMenu = { menuExpanded = true },
                    selectionMode = selectionMode,
                    selected = selected,
                    onToggleSelection = onToggleSelection
                )
            } else {
                MemoryNoteCardContent(
                    item = item,
                    category = category,
                    categoryLabel = categoryLabel,
                    accent = accent,
                    appLanguage = appLanguage,
                    onComplete = onComplete,
                    onRestore = onRestore,
                    onOpenMenu = { menuExpanded = true },
                    selectionMode = selectionMode,
                    selected = selected,
                    onToggleSelection = onToggleSelection
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(LanguageManager.tr("Endgültig löschen", appLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteRequest(entry.id)
                        },
                        modifier = Modifier.testTag("memory_entry_${entry.id}_delete")
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryNoteCardContent(
    item: MemoryEntryUi,
    category: MemoryCategoryEntity?,
    categoryLabel: String,
    accent: Color,
    appLanguage: String,
    onComplete: (String) -> Unit,
    onRestore: (String) -> Unit,
    onOpenMenu: () -> Unit,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit
) {
    val entry = item.entity
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 164.dp)
            .padding(16.dp)
    ) {
        MemoryCardTopRow(
            entryId = entry.id,
            item = item,
            category = category,
            accent = accent,
            appLanguage = appLanguage,
            onComplete = onComplete,
            onOpenMenu = onOpenMenu,
            selectionMode = selectionMode,
            selected = selected,
            onToggleSelection = onToggleSelection
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = entry.title,
            color = HarmonyText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        entry.body?.takeIf { it.isNotBlank() }?.let { body ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                color = HarmonyMuted,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.weight(1f, fill = false))
        Spacer(Modifier.height(14.dp))
        MemoryCardFooter(
            item = item,
            entryId = entry.id,
            categoryLabel = categoryLabel,
            category = category,
            accent = accent,
            appLanguage = appLanguage,
            onRestore = onRestore
        )
    }
}

@Composable
private fun MemoryLinkCardContent(
    item: MemoryEntryUi,
    category: MemoryCategoryEntity?,
    categoryLabel: String,
    accent: Color,
    appLanguage: String,
    previewFailed: Boolean,
    onComplete: (String) -> Unit,
    onRestore: (String) -> Unit,
    onRetryPreview: (String) -> Unit,
    onOpenMenu: () -> Unit,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit
) {
    val entry = item.entity
    var imageFailed by remember(entry.previewImageUrl) { mutableStateOf(false) }
    val showImage = !previewFailed && !imageFailed && !entry.previewImageUrl.isNullOrBlank()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontal = maxWidth >= 340.dp
        if (horizontal) {
            Row(modifier = Modifier.fillMaxWidth().heightIn(min = 164.dp)) {
                MemoryLinkVisual(
                    imageModel = entry.previewImageUrl,
                    title = entry.previewTitle ?: entry.title,
                    showImage = showImage,
                    accent = accent,
                    onImageError = { imageFailed = true },
                    modifier = Modifier.width(136.dp).fillMaxHeight()
                )
                MemoryLinkText(
                    item = item,
                    category = category,
                    categoryLabel = categoryLabel,
                    accent = accent,
                    appLanguage = appLanguage,
                    previewFailed = previewFailed || imageFailed,
                    onComplete = onComplete,
                    onRestore = onRestore,
                    onRetryPreview = onRetryPreview,
                    onOpenMenu = onOpenMenu,
                    selectionMode = selectionMode,
                    selected = selected,
                    onToggleSelection = onToggleSelection,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                MemoryLinkVisual(
                    imageModel = entry.previewImageUrl,
                    title = entry.previewTitle ?: entry.title,
                    showImage = showImage,
                    accent = accent,
                    onImageError = { imageFailed = true },
                    modifier = Modifier.fillMaxWidth().height(126.dp)
                )
                MemoryLinkText(
                    item = item,
                    category = category,
                    categoryLabel = categoryLabel,
                    accent = accent,
                    appLanguage = appLanguage,
                    previewFailed = previewFailed || imageFailed,
                    onComplete = onComplete,
                    onRestore = onRestore,
                    onRetryPreview = onRetryPreview,
                    selectionMode = selectionMode,
                    selected = selected,
                    onToggleSelection = onToggleSelection
                )
            }
        }
    }
}

@Composable
private fun MemoryLinkVisual(
    imageModel: String?,
    title: String,
    showImage: Boolean,
    accent: Color,
    onImageError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(accent.copy(alpha = 0.46f), HarmonyPurple.copy(alpha = 0.26f), HarmonySurface2)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        if (showImage) {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { onImageError() },
                loading = { MemoryLinkVisualFallback(accent) },
                error = { MemoryLinkVisualFallback(accent) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            MemoryLinkVisualFallback(accent)
        }
    }
}

@Composable
private fun MemoryLinkVisualFallback(accent: Color) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), accent.copy(alpha = 0.34f))
                )
            )
            .border(1.dp, accent.copy(alpha = 0.78f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MemoryLinkText(
    item: MemoryEntryUi,
    category: MemoryCategoryEntity?,
    categoryLabel: String,
    accent: Color,
    appLanguage: String,
    previewFailed: Boolean,
    onComplete: (String) -> Unit,
    onRestore: (String) -> Unit,
    onRetryPreview: (String) -> Unit,
    onOpenMenu: () -> Unit = {},
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entity
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        MemoryCardTopRow(
            entryId = entry.id,
            item = item,
            category = category,
            accent = accent,
            appLanguage = appLanguage,
            onComplete = onComplete,
            onOpenMenu = onOpenMenu,
            selectionMode = selectionMode,
            selected = selected,
            onToggleSelection = onToggleSelection,
            compact = true
        )
        val siteLabel = entry.previewSiteName ?: safeHost(entry.url) ?: LanguageManager.tr("Link", appLanguage)
        val personalNote = entry.body?.trim().orEmpty()
        val primaryText = personalNote.ifBlank {
            entry.previewTitle?.trim().orEmpty().ifBlank { siteLabel }
        }
        val previewTitle = entry.previewTitle
            ?.trim()
            ?.takeIf { it.isNotBlank() && it != primaryText }

        Text(
            text = siteLabel,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = primaryText,
            color = HarmonyText,
            fontSize = 17.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("memory_entry_${entry.id}_primary")
        )
        previewTitle?.let { title ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                color = HarmonyMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("memory_entry_${entry.id}_preview_title")
            )
        }
        entry.previewDescription
            ?.trim()
            ?.takeIf { it.isNotBlank() && personalNote.isBlank() && it != primaryText }
            ?.let { description ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    color = HarmonyMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        entry.url?.let { url ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = url,
                color = HarmonyMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (previewFailed) {
            TextButton(
                onClick = { onRetryPreview(entry.id) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("memory_entry_${entry.id}_retry")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(LanguageManager.tr("Vorschau erneut laden", appLanguage))
            }
        }
        MemoryCardFooter(
            item = item,
            entryId = entry.id,
            categoryLabel = categoryLabel,
            category = category,
            accent = accent,
            appLanguage = appLanguage,
            onRestore = onRestore
        )
    }
}

@Composable
private fun MemoryCardTopRow(
    entryId: String,
    item: MemoryEntryUi,
    category: MemoryCategoryEntity?,
    accent: Color,
    appLanguage: String,
    onComplete: (String) -> Unit,
    onOpenMenu: () -> Unit,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 38.dp else 44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = memoryCategoryIcon(category?.iconKey),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(if (compact) 20.dp else 23.dp)
            )
        }
        if (selectionMode) {
            IconButton(
                onClick = onToggleSelection,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("memory_entry_${entryId}_select")
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = LanguageManager.tr(
                        if (selected) "Auswahl aufheben" else "Auswählen",
                        appLanguage
                    ),
                    tint = if (selected) HarmonyPinkSoft else HarmonyMuted,
                    modifier = Modifier
                        .size(28.dp)
                        .then(
                            if (selected) Modifier
                            else Modifier.border(1.5.dp, HarmonyMuted, CircleShape).padding(5.dp)
                        )
                )
            }
        } else when (item.bucket) {
            MemoryBucket.CURRENT_OPEN -> IconButton(
                onClick = { onComplete(entryId) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("memory_entry_${entryId}_complete")
                    .semantics {
                        contentDescription = LanguageManager.tr("Erledigt", appLanguage)
                    }
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = HarmonyMuted,
                    modifier = Modifier
                        .size(28.dp)
                        .border(1.5.dp, HarmonyMuted, CircleShape)
                        .padding(5.dp)
                )
            }

            MemoryBucket.ARCHIVED -> IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("memory_entry_${entryId}_menu")
                    .semantics {
                        contentDescription = LanguageManager.tr("Endgültig löschen", appLanguage)
                    }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = HarmonyMuted)
            }
        }
    }
}

@Composable
private fun MemoryCardFooter(
    item: MemoryEntryUi,
    entryId: String,
    categoryLabel: String,
    category: MemoryCategoryEntity?,
    accent: Color,
    appLanguage: String,
    onRestore: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = memoryCategoryIcon(category?.iconKey),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = categoryLabel,
                color = accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (item.bucket == MemoryBucket.ARCHIVED) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HarmonyPinkSoft,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = LanguageManager.tr("Erledigt", appLanguage),
                    color = HarmonyPinkSoft,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = { onRestore(entryId) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("memory_entry_${entryId}_restore")
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(LanguageManager.tr("Wiederherstellen", appLanguage))
            }
        }
    }
}

@Composable
internal fun memoryCategoryLabel(category: MemoryCategoryEntity?, appLanguage: String): String =
    category?.systemKey?.let { LanguageManager.tr(it, appLanguage) }
        ?: category?.customName.orEmpty()

internal fun memoryCategoryIcon(iconKey: String?): ImageVector = when (iconKey) {
    "movie" -> Icons.Default.Movie
    "tv" -> Icons.Default.Tv
    "lightbulb" -> Icons.Default.Lightbulb
    "place" -> Icons.Default.Place
    "sparkles" -> Icons.Default.AutoAwesome
    "restaurant" -> Icons.Default.Restaurant
    "event" -> Icons.Default.Event
    "favorite" -> Icons.Default.Favorite
    "travel" -> Icons.Default.TravelExplore
    "link" -> Icons.Default.Link
    else -> Icons.Default.Bookmark
}

internal fun memoryCategoryColor(colorKey: String?): Color = when (colorKey) {
    "pink", "rose" -> HarmonyPinkSoft
    "orange" -> HarmonyFood
    "blue" -> HarmonyBlue
    "teal" -> HarmonyTeal
    "gold" -> HarmonyGold
    "purple" -> HarmonyPurpleLight
    else -> Color(0xFFA98CFF)
}

private fun safeHost(url: String?): String? = try {
    url?.let { URI(it).host?.removePrefix("www.") }
} catch (_: Exception) {
    null
}
