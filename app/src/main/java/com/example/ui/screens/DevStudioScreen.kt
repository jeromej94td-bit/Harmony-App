package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.DevAssetStore
import com.example.data.DevExporter
import com.example.data.DeveloperDataManager
import com.example.data.LinkEngine
import com.example.data.model.Category
import com.example.data.model.HarmonyPacksData
import com.example.data.model.Question
import com.example.data.model.QuestionPack
import com.example.ui.components.HarmonyCard
import com.example.ui.components.TotImageProvider
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyGold
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonyTeal
import com.example.ui.theme.HarmonyText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// =====================================================================
// Ein Bild im Wartebereich, bevor es zum Paket wird
// =====================================================================
private data class StagedRow(
    val id: Long,
    val uri: Uri?,
    val path: String?,
    val label: String
)

// =====================================================================
// HAUPTSCHIRM
// =====================================================================
@Composable
fun DevStudioScreen(
    onStartPack: (String) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var updateCounter by remember { mutableStateOf(0) }

    val categories = remember(updateCounter) { HarmonyPacksData.CATEGORIES }
    val packs = remember(updateCounter) { HarmonyPacksData.PACKS }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HarmonyBg)
            .padding(bottom = 90.dp)
    ) {
        DevHeader()

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = HarmonySurface,
            contentColor = HarmonyPurpleLight,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = HarmonyPurpleLight,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf(
                "📂 Ordner",
                "🏷️ Kategorien",
                "📝 Spiele",
                "🔗 Ketten",
                "🖼️ Bilder",
                "⚡ Test",
                "📤 Export"
            )
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == idx) HarmonyPurpleLight else HarmonyMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (selectedTab) {
            0 -> DevFolderImportTab(
                onDone = { pack ->
                    updateCounter++
                    onShowToast("🎉 '${pack.title}' angelegt · ${pack.pairs.size} Paare spielbereit")
                    selectedTab = 5
                },
                onShowToast = onShowToast
            )

            1 -> DevCategoriesTab(
                categories = categories,
                onChanged = { msg ->
                    updateCounter++
                    onShowToast(msg)
                }
            )

            2 -> DevPacksTab(
                packs = packs,
                categories = categories,
                onChanged = { msg ->
                    updateCounter++
                    onShowToast(msg)
                },
                onStartPack = onStartPack
            )

            3 -> DevLinkPacksTab(
                updateCounter = updateCounter,
                packs = packs,
                categories = categories,
                onChanged = { msg ->
                    updateCounter++
                    onShowToast(msg)
                },
                onStartPack = onStartPack
            )

            4 -> DevImagesTab(
                updateCounter = updateCounter,
                onChanged = { msg ->
                    updateCounter++
                    onShowToast(msg)
                }
            )

            5 -> DevTestTab(packs = packs, onStartPack = onStartPack)

            6 -> DevExportTab(
                packs = packs,
                onShowToast = onShowToast,
                onChanged = { updateCounter++ }
            )
        }
    }
}

@Composable
private fun DevHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HarmonyPurple.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(HarmonyPurple.copy(alpha = 0.2f))
                    .border(1.dp, HarmonyPurpleLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, null, tint = HarmonyPurpleLight, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Entwickler Studio", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = HarmonyText)
                Text("Ordner rein · Paare raus · Export für AI Studio", fontSize = 11.5.sp, color = HarmonyMuted)
            }
        }
    }
}

// =====================================================================
// TAB 0 — ORDNER-IMPORT
// =====================================================================
@Composable
private fun DevFolderImportTab(
    onDone: (QuestionPack) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var categoryName by remember { mutableStateOf("Eis") }
    var categoryEmoji by remember { mutableStateOf("🍦") }
    var packTitle by remember { mutableStateOf("Eissorten") }
    var packEmoji by remember { mutableStateOf("🍦") }

    val staged = remember { mutableStateListOf<StagedRow>() }
    var nextId by remember { mutableStateOf(0L) }
    var busyText by remember { mutableStateOf<String?>(null) }

    fun addFiles(files: List<DevAssetStore.PickedFile>) {
        val ordered = DeveloperDataManager.orderForPairing(files)
        val stripMarkers = DeveloperDataManager.usesPairMarkers(files)
        ordered.forEach { f ->
            staged.add(
                StagedRow(
                    id = nextId++,
                    uri = f.uri,
                    path = null,
                    label = DevAssetStore.labelFromFileName(f.displayName, stripMarkers)
                )
            )
        }
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busyText = "Ordner wird gelesen…"
            val files = withContext(Dispatchers.IO) {
                DevAssetStore.listImagesInTree(context, treeUri)
            }
            busyText = null
            if (files.isEmpty()) {
                onShowToast("Keine Bilder in diesem Ordner gefunden.")
            } else {
                addFiles(files)
                onShowToast("${files.size} Bilder geladen — Namen prüfen, dann erstellen.")
            }
        }
    }

    val multiPickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            busyText = "Bilder werden gelesen…"
            val files = withContext(Dispatchers.IO) {
                uris.map { DevAssetStore.PickedFile(it, DevAssetStore.displayNameOf(context, it)) }
            }
            busyText = null
            addFiles(files)
            onShowToast("${files.size} Bilder geladen.")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Bilder-Ordner einlesen",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Je zwei Bilder werden ein Paar. Die Reihenfolge unten ist die Reihenfolge im Spiel — " +
                                "verschiebe Zeilen mit den Pfeilen, wenn ein Paar nicht passt.",
                        fontSize = 12.sp,
                        color = HarmonyMuted
                    )
                }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { folderLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ordner", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { multiPickLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Image, null, tint = HarmonyPurpleLight, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Bilder", fontSize = 14.sp, color = HarmonyPurpleLight, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DevField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = "Kategorie (z.B. Eis)",
                        modifier = Modifier.weight(1f)
                    )
                    DevField(
                        value = categoryEmoji,
                        onValueChange = { categoryEmoji = it },
                        label = "Emoji",
                        modifier = Modifier.width(88.dp)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DevField(
                        value = packTitle,
                        onValueChange = { packTitle = it },
                        label = "Titel des Spiels",
                        modifier = Modifier.weight(1f)
                    )
                    DevField(
                        value = packEmoji,
                        onValueChange = { packEmoji = it },
                        label = "Spiel-Emoji",
                        modifier = Modifier.width(88.dp)
                    )
                }
            }

            item {
                val pairCount = staged.size / 2
                val rest = staged.size % 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HarmonySurface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "$pairCount Paare aus ${staged.size} Bildern",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarmonyText
                        )
                        if (rest == 1) {
                            Text(
                                "Ein Bild bleibt übrig und wird ignoriert.",
                                fontSize = 11.sp,
                                color = HarmonyGold
                            )
                        }
                    }
                    if (staged.isNotEmpty()) {
                        TextButton(onClick = { staged.clear() }) {
                            Text("Leeren", color = HarmonyMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            itemsIndexed(staged, key = { _, row -> row.id }) { index, row ->
                if (index % 2 == 0) {
                    Text(
                        "Paar ${index / 2 + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyPurpleLight,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                StagedRowItem(
                    row = row,
                    onLabelChange = { newLabel ->
                        staged[index] = row.copy(label = newLabel)
                    },
                    onUp = {
                        if (index > 0) {
                            val tmp = staged[index - 1]
                            staged[index - 1] = staged[index]
                            staged[index] = tmp
                        }
                    },
                    onDown = {
                        if (index < staged.size - 1) {
                            val tmp = staged[index + 1]
                            staged[index + 1] = staged[index]
                            staged[index] = tmp
                        }
                    },
                    onDelete = { staged.removeAt(index) }
                )
            }

            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HarmonyBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    Button(
                        onClick = {
                            if (staged.size < 2) {
                                onShowToast("Mindestens zwei Bilder auswählen.")
                                return@Button
                            }
                            if (packTitle.isBlank()) {
                                onShowToast("Dem Spiel fehlt noch ein Titel.")
                                return@Button
                            }
                            scope.launch {
                                val items = staged.map {
                                    DeveloperDataManager.StagedImage(
                                        sourceUri = it.uri,
                                        existingPath = it.path,
                                        label = it.label
                                    )
                                }
                                busyText = "Bilder werden gespeichert…"
                                val pack = withContext(Dispatchers.IO) {
                                    DeveloperDataManager.commitImagePack(
                                        context = context,
                                        categoryName = categoryName,
                                        categoryEmoji = categoryEmoji,
                                        packTitle = packTitle,
                                        packEmoji = packEmoji,
                                        items = items,
                                        onProgress = { done, total ->
                                            busyText = "Bild $done von $total…"
                                        }
                                    )
                                }
                                busyText = null
                                staged.clear()
                                onDone(pack)
                            }
                        },
                        enabled = staged.size >= 2 && busyText == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HarmonyPurple,
                            disabledContainerColor = HarmonySurface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("create_image_pack_button")
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Paket erstellen & freischalten",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Dateinamen wie 01a_vanille.jpg / 01b_schoko.jpg werden automatisch als Paar erkannt. " +
                                "Sonst zählt die alphabetische Reihenfolge.",
                        fontSize = 11.sp,
                        color = HarmonyMuted
                    )
                }
            }
        }

        busyText?.let { BusyOverlay(it) }
    }
}

