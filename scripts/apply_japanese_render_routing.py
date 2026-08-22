#!/usr/bin/env python3
"""Apply the reviewed per-render localization routing fixes.

The migration is deliberately exact and idempotent. It fails when neither the old nor the
expected new source is present, preventing a broad regex rewrite from silently damaging UI.
"""
from __future__ import annotations

import argparse
from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> tuple[str, bool]:
    if new in text:
        return text, False
    if old not in text:
        raise RuntimeError(f"Expected source not found for {label}")
    return text.replace(old, new, 1), True


def patch_file(root: Path, rel: str, replacements: list[tuple[str, str, str]]) -> bool:
    path = root / rel
    source = path.read_text(encoding="utf-8")
    changed = False
    for old, new, label in replacements:
        source, did_change = replace_once(source, old, new, f"{rel}: {label}")
        changed = changed or did_change
    if changed:
        path.write_text(source, encoding="utf-8")
        print(f"patched {rel}")
    else:
        print(f"already patched {rel}")
    return changed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()
    root = args.root.resolve()

    changed = False

    # PicShare has a nested appText(if/else) expression. The normal 300-character lookback is
    # deliberately conservative for the rest of the app, but it cut off the second real widget
    # branch ("Harmony PicShare · bereit"). Extend only this provider's lookback so both render
    # branches remain individual canonical occurrences.
    changed |= patch_file(root, "scripts/visible_copy_complete.py", [
        (
            '            before = source[max(0, start - 360):start]\n            if not any(call in before for call in NON_COMPOSE_RENDER_CALLS):\n                continue\n            nearest = max((before.rfind(call) for call in NON_COMPOSE_RENDER_CALLS), default=-1)\n            if nearest < 0 or len(before) - nearest > 300:\n                continue',
            '            is_picshare_widget = rel.endswith("/widget/PicShareWidgetProvider.kt")\n            lookback = 760 if is_picshare_widget else 360\n            max_distance = 700 if is_picshare_widget else 300\n            before = source[max(0, start - lookback):start]\n            if not any(call in before for call in NON_COMPOSE_RENDER_CALLS):\n                continue\n            nearest = max((before.rfind(call) for call in NON_COMPOSE_RENDER_CALLS), default=-1)\n            if nearest < 0 or len(before) - nearest > max_distance:\n                continue',
            "PicShare nested widget branch lookback",
        ),
    ])

    changed |= patch_file(root, "app/src/main/java/com/example/ui/screens/GamesScreen.kt", [
        ('contentDescription = "Suchfeld löschen"', 'contentDescription = LanguageManager.tr("Suchfeld löschen", appLanguage)', "clear-search a11y"),
        ('contentDescription = "Suche schließen"', 'contentDescription = LanguageManager.tr("Suche schließen", appLanguage)', "close-search a11y"),
    ])

    changed |= patch_file(root, "app/src/main/java/com/example/ui/screens/ChatScreen.kt", [
        ('Text(message.text, fontSize = 14.sp, color = Color.White, lineHeight = 19.sp)', 'Text(LanguageManager.tr(message.text, appLanguage), fontSize = 14.sp, color = Color.White, lineHeight = 19.sp)', "seeded chat copy"),
        ('contentDescription = "Vollbild schließen"', 'contentDescription = LanguageManager.tr("Vollbild schließen", appLanguage)', "fullscreen close a11y"),
    ])

    changed |= patch_file(root, "app/src/main/java/com/example/ui/components/CommonUI.kt", [
        ('contentDescription = "Refresh"', 'contentDescription = LanguageManager.tr("Refresh", LocalAppLanguage.current.code)', "refresh a11y"),
    ])

    changed |= patch_file(root, "app/src/main/java/com/example/ui/screens/IntrospectionGameScreen.kt", [
        ('contentDescription = "Hinweis"', 'contentDescription = com.example.ui.contentText("Hinweis")', "introspection hint a11y"),
    ])

    changed |= patch_file(root, "app/src/main/java/com/example/ui/screens/HomeScreen.kt", [
        ('Text("PicShare für euch", color = HarmonyText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)', 'Text(com.example.ui.contentText("PicShare für euch"), color = HarmonyText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)', "PicShare title"),
        ('Text("${pics.size} Bilder · auf diesem Gerät bereit", color = HarmonyMuted, fontSize = 11.5.sp)', 'Text(com.example.ui.contentText("${pics.size} Bilder · auf diesem Gerät bereit"), color = HarmonyMuted, fontSize = 11.5.sp)', "PicShare count"),
        ('label = "Bilder",\n                    icon = Icons.Default.AddPhotoAlternate', 'label = com.example.ui.contentText("Bilder"),\n                    icon = Icons.Default.AddPhotoAlternate', "home pictures action"),
        ('label = "Widget",\n                    icon = Icons.Default.Widgets', 'label = com.example.ui.contentText("Widget"),\n                    icon = Icons.Default.Widgets', "home widget action"),
        ('label = "Status",\n                    icon = Icons.Default.Home', 'label = com.example.ui.contentText("Status"),\n                    icon = Icons.Default.Home', "home status action"),
        ('Text(\n                "Partner-Synchronisierung folgt mit der späteren Verknüpfung · beide dürfen bearbeiten",', 'Text(\n                com.example.ui.contentText("Partner-Synchronisierung folgt mit der späteren Verknüpfung · beide dürfen bearbeiten"),', "PicShare sync note"),
        ('Text("PicShare Widget", fontWeight = FontWeight.ExtraBold)', 'Text(com.example.ui.contentText("PicShare Widget"), fontWeight = FontWeight.ExtraBold)', "PicShare dialog title"),
        ('Text("Kompakt einrichten · Wechsel alle 6 Sekunden", color = HarmonyMuted, fontSize = 11.sp)', 'Text(com.example.ui.contentText("Kompakt einrichten · Wechsel alle 6 Sekunden"), color = HarmonyMuted, fontSize = 11.sp)', "PicShare dialog subtitle"),
        ('Text("$selectedCount von ${pics.size} Bildern im Widget", fontWeight = FontWeight.Bold)', 'Text(com.example.ui.contentText("$selectedCount von ${pics.size} Bildern im Widget"), fontWeight = FontWeight.Bold)', "PicShare selected count"),
        ('Text("${profile.partnerName}: Verknüpfung folgt später", color = HarmonyMuted, fontSize = 11.sp)', 'Text(com.example.ui.contentText("${profile.partnerName}: Verknüpfung folgt später"), color = HarmonyMuted, fontSize = 11.sp)', "PicShare partner sync"),
        ('Text("Bilder auswählen", color = HarmonyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)', 'Text(com.example.ui.contentText("Bilder auswählen"), color = HarmonyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)', "select pictures"),
        ('Text("Antippen, um ein Bild ein- oder auszublenden", color = HarmonyMuted, fontSize = 10.5.sp)', 'Text(com.example.ui.contentText("Antippen, um ein Bild ein- oder auszublenden"), color = HarmonyMuted, fontSize = 10.5.sp)', "picture selection help"),
        ('contentDescription = "PicShare Bild auswählen"', 'contentDescription = com.example.ui.contentText("PicShare Bild auswählen")', "PicShare image a11y"),
        ('label = { Text("Widget-Text") }', 'label = { Text(com.example.ui.contentText("Widget-Text")) }', "widget text label"),
        ('supportingText = { Text("Dieser Text gilt für alle rotierenden Bilder.", fontSize = 10.sp) }', 'supportingText = { Text(com.example.ui.contentText("Dieser Text gilt für alle rotierenden Bilder."), fontSize = 10.sp) }', "widget help"),
        ('PicShareSettingToggle("Widget-Text anzeigen", showCaption)', 'PicShareSettingToggle(com.example.ui.contentText("Widget-Text anzeigen"), showCaption)', "show widget text"),
        ('PicShareSettingToggle("Harmony-Statuszeile anzeigen", showStatus)', 'PicShareSettingToggle(com.example.ui.contentText("Harmony-Statuszeile anzeigen"), showStatus)', "show status line"),
        ('PicShareSettingToggle("Bildreihenfolge mischen", shufflePictures)', 'PicShareSettingToggle(com.example.ui.contentText("Bildreihenfolge mischen"), shufflePictures)', "shuffle pictures"),
        ('Text("Ziel nach der Verknüpfung", fontSize = 12.sp, fontWeight = FontWeight.Bold)', 'Text(com.example.ui.contentText("Ziel nach der Verknüpfung"), fontSize = 12.sp, fontWeight = FontWeight.Bold)', "target title"),
        ('TargetChip("Startbildschirm", target == "partner_home")', 'TargetChip(com.example.ui.contentText("Startbildschirm"), target == "partner_home")', "home target"),
        ('TargetChip("Sperrbildschirm", target == "partner_lock")', 'TargetChip(com.example.ui.contentText("Sperrbildschirm"), target == "partner_lock")', "lock target"),
        ('CompactAction("Bilder", Icons.Default.AddPhotoAlternate, onAddPictures, Modifier.weight(1f))', 'CompactAction(com.example.ui.contentText("Bilder"), Icons.Default.AddPhotoAlternate, onAddPictures, Modifier.weight(1f))', "dialog pictures action"),
        ('CompactAction("Widget", Icons.Default.Widgets, onPinWidget, Modifier.weight(1f))', 'CompactAction(com.example.ui.contentText("Widget"), Icons.Default.Widgets, onPinWidget, Modifier.weight(1f))', "dialog widget action"),
        (') { Text("Speichern", color = HarmonyPink, fontWeight = FontWeight.ExtraBold) }', ') { Text(com.example.ui.contentText("Speichern"), color = HarmonyPink, fontWeight = FontWeight.ExtraBold) }', "save widget"),
        ('dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = HarmonyMuted) } }', 'dismissButton = { TextButton(onClick = onDismiss) { Text(com.example.ui.contentText("Abbrechen"), color = HarmonyMuted) } }', "cancel widget"),
        ('Text(label, color = HarmonyText, fontSize = 11.5.sp, modifier = Modifier.weight(1f))', 'Text(com.example.ui.contentText(label), color = HarmonyText, fontSize = 11.5.sp, modifier = Modifier.weight(1f))', "toggle helper"),
        ('text = label,\n        color = if (selected)', 'text = com.example.ui.contentText(label),\n        color = if (selected)', "target helper"),
        ('Text(label, color = HarmonyText, fontSize = 11.sp, fontWeight = FontWeight.Bold)', 'Text(com.example.ui.contentText(label), color = HarmonyText, fontSize = 11.sp, fontWeight = FontWeight.Bold)', "compact action helper"),
        ('title = { Text("Beantwortete Fragen", fontWeight = FontWeight.ExtraBold) }', 'title = { Text(com.example.ui.contentText("Beantwortete Fragen"), fontWeight = FontWeight.ExtraBold) }', "answer history title"),
        ('Text("Noch keine Antworten gespeichert.", color = HarmonyMuted)', 'Text(com.example.ui.contentText("Noch keine Antworten gespeichert."), color = HarmonyMuted)', "answer history empty"),
        ('pack == null -> "Frage ${answer.questionIndex + 1}"', 'pack == null -> com.example.ui.contentText("Frage ${answer.questionIndex + 1}")', "answer fallback question"),
        ('} ?: "Frage ${answer.questionIndex + 1}"', '} ?: com.example.ui.contentText("Frage ${answer.questionIndex + 1}")', "answer fallback question 2"),
        ('Text("${profile.userName}: ${coupleChoice.userChoice}", color = HarmonyMuted, fontSize = 12.sp)', 'Text("${profile.userName}: ${LanguageManager.tr(coupleChoice.userChoice, appLanguage)}", color = HarmonyMuted, fontSize = 12.sp)', "saved user choice"),
        ('Text("${profile.partnerName}: ${coupleChoice.partnerChoice}", color = HarmonyMuted, fontSize = 12.sp)', 'Text("${profile.partnerName}: ${LanguageManager.tr(coupleChoice.partnerChoice, appLanguage)}", color = HarmonyMuted, fontSize = 12.sp)', "saved partner choice"),
        ('confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen", color = HarmonyPink) } }', 'confirmButton = { TextButton(onClick = onDismiss) { Text(com.example.ui.contentText("Schließen"), color = HarmonyPink) } }', "answer history close"),
        ('if (onClick != null) Text("Liste öffnen", color = HarmonyPinkSoft, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)', 'if (onClick != null) Text(com.example.ui.contentText("Liste öffnen"), color = HarmonyPinkSoft, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)', "open list"),
    ])

    print("Japanese render routing migration complete" + (" with changes" if changed else " (already applied)"))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
