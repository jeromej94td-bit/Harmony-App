# Harmony localization

Harmony keeps German as the stable source language and translates complete display strings through locale packs. It intentionally has no word-by-word fallback, because partial substitutions create mixed sentences.

## Add another language

1. Add one `AppLanguage` entry in `ui/Language.kt` with its BCP-47 code, native name, and English name.
2. Create a reviewed exact-string map beside `EnglishContent.kt`, using the German source strings as keys.
3. Add locale-specific handlers for variable-bearing messages such as counts, partner names, and import progress.
4. Register both in `TranslationCatalog.kt`.

The profile selector is data-driven and only shows registered packs. Existing `tr(german, english)` calls also consult the active locale pack first, so adding Italian, Spanish, or another language does not require adding another parameter to every composable.

## Required coverage

A production language pack must cover all navigation and screen copy, categories and topics, pack titles, questions, answer options, image-pair labels, dialogs, toasts, accessibility descriptions, generated content, and AI output language instructions. User-authored text and internal IDs must remain unchanged.
