package com.example.ui

/** Shared dynamic localization for generated production catalogs. */
internal fun localizeGeneratedLocaleDynamicContent(
    text: String,
    exact: Map<String, String>
): String? {
    Regex("""^([\p{So}\p{Sk}\uFE0F\u200D]+\s+)(.+)$""").matchEntire(text)?.let { match ->
        exact[match.groupValues[2]]?.let { return match.groupValues[1] + it }
    }
    Regex("""^(\d+[.)]\s+)(.+)$""").matchEntire(text)?.let { match ->
        exact[match.groupValues[2]]?.let { return match.groupValues[1] + it }
    }

    // Resolve variable-bearing catalog templates while preserving names/counts exactly.
    for ((source, target) in exact) {
        val tokens = Regex("""(\\?\${'$'}\{[^}]+\}|\{[^}]+\})""").findAll(source).map { it.value }.toList()
        if (tokens.isEmpty()) continue
        var cursor = 0
        val pattern = StringBuilder("^")
        for (token in tokens) {
            val index = source.indexOf(token, cursor)
            pattern.append(Regex.escape(source.substring(cursor, index)))
            pattern.append("(.+?)")
            cursor = index + token.length
        }
        pattern.append(Regex.escape(source.substring(cursor))).append('$')
        val match = Regex(pattern.toString()).matchEntire(text) ?: continue
        var localized = target
        tokens.forEachIndexed { index, token ->
            localized = localized.replace(token, match.groupValues[index + 1])
        }
        return localized
    }

    if (text.contains(" · ")) {
        val parts = text.split(" · ")
        val translated = parts.map { exact[it] ?: it }
        if (translated != parts) return translated.joinToString(" · ")
    }
    return null
}
