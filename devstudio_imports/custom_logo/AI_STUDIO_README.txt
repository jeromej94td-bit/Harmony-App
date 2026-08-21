HARMONY — EXPORT FÜR GOOGLE AI STUDIO

Inhalt:
- app/src/main/java/com/example/data/GeneratedHarmonyContent.kt
- harmony-export-manifest.json
- 6 Originalbilder unter images/...
- 1 Spiele in verbindlicher Reihenfolge

ANWEISUNG FÜR AI STUDIO
1. Ersetze app/src/main/java/com/example/data/GeneratedHarmonyContent.kt vollständig durch die gleichnamige Datei aus diesem Paket.
2. Beachte harmony-export-manifest.json als verbindliche Zuordnung und Reihenfolge.
3. Bilddateien NICHT umbenennen. zipPath zeigt Spiel, Paar und Seite.
4. Bestehende Harmony-Spielmechanik, Navigation und andere Features nicht umbauen.
5. Falls Android-Ressourcennamen technisch angepasst werden müssen, muss die Zuordnung über optionKey/packId/pairIndex/side erhalten bleiben.
6. Danach kompilieren und alle GenPack-/GenQuestion-/GenAssetMeta-Aufrufe gegen DevGenTypes.kt prüfen.

Die Originaldateien bleiben im ZIP unverändert. Harmony selbst darf intern optimierte Arbeitskopien verwenden.

HINWEIS ZUR GITHUB-INTEGRATION:
Dieses Paket enthielt nur custom_logo. Deshalb wurde GeneratedHarmonyContent.kt im bestehenden Harmony-Repo nicht destruktiv ersetzt. custom_logo wird über GeneratedContentRegistry zusätzlich zum bereits vorhandenen generierten Content geladen. So bleiben alle bestehenden Spiele erhalten; optionKey, packId, pairIndex, side und Originaldateinamen entsprechen weiterhin diesem Manifest.
