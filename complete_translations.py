import re, json
import generate_translations
import build_language_kt

all_texts = generate_translations.all_texts
trans_map = dict(build_language_kt.TRANSLATION_MAP)

# Word and phrase level translation rules for remaining German sentences/phrases
def translate_german_text(s):
    if s in trans_map:
        return trans_map[s]
    
    orig = s
    
    # Common questions / sentences
    s_tr = s
    
    # Specific sentences/questions mappings
    rules = [
        ("Was gefällt dir an deinem Zuhause am besten?", "What do you like most about your home?"),
        ("Welcher Raum sagt am meisten über dich aus?", "Which room says the most about you?"),
        ("Was würdest du sofort ändern, wenn Geld keine Rolle spielt?", "What would you change immediately if money were no object?"),
        ("Wie sieht dein perfekter Sonntag zuhause aus?", "What does your perfect Sunday at home look like?"),
        ("Welche Umgebung würdest du dir für einen Antrag wünschen?", "What kind of setting would you wish for a proposal?"),
        ("Magst du einen öffentlichen oder privaten Antrag lieber?", "Would you prefer a public or private proposal?"),
        ("Soll der Antrag eine Überraschung sein?", "Should the proposal be a surprise?"),
        ("Wie wichtig ist dir, dass der Moment festgehalten wird?", "How important is it to you that the moment is captured?"),
        ("Bevorzugst du einen extravaganten Antrag oder etwas Dezentes?", "Do you prefer an extravagant proposal or something subtle?"),
        ("Was hältst du von Requisiten (z.B. Schilder, Luftballons, etc.)?", "What do you think of props (e.g. signs, balloons, etc.)?"),
        ("Wie wichtig ist dir, dass der Antrag deine Hobbys oder Interessen widerspiegelt?", "How important is it that the proposal reflects your hobbies or interests?"),
        ("Würdest du einen Antrag im Urlaub bevorzugen (mit Reisen verbunden)?", "Would you prefer a proposal on vacation (combined with travel)?"),
        ("Wie lange nach dem Antrag möchtest du mit anderen feiern?", "How long after the proposal would you like to celebrate with others?"),
        ("Hättest du gerne einen geschriebenen Brief als Teil des Antrags?", "Would you like a written letter as part of the proposal?"),
        ("Sollte der Antrag kulturelle oder traditionelle Elemente enthalten?", "Should the proposal include cultural or traditional elements?"),
        ("Was ist das Wichtigste beim Antrag?", "What is the most important thing about the proposal?"),
        ("Möchtest du, dass Haustiere beim Antrag dabei sind?", "Would you like pets to be present during the proposal?"),
        ("Sind wir in der Lage, alle Kosten für ein Kind/mehrere Kinder zu decken? 💰", "Are we able to cover all costs for a child/children? 💰"),
        ("Werden wir genug Zeit für das Kind / die Kinder haben? ⏳", "Will we have enough time for the child/children? ⌛"),
        ("Wie werden wir Zeit für unsere Beziehung finden, wenn das Baby da ist?", "How will we find time for our relationship when the baby arrives?"),
        ("Wie möchtest du, dass unsere Zukunft aussieht?", "How do you want our future to look?"),
        ("Wie würdest du sie/ihn nennen?", "What would you name him/her?"),
        ("Willst du ein Mädchen oder einen Jungen? 👶", "Do you want a girl or a boy? 👶"),
        ("Was soll aus unserem Kind werden, wenn es erwachsen ist?", "What do you hope our child becomes when they grow up?"),
        ("Wer übernimmt die tägliche Versorgung?", "Who will handle daily care?"),
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
        ("Möglicherweise müssen wir Einsparungen vornehmen", "We might need to make budget cutbacks"),
        ("Ja, wir werden sicherstellen, dass die Zeit mit der Familie Vorrang hat.", "Yes, we will ensure family time is prioritized."),
        ("Regelmäßige Rendezvous oder gemeinsame Zeit einplanen", "Schedule regular date nights or quality time together"),
        ("Gemeinsam die Welt bereisen, neue Kulturen und Küchen erkunden", "Travel the world together, explore new cultures and cuisines")
    ]
    
    for de, en in rules:
        if s == de:
            return en
            
    return None

