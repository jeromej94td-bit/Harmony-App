#!/usr/bin/env python3
"""Apply the production UI localization repair once; safe to run repeatedly in CI."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"


def replace_if_present(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    if old in text:
        path.write_text(text.replace(old, new), encoding="utf-8")


# Central lookup: committed overrides win over stale base catalogs and completeness is truthful.
p = UI / "TranslationCatalog.kt"
text = p.read_text(encoding="utf-8")
if "LOCALIZATION_UPDATES[language]" not in text:
    text = text.replace(
        "object TranslationCatalog {\n\n    fun hasCompletePack(language: AppLanguage): Boolean = true\n",
        '''object TranslationCatalog {\n\n    private val nonCustomerKeys = setOf(\n        "Entwickler Studio Öffnen", "Entwickler-Modus", "🛠️ Entwickler-Modus",\n        "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen",\n        ", listOf(", "aufwaermen", "custom_gourmet_eissorten", "dasoderdas", "disney",\n        "entertainment", "essen", "familie", "games", "harrypotter", "hochzeit", "iPhone",\n        "ichhabenochnie", "kinder", "oder", "parks", "party", "reden", "reisen", "tot",\n        "universal", "unterhaltung", "wer", "werwuerde", "zuhause", "{partner}", "{user}",\n        "☀️", "❤️"\n    )\n\n    fun hasCompletePack(language: AppLanguage): Boolean {\n        if (language == AppLanguage.GERMAN) return true\n        return EXACT_ENGLISH_CONTENT.keys\n            .asSequence()\n            .filterNot { it in nonCustomerKeys || "Entwickler" in it }\n            .all { exact(it, language) != null }\n    }\n'''
    )
    text = text.replace(
        '''    fun exact(german: String, language: AppLanguage): String? {\n        if (language == AppLanguage.GERMAN) return german\n        return when (language) {''',
        '''    fun exact(german: String, language: AppLanguage): String? {\n        if (language == AppLanguage.GERMAN) return german\n        LOCALIZATION_UPDATES[language]?.get(german)?.let { return it }\n        return when (language) {'''
    )
    p.write_text(text, encoding="utf-8")

# The introspection journey must never fall back to English for another selected language.
p = UI / "introspection/IntrospectionStrings.kt"
text = p.read_text(encoding="utf-8")
if "import com.example.ui.TranslationCatalog" not in text:
    text = text.replace("import com.example.ui.AppLanguage\n", "import com.example.ui.AppLanguage\nimport com.example.ui.TranslationCatalog\n")
old = '''    fun tr(key: IntrospectionStringKey, lang: AppLanguage): String {\n        return when (lang) {\n            AppLanguage.GERMAN -> germanStrings[key] ?: key.name\n            AppLanguage.ITALIAN -> italianStrings[key] ?: englishStrings[key] ?: germanStrings[key] ?: key.name\n            else -> englishStrings[key] ?: germanStrings[key] ?: key.name\n        }\n    }'''
new = '''    fun tr(key: IntrospectionStringKey, lang: AppLanguage): String {\n        val german = germanStrings[key] ?: return key.name\n        return when (lang) {\n            AppLanguage.GERMAN -> german\n            AppLanguage.ENGLISH -> englishStrings[key] ?: german\n            AppLanguage.ITALIAN -> italianStrings[key] ?: TranslationCatalog.translate(german, lang) ?: german\n            AppLanguage.FRENCH,\n            AppLanguage.JAPANESE,\n            AppLanguage.POLISH,\n            AppLanguage.SPANISH_LATIN_AMERICA,\n            AppLanguage.SPANISH_SPAIN,\n            AppLanguage.PORTUGUESE_BRAZIL,\n            AppLanguage.PORTUGUESE_PORTUGAL,\n            AppLanguage.DANISH,\n            AppLanguage.NORWEGIAN -> TranslationCatalog.translate(german, lang)\n                ?: englishStrings[key]\n                ?: german\n        }\n    }'''
text = text.replace(old, new)
old = '''    fun m(lang: AppLanguage, de: String, en: String, it: String): String {\n        return when (lang) {\n            AppLanguage.GERMAN -> de\n            AppLanguage.ITALIAN -> it\n            else -> en\n        }\n    }'''
new = '''    fun m(lang: AppLanguage, de: String, en: String, it: String): String {\n        return when (lang) {\n            AppLanguage.GERMAN -> de\n            AppLanguage.ENGLISH -> en\n            AppLanguage.ITALIAN -> it\n            AppLanguage.FRENCH,\n            AppLanguage.JAPANESE,\n            AppLanguage.POLISH,\n            AppLanguage.SPANISH_LATIN_AMERICA,\n            AppLanguage.SPANISH_SPAIN,\n            AppLanguage.PORTUGUESE_BRAZIL,\n            AppLanguage.PORTUGUESE_PORTUGAL,\n            AppLanguage.DANISH,\n            AppLanguage.NORWEGIAN -> TranslationCatalog.translate(de, lang) ?: en\n        }\n    }'''
text = text.replace(old, new)
p.write_text(text, encoding="utf-8")

# Chat: localize visible copy and accessibility labels, including the dynamic report prompt.
p = UI / "screens/ChatScreen.kt"
text = p.read_text(encoding="utf-8")
replacements = {
    'Text("Privater Paar-Chat", color = HarmonyMuted, fontSize = 11.sp)': 'Text(LanguageManager.tr("Privater Paar-Chat", appLanguage), color = HarmonyMuted, fontSize = 11.sp)',
    'contentDescription = "Nutzer melden"': 'contentDescription = LanguageManager.tr("Nutzer melden", appLanguage)',
    'contentDescription = "Bild hinzufügen"': 'contentDescription = LanguageManager.tr("Bild hinzufügen", appLanguage)',
    'contentDescription = "Senden"': 'contentDescription = LanguageManager.tr("Senden", appLanguage)',
    'Text("Meldung vorbereiten", color = HarmonyPink)': 'Text(LanguageManager.tr("Meldung vorbereiten", appLanguage), color = HarmonyPink)',
    'Text("Abbrechen")': 'Text(LanguageManager.tr("Abbrechen", appLanguage))',
    'ChatMessageBubble(message, onImageClick = { fullscreenImagePath = it })': 'ChatMessageBubble(message, appLanguage, onImageClick = { fullscreenImagePath = it })',
    'contentDescription = "Geteiltes Bild",': 'contentDescription = LanguageManager.tr("Geteiltes Bild", appLanguage),',
    'private fun ChatImageFullscreen(path: String, onDismiss: () -> Unit) {': 'private fun ChatImageFullscreen(path: String, appLanguage: String, onDismiss: () -> Unit) {',
    'contentDescription = "Geteiltes Bild im Vollbildmodus",': 'contentDescription = LanguageManager.tr("Geteiltes Bild im Vollbildmodus", appLanguage),',
}
for old, new in replacements.items():
    text = text.replace(old, new)
text = text.replace(
    '''title = { Text("Nutzer melden") },\n            text = { Text("Möchtest du $partnerName melden? Die Meldung wird erst nach deiner Bestätigung vorbereitet.") },''',
    '''title = { Text(LanguageManager.tr("Nutzer melden", appLanguage)) },\n            text = {\n                Text(\n                    LanguageManager.tr(\n                        "Möchtest du {partner} melden? Die Meldung wird erst nach deiner Bestätigung vorbereitet.",\n                        appLanguage\n                    ).replace("{partner}", partnerName)\n                )\n            },'''
)
text = text.replace(
    '''fun ChatMessageBubble(\n    message: ChatMessageEntity,\n    onImageClick: (String) -> Unit = {}\n) {''',
    '''fun ChatMessageBubble(\n    message: ChatMessageEntity,\n    appLanguage: String = "de",\n    onImageClick: (String) -> Unit = {}\n) {'''
)
text = text.replace(
    '''        ChatImageFullscreen(\n            path = path,\n            onDismiss = { fullscreenImagePath = null }\n        )''',
    '''        ChatImageFullscreen(\n            path = path,\n            appLanguage = appLanguage,\n            onDismiss = { fullscreenImagePath = null }\n        )'''
)
p.write_text(text, encoding="utf-8")

# Unanswered questions dialog.
p = UI / "screens/GamesScreen.kt"
text = p.read_text(encoding="utf-8")
text = text.replace('Text("Unbeantwortete Fragen", fontWeight = FontWeight.ExtraBold)', 'Text(LanguageManager.tr("Unbeantwortete Fragen", appLanguage), fontWeight = FontWeight.ExtraBold)')
text = text.replace('Text("${unanswered.size} Fragen warten auf euch", color = HarmonyMuted, fontSize = 12.sp)', 'Text(LanguageManager.tr("{count} Fragen warten auf euch", appLanguage).replace("{count}", unanswered.size.toString()), color = HarmonyMuted, fontSize = 12.sp)')
text = text.replace('Text("Ihr habt bereits alle Fragen beantwortet.", color = HarmonyMuted)', 'Text(LanguageManager.tr("Ihr habt bereits alle Fragen beantwortet.", appLanguage), color = HarmonyMuted)')
text = text.replace('TextButton(onClick = onDismiss) { Text("Schließen") }', 'TextButton(onClick = onDismiss) { Text(LanguageManager.tr("Schließen", appLanguage)) }')
p.write_text(text, encoding="utf-8")

# Category tags shown in cards/moments: normalize internal IDs before translating them.
p = UI / "components/CommonUI.kt"
text = p.read_text(encoding="utf-8")
if "LocalAppLanguage.current.code" not in text:
    if "import com.example.ui.LocalAppLanguage" not in text:
        text = text.replace("import com.example.util.LanguageManager\n", "import com.example.util.LanguageManager\nimport com.example.ui.LocalAppLanguage\n")
    start = text.index("@Composable\nfun CategoryTag(")
    brace = text.index("{", start)
    depth = 1
    end = brace + 1
    while depth:
        if text[end] == "{": depth += 1
        elif text[end] == "}": depth -= 1
        end += 1
    new_category = '''@Composable\nfun CategoryTag(tag: String, modifier: Modifier = Modifier) {\n    val appLanguage = LocalAppLanguage.current.code\n    val category = com.example.data.model.HarmonyPacksData.CATEGORIES.find {\n        it.id.equals(tag, ignoreCase = true) || it.name.equals(tag, ignoreCase = true)\n    }\n\n    val (bg, fg, label) = if (category != null) {\n        val catColor = Color(category.tagColorHex)\n        val localizedCategory = LanguageManager.translateCategory(category, appLanguage)\n        Triple(catColor.copy(alpha = 0.22f), catColor, "${category.emoji} ${localizedCategory.name}")\n    } else {\n        val normalized = when (tag.lowercase()) {\n            "unterhaltung", "entertainment" -> "Unterhaltung"\n            "dasoderdas", "tot", "oder" -> "Das oder das"\n            "hochzeit" -> "Hochzeit"\n            "kinder" -> "Kinder"\n            "reden" -> "Reden vor..."\n            "tiere" -> "Tiere"\n            "fürpaare", "fuerpaare" -> "Für Paare"\n            "party" -> "Party"\n            "wer", "werwuerde" -> "Wer würde eher?"\n            "ichhabenochnie" -> "Ich habe noch nie"\n            "essen" -> "Essen & Genuss"\n            "zuhause" -> "Zuhause & Alltag"\n            "games" -> "Spiele"\n            else -> tag.replaceFirstChar { it.uppercase() }\n        }\n        val localized = LanguageManager.tr(normalized, appLanguage)\n        when (tag.lowercase()) {\n            "unterhaltung", "entertainment" -> Triple(HarmonyPink.copy(alpha = 0.16f), HarmonyPinkSoft, localized)\n            "dasoderdas", "tot", "oder" -> Triple(HarmonyPurple.copy(alpha = 0.18f), HarmonyPurpleLight, localized)\n            "hochzeit" -> Triple(HarmonyGold.copy(alpha = 0.16f), HarmonyGold, localized)\n            "kinder" -> Triple(HarmonyTeal.copy(alpha = 0.16f), HarmonyTeal, localized)\n            "reden" -> Triple(HarmonyBlue.copy(alpha = 0.16f), HarmonyBlue, localized)\n            else -> Triple(Color.White.copy(alpha = 0.12f), HarmonyText, localized)\n        }\n    }\n\n    Box(\n        modifier = modifier\n            .clip(RoundedCornerShape(8.dp))\n            .background(bg)\n            .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(8.dp))\n            .padding(horizontal = 9.dp, vertical = 4.dp)\n    ) {\n        Text(\n            text = label.uppercase(Locale.ROOT),\n            fontSize = 9.5.sp,\n            fontWeight = FontWeight.ExtraBold,\n            color = fg,\n            letterSpacing = 0.4.sp\n        )\n    }\n}'''
    text = text[:start] + new_category + text[end:]
p.write_text(text, encoding="utf-8")

# Seeded/default moments must be localized; user-created unknown text naturally falls through unchanged.
p = UI / "screens/MomentsScreen.kt"
text = p.read_text(encoding="utf-8")
text = text.replace('text = "${moment.emoji} ${moment.title}"', 'text = "${moment.emoji} ${LanguageManager.tr(moment.title, appLanguage)}"')
text = text.replace('text = moment.content,', 'text = LanguageManager.tr(moment.content, appLanguage),')
p.write_text(text, encoding="utf-8")

# Panda either/or: preserve raw answer identity while translating every displayed label/choice.
p = UI / "screens/PandaEitherOrScreen.kt"
text = p.read_text(encoding="utf-8")
if "import com.example.util.LanguageManager" not in text:
    text = text.replace("import com.example.ui.theme.HarmonyText\n", "import com.example.ui.theme.HarmonyText\nimport com.example.util.LanguageManager\n")
if "displayPack = remember(appLanguage)" not in text:
    start = text.index("@Composable\nfun PandaEitherOrScreen(")
    end = text.index("private fun vibrateHighFive", start)
    new_block = r'''@Composable
fun PandaEitherOrScreen(
    profile: ProfileEntity,
    answers: List<AnswerEntity>,
    appLanguage: String = "de",
    onSaveAnswer: (questionIndex: Int, userChoice: String, partnerChoice: String) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawPack = remember { HarmonyPacksData.PACKS.first { it.id == PANDA_EITHER_OR_PACK_ID } }
    val displayPack = remember(appLanguage) { LanguageManager.translatePack(rawPack, appLanguage) }
    val completedBeforeStart = remember {
        answers.asSequence()
            .filter { it.packId == PANDA_EITHER_OR_PACK_ID }
            .filter { EitherOrAnswerCodec.decode(it.answerText) != null }
            .map { it.questionIndex }
            .toSet()
    }
    val questionOrder = remember { rawPack.pairs.indices.filterNot { it in completedBeforeStart }.shuffled() }

    var orderPosition by remember { mutableIntStateOf(0) }
    var step by remember { mutableStateOf(CoupleGameStep.USER_CHOICE) }
    var userChoice by remember { mutableStateOf<String?>(null) }
    var partnerChoice by remember { mutableStateOf<String?>(null) }
    var reactionKey by remember { mutableIntStateOf(0) }

    val questionIndex = questionOrder.getOrNull(orderPosition)
    val rawPair = questionIndex?.let(rawPack.pairs::get)
    val displayPair = questionIndex?.let(displayPack.pairs::get)
    val isMatch = userChoice != null && userChoice == partnerChoice
    val remaining = questionOrder.size - orderPosition

    fun localizedTemplate(source: String, vararg values: Pair<String, String>): String {
        var result = LanguageManager.tr(source, appLanguage)
        values.forEach { (key, value) -> result = result.replace("{$key}", value) }
        return result
    }

    LaunchedEffect(reactionKey) {
        if (reactionKey > 0 && isMatch) {
            delay(610)
            vibrateHighFive(context)
            playHighFiveClap()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF12051D), Color(0xFF260A37), Color(0xFF09020F))))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = LanguageManager.tr("Spiel schließen", appLanguage), tint = HarmonyText)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐼 ${LanguageManager.tr("Entweder oder", appLanguage)}", color = HarmonyText, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        if (questionIndex == null) LanguageManager.tr("Alle Fragen beantwortet", appLanguage)
                        else localizedTemplate("{count} offen · keine Wiederholungen", "count" to remaining.toString()),
                        color = HarmonyMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.height(14.dp))

            if (rawPair == null || displayPair == null || questionIndex == null) {
                CompletedEitherOrCard(rawPack.pairs.size, appLanguage, onExit, Modifier.weight(1f))
            } else {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "couple_game_step",
                    modifier = Modifier.weight(1f)
                ) { currentStep ->
                    when (currentStep) {
                        CoupleGameStep.USER_CHOICE -> CoupleChoicePanel(
                            profile.userName, rawPair, displayPair, appLanguage,
                            localizedTemplate(
                                "Frage {current} von {total}",
                                "current" to (completedBeforeStart.size + orderPosition + 1).toString(),
                                "total" to rawPack.pairs.size.toString()
                            )
                        ) { userChoice = it; step = CoupleGameStep.HANDOVER }

                        CoupleGameStep.HANDOVER -> HandoverPanel(profile.partnerName, appLanguage) {
                            step = CoupleGameStep.PARTNER_CHOICE
                        }

                        CoupleGameStep.PARTNER_CHOICE -> CoupleChoicePanel(
                            profile.partnerName, rawPair, displayPair, appLanguage,
                            LanguageManager.tr("Geheime Auswahl", appLanguage)
                        ) {
                            partnerChoice = it
                            onSaveAnswer(questionIndex, userChoice.orEmpty(), it)
                            reactionKey += 1
                            step = CoupleGameStep.REVEAL
                        }

                        CoupleGameStep.REVEAL -> RevealPanel(
                            profile, userChoice.orEmpty(), partnerChoice.orEmpty(), appLanguage,
                            isMatch, reactionKey
                        ) {
                            orderPosition += 1
                            userChoice = null
                            partnerChoice = null
                            step = CoupleGameStep.USER_CHOICE
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoupleChoicePanel(
    name: String,
    rawPair: Pair<String, String>,
    displayPair: Pair<String, String>,
    appLanguage: String,
    progressLabel: String,
    onChoice: (String) -> Unit
) {
    val chooserText = LanguageManager.tr("{name} entscheidet", appLanguage).replace("{name}", name)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(progressLabel, color = HarmonyPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(chooserText, color = HarmonyText, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(LanguageManager.tr("Der andere schaut kurz weg 🤫", appLanguage), color = HarmonyMuted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        ChoiceCard(displayPair.first, Color(0xFFFF5DAA)) { onChoice(rawPair.first) }
        Spacer(Modifier.height(14.dp))
        Text(LanguageManager.tr("ODER", appLanguage), color = HarmonyMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(14.dp))
        ChoiceCard(displayPair.second, Color(0xFF9E6BFF)) { onChoice(rawPair.second) }
    }
}

@Composable
private fun ChoiceCard(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.36f), HarmonySurface2)), RoundedCornerShape(28.dp))
            .border(1.5.dp, accent.copy(alpha = 0.82f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = HarmonyText, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HandoverPanel(partnerName: String, appLanguage: String, onReady: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(112.dp).background(HarmonyPurple.copy(alpha = 0.28f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = HarmonyPink, modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(LanguageManager.tr("Handy weitergeben", appLanguage), color = HarmonyText, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(LanguageManager.tr("Die erste Antwort bleibt geheim.", appLanguage), color = HarmonyMuted, fontSize = 15.sp)
        Spacer(Modifier.height(34.dp))
        Button(onClick = onReady, colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink), modifier = Modifier.fillMaxWidth().height(58.dp)) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(LanguageManager.tr("{partner} ist bereit", appLanguage).replace("{partner}", partnerName), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RevealPanel(
    profile: ProfileEntity,
    userChoice: String,
    partnerChoice: String,
    appLanguage: String,
    isMatch: Boolean,
    reactionKey: Int,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PandaReactionStage(isMatch, reactionKey, Modifier.fillMaxWidth().height(265.dp))
        Text(
            LanguageManager.tr(if (isMatch) "Volltreffer! High Five 💥" else "Heute verschieden – auch das gehört zu euch", appLanguage),
            color = HarmonyText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        AnswerRevealRow(profile.userName, LanguageManager.tr(userChoice, appLanguage), HarmonyPink)
        Spacer(Modifier.height(9.dp))
        AnswerRevealRow(profile.partnerName, LanguageManager.tr(partnerChoice, appLanguage), HarmonyPurple)
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = if (isMatch) HarmonyPink else HarmonyPurple), modifier = Modifier.fillMaxWidth().height(58.dp)) {
            Text(LanguageManager.tr("Nächste zufällige Frage", appLanguage), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnswerRevealRow(name: String, answer: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth().background(accent.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = accent, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.34f))
        Text(answer, color = HarmonyText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.66f), textAlign = TextAlign.End)
    }
}

@Composable
private fun CompletedEitherOrCard(completed: Int, appLanguage: String, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🐼💕🐼", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(LanguageManager.tr("Ihr kennt jede Entscheidung", appLanguage), color = HarmonyText, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(
            LanguageManager.tr("{count} von {total} Fragen beantwortet", appLanguage)
                .replace("{count}", completed.toString()).replace("{total}", completed.toString()),
            color = HarmonyMuted, fontSize = 15.sp
        )
        Spacer(Modifier.height(30.dp))
        Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)) {
            Text(LanguageManager.tr("Zurück zu den Spielen", appLanguage), fontWeight = FontWeight.Bold)
        }
    }
}

'''
    text = text[:start] + new_block + text[end:]
p.write_text(text, encoding="utf-8")

# Pass the selected language into the Panda game.
p = ROOT / "app/src/main/java/com/example/MainActivity.kt"
text = p.read_text(encoding="utf-8")
if "appLanguage = uiState.appLanguage" not in text[text.find("PandaEitherOrScreen("):]:
    text = text.replace(
        '''                    PandaEitherOrScreen(\n                        profile = uiState.profile,\n                        answers = uiState.answers,''',
        '''                    PandaEitherOrScreen(\n                        profile = uiState.profile,\n                        answers = uiState.answers,\n                        appLanguage = uiState.appLanguage,'''
    )
p.write_text(text, encoding="utf-8")

print("Localization UI repair applied")
