package com.example.ui.screens

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryDefaults
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.util.LanguageManager

internal val SUPPORTED_MEMORY_COLOR_KEYS = listOf("violet", "pink", "orange", "blue", "teal", "purple", "gold")
internal val SUPPORTED_MEMORY_ICON_KEYS = listOf(
    "bookmark",
    "movie",
    "tv",
    "lightbulb",
    "place",
    "sparkles",
    "restaurant",
    "event",
    "favorite",
    "travel"
)

@Composable
fun MemoryCategoryDialog(
    category: MemoryCategoryEntity?,
    categories: List<MemoryCategoryEntity>,
    entryCount: Int,
    appLanguage: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit,
    onUpdate: (String, String, String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDefault = category?.systemKey != null
    val isEditing = category != null
    var name by rememberSaveable(category?.id) { mutableStateOf(category?.customName.orEmpty()) }
    var colorKey by rememberSaveable(category?.id) {
        mutableStateOf(
            category?.colorKey?.takeIf(SUPPORTED_MEMORY_COLOR_KEYS::contains)
                ?: SUPPORTED_MEMORY_COLOR_KEYS.first()
        )
    }
    var iconKey by rememberSaveable(category?.id) {
        mutableStateOf(
            category?.iconKey?.takeIf(SUPPORTED_MEMORY_ICON_KEYS::contains)
                ?: SUPPORTED_MEMORY_ICON_KEYS.first()
        )
    }
    var isEditingName by rememberSaveable(category?.id) { mutableStateOf(false) }
    val moveTargets = remember(category?.id, categories) { categories.filter { it.id != category?.id } }
    val editableTextColor = HarmonyText.toArgb()
    val editableHintColor = HarmonyMuted.toArgb()
    var moveTargetId by rememberSaveable(category?.id, entryCount) {
        mutableStateOf(
            if (entryCount == 0) {
                moveTargets.firstOrNull { it.id == MemoryDefaults.OTHER_ID }?.id
                    ?: moveTargets.firstOrNull()?.id.orEmpty()
            } else {
                ""
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (isDefault) Modifier.heightIn(max = 320.dp)
                    else Modifier.height(660.dp)
                )
                .testTag("memory_category_dialog")
                .semantics {
                    contentDescription = LanguageManager.tr(
                        if (isEditing) "Kategorie bearbeiten" else "Kategorie hinzufügen",
                        appLanguage
                    )
                },
            color = HarmonySurface.copy(alpha = 0.99f),
            contentColor = HarmonyText,
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 10.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, HarmonyPinkSoft.copy(alpha = 0.34f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CategoryDialogHeader(
                    category = category,
                    isEditing = isEditing,
                    isDefault = isDefault,
                    appLanguage = appLanguage,
                    onDismiss = onDismiss
                )

                if (isDefault) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = memoryCategoryLabel(category, appLanguage),
                            color = memoryCategoryColor(category?.colorKey),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (entryCount > 0) {
                            CategoryFieldTitle(LanguageManager.tr("Einträge verschieben nach", appLanguage))
                            moveTargets.forEach { target ->
                                CategoryMoveTarget(
                                    target = target,
                                    selected = moveTargetId == target.id,
                                    appLanguage = appLanguage,
                                    onClick = { moveTargetId = target.id }
                                )
                            }
                        }
                        TextButton(
                            enabled = moveTargetId.isNotBlank(),
                            onClick = { category?.let { onDelete(it.id, moveTargetId) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("memory_category_delete")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(LanguageManager.tr("Löschen", appLanguage))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryFieldTitle(LanguageManager.tr("Name", appLanguage))
                        if (isEditingName) {
                            AndroidView(
                                factory = { context ->
                                    EditText(context).apply {
                                        id = View.generateViewId()
                                        tag = "memory_category_name_view"
                                        hint = LanguageManager.tr("Name", appLanguage)
                                        contentDescription = LanguageManager.tr("Name", appLanguage)
                                        setSingleLine(true)
                                        setText(name)
                                        setSelection(text.length)
                                        setTextColor(editableTextColor)
                                        setHintTextColor(editableHintColor)
                                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                        background = null
                                        val horizontalPadding = (16 * resources.displayMetrics.density).toInt()
                                        setPadding(horizontalPadding, 0, horizontalPadding, 0)
                                        addTextChangedListener(object : TextWatcher {
                                            override fun beforeTextChanged(
                                                value: CharSequence?,
                                                start: Int,
                                                count: Int,
                                                after: Int
                                            ) = Unit

                                            override fun onTextChanged(
                                                value: CharSequence?,
                                                start: Int,
                                                before: Int,
                                                count: Int
                                            ) {
                                                name = value?.toString().orEmpty()
                                            }

                                            override fun afterTextChanged(value: Editable?) = Unit
                                        })
                                        post {
                                            requestFocus()
                                            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                                                ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                                        }
                                    }
                                },
                                update = { field ->
                                    if (field.text.toString() != name) {
                                        field.setText(name)
                                        field.setSelection(field.text.length)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .background(HarmonySurface2.copy(alpha = 0.82f), RoundedCornerShape(16.dp))
                                    .border(1.dp, HarmonyPinkSoft, RoundedCornerShape(16.dp))
                                    .testTag("memory_category_name_editing")
                                    .semantics {
                                        contentDescription = LanguageManager.tr("Name", appLanguage)
                                    }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .background(HarmonySurface2.copy(alpha = 0.82f), RoundedCornerShape(16.dp))
                                    .border(1.dp, HarmonyLine, RoundedCornerShape(16.dp))
                                    .clickable(role = Role.Button) { isEditingName = true }
                                    .testTag("memory_category_name")
                                    .semantics {
                                        contentDescription = LanguageManager.tr("Name", appLanguage)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = name.ifBlank { LanguageManager.tr("Name", appLanguage) },
                                    color = if (name.isBlank()) HarmonyMuted else HarmonyText,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        CategoryFieldTitle(LanguageManager.tr("Farbe", appLanguage))
                        SUPPORTED_MEMORY_COLOR_KEYS.chunked(4).forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    val selected = key == colorKey
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                if (selected) memoryCategoryColor(key).copy(alpha = 0.22f)
                                                else HarmonySurface2,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) memoryCategoryColor(key) else HarmonyLine,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .selectable(
                                                selected = selected,
                                                role = Role.RadioButton,
                                                onClick = { colorKey = key }
                                            )
                                            .testTag("memory_category_color_$key")
                                            .semantics {
                                                this.selected = selected
                                                contentDescription = key
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(memoryCategoryColor(key), CircleShape)
                                                .then(
                                                    if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                                                    else Modifier
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        CategoryFieldTitle(LanguageManager.tr("Symbol", appLanguage))
                        SUPPORTED_MEMORY_ICON_KEYS.chunked(5).forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    val selected = key == iconKey
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                if (selected) HarmonyPink.copy(alpha = 0.18f) else HarmonySurface2,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) HarmonyPinkSoft else HarmonyLine,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .selectable(
                                                selected = selected,
                                                role = Role.RadioButton,
                                                onClick = { iconKey = key }
                                            )
                                            .testTag("memory_category_icon_$key")
                                            .semantics {
                                                this.selected = selected
                                                contentDescription = key
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            memoryCategoryIcon(key),
                                            contentDescription = null,
                                            tint = if (selected) HarmonyPinkSoft else HarmonyMuted,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isEditing && entryCount > 0) {
                            CategoryFieldTitle(LanguageManager.tr("Einträge verschieben nach", appLanguage))
                            moveTargets.forEach { target ->
                                val selected = moveTargetId == target.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .background(
                                            if (selected) memoryCategoryColor(target.colorKey).copy(alpha = 0.20f)
                                            else HarmonySurface2,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) memoryCategoryColor(target.colorKey) else HarmonyLine,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .selectable(
                                            selected = selected,
                                            role = Role.RadioButton,
                                            onClick = { moveTargetId = target.id }
                                        )
                                        .testTag("memory_category_move_${target.id}")
                                        .semantics { this.selected = selected }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        memoryCategoryIcon(target.iconKey),
                                        contentDescription = null,
                                        tint = if (selected) memoryCategoryColor(target.colorKey) else HarmonyMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = memoryCategoryLabel(target, appLanguage),
                                        color = if (selected) HarmonyText else HarmonyMuted
                                    )
                                }
                            }
                        }

                        if (isEditing) {
                            TextButton(
                                enabled = moveTargetId.isNotBlank(),
                                onClick = { onDelete(category.id, moveTargetId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("memory_category_delete")
                            ) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(LanguageManager.tr("Löschen", appLanguage))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(LanguageManager.tr("Abbrechen", appLanguage))
                    }
                    if (!isDefault) {
                        Spacer(Modifier.size(8.dp))
                        Button(
                            enabled = name.isNotBlank(),
                            onClick = {
                                val cleanName = name.trim()
                                if (category == null) {
                                    onCreate(cleanName, colorKey, iconKey)
                                } else {
                                    onUpdate(category.id, cleanName, colorKey, iconKey)
                                }
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("memory_category_save"),
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(LanguageManager.tr("Speichern", appLanguage), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDialogHeader(
    category: MemoryCategoryEntity?,
    isEditing: Boolean,
    isDefault: Boolean,
    appLanguage: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, top = 16.dp, end = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LanguageManager.tr(
                    if (isEditing) "Kategorie bearbeiten" else "Kategorie hinzufügen",
                    appLanguage
                ),
                color = HarmonyText,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (isDefault) {
                Text(
                    text = memoryCategoryLabel(category, appLanguage),
                    color = HarmonyMuted,
                    fontSize = 12.sp
                )
            }
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(48.dp)
                .testTag("memory_category_close")
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = LanguageManager.tr("Schließen", appLanguage),
                tint = HarmonyMuted
            )
        }
    }
}

@Composable
private fun CategoryFieldTitle(text: String) {
    Text(
        text = text,
        color = HarmonyText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CategoryMoveTarget(
    target: MemoryCategoryEntity,
    selected: Boolean,
    appLanguage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(
                if (selected) memoryCategoryColor(target.colorKey).copy(alpha = 0.20f) else HarmonySurface2,
                RoundedCornerShape(14.dp)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) memoryCategoryColor(target.colorKey) else HarmonyLine,
                shape = RoundedCornerShape(14.dp)
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .testTag("memory_category_move_${target.id}")
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            memoryCategoryIcon(target.iconKey),
            contentDescription = null,
            tint = if (selected) memoryCategoryColor(target.colorKey) else HarmonyMuted,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = memoryCategoryLabel(target, appLanguage),
            color = if (selected) HarmonyText else HarmonyMuted
        )
    }
}
