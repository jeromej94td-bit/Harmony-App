import re
import generate_translations
import build_full_language
import make_full_dict

all_texts = generate_translations.all_texts
master = dict(build_full_language.translations)
master.update(make_full_dict.EXTRA_MAP)

# Add my newly added options to the translation mapping
new_options = {
    "Habe ich": "I have",
    "Habe ich noch nie": "Never have I ever",
    "Stimmt": "Agree",
    "Stimmt nicht": "Disagree",
    "Teils teils": "Somewhat agree",
    "Unter 1000": "Under 1000",
    "1000 bis 3000": "1000 to 3000",
    "3000 bis 5000": "3000 to 5000",
    "Open End": "Open end",
    "Unter 1000 im Monat": "Under 1000 a month",
    "1000 - 1500 im Monat": "1000 - 1500 a month",
    "1500 - 2000 im Monat": "1500 - 2000 a month",
    "Über 2000 im Monat": "Over 2000 a month",
    "Stadt": "City",
    "Land": "Countryside",
    "Vorort": "Suburb",
    "1-5 Jahre": "1-5 years",
    "5-10 Jahre": "5-10 years",
    "Für immer": "Forever",
    "Weiß ich noch nicht": "I don't know yet",
    "Getrennte Konten": "Separate accounts",
    "Gemeinsame Konten": "Joint accounts",
    "Sowohl als auch": "Both",
    "Jeder zahlt 50%": "Each pays 50%",
    "Prozentual nach Einkommen": "Proportional to income",
    "Einer zahlt alles": "One pays everything",
    "Urlaub": "Vacation",
    "Haus/Wohnung": "House/Apartment",
    "Auto": "Car",
    "Für die Zukunft": "For the future",
    "Ab 50€": "From 50€",
    "Ab 100€": "From 100€",
    "Ab 500€": "From 500€",
    "Erst bei sehr großen Summen": "Only for very large amounts",
    "Leidenschaft": "Passion",
    "Romantik": "Romance",
    "Spaß & Abenteuer": "Fun & Adventure",
    "Verbindung": "Connection",
    "Ich liebe es": "I love it",
    "Ist okay für mich": "It's okay for me",
    "Nicht mein Fall": "Not my thing",
    "Sehr gerne": "Very much",
    "Ab und zu": "From time to time",
    "Lieber nicht": "Prefer not to",
    "Kuscheln": "Cuddling",
    "Worte/Komplimente": "Words/Compliments",
    "Kleine Geschenke": "Small gifts",
    "Hilfe im Alltag": "Help in everyday life",
    "Ja, in- und auswendig": "Yes, inside out",
    "Das meiste weiß ich": "I know most of it",
    "Bin manchmal unsicher": "I'm sometimes unsure",
    "Körperliche Annäherung": "Physical approach",
    "Romantische Stimmung": "Romantic mood",
    "Überraschend": "Surprising",
    "Mehr zuhören": "Listen more",
    "Mehr gemeinsame Zeit": "More time together",
    "Mehr Unterstützung": "More support",
    "Ist schon perfekt": "It's already perfect",
    "Viel getrennt": "A lot apart",
    "Ein paar Stunden": "A few hours",
    "Am liebsten alles zusammen": "Preferably everything together",
    "Kultur & Sehenswürdigkeiten": "Culture & Sights",
    "Entspannung": "Relaxation",
    "Abenteuer": "Adventure",
    "Gutes Essen": "Good food",
    "Müssen sparen": "We have to save",
    "Eher knapp": "Rather tight",
    "Ja, genug": "Yes, enough",
    "Müssen es planen": "We have to plan it",
    "Wird stressig": "It will be stressful",
    "Feste Date-Nights": "Fixed date nights",
    "Zeit, wenn das Baby schläft": "Time when the baby sleeps",
    "Spontan": "Spontaneous",
    "Große Familie": "Big family",
    "Zu zweit reisen": "Travel as a couple",
    "Karriere & Erfolg": "Career & Success",
    "Traditioneller Name": "Traditional name",
    "Moderner Name": "Modern name",
    "Ausgefallener Name": "Unusual name",
    "Mädchen": "Girl",
    "Junge": "Boy",
    "Egal, Hauptsache gesund": "Doesn't matter, as long as it's healthy",
    "Erfolgreich": "Successful",
    "Glücklich und frei": "Happy and free",
    "Familienmensch": "Family person",
    "Kommt mit": "Comes with us",
    "Familie/Freunde": "Family/Friends",
    "Tierpension": "Pet boarding",
    "Unter 50€/Monat": "Under 50€/month",
    "50-100€/Monat": "50-100€/month",
    "Über 100€/Monat": "Over 100€/month",
    "Ja, perfekt": "Yes, perfect",
    "Mit Kompromissen": "With compromises",
    "Eher schwierig": "Rather difficult",
    "Mich überraschen": "Surprise me",
    "Zuhören": "Listen",
    "Im Haushalt helfen": "Help in the household",
    "Zärtlich sein": "Be affectionate",
    "Ein lustiges Bild": "A funny picture",
    "Ein romantisches Bild": "A romantic picture",
    "Aus dem Urlaub": "From the vacation",
    "Keine Musik": "No music",
    "Humor": "Humor",
    "Aussehen": "Looks",
    "Intelligenz": "Intelligence",
    "Fürsorglichkeit": "Caring",
    "Keine Privatsphäre": "No privacy",
    "Streit über Haushalt": "Arguments about household",
    "Alltagsroutine": "Everyday routine",
    "Geduld": "Patience",
    "Gelassenheit": "Serenity",
    "Neues Hobby": "New hobby",
    "Besser kommunizieren": "Communicate better",
    "Beide": "Both",
    "Niemand": "Nobody",
    "Durchgeplant": "Fully planned",
    "Ja, absolut": "Yes, absolutely",
}

