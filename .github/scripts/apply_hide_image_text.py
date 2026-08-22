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
                if (newValue.isBlank() && text.isNotBlank()) {
                    onTextChange(DeveloperDataManager.hideOptionLabel(context, text))
                } else {
                    onTextChange(newValue)
                }
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
                    onTextChange(DeveloperDataManager.hideOptionLabel(context, text))
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
     * Blendet nur den sichtbaren Text aus, ohne die Bildzuordnung zu verlieren.
     * Dafür wird ein interner, nicht sichtbarer Schlüssel erzeugt und dieselbe
     * Bildquelle unter diesem Schlüssel weitergeführt.
     */
    fun hideOptionLabel(context: Context, optionName: String): String {
        val sourceKey = optionName.trim()
        if (sourceKey.isEmpty()) return sourceKey
        if (!DevAssetStore.isUserFacingLabel(sourceKey)) return sourceKey

        val hiddenKey = "img_hidden_${System.currentTimeMillis()}_${DevAssetStore.slug(sourceKey).take(24)}"
        val path = imagePathFor(sourceKey)
        if (!path.isNullOrBlank()) {
            imageOverrides[hiddenKey] = path
            saveData(context)
        } else {
            // Built-in/URL image: preserve lookup for the current session as alias.
            TotImageProvider.setAlias(hiddenKey, sourceKey)
        }
        return hiddenKey
    }
'''
assert old in data, 'setImageFromUri not found'
data = data.replace(old, new, 1)

screen_path.write_text(screen)
data_path.write_text(data)
print('Applied Dev Studio image-text decoupling fix')
