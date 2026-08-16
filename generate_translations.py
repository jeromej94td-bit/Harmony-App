import re

with open("app/src/main/java/com/example/data/model/Models.kt") as f:
    models_text = f.read()

with open("app/src/main/java/com/example/data/GeneratedHarmonyContent.kt") as f:
    gen_text = f.read()

with open("app/src/main/java/com/example/ui/components/TotImageProvider.kt") as f:
    tot_text = f.read()

with open("app/src/main/java/com/example/data/LinkEngine.kt") as f:
    link_text = f.read()

all_texts = set()

# Parse Questions
for q in re.findall(r"Question\(\"([^\"]+)\"", models_text + gen_text):
    all_texts.add(q.strip())

# Parse defaultMine
for dm in re.findall(r"defaultMine\s*=\s*\"([^\"]+)\"", models_text + gen_text):
    all_texts.add(dm.strip())

# Parse options from listOf(...)
for block in re.findall(r"listOf\((.*?)\)", models_text + gen_text, re.DOTALL):
    for s in re.findall(r"\"([^\"]+)\"", block):
        s_clean = s.strip()
        if len(s_clean) > 1 and not s_clean.startswith("http") and not s_clean.startswith("ic_") and not s_clean.startswith("com."):
            all_texts.add(s_clean)

# Parse pairs "..." to "..."
for p1, p2 in re.findall(r"\"([^\"]+)\"\s*to\s*\"([^\"]+)\"", models_text + gen_text + tot_text, re.DOTALL):
    if not p1.startswith("http") and not p1.startswith("com.") and not p1.startswith("R.drawable"):
        all_texts.add(p1.strip())
    if not p2.startswith("http") and not p2.startswith("com.") and not p2.startswith("R.drawable"):
        all_texts.add(p2.strip())

# Parse captions from LinkEngine
for cap in re.findall(r"caption\s*=\s*\"([^\"]+)\"", link_text):
    all_texts.add(cap.strip())

# Add common UI strings from Language.kt
existing_ui_strings = [
    "Fragen & Spiele", "unterhaltung", "dasoderdas", "hochzeit", "kinder", "reden", "reisen", "familie",
    "Kategorien", "Tägliche Aktivität", "Für dich empfohlen", "Paar-Statistiken", "Gemeinsame Tage",
    "Beantwortete Fragen", "Besuchte Städte", "Besuchte Länder", "Widgets", "Du fehlst mir", "Denke an dich",
    "Kuss senden", "Aufwärmen", "Beziehung", "Sex & Liebe", "Moralische Werte", "Geld & Finanzen",
    "Einander kennenlernen", "Reisen", "Familie", "Hobbys", "Wer würde eher?", "Zeichnen", "Das oder das?",
    "Zustimmen oder Ablehnen", "Ich habe noch nie", "Was magst du lieber?", "Antwort mit einem Foto",
    "Tiefe Gespräche", "Reden vor ...", "Zuhause & Alltag", "Der perfekte Heiratsantrag", "Vorlieben für den Antrag",
    "Diskutiere vor dem Kinderkriegen", "Vor der Anschaffung eines Haustiers besprechen",
    "Vor der gemeinsamen Reise besprechen", "Vor dem Kauf eines Hauses besprechen", "Reiseziele", "Alle",
    "Du bist dran", "Beantwortet", "Überspringen", "Weiter", "Schließen", "Bearbeiten", "Profil", "Dein Name",
    "Partnerin", "Partner", "Zusammen seit", "Partner-Simulator", "Entwickler-Modus", "Entwickler Studio Öffnen",
    "Sprache", "Deutsch", "Englisch", "KI-Beziehungscoach", "KI-Date-Ideen", "Analyse starten", "Ideen generieren",
    "Namen und Startdatum eurer Beziehung.", "Name Partnerin", "Diskutiert eure Antworten", "ERGEBNISSE",
    "BEANTWORTE", "Alle Pakete", "Fertig", "Abbrechen", "Speichern", "Hinzufügen", "Momente", "Titel",
    "Was ist passiert?", "Zurück", "Quiz verlassen?", "Weiter spielen", "Übernehmen", "Deine eigene Antwort",
    "Deine Antwort...", "Frage hinzufügen", "Paar", "Galerie", "Testen", "Spiel bearbeiten", "Neues Spiel",
    "Antwortähnlichkeit", "Diskussion", "✎ Tippe, um zu antworten", "Möchtest du das Quiz wirklich verlassen? Dein bisheriger Fortschritt bleibt gespeichert.",
    "Schreib frei, was dir wirklich dazu einfällt.", "Habt ihr überraschende Unterschiede entdeckt? Sprecht darüber, warum euch bestimmte Optionen besser gefallen!",
    "Deine Antworten sind gespeichert. Sobald Partner das Paket beendet, werden beide Antworten gemeinsam sichtbar.",
    "Schreibe deine eigene Antwort", "Oder", "oder"
]

for s in existing_ui_strings:
    all_texts.add(s)

print(f"Total strings to process: {len(all_texts)}")
