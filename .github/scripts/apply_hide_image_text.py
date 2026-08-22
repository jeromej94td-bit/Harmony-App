from pathlib import Path

screen_path = Path('app/src/main/java/com/example/ui/screens/DevStudioScreen.kt')
data_path = Path('app/src/main/java/com/example/data/DeveloperDataManager.kt')

screen = screen_path.read_text()
data = data_path.read_text()

old = '''private fun OptionSlot(
    text: String,
    imageVersion: Int,
    onTextChange: (String) -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserFacing = DevAssetStore.isUserFacingLabel(text)
    val displayValue = if (isUserFacing) text else ""
'''
new = '''private fun OptionSlot(
    text: String,
    imageVersion: Int,
    onTextChange: (String) -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUserFacing = DevAssetStore.isUserFacingLabel(text)
    val displayValue = if (isUserFacing) text else ""
'''
assert old in screen, 'OptionSlot header not found'
screen = screen.replace(old, new, 1)

old = '''        OutlinedTextField(
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
'''
new = '''        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
                onTextChange(
                    DeveloperDataManager.renameOptionKeepingImage(
                        context = context,
                        oldKey = text,
                        newLabel = newValue
                    )
                )
            },
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
        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = {
                if (text.isNotBlank()) {
                    onTextChange(
                        DeveloperDataManager.renameOptionKeepingImage(
                            context = context,
                            oldKey = text,
                            newLabel = ""
                        )
                    )
                }
            },
            enabled = text.isNotBlank() && isUserFacing,
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Bildtext nicht anzeigen",
                fontSize = 10.5.sp,
                color = HarmonyPurpleLight
            )
        }
'''
assert old in screen, 'OptionSlot text field not found'
screen = screen.replace(old, new, 1)

old = '''    fun setImageFromUri(context: Context, optionName: String, uri: Uri): String? {
        val key = optionName.trim()
        if (key.isEmpty()) return null
        val path = DevAssetStore.importFromUri(context, uri, key) ?: return null
        imageOverrides[key] = path
        saveData(context)
        return path
    }
'''
new = '''    fun setImageFromUri(context: Context, optionName: String, uri: Uri): String? {
        val key = optionName.trim()
        if (key.isEmpty()) return null
        val path = DevAssetStore.importFromUri(context, uri, key) ?: return null
        imageOverrides[key] = path
        saveData(context)
        return path
    }

    /**
     * Changes only the visible label while preserving the image identity.
     * Drive/gallery imports are copied under the new internal lookup key, so
     * typing a new name or hiding the text cannot detach the image anymore.
     */
    fun renameOptionKeepingImage(context: Context, oldKey: String, newLabel: String): String {
        val sourceKey = oldKey.trim()
        val typedLabel = newLabel.trim()

        if (sourceKey.isEmpty()) return typedLabel
        if (typedLabel.equals(sourceKey, ignoreCase = false)) return sourceKey
        if (typedLabel.isEmpty() && !DevAssetStore.isUserFacingLabel(sourceKey)) return sourceKey

        val targetKey = if (typedLabel.isNotEmpty()) {
            typedLabel
        } else {
            "img_hidden_${System.currentTimeMillis()}_${DevAssetStore.slug(sourceKey).take(24)}"
        }

        val path = imagePathFor(sourceKey)
        if (!path.isNullOrBlank()) {
            // Keep the old mapping for backward compatibility/other packs and
            // mirror the same physical image under the new display/internal key.
            imageOverrides[targetKey] = path
            TotImageProvider.setCustomImage(targetKey, path)
        } else {
            // Built-in images and URL mappings can use the provider alias directly.
            TotImageProvider.setAlias(targetKey, sourceKey)
        }

        return targetKey
    }
'''
assert old in data, 'setImageFromUri not found'
data = data.replace(old, new, 1)

screen_path.write_text(screen)
data_path.write_text(data)
print('Applied Dev Studio image-label decoupling fix')
