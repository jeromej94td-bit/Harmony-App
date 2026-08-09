# Harmony App editing rules

- Use exact file edits and inspect the original code before changing it.
- Before changing a composable function signature, search every call site and update all calls deliberately.
- Prefer the existing CompositionLocal language system (LocalAppLanguage, tr, and contentText) over threading new language parameters through the UI tree.
- Never use chained global search-and-replace commands for Kotlin refactors.
- After each edit, inspect the affected block and check for duplicate parameters, broken function declarations, imports, and string escaping.
- Keep German source content intact for German mode; add explicit English UI/content mappings for English mode.
- Never translate individual words as a fallback. Every customer-facing translation must use a reviewed full-string entry or a locale-specific dynamic template.
- A new language may appear in the profile selector only after its complete locale pack is registered in `TranslationCatalog`.
- When adding a language, audit categories, topics, every pack title, every question, every answer option, every image-pair label, dialogs, toasts, accessibility descriptions, AI prompts, and generated content.
- Treat user-authored content as user data: do not silently rewrite it.
