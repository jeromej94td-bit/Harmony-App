package com.example.data

import com.example.data.model.AnswerEntity
import com.example.data.model.QuestionPack
import com.example.ui.components.TotImageProvider
import org.json.JSONArray
import org.json.JSONObject

/**
 * Die Ketten-Engine.
 *
 * Idee: Was jemand in einem früheren Spiel gewählt hat, wird zur Zutat einer
 * neuen Frage. Wer bei "Paris oder Rom" auf Rom getippt hat, bekommt später
 * "1 Jahr lang in Rom" gegen "6 Monate im Altbau mit Charme" — mit den Fotos,
 * die schon zu Rom und zum Altbau gehören.
 *
 * Ein Ketten-Paket speichert keine fertigen Paare, sondern Bauanleitungen.
 * Die Paare entstehen bei jedem Rebuild neu aus den aktuellen Antworten.
 */
object LinkEngine {

    // Woher kommt der Text für eine Seite?
    const val SRC_PICKED = "PICKED"    // was der Nutzer gewählt hat
    const val SRC_DROPPED = "DROPPED"  // was er stehen gelassen hat
    const val SRC_OPTION = "OPTION"    // eine feste Option aus einem Paket
    const val SRC_TEXT = "TEXT"        // freier Text

    /** Platzhalter im Template. */
    const val PLACEHOLDER = "{}"

    data class LinkSlot(
        val source: String = SRC_PICKED,
        val packId: String = "",
        /** -1 = irgendein beantwortetes Paar aus dem Paket */
        val pairIndex: Int = -1,
        /** nur für OPTION: 0 = linke Seite, 1 = rechte Seite */
        val side: Int = 0,
        val text: String = ""
    )

    data class LinkStep(
        val templateA: String = PLACEHOLDER,
        val slotA: LinkSlot = LinkSlot(),
        val templateB: String = PLACEHOLDER,
        val slotB: LinkSlot = LinkSlot(source = SRC_OPTION),
        /** Zeile über den Karten. {A} und {B} werden ersetzt. */
        val caption: String = ""
    )

    data class LinkPack(
        val id: String,
        val title: String,
        val cat: String,
        val steps: List<LinkStep>
    )

    /** Ergebnis einer aufgelösten Seite: was dasteht und welches Bild dazugehört. */
    data class Resolved(
        val display: String,
        val imageKey: String,
        val fromAnswer: Boolean
    )

    // -----------------------------------------------------------------
    // Zustand
    // -----------------------------------------------------------------

    /** packId -> (pairIndex -> gewählter Text) */
    private var answerMap: Map<String, Map<Int, String>> = emptyMap()

    /** packId -> Zeile pro Schritt, für die Kopfzeile im Runner */
    private val captions = mutableMapOf<String, List<String>>()

    private var answerSignature: Int = Int.MIN_VALUE

    /** Gibt true zurück, wenn sich wirklich etwas geändert hat. */
    fun setAnswers(answers: List<AnswerEntity>): Boolean {
        val signature = answers.fold(7) { acc, a ->
            acc * 31 + a.packId.hashCode() * 31 + a.questionIndex * 31 + a.answerText.hashCode()
        } * 31 + answers.size
        if (signature == answerSignature) return false
        answerSignature = signature
        answerMap = answers.groupBy { it.packId }
            .mapValues { entry -> entry.value.associate { it.questionIndex to it.answerText } }
        return true
    }

    /** Erzwingt beim nächsten setAnswers einen Neuaufbau. */
    fun invalidateAnswers() {
        answerSignature = Int.MIN_VALUE
    }

    fun answersFor(packId: String): Map<Int, String> = answerMap[packId] ?: emptyMap()

    fun hasAnswers(packId: String): Boolean = !answerMap[packId].isNullOrEmpty()

    /** Kopfzeile für einen Schritt, oder null wenn das kein Ketten-Paket ist. */
    fun captionFor(packId: String, index: Int): String? =
        captions[packId]?.getOrNull(index)?.takeIf { it.isNotBlank() }

    fun isLinkPack(packId: String): Boolean = captions.containsKey(packId)

    // -----------------------------------------------------------------
    // Auflösen
    // -----------------------------------------------------------------

    fun resolveSlot(slot: LinkSlot, packs: List<QuestionPack>): Resolved {
        if (slot.source == SRC_TEXT) {
            val t = slot.text.trim().ifEmpty { "…" }
            return Resolved(t, t, false)
        }

        val pack = packs.find { it.id == slot.packId }
            ?: return Resolved("…", "", false)
        if (pack.pairs.isEmpty()) return Resolved("…", "", false)

        val given = answersFor(pack.id)

        val index = when {
            slot.pairIndex in pack.pairs.indices -> slot.pairIndex
            given.isNotEmpty() -> given.keys.filter { it in pack.pairs.indices }.minOrNull() ?: 0
            else -> 0
        }
        val pair = pack.pairs[index]

        return when (slot.source) {
            SRC_OPTION -> {
                val t = if (slot.side == 1) pair.second else pair.first
                Resolved(t, t, false)
            }

            SRC_DROPPED -> {
                val picked = given[index]
                val t = when (picked) {
                    pair.first -> pair.second
                    pair.second -> pair.first
                    else -> pair.second
                }
                Resolved(t, t, picked != null)
            }

            else -> { // SRC_PICKED
                val picked = given[index]
                val t = picked ?: pair.first
                Resolved(t, t, picked != null)
            }
        }
    }

