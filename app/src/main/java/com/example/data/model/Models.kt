package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- ROOM ENTITIES ---

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "",
    val partnerName: String = "",
    val startDate: Long = 0L,
    val simulatorEnabled: Boolean = true
)

@Entity(tableName = "answers", primaryKeys = ["packId", "questionIndex"])
data class AnswerEntity(
    val packId: String,
    val questionIndex: Int,
    val answerText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "me" or "them"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "moments")
data class MomentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val emoji: String = "💕",
    val timestamp: Long = System.currentTimeMillis(),
    val isMilestone: Boolean = false
)

@Entity(tableName = "couple_stats")
data class CoupleStatsEntity(
    @PrimaryKey val id: Int = 1,
    val visitedCities: Int = 7,
    val visitedCountries: Int = 3
)

// --- DOMAIN MODELS & PACK DEFINITIONS ---

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val tagColorHex: Long
)

data class Topic(
    val id: String,
    val name: String,
    val emoji: String
)

data class Question(
    val q: String,
    val options: List<String> = emptyList(),
    val defaultMine: String? = null
)

data class QuestionPack(
    val id: String,
    val title: String,
    val tags: List<String>,
    val cat: String,
    val topic: String,
    val type: String, // "quiz", "tot", "disc"
    val questions: List<Question> = emptyList(),
    val pairs: List<Pair<String, String>> = emptyList(),
    val emoji: String = "",
    /** Empty means global; otherwise this pack is shown only for these BCP-47 locale codes. */
    val availableLanguageCodes: Set<String> = emptySet()
)

fun QuestionPack.isAvailableIn(languageCode: String): Boolean =
    availableLanguageCodes.isEmpty() || availableLanguageCodes.any { it.equals(languageCode, true) }

object HarmonyPacksData {

    private val DEFAULT_CATEGORIES = listOf(
        Category("wer", "Wer würde eher?", "🤔", 0xFFFF2E63),
        Category("zeich", "Zeichnen", "🎨", 0xFF9E59BD),
        Category("tot", "Das oder das?", "⚖️", 0xFFFFC46B),
        Category("zust", "Zustimmen oder Ablehnen", "👍", 0xFF9DB2FF),
        Category("nie", "Ich habe noch nie", "🙈", 0xFFFF6B8F),
        Category("lieber", "Was magst du lieber?", "💫", 0xFFC89BE0),
        Category("foto", "Antwort mit einem Foto", "📷", 0xFF7BD8CB),
        Category("tief", "Tiefe Gespräche", "🌊", 0xFF9DB2FF),
        Category("reden", "Reden vor ...", "🗣️", 0xFFFFC46B)
    )

    private val dynamicCategories = mutableListOf<Category>()
    private val dynamicPacks = mutableListOf<QuestionPack>()

    fun setDynamicCategories(cats: List<Category>) {
        dynamicCategories.clear()
        dynamicCategories.addAll(cats)
    }

    fun setDynamicPacks(packs: List<QuestionPack>) {
        dynamicPacks.clear()
        dynamicPacks.addAll(packs)
    }

    val CATEGORIES: List<Category>
        get() {
            val result = DEFAULT_CATEGORIES.toMutableList()
            for (dc in dynamicCategories) {
                val idx = result.indexOfFirst { it.id == dc.id }
                if (idx >= 0) {
                    result[idx] = dc
                } else {
                    result.add(dc)
                }
            }
            return result
        }

    val TOPICS = listOf(
        Topic("aufwaermen", "Aufwärmen", "☀️"),
        Topic("beziehung", "Beziehung", "💗"),
        Topic("sex", "Sex & Liebe", "🔥"),
        Topic("moral", "Moralische Werte", "⚖️"),
        Topic("geld", "Geld & Finanzen", "💰"),
        Topic("kennen", "Einander kennenlernen", "🫶"),
        Topic("reisen", "Reisen", "✈️"),
        Topic("familie", "Familie", "👨‍👩‍👧"),
        Topic("hobbys", "Hobbys", "🎯")
    )