@Composable
private fun StagedRowItem(
    row: StagedRow,
    onLabelChange: (String) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HarmonySurface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = row.uri ?: row.path?.let { File(it) },
            contentDescription = row.label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.Black)
        )
        Spacer(Modifier.width(10.dp))
        OutlinedTextField(
            value = row.label,
            onValueChange = onLabelChange,
            placeholder = { Text("Name (optional - leer lassen für kein Text)", fontSize = 11.5.sp, color = HarmonyMuted) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = HarmonyText),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HarmonyPurpleLight,
                unfocusedBorderColor = HarmonyLine
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        )
        Column {
            IconButton(onClick = onUp, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowUpward, "Nach oben", tint = HarmonyMuted, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDown, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowDownward, "Nach unten", tint = HarmonyMuted, modifier = Modifier.size(16.dp))
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Entfernen", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
        }
    }
}

// =====================================================================
// TAB 1 — SPIELE BEARBEITEN
// =====================================================================
@Composable
private fun DevPacksTab(
    packs: List<QuestionPack>,
    categories: List<Category>,
    onChanged: (String) -> Unit,
    onStartPack: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("all") }
    var editingPack by remember { mutableStateOf<QuestionPack?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    val filtered = remember(packs, searchQuery, filterType) {
        packs.filter { p ->
            val matchQuery = searchQuery.isBlank() ||
                    p.title.contains(searchQuery, true) || p.cat.contains(searchQuery, true)
            val matchType = filterType == "all" || p.type == filterType
            matchQuery && matchType
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Spiel suchen…", color = HarmonyMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = HarmonyMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HarmonyPurpleLight,
                    unfocusedBorderColor = HarmonyLine,
                    focusedContainerColor = HarmonySurface,
                    unfocusedContainerColor = HarmonySurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            )
            IconButton(
                onClick = { creatingNew = true },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HarmonyPurple)
            ) {
                Icon(Icons.Default.Add, "Neues Spiel", tint = Color.White)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            val types = listOf(
                "all" to "Alle",
                "tot" to "⚖️ Das oder Das",
                "quiz" to "🧠 Quiz",
                "disc" to "🗣️ Diskussion"
            )
            items(types) { (key, label) ->
                val isSel = filterType == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) HarmonyPurple else HarmonySurface)
                        .border(1.dp, if (isSel) HarmonyPurpleLight else HarmonyLine, RoundedCornerShape(20.dp))
                        .clickable { filterType = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else HarmonyMuted
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            items(filtered, key = { it.id }) { pack ->
                HarmonyCard(onClick = { editingPack = pack }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val firstOption = pack.pairs.firstOrNull()?.first
                            ?: pack.questions.firstOrNull()?.options?.firstOrNull()
                        if (firstOption != null && pack.type == "tot") {
                            AsyncImage(
                                model = remember(firstOption, TotImageProvider.version) {
                                    TotImageProvider.getImageUrl(firstOption)
                                },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(categories.find { it.id == pack.cat }?.emoji ?: "🎯", fontSize = 15.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    pack.title,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarmonyText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            val count = if (pack.type == "tot") "${pack.pairs.size} Paare"
                            else "${pack.questions.size} Fragen"
                            val ownTag = if (DeveloperDataManager.isEditable(pack.id)) " · eigen" else ""
                            Text(
                                "${pack.type.uppercase()} · $count$ownTag",
                                fontSize = 11.5.sp,
                                color = HarmonyMuted
                            )
                        }

                        IconButton(onClick = { editingPack = pack }) {
                            Icon(Icons.Default.Edit, "Bearbeiten", tint = HarmonyPurpleLight)
                        }
                        if (DeveloperDataManager.isEditable(pack.id)) {
                            IconButton(onClick = {
                                DeveloperDataManager.deletePack(context, pack.id)
                                onChanged("'${pack.title}' gelöscht.")
                            }) {
                                Icon(Icons.Default.Delete, "Löschen", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (creatingNew) {
        EditPackSheet(
            pack = QuestionPack(
                id = "",
                title = "",
                tags = listOf("dasoderdas", "unterhaltung"),
                cat = categories.firstOrNull()?.id ?: "tot",
                topic = "reisen",
                type = "tot",
                questions = emptyList(),
                pairs = listOf("" to "")
            ),
            categories = categories,
            isNew = true,
            onDismiss = { creatingNew = false },
            onSave = { updated ->
                DeveloperDataManager.savePack(context, updated)
                creatingNew = false
                onChanged("'${updated.title}' gespeichert.")
            },
            onTest = null
        )
    }

    editingPack?.let { pack ->
        EditPackSheet(
            pack = pack,
            categories = categories,
            isNew = false,
            onDismiss = { editingPack = null },
            onSave = { updated ->
                DeveloperDataManager.savePack(context, updated)
                editingPack = null
                onChanged("'${updated.title}' gespeichert.")
            },
            onTest = {
                editingPack = null
                onStartPack(pack.id)
            }
        )
    }
}

// =====================================================================
// Vollbild-Editor für ein Paket
// =====================================================================
@Composable
private fun EditPackSheet(
    pack: QuestionPack,
    categories: List<Category>,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (QuestionPack) -> Unit,
    onTest: (() -> Unit)?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(pack.title) }
    var packEmoji by remember { mutableStateOf(pack.emoji) }
    var categoryId by remember { mutableStateOf(pack.cat) }
    var type by remember { mutableStateOf(pack.type) }
    val pairs = remember { mutableStateListOf<Pair<String, String>>().apply { addAll(pack.pairs) } }
    val questions = remember { mutableStateListOf<Question>().apply { addAll(pack.questions) } }

    var imageTarget by remember { mutableStateOf<String?>(null) }
    var imageVersion by remember { mutableStateOf(0) }
    var busyText by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val target = imageTarget
        imageTarget = null
        if (uri == null || target.isNullOrBlank()) return@rememberLauncherForActivityResult
        scope.launch {
            busyText = "Bild wird übernommen…"
            withContext(Dispatchers.IO) {
                DeveloperDataManager.setImageFromUri(context, target, uri)
            }
            busyText = null
            imageVersion++
        }
    }

    val multiImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            busyText = "Bilder werden eingelesen…"
            val files = withContext(Dispatchers.IO) {
                uris.map { DevAssetStore.PickedFile(it, DevAssetStore.displayNameOf(context, it)) }
            }
            val ordered = DeveloperDataManager.orderForPairing(files)
            val stripMarkers = DeveloperDataManager.usesPairMarkers(files)
            
            val namesAndUris = ordered.mapIndexed { idx, f ->
                val rawLabel = DevAssetStore.labelFromFileName(f.displayName, stripMarkers)
                val label = if (rawLabel.isNotEmpty()) rawLabel else "img_${pack.id.ifBlank { "pack" }}_${System.currentTimeMillis()}_${idx}"
                label to f.uri
            }
            
            val newPairs = mutableListOf<Pair<String, String>>()
            for (i in 0 until namesAndUris.size - 1 step 2) {
                val f1 = namesAndUris[i]
                val f2 = namesAndUris[i + 1]
                newPairs.add(f1.first to f2.first)
            }
            
            withContext(Dispatchers.IO) {
                var done = 0
                namesAndUris.forEach { (label, uri) ->
                    if (uri != null) {
                        DeveloperDataManager.setImageFromUri(context, label, uri)
                    }
                    done++
                    busyText = "Speichere Bild $done von ${namesAndUris.size}…"
                }
            }
            
            pairs.addAll(newPairs)
            busyText = null
            imageVersion++
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HarmonyBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HarmonySurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Schließen", tint = HarmonyMuted)
                    }
                    Text(
                        if (isNew) "Neues Spiel" else "Spiel bearbeiten",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText,
                        modifier = Modifier.weight(1f)
                    )
                    onTest?.let {
                        TextButton(onClick = it) {
                            Text("Testen", color = HarmonyTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            val cleanPairs = pairs.filter { 
                                it.first.isNotBlank() || it.second.isNotBlank() || 
                                DeveloperDataManager.imagePathFor(it.first) != null || 
                                DeveloperDataManager.imagePathFor(it.second) != null 
                            }
                            val cleanQuestions = questions.filter { it.q.isNotBlank() }
                            val id = if (pack.id.isBlank()) {
                                DeveloperDataManager.makePackId(title.ifBlank { "neues_spiel" })
                            } else pack.id
                            onSave(
                                pack.copy(
                                    id = id,
                                    title = title.ifBlank { "Neues Spiel" },
                                    cat = categoryId,
                                    type = type,
                                    emoji = packEmoji,
                                    pairs = if (type == "tot") cleanPairs else emptyList(),
                                    questions = if (type != "tot") cleanQuestions else emptyList()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Speichern", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            DevField(
                                value = title,
                                onValueChange = { title = it },
                                label = "Titel",
                                modifier = Modifier.weight(1f)
                            )
                            DevField(
                                value = packEmoji,
                                onValueChange = { packEmoji = it },
                                label = "Emoji",
                                modifier = Modifier.width(88.dp)
                            )
                        }
                    }

                    item {
                        Text("Kategorie", fontSize = 12.sp, color = HarmonyMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories, key = { it.id }) { cat ->
                                val sel = cat.id == categoryId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (sel) HarmonyPurple else HarmonySurface)
                                        .border(
                                            1.dp,
                                            if (sel) HarmonyPurpleLight else HarmonyLine,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { categoryId = cat.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "${cat.emoji} ${cat.name}",
                                        fontSize = 11.5.sp,
                                        color = if (sel) Color.White else HarmonyText
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Spieltyp", fontSize = 12.sp, color = HarmonyMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "tot" to "⚖️ Das oder Das",
                                "quiz" to "🧠 Quiz",
                                "disc" to "🗣️ Diskussion"
                            ).forEach { (key, label) ->
                                val sel = type == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (sel) HarmonyPurple else HarmonySurface)
                                        .clickable { type = key }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.5.sp,
                                        color = if (sel) Color.White else HarmonyMuted
                                    )
                                }
                            }
                        }
                    }

                    if (type == "tot") {
                        item {
                            Text(
                                "Paare — tippe auf ein Bild, um es zu tauschen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyPurpleLight,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        itemsIndexed(pairs) { index, pair ->
                            PairEditor(
                                index = index,
                                pair = pair,
                                imageVersion = imageVersion,
                                onChange = { newPair -> pairs[index] = newPair },
                                onDelete = { pairs.removeAt(index) },
                                onPickImage = { name ->
                                    if (name.isNotBlank()) {
                                        imageTarget = name
                                        imagePicker.launch("image/*")
                                    }
                                }
                            )
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { pairs.add("" to "") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = HarmonyPurpleLight, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Paar", color = HarmonyPurpleLight, fontSize = 13.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { multiImagePicker.launch("image/*") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Image, null, tint = HarmonyPurpleLight, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Galerie", color = HarmonyPurpleLight, fontSize = 13.sp)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { 
                                        val shuffled = pairs.map { 
                                            if (Math.random() > 0.5) it.second to it.first else it
                                        }.shuffled()
                                        pairs.clear()
                                        pairs.addAll(shuffled)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Shuffle, null, tint = HarmonyPurpleLight, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Paare mischen", color = HarmonyPurpleLight, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                "Fragen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyPurpleLight,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        itemsIndexed(questions) { index, q ->
                            QuestionEditor(
                                index = index,
                                question = q,
                                onChange = { updated -> questions[index] = updated },
                                onDelete = { questions.removeAt(index) }
                            )
                        }
                        item {
                            OutlinedButton(
                                onClick = { questions.add(Question(q = "")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null, tint = HarmonyPurpleLight, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Frage hinzufügen", color = HarmonyPurpleLight, fontSize = 13.sp)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }

            busyText?.let { BusyOverlay(it) }
        }
    }
}

@Composable
private fun PairEditor(
    index: Int,
    pair: Pair<String, String>,
    imageVersion: Int,
    onChange: (Pair<String, String>) -> Unit,
    onDelete: () -> Unit,
    onPickImage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HarmonySurface)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paar ${index + 1}", fontSize = 11.sp, color = HarmonyMuted, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "Paar löschen", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionSlot(
                text = pair.first,
                imageVersion = imageVersion,
                onTextChange = { onChange(it to pair.second) },
                onPickImage = {
                    val key = if (pair.first.isBlank()) "img_${System.currentTimeMillis()}_a" else pair.first
                    if (key != pair.first) onChange(key to pair.second)
                    onPickImage(key)
                },
                modifier = Modifier.weight(1f)
            )
            OptionSlot(
                text = pair.second,
                imageVersion = imageVersion,
                onTextChange = { onChange(pair.first to it) },
                onPickImage = {
                    val key = if (pair.second.isBlank()) "img_${System.currentTimeMillis()}_b" else pair.second
                    if (key != pair.second) onChange(pair.first to key)
                    onPickImage(key)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OptionSlot(
    text: String,
    imageVersion: Int,
    onTextChange: (String) -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserFacing = DevAssetStore.isUserFacingLabel(text)
    val displayValue = if (isUserFacing) text else ""

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .border(1.dp, HarmonyLine, RoundedCornerShape(10.dp))
                .clickable { onPickImage() }
        ) {
            if (text.isNotBlank()) {
                AsyncImage(
                    model = remember(text, imageVersion, TotImageProvider.version) {
                        TotImageProvider.getImageUrl(text)
                    },
                    contentDescription = text,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("Bild ändern", fontSize = 9.5.sp, color = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = displayValue,
            onValueChange = onTextChange,
            placeholder = { Text("Name (optional)", fontSize = 11.5.sp, color = HarmonyMuted) },
            singleLine = false,
            maxLines = 3,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, color = HarmonyText),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HarmonyPurpleLight,
                unfocusedBorderColor = HarmonyLine
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuestionEditor(
    index: Int,
    question: Question,
    onChange: (Question) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HarmonySurface)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Frage ${index + 1}", fontSize = 11.sp, color = HarmonyMuted, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "Frage löschen", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
            }
        }
        OutlinedTextField(
            value = question.q,
            onValueChange = { onChange(question.copy(q = it)) },
            placeholder = { Text("Fragetext", fontSize = 13.sp, color = HarmonyMuted) },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HarmonyPurpleLight,
                unfocusedBorderColor = HarmonyLine
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = question.options.joinToString(" | "),
            onValueChange = { raw ->
                val opts = raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                onChange(question.copy(options = opts))
            },
            label = { Text("Antworten, getrennt mit |", fontSize = 11.sp) },
            maxLines = 3,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, color = HarmonyText),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HarmonyPurpleLight,
                unfocusedBorderColor = HarmonyLine
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =====================================================================
// TAB 2 — BILDER
// =====================================================================
@Composable
private fun DevImagesTab(
    updateCounter: Int,
    onChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var filter by remember { mutableStateOf("") }
    var onlyMissing by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf<String?>(null) }
    var busyText by remember { mutableStateOf<String?>(null) }
    var localVersion by remember { mutableStateOf(0) }

    val allNames = remember(updateCounter, localVersion) { DeveloperDataManager.allOptionNames() }
    val shown = remember(allNames, filter, onlyMissing, localVersion) {
        allNames.filter { name ->
            val matchesText = filter.isBlank() || name.contains(filter, true)
            val matchesMissing = !onlyMissing || DeveloperDataManager.imagePathFor(name) == null
            matchesText && matchesMissing
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val name = target
        target = null
        if (uri == null || name.isNullOrBlank()) return@rememberLauncherForActivityResult
        scope.launch {
            busyText = "Bild wird übernommen…"
            val path = withContext(Dispatchers.IO) {
                DeveloperDataManager.setImageFromUri(context, name, uri)
            }
            busyText = null
            localVersion++
            onChanged(if (path != null) "Bild für '$name' gesetzt." else "Bild konnte nicht gelesen werden.")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text(
                    "Bilder & Städte",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                Text(
                    "Jede Option aus allen Spielen. Tippen und ein eigenes Bild aus der Galerie wählen.",
                    fontSize = 12.sp,
                    color = HarmonyMuted
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    placeholder = { Text("Option suchen…", color = HarmonyMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = HarmonyMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarmonyPurpleLight,
                        unfocusedBorderColor = HarmonyLine,
                        focusedContainerColor = HarmonySurface,
                        unfocusedContainerColor = HarmonySurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Switch(
                        checked = onlyMissing,
                        onCheckedChange = { onlyMissing = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = HarmonyPurpleLight)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Nur Optionen ohne eigenes Bild", fontSize = 12.sp, color = HarmonyMuted)
                }
                Text("${shown.size} Einträge", fontSize = 11.sp, color = HarmonyMuted)
                Spacer(Modifier.height(6.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                items(shown, key = { it }) { name ->
                    val ownPath = DeveloperDataManager.imagePathFor(name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HarmonySurface)
                            .clickable {
                                target = name
                                picker.launch("image/*")
                            }
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = remember(name, localVersion, TotImageProvider.version) {
                                TotImageProvider.getImageUrl(name)
                            },
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.Black)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                when {
                                    ownPath == null -> "Standardbild"
                                    ownPath.startsWith("/") -> "Eigenes Bild"
                                    else -> "URL"
                                },
                                fontSize = 11.sp,
                                color = if (ownPath != null) HarmonyGold else HarmonyMuted
                            )
                        }
                        if (DeveloperDataManager.hasUserImage(name)) {
                            IconButton(onClick = {
                                DeveloperDataManager.deleteImageOverride(context, name)
                                localVersion++
                                onChanged("Eigenes Bild für '$name' entfernt.")
                            }) {
                                Icon(Icons.Default.Delete, "Zurücksetzen", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }

        busyText?.let { BusyOverlay(it) }
    }
}

// =====================================================================
// TAB 3 — TEST
// =====================================================================
@Composable
private fun DevTestTab(
    packs: List<QuestionPack>,
    onStartPack: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Text("Schnelltest", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
            Text(
                "Startet ein Spiel im echten Runner — genau so, wie ihr es später spielt.",
                fontSize = 12.sp,
                color = HarmonyMuted
            )
        }
        items(packs, key = { it.id }) { pack ->
            HarmonyCard(onClick = { onStartPack(pack.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pack.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                        Spacer(Modifier.height(2.dp))
                        val details = if (pack.type == "tot") "${pack.pairs.size} Paare"
                        else "${pack.questions.size} Fragen"
                        Text("${pack.cat} · $details", fontSize = 12.sp, color = HarmonyMuted)
                    }
                    Button(
                        onClick = { onStartPack(pack.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Testen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

// =====================================================================
// TAB 4 — EXPORT
// =====================================================================
@Composable
private fun DevExportTab(
    packs: List<QuestionPack>,
    onShowToast: (String) -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var includeImages by remember { mutableStateOf(true) }
    var quality by remember { mutableStateOf(DevExporter.Quality.MITTEL) }
    var busyText by remember { mutableStateOf<String?>(null) }
    var lastResult by remember { mutableStateOf<DevExporter.Result?>(null) }
    var showRestore by remember { mutableStateOf(false) }
    var restoreJson by remember { mutableStateOf("") }

    val ownPacks = remember(packs) { DeveloperDataManager.getAllOwnPacks() }
    val selected = remember { mutableStateListOf<String>() }
    val effectivePacks = remember(ownPacks, selected.toList()) {
        if (selected.isEmpty()) ownPacks else ownPacks.filter { selected.contains(it.id) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ) {
            Text("Export für Google AI Studio", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
            Text(
                "Eine einzige Textdatei mit Paketen und Bildern. AI Studio nimmt keine ZIPs — " +
                        "deshalb stecken die Bilder als Base64 direkt im Kotlin-Code.",
                fontSize = 12.sp,
                color = HarmonyMuted
            )

            Spacer(Modifier.height(14.dp))

            if (ownPacks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HarmonySurface)
                        .padding(16.dp)
                ) {
                    Text(
                        "Noch nichts zum Exportieren. Leg im Tab \"Ordner\" ein erstes Paket an.",
                        fontSize = 13.sp,
                        color = HarmonyMuted
                    )
                }
            } else {
                Text("Pakete (nichts ausgewählt = alle)", fontSize = 12.sp, color = HarmonyMuted)
                Spacer(Modifier.height(6.dp))
                ownPacks.forEach { pack ->
                    val isSel = selected.contains(pack.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) HarmonyPurple.copy(alpha = 0.25f) else HarmonySurface)
                            .clickable {
                                if (isSel) selected.remove(pack.id) else selected.add(pack.id)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isSel) "☑" else "☐", fontSize = 15.sp, color = HarmonyPurpleLight)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pack.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                            Text(
                                "${pack.pairs.size} Paare · ${pack.questions.size} Fragen",
                                fontSize = 11.sp,
                                color = HarmonyMuted
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = includeImages,
                    onCheckedChange = { includeImages = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = HarmonyPurpleLight)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Bilder mitschicken", fontSize = 13.sp, color = HarmonyText, fontWeight = FontWeight.Bold)
                    Text("Aus: nur Texte, Datei bleibt winzig", fontSize = 11.sp, color = HarmonyMuted)
                }
            }

            if (includeImages) {
                Spacer(Modifier.height(10.dp))
                Text("Bildgröße im Export", fontSize = 12.sp, color = HarmonyMuted)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DevExporter.Quality.values().forEach { q ->
                        val sel = quality == q
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) HarmonyPurple else HarmonySurface)
                                .clickable { quality = q }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                q.label,
                                fontSize = 11.sp,
                                color = if (sel) Color.White else HarmonyMuted,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (effectivePacks.isEmpty()) {
                        onShowToast("Keine Pakete zum Exportieren.")
                        return@Button
                    }
                    scope.launch {
                        busyText = "Export wird gebaut…"
                        val result = withContext(Dispatchers.IO) {
                            DevExporter.build(
                                context = context,
                                packs = effectivePacks,
                                includeImages = includeImages,
                                quality = quality,
                                onProgress = { done, total -> busyText = "Bild $done von $total…" }
                            )
                        }
                        val file = withContext(Dispatchers.IO) {
                            DevExporter.writeToFile(
                                context,
                                DevExporter.suggestFileName("harmony_content"),
                                result.text
                            )
                        }
                        busyText = null
                        lastResult = result
                        DevExporter.shareFile(context, file)
                    }
                },
                enabled = busyText == null,
                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Für AI Studio exportieren",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        busyText = "Projekt wird als ZIP gepackt…"
                        val zipFile = withContext(Dispatchers.IO) {
                            DevExporter.exportFullProjectZip(context)
                        }
                        busyText = null
                        onShowToast("🎉 Vollständiges Android Studio Projekt vorbereitet!")
                        DevExporter.shareFile(context, zipFile, mime = "application/zip")
                    }
                },
                enabled = busyText == null,
                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurpleLight),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Folder, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Kompletten Code exportieren",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            lastResult?.let { r ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HarmonySurface)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "Fertig: ${r.packCount} Pakete · ${r.imageCount} Bilder · " +
                                    "${r.approxBytes / 1024} kB",
                            fontSize = 12.5.sp,
                            color = HarmonyText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "In AI Studio hochladen und schreiben: \"Ersetze GeneratedHarmonyContent.kt " +
                                    "komplett durch den Inhalt dieser Datei.\"",
                            fontSize = 11.5.sp,
                            color = HarmonyMuted
                        )
                        if (r.approxBytes > 3_000_000) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Über 3 MB — nimm eine kleinere Bildgröße oder exportiere die Pakete einzeln.",
                                fontSize = 11.5.sp,
                                color = HarmonyGold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val paths = DeveloperDataManager.getImageOverrides().values
                        .filter { it.startsWith("/") }
                    if (paths.isEmpty()) {
                        onShowToast("Keine eigenen Bilder vorhanden.")
                    } else {
                        DevExporter.shareImages(context, paths)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.Image, null, tint = HarmonyPurpleLight, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Bilder einzeln teilen", color = HarmonyPurpleLight, fontSize = 13.sp)
            }

            Spacer(Modifier.height(22.dp))
            Text("Vollständiges Projekt-Backup (JSON)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
            Text(
                "Enthält alle Spiele, Ketten, Kategorien UND alle eingebetteten Bilder als Base64. Ideal zur Sicherung, Übertragung & Wiederherstellung.",
                fontSize = 11.5.sp,
                color = HarmonyMuted
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            busyText = "JSON Backup wird vorbereitet…"
                            val json = withContext(Dispatchers.IO) {
                                DeveloperDataManager.exportProjectJson(context, includeImages = true)
                            }
                            DevExporter.copyToClipboard(context, "Harmony Backup", json)
                            busyText = null
                            onShowToast("🎉 JSON inkl. Bildern & Ketten kopiert!")
                        }
                    },
                    enabled = busyText == null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Kopieren", color = HarmonyPurpleLight, fontSize = 12.5.sp)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            busyText = "JSON Backup Datei wird erstellt…"
                            val json = withContext(Dispatchers.IO) {
                                DeveloperDataManager.exportProjectJson(context, includeImages = true)
                            }
                            val file = withContext(Dispatchers.IO) {
                                DevExporter.writeToFile(
                                    context,
                                    DevExporter.suggestFileName("harmony_backup").replace(".txt", ".json"),
                                    json
                                )
                            }
                            busyText = null
                            DevExporter.shareFile(context, file, mime = "application/json")
                        }
                    },
                    enabled = busyText == null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Share, null, tint = HarmonyPurpleLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("JSON teilen", color = HarmonyPurpleLight, fontSize = 12.5.sp)
                }

                OutlinedButton(
                    onClick = { showRestore = !showRestore },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Einspielen", color = HarmonyPurpleLight, fontSize = 12.5.sp)
                }
            }

            if (showRestore) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = restoreJson,
                    onValueChange = { restoreJson = it },
                    placeholder = { Text("JSON oder Export-Inhalt (mit Base64) hier einfügen", fontSize = 12.sp, color = HarmonyMuted) },
                    minLines = 4,
                    maxLines = 8,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = HarmonyText
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarmonyPurpleLight,
                        unfocusedBorderColor = HarmonyLine
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        scope.launch {
                            busyText = "Daten werden eingespielt…"
                            val count = withContext(Dispatchers.IO) {
                                DeveloperDataManager.importProjectJson(context, restoreJson, false)
                            }
                            restoreJson = ""
                            showRestore = false
                            busyText = null
                            onChanged()
                            onShowToast("🎉 $count Pakete/Ketten & Bilder erfolgreich eingespielt!")
                        }
                    },
                    enabled = busyText == null && restoreJson.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Inhalt & Bilder jetzt einspielen", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        busyText?.let { BusyOverlay(it) }
    }
}

// =====================================================================
// TAB 2 — KETTEN-SPIELE (DYNAMISCHE BAUANLEITUNGEN)
// =====================================================================
@Composable
private fun DevLinkPacksTab(
    updateCounter: Int,
    packs: List<QuestionPack>,
    categories: List<Category>,
    onChanged: (String) -> Unit,
    onStartPack: (String) -> Unit
) {
    val context = LocalContext.current
    var activeLinkPackForEdit by remember { mutableStateOf<LinkEngine.LinkPack?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val linkPacks = remember(updateCounter) {
        DeveloperDataManager.getAllLinkPacks()
    }

    val regularPacks = remember(updateCounter) {
        packs.filter { !LinkEngine.isLinkPack(it.id) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔗 Ketten-Spiele", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                    Text("Pakete mit dynamischen Bauanleitungen, die sich aus bisherigen Antworten zusammensetzen.", fontSize = 12.sp, color = HarmonyMuted)
                }
                Button(
                    onClick = {
                        isCreatingNew = true
                        activeLinkPackForEdit = LinkEngine.LinkPack(
                            id = "link_" + System.currentTimeMillis(),
                            title = "Neue Kette",
                            cat = "tot",
                            steps = listOf(
                                LinkEngine.LinkStep(
                                    templateA = "1 Jahr lang in {}",
                                    slotA = LinkEngine.LinkSlot(
                                        source = LinkEngine.SRC_PICKED,
                                        packId = regularPacks.firstOrNull()?.id ?: ""
                                    ),
                                    templateB = "6 Monate im {}",
                                    slotB = LinkEngine.LinkSlot(
                                        source = LinkEngine.SRC_OPTION,
                                        packId = regularPacks.getOrNull(1)?.id ?: (regularPacks.firstOrNull()?.id ?: "")
                                    ),
                                    caption = "Weil du {A} gewählt hast …"
                                )
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Kette bauen", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (linkPacks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HarmonySurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔗", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Noch keine Ketten angelegt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                        Spacer(Modifier.height(4.dp))
                        Text("Erstelle ein Ketten-Paket, um Fragen dynamisch mit bisherigen Antworten zu verknüpfen.", fontSize = 12.sp, color = HarmonyMuted, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(linkPacks, key = { it.id }) { linkPack ->
                HarmonyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(linkPack.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                                Text("${linkPack.steps.size} Schritt(e)", fontSize = 12.sp, color = HarmonyPurpleLight)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        DeveloperDataManager.saveLinkPack(context, linkPack)
                                        onChanged("Kette gestartet")
                                        onStartPack(linkPack.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Spielen", fontSize = 12.sp)
                                }
                                IconButton(onClick = {
                                    isCreatingNew = false
                                    activeLinkPackForEdit = linkPack
                                }) {
                                    Icon(Icons.Default.Edit, "Bearbeiten", tint = HarmonyMuted)
                                }
                                IconButton(onClick = {
                                    DeveloperDataManager.deleteLinkPack(context, linkPack.id)
                                    onChanged("Kette '${linkPack.title}' gelöscht.")
                                }) {
                                    Icon(Icons.Default.Delete, "Löschen", tint = HarmonyPink)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        // Show step previews
                        linkPack.steps.forEachIndexed { sIdx, step ->
                            val (resA, resB, cap) = LinkEngine.previewStep(step, regularPacks)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    if (cap.isNotBlank()) {
                                        Text(cap, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HarmonyGold)
                                    }
                                    Text("• A: ${resA.display} (Bild: ${resA.imageKey})", fontSize = 11.sp, color = HarmonyText)
                                    Text("• B: ${resB.display} (Bild: ${resB.imageKey})", fontSize = 11.sp, color = HarmonyMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }

    activeLinkPackForEdit?.let { packToEdit ->
        DevLinkPackEditorDialog(
            linkPack = packToEdit,
            regularPacks = regularPacks,
            onDismiss = { activeLinkPackForEdit = null },
            onSaveAndTest = { savedPack ->
                DeveloperDataManager.saveLinkPack(context, savedPack)
                activeLinkPackForEdit = null
                onChanged("Kette '${savedPack.title}' gespeichert.")
                onStartPack(savedPack.id)
            }
        )
    }
}

@Composable
private fun DevLinkPackEditorDialog(
    linkPack: LinkEngine.LinkPack,
    regularPacks: List<QuestionPack>,
    onDismiss: () -> Unit,
    onSaveAndTest: (LinkEngine.LinkPack) -> Unit
) {
    var title by remember { mutableStateOf(linkPack.title) }
    var steps by remember { mutableStateOf(linkPack.steps) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = HarmonyBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, HarmonyLine)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔗 Ketten-Bauer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HarmonyText)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Schließen", tint = HarmonyMuted)
                    }
                }

                Spacer(Modifier.height(8.dp))

                DevField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Titel des Ketten-Spiels",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(steps) { idx, step ->
                        HarmonyCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Schritt ${idx + 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HarmonyPurpleLight)
                                    Row {
                                        if (idx > 0) {
                                            IconButton(onClick = {
                                                val mutable = steps.toMutableList()
                                                val tmp = mutable[idx]
                                                mutable[idx] = mutable[idx - 1]
                                                mutable[idx - 1] = tmp
                                                steps = mutable
                                            }) {
                                                Icon(Icons.Default.ArrowUpward, "Nach oben", tint = HarmonyMuted, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        if (idx < steps.lastIndex) {
                                            IconButton(onClick = {
                                                val mutable = steps.toMutableList()
                                                val tmp = mutable[idx]
                                                mutable[idx] = mutable[idx + 1]
                                                mutable[idx + 1] = tmp
                                                steps = mutable
                                            }) {
                                                Icon(Icons.Default.ArrowDownward, "Nach unten", tint = HarmonyMuted, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        if (steps.size > 1) {
                                            IconButton(onClick = {
                                                steps = steps.filterIndexed { i, _ -> i != idx }
                                            }) {
                                                Icon(Icons.Default.Delete, "Löschen", tint = HarmonyPink, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                DevField(
                                    value = step.caption,
                                    onValueChange = { newCap ->
                                        val mutable = steps.toMutableList()
                                        mutable[idx] = mutable[idx].copy(caption = newCap)
                                        steps = mutable
                                    },
                                    label = "Kopfzeile (z.B. Weil du {A} gewählt hast …)",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                // --- Seite A ---
                                Text("Linke Karte (Seite A)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HarmonyGold)
                                DevField(
                                    value = step.templateA,
                                    onValueChange = { newT ->
                                        val mutable = steps.toMutableList()
                                        mutable[idx] = mutable[idx].copy(templateA = newT)
                                        steps = mutable
                                    },
                                    label = "Satzmuster (z.B. 1 Jahr lang in {})",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(6.dp))
                                DevSlotEditor(
                                    slot = step.slotA,
                                    regularPacks = regularPacks,
                                    onSlotChanged = { newSlot ->
                                        val mutable = steps.toMutableList()
                                        mutable[idx] = mutable[idx].copy(slotA = newSlot)
                                        steps = mutable
                                    }
                                )

                                Spacer(Modifier.height(12.dp))

                                // --- Seite B ---
                                Text("Rechte Karte (Seite B)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HarmonyGold)
                                DevField(
                                    value = step.templateB,
                                    onValueChange = { newT ->
                                        val mutable = steps.toMutableList()
                                        mutable[idx] = mutable[idx].copy(templateB = newT)
                                        steps = mutable
                                    },
                                    label = "Satzmuster (z.B. 6 Monate im {})",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(6.dp))
                                DevSlotEditor(
                                    slot = step.slotB,
                                    regularPacks = regularPacks,
                                    onSlotChanged = { newSlot ->
                                        val mutable = steps.toMutableList()
                                        mutable[idx] = mutable[idx].copy(slotB = newSlot)
                                        steps = mutable
                                    }
                                )

                                Spacer(Modifier.height(10.dp))

                                // Live Preview Box for Step
                                val (resA, resB, previewCap) = LinkEngine.previewStep(step, regularPacks)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(HarmonySurface)
                                        .border(1.dp, HarmonyLine, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("Vorschau:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HarmonyMuted)
                                        if (previewCap.isNotBlank()) {
                                            Text(previewCap, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HarmonyGold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("A: ${resA.display}", fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                                            Text("B: ${resB.display}", fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                steps = steps + LinkEngine.LinkStep(
                                    templateA = "{}",
                                    slotA = LinkEngine.LinkSlot(source = LinkEngine.SRC_PICKED, packId = regularPacks.firstOrNull()?.id ?: ""),
                                    templateB = "{}",
                                    slotB = LinkEngine.LinkSlot(source = LinkEngine.SRC_OPTION, packId = regularPacks.firstOrNull()?.id ?: ""),
                                    caption = ""
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Neuen Schritt hinzufügen", color = HarmonyPurpleLight)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Abbrechen", color = HarmonyMuted)
                    }
                    Button(
                        onClick = {
                            val saved = linkPack.copy(
                                title = title.ifBlank { "Unbenannte Kette" },
                                steps = steps
                            )
                            onSaveAndTest(saved)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Speichern & testen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DevSlotEditor(
    slot: LinkEngine.LinkSlot,
    regularPacks: List<QuestionPack>,
    onSlotChanged: (LinkEngine.LinkSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HarmonySurface)
            .padding(8.dp)
    ) {
        Text("Quelle wählen:", fontSize = 11.sp, color = HarmonyMuted)
        Spacer(Modifier.height(4.dp))

        // Segmented control for sources
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val sources = listOf(
                LinkEngine.SRC_PICKED to "Gewählt",
                LinkEngine.SRC_DROPPED to "Verworfen",
                LinkEngine.SRC_OPTION to "Feste Option",
                LinkEngine.SRC_TEXT to "Freier Text"
            )
            sources.forEach { (srcKey, srcLabel) ->
                val isSelected = slot.source == srcKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) HarmonyPurple else Color.White.copy(alpha = 0.05f))
                        .clickable { onSlotChanged(slot.copy(source = srcKey)) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        srcLabel,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else HarmonyMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        if (slot.source == LinkEngine.SRC_TEXT) {
            DevField(
                value = slot.text,
                onValueChange = { onSlotChanged(slot.copy(text = it)) },
                label = "Freier Text",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Pick pack
            Text("Quell-Paket:", fontSize = 11.sp, color = HarmonyMuted)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(regularPacks, key = { it.id }) { p ->
                    val sel = slot.packId == p.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) HarmonyPurpleLight.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (sel) HarmonyPurpleLight else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onSlotChanged(slot.copy(packId = p.id, pairIndex = 0)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(p.title, fontSize = 11.sp, color = if (sel) Color.White else HarmonyMuted)
                    }
                }
            }

            val selectedPack = regularPacks.find { it.id == slot.packId }
            if (selectedPack != null && selectedPack.pairs.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Paar / Option:", fontSize = 11.sp, color = HarmonyMuted)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    item {
                        val sel = slot.pairIndex == -1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) HarmonyGold.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                .clickable { onSlotChanged(slot.copy(pairIndex = -1)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Beliebiges / letztes Paar", fontSize = 11.sp, color = if (sel) Color.White else HarmonyMuted)
                        }
                    }
                    itemsIndexed(selectedPack.pairs) { pIdx, pair ->
                        val sel = slot.pairIndex == pIdx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) HarmonyPurpleLight.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                .clickable { onSlotChanged(slot.copy(pairIndex = pIdx)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${pIdx + 1}: ${pair.first} / ${pair.second}", fontSize = 11.sp, color = if (sel) Color.White else HarmonyMuted)
                        }
                    }
                }

                if (slot.source == LinkEngine.SRC_OPTION) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Seite:", fontSize = 11.sp, color = HarmonyMuted)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (slot.side == 0) HarmonyPurple else Color.White.copy(alpha = 0.05f))
                                .clickable { onSlotChanged(slot.copy(side = 0)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Linke Option (0)", fontSize = 10.sp, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (slot.side == 1) HarmonyPurple else Color.White.copy(alpha = 0.05f))
                                .clickable { onSlotChanged(slot.copy(side = 1)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Rechte Option (1)", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// Kleinteile
// =====================================================================
@Composable
private fun DevField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HarmonyPurpleLight,
            unfocusedBorderColor = HarmonyLine,
            focusedContainerColor = HarmonySurface,
            unfocusedContainerColor = HarmonySurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}

@Composable
private fun BusyOverlay(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = HarmonyPurpleLight)
            Spacer(Modifier.height(12.dp))
            Text(text, fontSize = 13.sp, color = Color.White)
        }
    }
}
@Composable
fun DevCategoriesTab(
    categories: List<Category>,
    onChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<Category?>(null) }
    
    val ownCategories = categories.filter { c -> 
        DeveloperDataManager._customCategories.any { it.id == c.id } 
    }
    val generatedCategories = categories.filter { c ->
        DeveloperDataManager.getGeneratedCategories().any { it.id == c.id } && 
        DeveloperDataManager._customCategories.none { it.id == c.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Eigene Kategorien",
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (ownCategories.isEmpty()) {
                item {
                    Text(
                        text = "Noch keine eigenen Kategorien vorhanden.",
                        color = HarmonyMuted,
                        fontSize = 13.sp
                    )
                }
            }
            items(ownCategories, key = { it.id }) { cat ->
                CategoryEditCard(
                    category = cat,
                    onEdit = {
                        editCategory = cat
                        showDialog = true
                    },
                    onDelete = {
                        DeveloperDataManager.deleteCategory(context, cat.id)
                        DeveloperDataManager.syncWithHarmonyData()
                        onChanged("Kategorie gelöscht")
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                Text(
                    text = "Generierte Kategorien",
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(generatedCategories, key = { it.id }) { cat ->
                CategoryEditCard(
                    category = cat,
                    onEdit = {
                        editCategory = cat
                        showDialog = true
                    },
                    onDelete = null // Generierte Kategorien können nicht direkt gelöscht werden
                )
            }
        }

        Button(
            onClick = {
                editCategory = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurpleLight),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Kategorie hinzufügen", tint = Color.White)
        }
    }

    if (showDialog) {
        CategoryEditDialog(
            category = editCategory,
            onDismiss = { showDialog = false },
            onSave = { name, emoji ->
                val newCat = DeveloperDataManager.addOrUpdateCategory(context, name, emoji)
                DeveloperDataManager.syncWithHarmonyData()
                showDialog = false
                if (editCategory == null) {
                    onChanged("Kategorie '${newCat.name}' erstellt")
                } else {
                    onChanged("Kategorie '${newCat.name}' aktualisiert")
                }
            }
        )
    }
}

@Composable
fun CategoryEditCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HarmonySurface)
            .border(1.dp, HarmonyLine, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.name, fontWeight = FontWeight.Bold, color = HarmonyText)
            Text(text = category.id, fontSize = 11.sp, color = HarmonyMuted)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = HarmonyMuted)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = HarmonyPink)
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var emoji by remember { mutableStateOf(category?.emoji ?: "🎯") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = HarmonySurface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (category == null) "Neue Kategorie" else "Kategorie bearbeiten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarmonyPurpleLight,
                        unfocusedBorderColor = HarmonyLine,
                        focusedTextColor = HarmonyText,
                        unfocusedTextColor = HarmonyText,
                        focusedLabelColor = HarmonyPurpleLight
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji (z.B. 🚀)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarmonyPurpleLight,
                        unfocusedBorderColor = HarmonyLine,
                        focusedTextColor = HarmonyText,
                        unfocusedTextColor = HarmonyText,
                        focusedLabelColor = HarmonyPurpleLight
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", color = HarmonyMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, emoji)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurpleLight)
                    ) {
                        Text("Speichern", color = Color.White)
                    }
                }
            }
        }
    }
}
