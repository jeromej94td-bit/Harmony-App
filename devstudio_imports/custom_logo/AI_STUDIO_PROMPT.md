# Google AI Studio Prompt – Harmony custom_logo

Arbeite mit folgendem GitHub-Repository und ausschließlich mit dem angegebenen Feature-Branch:

Repository: https://github.com/jeromej94td-bit/Harmony-App
Branch: feat/devstudio-ai-export-v2
Pull Request: #30

## Aufgabe

Übernimm den bereits vollständig vorbereiteten Dev-Studio-/AI-Studio-Export und das neue Bildspiel `custom_logo` in die Harmony Android-App. Alle für diese Integration benötigten Dateien und Bildassets liegen bereits im GitHub-Branch. Lade nichts zusätzlich aus Google Drive herunter und erzeuge keine Ersatzbilder.

## Verbindliche Quellen im Repository

- `devstudio_imports/custom_logo/AI_STUDIO_README.txt`
- `devstudio_imports/custom_logo/harmony-export-manifest.json`
- `app/src/main/java/com/example/data/GeneratedHarmonyNewPicGame.kt`
- `app/src/main/java/com/example/data/GeneratedContentRegistry.kt`
- `app/src/main/java/com/example/data/DevGenTypes.kt`
- `app/src/main/java/com/example/data/DeveloperDataManager.kt`
- `images/custom_logo/`

Der aktuelle Branch enthält bereits eine sichere additive Integration. Deshalb `GeneratedHarmonyContent.kt` NICHT blind durch die Einzelspiel-Datei ersetzen. Der vorhandene `GeneratedContentRegistry` ist absichtlich dafür zuständig, bisherigen Generated-Content und `custom_logo` zusammenzuführen, damit keine bestehenden Spiele verschwinden.

## Bildzuordnung – unverändert übernehmen

Das Manifest ist die verbindliche Wahrheit für `optionKey`, `packId`, `pairIndex`, `side` und Bildpfad.

`custom_logo` muss exakt diese drei Paare verwenden:

1. Pair 001
   - Seite A: `images/custom_logo/pair-001/a/1000110101.png`
   - Seite B: `images/custom_logo/pair-001/b/1000110102.png`

2. Pair 002
   - Seite A: `images/custom_logo/pair-002/a/1000110103.png`
   - Seite B: `images/custom_logo/pair-002/b/1000110104.png`

3. Pair 003
   - Seite A: `images/custom_logo/pair-003/a/1000110105.png`
   - Seite B: `images/custom_logo/pair-003/b/1000110111.jpg`

Die Bilddateien dürfen nicht umbenannt, vertauscht, neu generiert oder durch andere Bilder ersetzt werden. Wenn Android intern andere Ressourcennamen benötigt, muss die Zuordnung über `optionKey` / `packId` / `pairIndex` / `side` vollständig erhalten bleiben.

## Technische Regeln

1. Bestehende Harmony-Spiele, Spielmechanik, Navigation, Lokalisierungen und sonstige Features nicht entfernen oder grundlos umbauen.
2. `custom_logo` additiv integrieren. Kein vorhandenes Spiel darf dadurch verschwinden.
3. Die Reihenfolge und Paarzuordnung aus `harmony-export-manifest.json` exakt respektieren.
4. `GenPack`, `GenQuestion` und `GenAssetMeta` gegen `DevGenTypes.kt` prüfen.
5. Den bestehenden `GeneratedContentRegistry` und die bereits implementierte `DeveloperDataManager`-Integration verwenden.
6. Die sechs Dateien unter `images/custom_logo/` als die zum Spiel gehörenden Bildassets behandeln.
7. Keine Google-Drive-Abhängigkeit einbauen. Nach dem Clone/Import des GitHub-Branches muss die Integration vollständig aus dem Repository möglich sein.
8. Keine Bilder anhand von Dateinamen erraten. Immer die Manifest-Zuordnung verwenden.
9. Bestehende Optimierungs-/Cache-Logik darf verwendet werden, solange sichtbares Bild und Paarzuordnung korrekt bleiben.
10. Keine unnötigen Architekturänderungen durchführen.

## Verifikation vor Abschluss

Führe einen vollständigen Android-Build und die vorhandenen Unit-Tests aus. Behebe echte Compile- oder Integrationsfehler, ohne die Manifest-Zuordnung zu verändern.

Prüfe danach explizit:

- `custom_logo` existiert genau einmal.
- `custom_logo` besitzt genau 3 Paare.
- Alle 6 oben genannten GitHub-Bildpfade existieren.
- A/B-Zuordnung jedes Paares stimmt mit dem Manifest überein.
- Alle sechs `optionKey`-Zuordnungen stimmen mit dem Manifest überein.
- Die Bilder werden in der App tatsächlich angezeigt.
- Bestehende generierte Spiele sind weiterhin vorhanden.
- Keine Google-Drive-Datei wird für Build oder Laufzeit benötigt.

Am Ende nenne kurz alle geänderten Dateien und bestätige ausdrücklich, dass alle sechs Bildpfade vorhanden sind, `custom_logo` korrekt angezeigt wird und kein bestehendes Spiel ersetzt oder gelöscht wurde.
