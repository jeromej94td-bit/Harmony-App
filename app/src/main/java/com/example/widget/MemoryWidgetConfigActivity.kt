package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.HarmonyDatabase
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonyText
import com.example.ui.theme.HarmonyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        val initialConfig = MemoryWidgetPreferences.load(applicationContext, appWidgetId)

        setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryWidgetConfigScreen(
                    initialConfig = initialConfig,
                    onSave = ::saveConfig
                )
            }
        }
    }

    private fun saveConfig(config: MemoryWidgetConfig) {
        MemoryWidgetPreferences.save(applicationContext, appWidgetId, config)
        MemoryWidgetProvider.updateOne(applicationContext, appWidgetId)
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}

@Composable
private fun MemoryWidgetConfigScreen(
    initialConfig: MemoryWidgetConfig,
    onSave: (MemoryWidgetConfig) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var mode by remember { mutableStateOf(initialConfig.mode) }
    var maxItems by remember { mutableIntStateOf(initialConfig.maxItems.coerceIn(1, 3)) }
    val selectedIds = remember {
        mutableStateListOf<String>().apply { addAll(initialConfig.pinnedIds.take(3)) }
    }
    var entries by remember { mutableStateOf<List<MemoryEntryEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        entries = withContext(Dispatchers.IO) {
            HarmonyDatabase.getInstance(context.applicationContext)
                .memoryDao()
                .getOpenEntriesForWidget()
        }
        val validIds = entries.mapTo(hashSetOf()) { it.id }
        selectedIds.removeAll { it !in validIds }
        loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF160A24),
                        Color(0xFF0D0618),
                        Color(0xFF05020B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "♥  Harmony · Merken",
                color = HarmonyPinkSoft,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "Das müssen wir uns merken",
                color = HarmonyText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Wählt, was auf eurem Homescreen sichtbar sein soll.",
                color = HarmonyMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle("INHALT")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Automatisch",
                    subtitle = "Neueste Einträge",
                    selected = mode == MemoryWidgetMode.AUTOMATIC,
                    onClick = { mode = MemoryWidgetMode.AUTOMATIC }
                )
                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Bestimmte auswählen",
                    subtitle = "Bis zu 3 festpinnen",
                    selected = mode == MemoryWidgetMode.PINNED,
                    onClick = { mode = MemoryWidgetMode.PINNED }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("ANZAHL")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..3).forEach { count ->
                    CountCard(
                        modifier = Modifier.weight(1f),
                        count = count,
                        selected = maxItems == count,
                        onClick = { maxItems = count }
                    )
                }
            }

            if (mode == MemoryWidgetMode.PINNED) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("EINTRÄGE")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${selectedIds.size}/3 gewählt",
                        color = if (selectedIds.size == 3) HarmonyPinkSoft else HarmonyMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HarmonyPink)
                    }
                } else if (entries.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = Color(0xAA171022),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, HarmonyPurple.copy(alpha = 0.28f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Noch nichts gemerkt.\nFügt zuerst etwas in Harmony hinzu.",
                                color = HarmonyMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            val order = selectedIds.indexOf(entry.id).takeIf { it >= 0 }?.plus(1)
                            MemorySelectionRow(
                                entry = entry,
                                order = order,
                                onClick = {
                                    val existing = selectedIds.indexOf(entry.id)
                                    if (existing >= 0) {
                                        selectedIds.removeAt(existing)
                                    } else if (selectedIds.size < 3) {
                                        selectedIds.add(entry.id)
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        MemoryWidgetConfig(
                            mode = mode,
                            maxItems = maxItems,
                            pinnedIds = if (mode == MemoryWidgetMode.PINNED) selectedIds.toList() else emptyList()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HarmonyPink,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Widget hinzufügen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = HarmonyPurpleLight,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun ModeCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) HarmonyPinkSoft else HarmonyPurple.copy(alpha = 0.3f)
    val surfaceColor = if (selected) Color(0xCC32132E) else Color(0xB8171022)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (selected) "●  $title" else "○  $title",
                color = if (selected) HarmonyPinkSoft else HarmonyText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = HarmonyMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CountCard(
    modifier: Modifier,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        color = if (selected) Color(0xCC32132E) else Color(0xB8171022),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) HarmonyPinkSoft else HarmonyPurple.copy(alpha = 0.28f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = count.toString(),
                color = if (selected) HarmonyPinkSoft else HarmonyText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun MemorySelectionRow(
    entry: MemoryEntryEntity,
    order: Int?,
    onClick: () -> Unit
) {
    val selected = order != null
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) Color(0xC72A1738) else Color(0xA8171022),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(
            if (selected) 1.4.dp else 1.dp,
            if (selected) HarmonyPinkSoft.copy(alpha = 0.85f) else HarmonyPurple.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) HarmonyPink.copy(alpha = 0.22f) else HarmonyPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order?.toString() ?: if (entry.kind == MemoryEntryKind.LINK) "↗" else "✦",
                    color = if (selected) HarmonyPinkSoft else HarmonyPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.size(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.previewTitle?.takeIf { entry.kind == MemoryEntryKind.LINK && it.isNotBlank() }
                        ?: entry.title,
                    color = HarmonyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val secondary = if (entry.kind == MemoryEntryKind.LINK) {
                    entry.previewSiteName ?: entry.url ?: "Link"
                } else {
                    entry.body
                }
                if (!secondary.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = secondary,
                        color = HarmonyMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (selected) {
                Text(
                    text = "✓",
                    color = HarmonyPinkSoft,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
