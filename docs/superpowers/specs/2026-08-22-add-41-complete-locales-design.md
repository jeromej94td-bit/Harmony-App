# Harmony: 41 weitere vollständige Sprachen

## Ziel
Harmony erhält die 41 noch fehlenden Sprachen aus der freigegebenen Liste auf demselben Produktionsniveau wie die bereits vollständigen Sprachpakete. Übersetzungen werden als Offline-Kataloge ausgeliefert; zur Laufzeit wird kein externer Übersetzungsdienst benötigt.

## Neue Sprachen
- hu — Magyar — Hungarian
- ro — Română — Romanian
- bg — Български — Bulgarian
- uk — Українська — Ukrainian
- ru — Русский — Russian
- el — Ελληνικά — Greek
- tr — Türkçe — Turkish
- ar — العربية — Arabic
- he — עברית — Hebrew
- fa — فارسی — Persian
- hi — हिन्दी — Hindi
- bn — বাংলা — Bengali
- ur — اردو — Urdu
- ta — தமிழ் — Tamil
- te — తెలుగు — Telugu
- mr — मराठी — Marathi
- gu — ગુજરાતી — Gujarati
- kn — ಕನ್ನಡ — Kannada
- ml — മലയാളം — Malayalam
- th — ไทย — Thai
- vi — Tiếng Việt — Vietnamese
- id — Bahasa Indonesia — Indonesian
- ms — Bahasa Melayu — Malay
- fil — Filipino — Filipino
- my — မြန်မာ — Burmese
- km — ខ្មែរ — Khmer
- lo — ລາວ — Lao
- sw — Kiswahili — Swahili
- af — Afrikaans — Afrikaans
- am — አማርኛ — Amharic
- yo — Yorùbá — Yoruba
- ig — Igbo — Igbo
- ha — Hausa — Hausa
- zu — isiZulu — Zulu
- xh — isiXhosa — Xhosa
- so — Soomaali — Somali
- et — Eesti — Estonian
- lv — Latviešu — Latvian
- lt — Lietuvių — Lithuanian
- sl — Slovenščina — Slovenian
- sr — Српски — Serbian (Cyrillic)

## Architektur
1. `AppLanguage` wird um alle 41 BCP-47-Codes ergänzt. Die bestehende enum-basierte Sprachwahl übernimmt sie automatisch.
2. Jede Sprache erhält einen eigenen vollständigen Kotlin-Katalog und eine dynamische Lokalisierungsfunktion auf Basis des bestehenden `GeneratedLocaleSupport`-Mechanismus.
3. `TranslationCatalog` routet Exact- und Dynamic-Lookups für alle neuen Sprachen.
4. `IntrospectionStrings` wird so erweitert, dass auch die neuen Sprachen denselben vollständig lokalisierten Introspection-Pfad verwenden.
5. Der bestehende Localization-Audit wird auf alle 41 Sprachen erweitert. Der kanonische Kundenkatalog bleibt die gemeinsame Quelle. Neue produktive Strings, die der statische Scan außerhalb des Katalogs findet, werden in die kanonische Prüfung aufgenommen statt still als deutscher Fallback akzeptiert.
6. Developer Studio / Developer Mode bleibt weiterhin ausdrücklich außerhalb des Kundenscope.

## RTL
Arabisch (`ar`), Hebräisch (`he`), Persisch (`fa`) und Urdu (`ur`) werden als RTL-Sprachen behandelt. Das Android-Manifest unterstützt RTL bereits. Zusätzlich prüft CI, dass diese Codes als RTL markiert sind und keine manuell erzwungenen LTR-Annahmen in den lokalisierten Kernpfaden eingeführt werden.

## Übersetzungserzeugung
Die 41 Kataloge werden in mehreren unabhängigen Gruppen erzeugt, damit ein einzelner Übersetzungsfehler nicht den gesamten Lauf verwirft. Die Erzeugung basiert auf dem aktuellen deutschen kanonischen Kundenkatalog. Platzhalter, Kotlin-Interpolation, Prozent-Formate, Namen, Zähler, Emojis und Markenbegriffe werden geschützt und nach der Übersetzung unverändert wieder eingesetzt.

Besonders sichtbare UI-Texte und Harmony-spezifische Begriffe erhalten pro Sprache eine reviewed-override-Schicht. Dazu zählen unter anderem Navigation, Schließen, Frage, unbeantwortet, privater Paar-Chat, Entweder-oder, erste Begegnung, Aussehen, Unterbewusstseins-Reise, Übergabe des Handys sowie CNV/NVC-Begriffe.

## Qualitätsregeln
Für jede neue Sprache muss vor Merge gelten:
- vollständige Abdeckung aller kanonischen kundenrelevanten Schlüssel;
- kein deutscher Fallback für einen geprüften kanonischen Schlüssel;
- keine beschädigten `${...}`, `{...}` oder printf-Platzhalter;
- keine versehentlich übersetzten Kotlin-Variablennamen;
- keine doppelten Kotlin-Dollar-/Backslash-Escapes;
- Exact- und Dynamic-Runtime-Routing vorhanden;
- Introspection-Routing vorhanden;
- RTL-Metadaten korrekt für ar/he/fa/ur;
- kompletter Source-Audit grün;
- kompletter Effective/Runtime-Audit grün;
- `:app:compileDebugKotlin` grün.

## CI-Aufteilung
Die Katalogerzeugung wird in mehrere Jobs bzw. Batches aufgeteilt, die jeweils idempotent arbeiten. Ein abschließender Aggregations-/Audit-Lauf prüft danach alle vorhandenen Sprachen gemeinsam und kompiliert die Android-App. Generierte Kataloge werden vor dem finalen Audit in den PR-Branch persistiert, damit ein späterer Buildfehler keine Übersetzungsarbeit verliert.

## Merge-Strategie
Die gesamte Erweiterung landet in einem neuen PR von `agent/add-41-complete-locales-20260822` nach `main`. Der PR wird erst auf Ready gestellt und gemergt, wenn die vollständige CI für alle neuen Sprachen sowie der Kotlin-Compile grün sind.
