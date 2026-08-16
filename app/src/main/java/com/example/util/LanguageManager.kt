package com.example.util

import com.example.data.model.Category
import com.example.data.model.Question
import com.example.data.model.QuestionPack
import com.example.data.model.Topic
import com.example.ui.AppLanguage
import com.example.ui.TranslationCatalog

object LanguageManager {

    fun tr(text: String, lang: String): String {
        val appLang = AppLanguage.fromCode(lang)
        if (appLang == AppLanguage.GERMAN) return text
        val translated = TranslationCatalog.getTranslation(text, appLang)
        return if (translated.isNotEmpty()) translated else text
    }

    fun translateCategory(cat: Category, lang: String): Category {
        if (lang == "de") return cat
        return cat.copy(name = tr(cat.name, lang))
    }

    fun translateTopic(topic: Topic, lang: String): Topic {
        if (lang == "de") return topic
        return topic.copy(name = tr(topic.name, lang))
    }

    fun translatePack(pack: QuestionPack, lang: String): QuestionPack {
        if (lang == "de") return pack
        val translatedTitle = tr(pack.title, lang)
        val translatedQuestions = pack.questions.map { q ->
            Question(
                q = tr(q.q, lang),
                options = q.options.map { tr(it, lang) },
                defaultMine = q.defaultMine?.let { tr(it, lang) }
            )
        }
        val translatedPairs = pack.pairs.map { pair ->
            Pair(tr(pair.first, lang), tr(pair.second, lang))
        }
        return pack.copy(
            title = translatedTitle,
            questions = translatedQuestions,
            pairs = translatedPairs
        )
    }
}