    val DEFAULT_PACKS = listOf(
        // ★ Zuhause & Alltag
        QuestionPack(
            id = "zuhause",
            title = "Zuhause & Alltag",
            tags = listOf("unterhaltung"),
            cat = "tief",
            topic = "kennen",
            type = "quiz",
            questions = listOf(
                Question("Was gefällt dir an deinem Zuhause am besten?", listOf("Die Gemütlichkeit und Ruhe", "Dass alles meinen Stil hat", "Die Menschen, die darin wohnen", "Der Blick nach draußen")),
                Question("Welcher Raum sagt am meisten über dich aus?", listOf("Küche", "Schlafzimmer", "Wohnzimmer", "Mein Arbeitsplatz")),
                Question("Was würdest du sofort ändern, wenn Geld keine Rolle spielt?", listOf("Größere Küche", "Ein Balkon oder Garten", "Bessere Lage", "Nichts — es passt so")),
                Question("Wie sieht dein perfekter Sonntag zuhause aus?", listOf("Ausschlafen und nichts tun", "Kochen und Freunde einladen", "Serienmarathon auf dem Sofa", "Aufräumen und Projekte angehen"))
            )
        ),

        // ★ Der perfekte Heiratsantrag
        QuestionPack(
            id = "antrag",
            title = "Der perfekte Heiratsantrag",
            tags = listOf("hochzeit", "unterhaltung"),
            cat = "tief",
            topic = "beziehung",
            type = "quiz",
            questions = listOf(
                Question("Welche Umgebung würdest du dir für einen Antrag wünschen?", listOf("Zu Hause, gemütlich und privat", "Draußen mit der Natur als Kulisse", "Schickes Restaurant oder Hotel")),
                Question("Magst du einen öffentlichen oder privaten Antrag lieber?", listOf("Nur wir beide", "Mit Familie und engen Freunden", "An einem öffentlichen Ort mit vielen Zuschauern")),
                Question("Soll der Antrag eine Überraschung sein?", listOf("Ja, komplett überraschend", "Lieber vorher grob absprechen", "Wir entscheiden es gemeinsam")),
                Question("Wie wichtig ist dir, dass der Moment festgehalten wird?", listOf("Sehr wichtig — Fotos und Video", "Ein paar Handyfotos reichen", "Gar nicht, der Moment gehört uns"))
            )
        ),

        QuestionPack(
            id = "antragvor",
            title = "Vorlieben für den Antrag",
            tags = listOf("hochzeit", "unterhaltung"),
            cat = "lieber",
            topic = "beziehung",
            type = "quiz",
            questions = listOf(
                Question("Bevorzugst du einen extravaganten Antrag oder etwas Dezentes?", listOf("Übertrieben und großartig", "Mittelweg mit besonderen Details", "Einfach und intim")),
                Question("Was hältst du von Requisiten (z.B. Schilder, Luftballons, etc.)?", listOf("Liebe sie, macht mehr Spaß", "Vielleicht ein oder zwei", "Nein, zu ablenkend")),
                Question("Möchtest du, dass der Moment sofort in den sozialen Medien geteilt wird?", listOf("Ja, sofort teilen", "Nein, erstmal privat halten", "Erst nur enge Freunde/Familie")),
                Question("Wie wichtig ist es dir, dass der Antrag deine Hobbys oder Interessen widerspiegelt?", listOf("Sehr wichtig, es sollte persönlich sein", "Etwas, eine kleine Note wäre schön", "Nicht nötig, lieber klassisch bleiben")),
                Question("Würdest du einen Antrag im Urlaub bevorzugen (mit Reisen verbunden)?", listOf("Ja, in einer aufregenden Stadt oder Sehenswürdigkeit", "Vielleicht, wenn es machbar ist", "Nein, lieber vor Ort")),
                Question("Wie lange nach dem Antrag möchtest du mit anderen feiern?", listOf("Am selben Tag mit Freunden/Familie", "Etwa eine Woche später, erst mal nur wir", "Keine Party nötig, privat halten")),
                Question("Hättest du gerne einen geschriebenen Brief als Teil des Antrags?", listOf("Ja, etwas zum Lesen und Aufbewahren", "Vielleicht, aber mündlich reicht", "Nein, einfach halten")),
                Question("Sollte der Antrag kulturelle oder traditionelle Elemente enthalten?", listOf("Ja, Traditionen einbinden", "Ein paar kulturelle Anklänge sind okay", "Nein, modern halten")),
                Question("Was ist das Wichtigste beim Antrag?", listOf("Die Umgebung und Atmosphäre", "Die Worte und Emotionen", "Der Ring und die Geste")),
                Question("Möchtest du, dass Haustiere beim Antrag dabei sind?", listOf("Ja, sie gehören zur Familie!", "Vielleicht, wenn es passt", "Nein, nur wir"))
            )
        ),

        // ★ Kinderkriegen Diskussionspaket
        QuestionPack(
            id = "kinder",
            title = "Diskutiere vor dem Kinderkriegen",
            tags = listOf("kinder", "unterhaltung"),
            cat = "reden",
            topic = "familie",
            type = "disc",
            questions = listOf(
                Question("Sind wir in der Lage, alle Kosten für ein Kind/mehrere Kinder zu decken? 💰", defaultMine = "Möglicherweise müssen wir Einsparungen vornehmen"),
                Question("Werden wir genug Zeit für das Kind / die Kinder haben? ⏳", defaultMine = "Ja, wir werden sicherstellen, dass die Zeit mit der Familie Vorrang hat."),
                Question("Wie werden wir Zeit für unsere Beziehung finden, wenn das Baby da ist?", defaultMine = "Regelmäßige Rendezvous oder gemeinsame Zeit einplanen"),
                Question("Wie möchtest du, dass unsere Zukunft aussieht?", defaultMine = "Gemeinsam die Welt bereisen, neue Kulturen und Küchen erkunden"),
                Question("Wie würdest du sie/ihn nennen?"),
                Question("Willst du ein Mädchen oder einen Jungen? 👶"),
                Question("Was soll aus unserem Kind werden, wenn es erwachsen ist?")
            )
        ),

        QuestionPack(
            id = "haustier",
            title = "Vor der Anschaffung eines Haustiers besprechen",
            tags = listOf("reden", "unterhaltung"),
            cat = "reden",
            topic = "familie",
            type = "disc",
            questions = listOf(
                Question("Wer übernimmt die tägliche Versorgung?"),
                Question("Was passiert mit dem Tier, wenn wir verreisen?"),
                Question("Welches Budget planen wir für Futter und Tierarzt ein?"),
                Question("Passt ein Tier überhaupt zu unserem Alltag?")
            )
        ),

        QuestionPack(
            id = "reisevor",
            title = "Vor der gemeinsamen Reise besprechen",
            tags = listOf("reden", "unterhaltung"),
            cat = "reden",
            topic = "reisen",
            type = "disc",
            questions = listOf(
                Question("Wie viel wollen wir insgesamt ausgeben?"),
                Question("Lieber durchgeplant oder spontan?"),
                Question("Wie viel Zeit wollen wir getrennt verbringen?"),
                Question("Was ist für jeden von uns das absolute Highlight?")
            )
        ),

        QuestionPack(
            id = "hauskauf",
            title = "Vor dem Kauf eines Hauses besprechen",
            tags = listOf("reden", "unterhaltung"),
            cat = "reden",
            topic = "geld",
            type = "disc",
            questions = listOf(
                Question("Wie viel Kredit ist für uns realistisch tragbar?"),
                Question("Stadt oder Land — was ist uns wichtiger?"),
                Question("Wie lange wollen wir dort mindestens bleiben?"),
                Question("Wer kümmert sich um Renovierung und Instandhaltung?")
            )
        ),

        // ★ Das oder Das - Reiseziele
        QuestionPack(
            id = "reiseziele",
            title = "Reiseziele",
            tags = listOf("dasoderdas"),
            cat = "tot",
            topic = "reisen",
            type = "tot",
            pairs = listOf(
                "Paris, Frankreich" to "Rom, Italien",
                "Bali, Indonesien" to "Santorini, Griechenland",
                "London, England" to "New York, USA",
                "Malediven" to "Seychellen",
                "Tokyo, Japan" to "Dubai, VAE",
                "Venedig, Italien" to "Amsterdam, Niederlande",
                "Lappland, Finnland" to "Island"
            )
        ),

        QuestionPack(
            id = "traumhaus",
            title = "Unser Traumhaus",
            tags = listOf("dasoderdas"),
            cat = "tot",
            topic = "geld",
            type = "tot",
            pairs = listOf(
                "Altbau mit Charme" to "Neubau mit Smart Home",
                "Offene Wohnküche" to "Separate Küche",
                "Prasselnder Kamin" to "Fußbodenheizung",
                "Großer Garten" to "Sonnige Dachterrasse",
                "Stadtvilla" to "Landhaus",
                "Glasfassade" to "Natursteinfassade",
                "Penthouse mit Ausblick" to "Haus am See",
                "Minimalistisches Interieur" to "Landhausstil",
                "Bibliothek" to "Heimkino",
                "Innenpool" to "Wellnessbad",
                "Große Fensterfront" to "Privater Innenhof",
                "Tiny House" to "Mehrgenerationenhaus"
            )
        ),

        QuestionPack(
            id = "aussen",
            title = "Traumhaus Außenbereich",
            tags = listOf("dasoderdas"),
            cat = "tot",
            topic = "geld",
            type = "tot",
            pairs = listOf(
                "Großer Außenpool" to "Outdoor-Whirlpool",
                "Moderne Grillstation" to "Gemütliche Feuerstelle",
                "Eigenes Gemüsebeet" to "Bunte Blumenwiese",
                "Entspannte Hängematte" to "Stilvolles Outdoor-Sofa",
                "Infinity-Pool" to "Naturteich",
                "Outdoor-Küche" to "Pizzaofen",
                "Pergola mit Lounge" to "Wintergarten",
                "Kräuterbeet" to "Obstgarten",
                "Dachgarten mit Lounge" to "Mediterraner Innenhof",
                "Feuerstelle" to "Außenkamin",
                "Spielbereich für Kinder" to "Sportplatz",
                "Gewächshaus" to "Saunahaus"
            )
        ),

        QuestionPack(
            id = "aktivitaeten",
            title = "Aktivitäten",
            tags = listOf("dasoderdas"),
            cat = "tot",
            topic = "hobbys",
            type = "tot",
            pairs = listOf(
                "Wandern" to "Strandtag",
                "Konzert" to "Kino",
                "Kochkurs" to "Restaurant",
                "Museum" to "Freizeitpark"
            )
        ),

        QuestionPack(
            id = "essen",
            title = "Essensvorlieben",
            tags = listOf("dasoderdas"),
            cat = "tot",
            topic = "kennen",
            type = "tot",
            pairs = listOf(
                "Pizza" to "Pasta",
                "Sushi" to "Burger",
                "Süß" to "Herzhaft",
                "Selbst kochen" to "Bestellen"
            )
        ),

        // ★ Italienisch-exklusive lokale Bildkarten. Die Texte sind absichtlich italienisch,
        // weil dieses regionale Deck ausschließlich gewählt wird, wenn Italiano aktiv ist.
        QuestionPack(
            id = "tot_italian_cuisine_mixed",
            title = "🍝 Cucina italiana — scelte regionali",
            tags = listOf("dasoderdas", "cucina", "italia"),
            cat = "tot",
            topic = "kennen",
            type = "tot",
            emoji = "🍝",
            availableLanguageCodes = setOf("it"),
            pairs = listOf(
                "Pizza napoletana" to "Calzone",
                "Sformatino di zucchine con fonduta di pecorino" to "Pappa al pomodoro",
                "Spaghetti alle vongole" to "Fritto misto di mare",
                "Carbonara" to "Amatriciana",
                "Pasta alla Norma" to "Risotto ai funghi",
                "Pappardelle al cinghiale" to "Pici senesi al ragù di chianina",
                "Lasagne alla bolognese" to "Cannelloni",
                "Cacciucco" to "Brodetto di pesce",
                "Risotto alla milanese" to "Polenta",
                "Arancini" to "Supplì",
                "Ribollita" to "Panzanella",
                "Bruschetta al pomodoro e basilico" to "Crostini toscani con fegato di pollo",
                "Pesto alla genovese" to "Ragù alla bolognese",
                "Baccalà mantecato" to "Sarde in saor",
                "Orecchiette alle cime di rapa" to "Trofie al pesto",
                "Insalata Caprese" to "Fiori di zucca ripieni",
                "Parmigiana di melanzane" to "Caponata",
                "Trippa alla fiorentina" to "Peposo all’Impruneta",
                "Risotto alla pescatora" to "Orata al cartoccio",
                "Ossobuco" to "Saltimbocca alla romana",
                "Gnocchi alla sorrentina" to "Pasta e fagioli",
                "Bistecca alla fiorentina" to "Arrosticini",
                "Insalata di polpo" to "Spaghetti allo scoglio",
                "Focaccia genovese" to "Piadina romagnola",
                "Carciofi alla romana" to "Polenta ai funghi",
                "Gnocchi" to "Ravioli",
                "Cantucci con vin santo" to "Tortino al cioccolato con cuore caldo",
                "Tiramisù" to "Panna cotta",
                "Cannoli siciliani" to "Sfogliatella",
                "Gelato" to "Semifreddo"
            )
        ),

        // Polnisch-exklusive lokale Bildkarten. Das Deck wird freigeschaltet, sobald
        // das vollständige Locale-Paket `pl` in der Sprachauswahl verfügbar ist.
        QuestionPack(
            id = "tot_polish_cuisine_traditional",
            title = "🇵🇱 Tradycyjna kuchnia polska",
            tags = listOf("dasoderdas", "kuchnia", "polska"),
            cat = "tot",
            topic = "kennen",
            type = "tot",
            emoji = "🇵🇱",
            availableLanguageCodes = setOf("pl"),
            pairs = listOf(
                "Pierogi ruskie" to "Bigos",
                "Żurek" to "Barszcz czerwony",
                "Kotlet schabowy" to "Placki ziemniaczane",
                "Gołąbki" to "Kopytka",
                "Rosół" to "Flaki",
                "Żeberka w kapuście" to "Kaszanka",
                "Łazanki" to "Zrazy wołowe",
                "Kaczka po poznańsku" to "Gulasz",
                "Ryba po grecku" to "Śledź w śmietanie",
                "Placki po węgiersku" to "Racuchy",
                "Pyzy ziemniaczane" to "Kartacze",
                "Oscypek z żurawiną" to "Bryndza",
                "Makowiec" to "Sernik",
                "Szarlotka" to "Pączki"
            )
        ),

        QuestionPack(
            id = "ringe",
            title = "Verlobungsringe",
            tags = listOf("hochzeit", "dasoderdas"),
            cat = "tot",
            topic = "beziehung",
            type = "tot",
            pairs = listOf(
                "Klassisch Solitär" to "Vintage verspielt",
                "Gelbgold" to "Weißgold",
                "Großer Stein" to "Filigran & schlicht",
                "Diamant" to "Farbedelstein",
                "Platin" to "Roségold",
                "Drei-Stein-Ring" to "Moderner Solitär",
                "Ovaler Diamant" to "Runder Diamant",
                "Schmal & zart" to "Markant & breit",
                "Moissanit" to "Saphir",
                "Vintage Art déco" to "Modern geometrisch",
                "Gravur innen" to "Diamanten im Band",
                "Ohne Stein" to "Statement-Ring"
            )
        ),

        QuestionPack(
            id = "straeusse",
            title = "Hochzeitssträuße",
            tags = listOf("hochzeit", "dasoderdas"),
            cat = "tot",
            topic = "beziehung",
            type = "tot",
            pairs = listOf(
                "Weiße Rosen" to "Pfingstrosen",
                "Wildblumen" to "Klassisch gebunden",
                "Groß & üppig" to "Klein & zart",
                "Pastell" to "Kräftige Farben"
            )
        ),

        QuestionPack(
            id = "traumhochzeit",
            title = "Traumhochzeit",
            tags = listOf("hochzeit", "dasoderdas"),
            cat = "tot",
            topic = "beziehung",
            type = "tot",
            pairs = listOf(
                "Große Feier" to "Kleine Runde",
                "Am Strand" to "In den Bergen",
                "Kirchlich" to "Standesamt & Party",
                "Sommerhochzeit" to "Winterhochzeit"
            )
        ),

        QuestionPack(
            id = "gelegenheit",
            title = "Fragen für jede Gelegenheit",
            tags = listOf("unterhaltung"),
            cat = "lieber",
            topic = "aufwaermen",
            type = "quiz",
            questions = listOf(
                Question("Was magst du lieber: früh aufstehen oder lange wach bleiben?", listOf("Früh aufstehen", "Lange wach bleiben", "Kommt auf den Tag an")),
                Question("Lieber ein ruhiger Abend zu zweit oder unter Leuten?", listOf("Ruhig zu zweit", "Unter Leuten", "Gemischt")),
                Question("Was entspannt dich mehr?", listOf("Musik", "Spazieren", "Serie schauen", "Gar nichts tun")),
                Question("Wobei lachst du am meisten?", listOf("Bei Insider-Witzen", "Bei Memes", "Wenn ich müde bin", "Über mich selbst"))
            )
        ),

        QuestionPack(
            id = "schnapp",
            title = "Schnappschüsse aus unserer Liebesgeschichte",
            tags = listOf("unterhaltung"),
            cat = "foto",
            topic = "beziehung",
            type = "quiz",
            questions = listOf(
                Question("Welcher Moment war für dich der Anfang von „uns\"?", listOf("Unser erstes Gespräch", "Das erste Treffen", "Der erste Kuss", "Als es einfach klar war")),
                Question("Welches gemeinsame Foto ist dein Lieblingsfoto?", listOf("Das erste Selfie", "Ein Urlaubsfoto", "Ein zufälliger Schnappschuss", "Eins, das nur wir kennen")),
                Question("Woran erinnerst du dich am lebhaftesten?", listOf("An einen Geruch", "An einen Song", "An etwas, das ich gesagt habe", "An das Gefühl"))
            )
        ),

        QuestionPack(
            id = "aufwaermen1",
            title = "Aufwärmen: Einander kennenlernen",
            tags = listOf("unterhaltung"),
            cat = "tief",
            topic = "aufwaermen",
            type = "quiz",
            questions = listOf(
                Question("Was war dein schönster Moment mit mir bisher?", listOf("Unser erstes Treffen", "Ein ganz normaler Alltagstag", "Eine gemeinsame Reise", "Ein schwerer Moment, den wir geschafft haben")),
                Question("Was würdest du an einem gemeinsamen Tag am liebsten machen?", listOf("Ausschlafen und faulenzen", "Etwas Neues ausprobieren", "Rausgehen in die Natur", "Freunde treffen")),
                Question("Wie fühlst du dich am meisten geliebt?", listOf("Durch Worte", "Durch Zeit zu zweit", "Durch Berührung", "Durch kleine Gesten")),
                Question("Worauf freust du dich bei uns am meisten?", listOf("Unsere nächste Reise", "Zusammenziehen", "Einfach mehr Alltag", "Alles, was noch kommt"))
            )
        ),

        QuestionPack(
            id = "wergehteher",
            title = "Wer würde eher?",
            tags = listOf("unterhaltung"),
            cat = "wer",
            topic = "aufwaermen",
            type = "quiz",
            questions = listOf(
                Question("Wer würde eher zu spät kommen?", listOf("Ich", "Mein Partner", "Beide gleich", "Keiner von uns")),
                Question("Wer würde eher bei einem Streit als Erstes einlenken?", listOf("Ich", "Mein Partner", "Kommt drauf an", "Wir treffen uns in der Mitte")),
                Question("Wer würde eher spontan eine Reise buchen?", listOf("Ich", "Mein Partner", "Beide zusammen", "Niemand ohne Plan")),
                Question("Wer vergisst eher einen Jahrestag?", listOf("Ich", "Mein Partner", "Keiner", "Beide, aber wir tun so als ob nicht"))
            )
        ),

        QuestionPack(
            id = "nienie",
            title = "Ich habe noch nie",
            tags = listOf("unterhaltung"),
            cat = "nie",
            topic = "kennen",
            type = "quiz",
            questions = listOf(
                Question("Ich habe noch nie … ein Date abgesagt, um zuhause zu bleiben.", listOf("Stimmt, noch nie", "Doch, schon mal", "Öfter als ich zugebe")),
                Question("Ich habe noch nie … heimlich das Handy meines Partners angeschaut.", listOf("Stimmt, noch nie", "Einmal", "Ich würde es nie tun")),
                Question("Ich habe noch nie … eine Nachricht 10x umformuliert.", listOf("Stimmt, noch nie", "Ständig", "Nur bei wichtigen Themen"))
            )
        ),

        QuestionPack(
            id = "tiefe",
            title = "Tiefe Gespräche",
            tags = listOf("unterhaltung"),
            cat = "tief",
            topic = "moral",
            type = "quiz",
            questions = listOf(
                Question("Was bedeutet Vertrauen für dich konkret?", listOf("Dass ich alles erzählen kann", "Dass ich mich nicht sorgen muss", "Dass Zusagen gehalten werden", "Alles davon")),
                Question("Was ist für dich ein absoluter Dealbreaker?", listOf("Lügen", "Respektlosigkeit", "Gleichgültigkeit", "Untreue")),
                Question("Wann fühlst du dich mir am nächsten?", listOf("Beim Reden", "In Stille nebeneinander", "Wenn wir zusammen lachen", "Wenn es schwierig ist")),
                Question("Wovor hast du in unserer Beziehung am meisten Angst?", listOf("Uns auseinanderzuleben", "Missverständnisse", "Die Distanz", "Vor nichts"))
            )
        ),

        QuestionPack(
            id = "geldpack",
            title = "Geld & Finanzen",
            tags = listOf("unterhaltung"),
            cat = "reden",
            topic = "geld",
            type = "disc",
            questions = listOf(
                Question("Führen wir getrennte oder gemeinsame Konten?"),
                Question("Wie gehen wir mit unterschiedlichen Einkommen um?"),
                Question("Wofür sparen wir gemeinsam?"),
                Question("Ab welchem Betrag sprechen wir vor einer Anschaffung?")
            )
        ),

        QuestionPack(
            id = "naehe",
            title = "Nähe & Intimität",
            tags = listOf("unterhaltung"),
            cat = "tief",
            topic = "sex",
            type = "quiz",
            questions = listOf(
                Question("Wann fühlst du dich mir körperlich am nächsten?", listOf("Beim Einschlafen nebeneinander", "Wenn wir uns lange umarmen", "Bei einer spontanen Berührung", "Wenn wir zusammen lachen")),
                Question("Was fehlt dir über die Distanz am meisten?", listOf("Körperliche Nähe", "Einfach nebeneinander sein", "Gemeinsame Nächte", "Alltägliche Berührungen")),
                Question("Wie leicht fällt es dir, über Wünsche zu sprechen?", listOf("Sehr leicht", "Geht so", "Eher schwer", "Ich übe noch")),
                Question("Was macht einen Moment für dich romantisch?", listOf("Aufmerksamkeit", "Überraschung", "Vertrautheit", "Dass wir ungestört sind"))
            )
        ),

        QuestionPack(
            id = "zeichnen",
            title = "Zeichne für mich",
            tags = listOf("unterhaltung"),
            cat = "zeich",
            topic = "hobbys",
            type = "quiz",
            questions = listOf(
                Question("Zeichne unser erstes Date — was gehört unbedingt aufs Bild?", listOf("Der Ort", "Was wir gegessen haben", "Unsere Gesichter", "Das Wetter an dem Tag")),
                Question("Zeichne unser Traumhaus in einem Strich. Was ist das Auffälligste?", listOf("Große Fenster", "Der Garten", "Ein Herz an der Tür", "Zwei Stühle davor")),
                Question("Zeichne mich als Tier — welches wäre ich?", listOf("Katze", "Hund", "Pinguin", "Etwas ganz anderes")),
                Question("Zeichne unser Gefühl zueinander als Symbol.", listOf("Ein Herz", "Zwei Kreise, die sich überschneiden", "Eine Brücke", "Ein Anker"))
            )
        ),

        QuestionPack(
            id = "zustimmen",
            title = "Zustimmen oder Ablehnen",
            tags = listOf("unterhaltung"),
            cat = "zust",
            topic = "moral",
            type = "quiz",
            questions = listOf(
                Question("In einer Beziehung sollte man alles voneinander wissen.", listOf("Stimme voll zu", "Eher zu", "Eher nicht", "Lehne ab")),
                Question("Man sollte nie streitend einschlafen.", listOf("Stimme voll zu", "Eher zu", "Eher nicht", "Lehne ab")),
                Question("Getrennte Urlaube tun einer Beziehung gut.", listOf("Stimme voll zu", "Eher zu", "Eher nicht", "Lehne ab")),
                Question("Eifersucht ist ein Zeichen von Liebe.", listOf("Stimme voll zu", "Eher zu", "Eher nicht", "Lehne ab")),
                Question("Freundschaften mit Ex-Partnern sind okay.", listOf("Stimme voll zu", "Eher zu", "Eher nicht", "Lehne ab"))
            )
        ),

        QuestionPack(
            id = "tagesfragen",
            title = "Tägliche Aktivität",
            tags = listOf("unterhaltung"),
            cat = "tief",
            topic = "aufwaermen",
            type = "disc",
            questions = listOf(
                Question("Wie kann dein Partner ein noch besserer Partner für dich sein?"),
                Question("Kennst du die Essensvorlieben deines Partners?")
            )
        ),

        QuestionPack(
            id = "niealltag",
            title = "Das tägliche Leben",
            tags = listOf("unterhaltung"),
            cat = "nie",
            topic = "kennen",
            type = "quiz",
            questions = listOf(
                Question("Ich bin noch nie in einem Kino eingeschlafen.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie Pläne abgesagt, um zu Hause zu bleiben und eine Fernsehsendung zu sehen.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie in einer Universitätssportmannschaft mitgespielt.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie die nassen Klamotten für ein paar Tage in der Waschmaschine vergessen.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie ein Elektroauto gefahren.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie einen Nachtzug genommen.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich musste noch nie rennen, um einen Anschlussflug zu erwischen.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie ein Auto in einem fremden Land gefahren.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie einen Freund gehabt, der in ein anderes Land gezogen ist.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie einen Freund gefunden, der eine andere Muttersprache spricht.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie einen Social-Media-Beitrag gelöscht, weil er nicht genug Likes bekommen hat.", listOf("Ich habe noch nie!", "Ich habe!")),
                Question("Ich habe noch nie wegen des Finales einer Fernsehsendung geweint.", listOf("Ich habe noch nie!", "Ich habe!"))
            )
        ),

        QuestionPack(
            id = "intimleben",
            title = "Unser Intimleben",
            tags = listOf("reden"),
            cat = "reden",
            topic = "sex",
            type = "disc",
            questions = listOf(
                Question("Wie kann dein Partner am besten Sex mit dir initiieren?"),
                Question("Wie stehst du zu schmutzigem Gerede beim Sex?"),
                Question("Was ist das Wichtigste, das du bei einer sexuellen Begegnung suchst?"),
                Question("Was hältst du davon, gemeinsam erotische Inhalte anzuschauen?"),
                Question("Wie zeigst du deine Zuneigung am liebsten außerhalb von Sex?"),
                Question("Beschreibe unser Sexleben mit einem Emoji.")
            )
        ),

        QuestionPack(
            id = "unbeliebt",
            title = "Unbeliebte Meinungen",
            tags = listOf("unterhaltung"),
            cat = "zust",
            topic = "moral",
            type = "disc",
            questions = listOf(
                Question("Ananas auf der Pizza schmeckt köstlich."),
                Question("Berühmte Touristenorte sind immer eine Enttäuschung."),
                Question("Geister existieren wirklich."),
                Question("Fernsehwerbung ist manchmal interessant anzusehen."),
                Question("Eine neue Sprache zu lernen ist einfach."),
                Question("Denselben Film zweimal zu schauen, ist Zeitverschwendung.")
            )
        ),

        QuestionPack(
            id = "ehepaar",
            title = "Ehepaar Leben",
            tags = listOf("unterhaltung"),
            cat = "wer",
            topic = "beziehung",
            type = "disc",
            questions = listOf(
                Question("Wer ist romantischer?"),
                Question("Wer ist der beste Tänzer?"),
                Question("Wer gibt mehr Herzlichkeit?"),
                Question("Wer hat den besseren Musikgeschmack?"),
                Question("Wer findet die besten Restaurants?"),
                Question("Wer hängt mehr an seinen Eltern?"),
                Question("Wer ist besser organisiert?"),
                Question("Wer ist der Beste bei der Filmauswahl?"),
                Question("Wer kocht besser?"),
                Question("Wer ist der Beste bei der Planung romantischer Dates?")
            )
        ),

        QuestionPack(
            id = "gespraechsanreger",
            title = "Gesprächsanreger",
            tags = listOf("reden"),
            cat = "reden",
            topic = "beziehung",
            type = "disc",
            questions = listOf(
                Question("Was möchtest du, dass dein Partner öfter tut?"),
                Question("Was ist dein Lieblingsfoto von uns? 📸"),
                Question("Welches Lied macht dich an? 🥵"),
                Question("Was magst du an deinem Partner am liebsten?"),
                Question("Was ist deine größte Angst vor dem Zusammenleben?"),
                Question("Was hast du von deinem Partner gelernt?")
            )
        ),

        QuestionPack(
            id = "liebegleichgewicht",
            title = "Liebe im Gleichgewicht",
            tags = listOf("dasoderdas"),
            cat = "lieber",
            topic = "beziehung",
            type = "tot",
            pairs = listOf(
                "Lass dich von deinem Partner inspirieren, dein bestes Selbst zu sein" to "Werde so akzeptiert, wie du bist",
                "Ein Jahr lang eine Fernbeziehung führen" to "Einen Monat lang überhaupt nicht miteinander reden",
                "Intime Momente nur dann zu haben, wenn dein Partner sie initiiert" to "Alle intimen Momente selbst initiieren",
                "Deine tiefsten Geheimnisse lieber mit deinem Partner teilen" to "Einige Dinge für dich behalten",
                "Eine Million Dollar gewinnen" to "Eine Million Dollar verdienen",
                "Einen sehr emotionalen Partner haben" to "Einen sehr logischen Partner haben",
                "Deine Beziehung stabil und sicher machen" to "Deine Beziehung abenteuerlich und spontan machen",
                "Deinen besten Freund verlieren" to "Alle deine Freunde verlieren, außer deinem besten Freund",
                "Teile alle deine Hobbys mit deinem Partner" to "Von deinem Partner in neue Hobbys eingeführt werden",
                "Derjenige sein, der umarmt wird" to "Diejenige sein, die umarmt",
                "Verbringe die Feiertage mit deiner Familie" to "Die Feiertage mit der Familie deines Partners verbringen"
            )
        ),

        QuestionPack(
            id = "neueliebe",
            title = "Neue Liebe, neue Erfahrungen",
            tags = listOf("unterhaltung"),
            cat = "zust",
            topic = "beziehung",
            type = "disc",
            questions = listOf(
                Question("Ich lache viel mit meinem Partner."),
                Question("Ich entdecke gerne neue Hobbys oder Aktivitäten mit meinem Partner."),
                Question("Ich teile gerne Memes und Witze mit meinem Partner."),
                Question("Ich priorisiere es, Zeit mit meinem Partner zu verbringen, auch wenn ich beschäftigt bin."),
                Question("Ich schreibe meinem Partner jeden Tag."),
                Question("Ich teile gerne alle Details meines Tages mit meinem Partner."),
                Question("Ich lerne gerne die Hobbys und Interessen meines Partners kennen."),
                Question("Es nervt mich, wenn mein Partner zu lange braucht, um auf Nachrichten zu antworten."),
                Question("Ich plane gerne Dates mit meinem Partner."),
                Question("Ich liebe es, Filme mit meinem Partner zu schauen, die keiner von uns zuvor gesehen hat."),
                Question("Ich liebe es, meine Lieblingsmusik mit meinem Partner zu teilen."),
                Question("Romantische Gesten wie Nachrichten, Zettelchen oder Geschenke machen mich glücklich."),
                Question("Ich bevorzuge romantische Spaziergänge im Park gegenüber einem Kinobesuch."),
                Question("Ich genieße es, mit meinem Partner neue Restaurants auszuprobieren, anstatt immer an die gleichen Orte zu gehen.")
            )
        ),

        QuestionPack(
            id = "liebervideo",
            title = "Was magst du lieber?",
            tags = listOf("dasoderdas"),
            cat = "lieber",
            topic = "aufwaermen",
            type = "tot",
            pairs = listOf(
                "Ein Filmabend" to "Ein Spieleabend",
                "Ein aktives Abenteuer" to "Ein entspannender Spa-Tag",
                "Ein gemütliches Date drinnen" to "Eine Autoreise",
                "Einen Film anschauen" to "Gemeinsam ein Lego bauen",
                "Eine Weinverkostung" to "Eine Schokoladenverkostung",
                "Eine gemütliche Nacht zu Hause" to "Ein Abenteuer in einer neuen Stadt",
                "Zu einem Picknick gehen" to "Zu einem ausgefallenen Abendessen gehen",
                "Ein romantischer Abend" to "Eine Nacht in einem Club",
                "Eine Date-Nacht unter den Sternen" to "Ein romantisches Abendessen bei Kerzenlicht",
                "In einen Coffeeshop gehen" to "In eine Bar gehen",
                "Ein gemütlicher Abend im Haus während eines Gewitters" to "Ein tolles Date im Freien unter dem Mondlicht",
                "Einen Vergnügungspark erkunden" to "Ein Museum besuchen",
                "Ein Spieleabend mit Freunden" to "Ein romantisches Picknick an einem schönen Ort",
                "Gemeinsam in der Küche ein neues Rezept ausprobieren" to "In einem feinen Restaurant essen gehen",
                "Zelten gehen" to "Einen Wellness-Tag",
                "Einen Tanzkurs besuchen" to "Eine Wanderung mit Panoramablick machen",
                "Spiele spielen und Spaß haben" to "Tiefgründige Gespräche führen",
                "Ein Live-Musik-Konzert besuchen" to "Auf eine Bootsparty gehen",
                "Geh früh am Morgen, um den Sonnenaufgang zu sehen" to "In eine Strandbar gehen, um den Sonnenuntergang zu sehen"
            )
        )
    )

    val PACKS: List<QuestionPack>
        get() {
            val result = DEFAULT_PACKS.toMutableList()
            for (dp in dynamicPacks) {
                val idx = result.indexOfFirst { it.id == dp.id }
                if (idx >= 0) {
                    result[idx] = dp
                } else {
                    result.add(0, dp)
                }
            }
            return result
        }
}