master.update(new_options)

def auto_translate(text):
    if text in master:
        return master[text]
    t = text
    # Replacements for key terms
    replacements = [
        ("Sind wir in der Lage, alle Kosten für ein Kind/mehrere Kinder zu decken? 💰", "Are we able to cover all costs for a child/children? 💰"),
        ("Werden wir genug Zeit für das Kind / die Kinder haben? ⏳", "Will we have enough time for the child/children? ⌛"),
        ("Wie werden wir Zeit für unsere Beziehung finden, wenn das Baby da ist?", "How will we find time for our relationship when the baby arrives?"),
        ("Wie möchtest du, dass unsere Zukunft aussieht?", "How do you want our future to look?"),
        ("Wie würdest du sie/ihn nennen?", "What would you name him/her?"),
        ("Willst du ein Mädchen oder einen Jungen? 👶", "Do you want a girl or a boy? 👶"),
        ("Was soll aus unserem Kind werden, wenn es erwachsen ist?", "What do you hope our child becomes when they grow up?"),
        ("Möglicherweise müssen wir Einsparungen vornehmen", "We might need to make budget cutbacks"),
        ("Ja, wir werden sicherstellen, dass die Zeit mit der Familie Vorrang hat.", "Yes, we will ensure family time is prioritized."),
        ("Regelmäßige Rendezvous oder gemeinsame Zeit einplanen", "Schedule regular date nights or quality time together"),
        ("Gemeinsam die Welt bereisen, neue Kulturen und Küchen erkunden", "Travel the world together, explore new cultures and cuisines"),
        ("Wer übernimmt die tägliche Versorgung?", "Who handles daily care?"),
        ("Was passiert mit dem Tier, wenn wir verreisen?", "What happens to the pet when we travel?"),
        ("Welches Budget planen wir für Futter und Tierarzt ein?", "What budget do we plan for food and vet costs?"),
        ("Passt ein Tier überhaupt zu unserem Alltag?", "Does a pet fit into our daily routine at all?"),
        ("Wie viel wollen wir insgesamt ausgeben?", "How much do we want to spend in total?"),
        ("Lieber durchgeplant oder spontan?", "Rather fully planned or spontaneous?"),
        ("Wie viel Zeit wollen wir getrennt verbringen?", "How much time do we want to spend apart?"),
        ("Was ist für jeden von uns das absolute Highlight?", "What is the absolute highlight for each of us?"),
        ("Wie viel Kredit ist für uns realistisch tragbar?", "How much mortgage can we realistically afford?"),
        ("Stadt oder Land — was ist uns wichtiger?", "City or countryside — which is more important to us?"),
        ("Wie lange wollen wir dort mindestens bleiben?", "How long do we plan to stay there at minimum?"),
        ("Wer kümmert sich um Renovierung und Instandhaltung?", "Who handles renovations and maintenance?"),
        ("Ich habe noch nie einen der dicken Harry Potter Bände gelesen.", "Never have I ever read one of the thick Harry Potter books.")
    ]
    
    for de, en in replacements:
        if t == de:
            return en
            
    # Check if the text matches dynamic text exactly
    if t == "{user}": return "{user}"
    if t == "{partner}": return "{partner}"
    
    return t

final_map = {}
for text in sorted(all_texts):
    if not text or text.startswith("http") or text.startswith("R.drawable") or text == "', listOf('":
        continue
    translated = auto_translate(text)
    final_map[text] = translated

header = """package com.example.ui

/**
 * Complete English Harmony catalog.
 *
 * The entries preserve the German display source as the lookup key. User-entered
 * names, messages and answers intentionally remain unchanged at runtime.
 */
internal val EXACT_ENGLISH_CONTENT: Map<String, String> = mapOf(
"""
footer = """)"""

entries = []
for k, v in sorted(final_map.items()):
    if k == v and not k in new_options: 
        # Optional: skip identity mapping if you want to save space, but let's include it for completeness
        pass
    k_esc = k.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
    v_esc = v.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
    entries.append(f'    "{k_esc}" to "{v_esc}"')

# Also forcefully inject new_options to make sure they are included
for k, v in new_options.items():
    if k not in final_map:
        k_esc = k.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
        v_esc = v.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
        entries.append(f'    "{k_esc}" to "{v_esc}"')

content = header + ",\n".join(entries) + footer
with open("app/src/main/java/com/example/ui/EnglishContent.kt", "w") as f:
    f.write(content)

print(f"Generated EnglishContent.kt with {len(entries)} entries.")
