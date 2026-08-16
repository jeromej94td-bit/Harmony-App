import re
import generate_translations
import build_full_language
import make_full_dict

all_texts = generate_translations.all_texts
master = dict(build_full_language.translations)
master.update(make_full_dict.EXTRA_MAP)

# Fallback auto-translator for any remaining text using word/phrase replacement
def auto_translate(text):
    if text in master:
        return master[text]
    
    # Clean syntax artifacts
    if text.startswith("', listOf('") or text.startswith("http"):
        return text

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
        ("Vor der Anschaffung eines Haustiers besprechen", "Discuss before getting a pet"),
        ("Vor der gemeinsamen Reise besprechen", "Discuss before travelling together"),
        ("Vor dem Kauf eines Hauses besprechen", "Discuss before buying a house"),
        ("Diskutiere vor dem Kinderkriegen", "Discuss before having children"),
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
        ("Antwort mit einem Foto", "Answer with a photo"),
        ("Tiefe Gespräche", "Deep conversations"),
        ("Reden vor ...", "Talk before ..."),
        ("Zuhause & Alltag", "Home & everyday life"),
        ("Der perfekte Heiratsantrag", "The perfect proposal"),
        ("Vorlieben für den Antrag", "Proposal preferences"),
        ("Reiseziele", "Travel destinations"),
        ("Wer würde eher?", "Who's more likely?"),
        ("Zeichnen", "Draw"),
        ("Das oder das?", "This or That?"),
        ("Zustimmen oder Ablehnen", "Agree or Disagree"),
        ("Ich habe noch nie", "Never have I ever"),
        ("Was magst du lieber?", "Which do you prefer?"),
        ("Einander kennenlernen", "Getting to know each other"),
        ("Geld & Finanzen", "Money & Finances"),
        ("Moralische Werte", "Moral values"),
        ("Sex & Liebe", "Sex & Love"),
        ("Aufwärmen", "Warm-up"),
        ("Kuss senden", "Send a kiss"),
        ("Denke an dich", "Thinking of you"),
        ("Du fehlst mir", "I miss you"),
        ("Beantwortete Fragen", "Questions answered"),
        ("Gemeinsame Tage", "Days together"),
        ("Paar-Statistiken", "Couple statistics"),
        ("Für dich empfohlen", "Recommended for you"),
        ("Tägliche Aktivität", "Daily activity"),
        ("Kategorien", "Categories")
    ]
    
    for de, en in replacements:
        if t == de:
            return en
            
    # Simple word-level heuristics if needed
    t_en = t
    t_en = re.sub(r"\bWie\b", "How", t_en)
    t_en = re.sub(r"\bWas\b", "What", t_en)
    t_en = re.sub(r"\bWer\b", "Who", t_en)
    t_en = re.sub(r"\bWo\b", "Where", t_en)
    t_en = re.sub(r"\bWarum\b", "Why", t_en)
    t_en = re.sub(r"\bWelche[sr]?\b", "Which", t_en)
    t_en = re.sub(r"\bWann\b", "When", t_en)
    t_en = re.sub(r"\boder\b", "or", t_en)
    t_en = re.sub(r"\bund\b", "and", t_en)
    t_en = re.sub(r"\bneue[sr]?\b", "new", t_en)
    t_en = re.sub(r"\bmit\b", "with", t_en)
    t_en = re.sub(r"\bohne\b", "without", t_en)
    t_en = re.sub(r"\buns\b", "us", t_en)
    t_en = re.sub(r"\bdein(em|er)?\b", "your", t_en)
    t_en = re.sub(r"\bmein(em|er)?\b", "my", t_en)

    return t_en

# Collect all translations
final_map = {}
for text in sorted(all_texts):
    if not text or text.startswith("http") or text.startswith("R.drawable") or text == "', listOf('":
        continue
    translated = auto_translate(text)
    final_map[text] = translated

print(f"Final map size: {len(final_map)}")

