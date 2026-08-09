# Harmony App editing rules

- Use exact file edits and inspect the original code before changing it.
- Before changing a composable function signature, search every call site and update all calls deliberately.
- Prefer the existing CompositionLocal language system (LocalAppLanguage, tr, and contentText) over threading new language parameters through the UI tree.
- Never use chained global search-and-replace commands for Kotlin refactors.
- After each edit, inspect the affected block and check for duplicate parameters, broken function declarations, imports, and string escaping.
- Keep German source content intact for German mode; add explicit English UI/content mappings for English mode.
- Treat user-authored content as user data: do not silently rewrite it.
