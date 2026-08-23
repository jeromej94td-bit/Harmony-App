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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
        val initial = MemoryWidgetPreferences.load(applicationContext, appWidgetId)
        setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryWidgetConfigScreen(initial) { config ->
                    MemoryWidgetPreferences.save(applicationContext, appWidgetId, config)
                    MemoryWidgetProvider.updateOne(applicationContext, appWidgetId)
                    setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    )
                    finish()
                }
            }
        }
    }
}

@Composable
private fun MemoryWidgetConfigScreen(
    initial: MemoryWidgetConfig,
    onSave: (MemoryWidgetConfig) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var mode by remember { mutableStateOf(initial.mode) }
    var maxItems by remember { mutableIntStateOf(initial.maxItems.coerceIn(1, 3)) }
    val selectedIds = remember { mutableStateListOf<String>().apply { addAll(initial.pinnedIds.take(3)) } }
    var entries by remember { mutableStateOf<List<MemoryEntryEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        entries = withContext(Dispatchers.IO) {
            HarmonyDatabase.getInstance(context.applicationContext).memoryDao().getOpenEntriesForWidget()
        }
        val valid = entries.mapTo(hashSetOf()) { it.id }
        selectedIds.removeAll { it !in valid }
        loading = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF160A24), Color(0xFF0D0618), Color(0xFF05020B))
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Text("♥  HARMONY · MERKEN", color = HarmonyPinkSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Das müssen wir uns merken",
                color = HarmonyText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Wählt, was euch direkt auf dem Homescreen begleiten soll.",
                color = HarmonyMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle("INHALT")
            Spacer(Modifier.height(8.dp))
            ChoiceCard(
                title = "Automatisch",
                subtitle = "Immer die neuesten offenen Einträge",
                selected = mode == MemoryWidgetMode.AUTOMATIC,
                onClick = { mode = MemoryWidgetMode.AUTOMATIC }
            )
            Spacer(Modifier.height(8.dp))
            ChoiceCard(
                title = "Bestimmte auswählen",
                subtitle = "Bis zu drei Einträge in eurer Reihenfolge",
                selected = mode == MemoryWidgetMode.PINNED,
                onClick = { mode = MemoryWidgetMode.PINNED }
            )

            Spacer(Modifier.height(18.dp))
            SectionTitle("ANZAHL AUF DEM HOMESCREEN")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..3).forEach { count ->
                    CountChoice(count, maxItems == count) { maxItems = count }
                }
            }

            if (mode == MemoryWidgetMode.PINNED) {
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("EINTRÄGE")
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${selectedIds.size}/3 gewählt",
                        color = if (selectedIds.size == 3) HarmonyPinkSoft else HarmonyMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(
                        Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HarmonyPink)
                    }
                } else if (entries.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        color = Color(0xAA171022),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, HarmonyPurple.copy(alpha = 0.28f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Noch nichts gemerkt.\nFügt zuerst etwas in Harmony hinzu.",
                                color = HarmonyMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            val order = selectedIds.indexOf(entry.id).takeIf { it >= 0 }?.plus(1)
                            MemoryChoiceRow(entry, order) {
                                val existing = selectedIds.indexOf(entry.id)
                                if (existing >= 0) selectedIds.removeAt(existing)
                                else if (selectedIds.size < 3) selectedIds.add(entry.id)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Widget hinzufügen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = HarmonyPurpleLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun ChoiceCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) Color(0xCC32132E) else Color(0xB8171022),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) HarmonyPinkSoft else HarmonyPurple.copy(alpha = 0.28f)
        )
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                (if (selected) "●  " else "○  ") + title,
                color = if (selected) HarmonyPinkSoft else HarmonyText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = HarmonyMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CountChoice(count: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(62.dp).clickable(onClick = onClick),
        color = if (selected) Color(0xCC32132E) else Color(0xB8171022),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) HarmonyPinkSoft else HarmonyPurple.copy(alpha = 0.28f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                count.toString(),
                color = if (selected) HarmonyPinkSoft else HarmonyText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MemoryChoiceRow(entry: MemoryEntryEntity, order: Int?, onClick: () -> Unit) {
    val selected = order != null
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) Color(0xC72A1738) else Color(0xA8171022),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 1.4.dp else 1.dp,
            if (selected) HarmonyPinkSoft.copy(alpha = 0.85f) else HarmonyPurple.copy(alpha = 0.22f)
        )
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) HarmonyPink.copy(alpha = 0.22f) else HarmonyPurple.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    order?.toString() ?: if (entry.kind == MemoryEntryKind.LINK) "↗" else "✦",
                    color = if (selected) HarmonyPinkSoft else HarmonyPurpleLight,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    entry.previewTitle?.takeIf { entry.kind == MemoryEntryKind.LINK && it.isNotBlank() } ?: entry.title,
                    color = HarmonyText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val secondary = if (entry.kind == MemoryEntryKind.LINK) entry.previewSiteName ?: entry.url else entry.body
                if (!secondary.isNullOrBlank()) {
                    Text(
                        secondary,
                        color = HarmonyMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