    fun applyTemplate(template: String, value: String): String {
        val t = template.ifBlank { PLACEHOLDER }
        return if (t.contains(PLACEHOLDER)) t.replace(PLACEHOLDER, value).trim()
        else "$t $value".trim()
    }

    // -----------------------------------------------------------------
    // Materialisieren
    // -----------------------------------------------------------------

    /**
     * Baut aus einer Bauanleitung ein echtes "Das oder Das"-Paket und meldet
     * die passenden Bilder als Alias an, damit die Fotos automatisch mitkommen.
     */
    fun materialize(linkPack: LinkPack, packs: List<QuestionPack>): QuestionPack {
        val pairs = mutableListOf<Pair<String, String>>()
        val stepCaptions = mutableListOf<String>()

        linkPack.steps.forEach { step ->
            val a = resolveSlot(step.slotA, packs)
            val b = resolveSlot(step.slotB, packs)

            val textA = applyTemplate(step.templateA, a.display)
            val textB = applyTemplate(step.templateB, b.display)

            if (a.imageKey.isNotBlank() && textA != a.imageKey) {
                TotImageProvider.setAlias(textA, a.imageKey)
            }
            if (b.imageKey.isNotBlank() && textB != b.imageKey) {
                TotImageProvider.setAlias(textB, b.imageKey)
            }

            pairs.add(textA to textB)
            stepCaptions.add(
                step.caption
                    .replace("{A}", a.display)
                    .replace("{B}", b.display)
                    .trim()
            )
        }

        captions[linkPack.id] = stepCaptions

        return QuestionPack(
            id = linkPack.id,
            title = linkPack.title,
            tags = listOf("dasoderdas", "unterhaltung"),
            cat = linkPack.cat,
            topic = "reisen",
            type = "tot",
            questions = emptyList(),
            pairs = pairs
        )
    }

    fun clearCaptions() {
        captions.clear()
    }

    /** Vorschau für den Builder: was stünde gerade auf den beiden Karten? */
    fun previewStep(step: LinkStep, packs: List<QuestionPack>): Triple<Resolved, Resolved, String> {
        val a = resolveSlot(step.slotA, packs)
        val b = resolveSlot(step.slotB, packs)
        val caption = step.caption
            .replace("{A}", a.display)
            .replace("{B}", b.display)
            .trim()
        return Triple(
            a.copy(display = applyTemplate(step.templateA, a.display)),
            b.copy(display = applyTemplate(step.templateB, b.display)),
            caption
        )
    }

    /** Kurzbeschreibung eines Slots für die Auswahlliste. */
    fun describeSlot(slot: LinkSlot, packs: List<QuestionPack>): String {
        if (slot.source == SRC_TEXT) {
            return "Fester Text · " + slot.text.ifBlank { "leer" }
        }
        val pack = packs.find { it.id == slot.packId } ?: return "Kein Paket gewählt"
        val label = when (slot.source) {
            SRC_PICKED -> "Gewählte Antwort"
            SRC_DROPPED -> "Verworfene Antwort"
            else -> "Feste Option"
        }
        val pairPart = if (slot.pairIndex in pack.pairs.indices) {
            val p = pack.pairs[slot.pairIndex]
            when (slot.source) {
                SRC_OPTION -> if (slot.side == 1) p.second else p.first
                else -> "${p.first} / ${p.second}"
            }
        } else {
            "beliebiges Paar"
        }
        return "$label · ${pack.title} · $pairPart"
    }

    // -----------------------------------------------------------------
    // JSON
    // -----------------------------------------------------------------

    fun slotToJson(slot: LinkSlot): JSONObject = JSONObject()
        .put("source", slot.source)
        .put("packId", slot.packId)
        .put("pairIndex", slot.pairIndex)
        .put("side", slot.side)
        .put("text", slot.text)

    fun slotFromJson(obj: JSONObject): LinkSlot = LinkSlot(
        source = obj.optString("source", SRC_PICKED),
        packId = obj.optString("packId", ""),
        pairIndex = obj.optInt("pairIndex", -1),
        side = obj.optInt("side", 0),
        text = obj.optString("text", "")
    )

    fun packToJson(pack: LinkPack): JSONObject {
        val steps = JSONArray()
        pack.steps.forEach { s ->
            steps.put(
                JSONObject()
                    .put("templateA", s.templateA)
                    .put("slotA", slotToJson(s.slotA))
                    .put("templateB", s.templateB)
                    .put("slotB", slotToJson(s.slotB))
                    .put("caption", s.caption)
            )
        }
        return JSONObject()
            .put("id", pack.id)
            .put("title", pack.title)
            .put("cat", pack.cat)
            .put("steps", steps)
    }

    fun packFromJson(obj: JSONObject): LinkPack {
        val steps = mutableListOf<LinkStep>()
        obj.optJSONArray("steps")?.let { arr ->
            for (i in 0 until arr.length()) {
                val s = arr.getJSONObject(i)
                steps.add(
                    LinkStep(
                        templateA = s.optString("templateA", PLACEHOLDER),
                        slotA = slotFromJson(s.optJSONObject("slotA") ?: JSONObject()),
                        templateB = s.optString("templateB", PLACEHOLDER),
                        slotB = slotFromJson(s.optJSONObject("slotB") ?: JSONObject()),
                        caption = s.optString("caption", "")
                    )
                )
            }
        }
        return LinkPack(
            id = obj.getString("id"),
            title = obj.optString("title", "Kette"),
            cat = obj.optString("cat", "tot"),
            steps = steps
        )
    }
}
