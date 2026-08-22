# 41 Complete Harmony Locales Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the 41 approved languages as complete offline Harmony production locales and keep all existing locales green.

**Architecture:** Generalize the existing generated-locale pipeline instead of creating 41 one-off implementations. A shared locale registry drives AppLanguage, TranslationCatalog wiring, per-language Kotlin catalogs, completeness tests, RTL metadata, and a matrix-based GitHub Actions generation pipeline. Parallel jobs produce artifacts; one aggregation job commits generated catalogs once, then runs all audits and Kotlin compilation.

**Tech Stack:** Kotlin/Android Compose, Python 3 localization tooling, GitHub Actions, existing TranslationCatalog/GeneratedLocaleSupport architecture.

**Spec:** `docs/superpowers/specs/2026-08-22-add-41-complete-locales-design.md`

## Global Constraints
- Add exactly: hu, ro, bg, uk, ru, el, tr, ar, he, fa, hi, bn, ur, ta, te, mr, gu, kn, ml, th, vi, id, ms, fil, my, km, lo, sw, af, am, yo, ig, ha, zu, xh, so, et, lv, lt, sl, sr.
- Serbian uses Cyrillic (`sr`).
- ar/he/fa/ur are RTL.
- No runtime translation API.
- Preserve Kotlin interpolation, `{...}` and printf placeholders exactly.
- Developer Studio / Developer Mode remains excluded from customer localization scope.
- Merge only after source audit, effective/runtime audit, RTL verification, and `:app:compileDebugKotlin` all pass.

---

### Task 1: Registry and failing 41-locale readiness test

**Files:**
- Create: `scripts/production_locale_registry.py`
- Create: `scripts/verify_41_new_locales.py`
- Modify: `app/src/main/java/com/example/ui/Language.kt`
- Modify: `app/src/main/java/com/example/ui/TranslationCatalog.kt`
- Modify: `app/src/main/java/com/example/ui/introspection/IntrospectionStrings.kt`

**Interfaces:**
- Produces Python `NEW_LOCALES` metadata consumed by generator/audit tooling.
- Produces Kotlin enum entries and runtime routes for all 41 locale codes.

- [ ] Write `verify_41_new_locales.py` first. It must fail unless every approved code has an AppLanguage entry, exact catalog symbol, dynamic localizer route, Introspection route, non-empty catalog file, and RTL metadata for ar/he/fa/ur.
- [ ] Run the test in CI and confirm RED on the branch.
- [ ] Add the 41 `AppLanguage` entries using native language names and flags.
- [ ] Add exact/dynamic TranslationCatalog routing and Introspection routing.
- [ ] Re-run readiness; it may still fail only because catalog files are not generated yet.

### Task 2: Generic catalog generator with placeholder safety

**Files:**
- Create: `scripts/generate_production_locale_catalogs.py`
- Modify: `scripts/repair_build_blockers.py`
- Modify: `scripts/audit_localization.py`
- Modify: `scripts/verify_localization_repair.py`

**Interfaces:**
- `generate_production_locale_catalogs.py --codes <comma-separated>` generates `<LocaleName>Content.kt` for requested codes only.
- Each generated file exposes `EXACT_<LOCALE>_CONTENT` and `localize<Locale>DynamicContent(text: String): String?`.

- [ ] Add generator tests inside `verify_41_new_locales.py` for protected `${...}`, `{...}`, `%s/%1$s`, escaped newlines, brand names, and Kotlin dollar escaping.
- [ ] Implement generic protection/restoration and Kotlin escaping by reusing the proven six-locale generator behavior.
- [ ] Add reviewed high-visibility overrides for every language for core navigation/game/introspection terms.
- [ ] Extend `repair_build_blockers.py` so all generated locale files normalize repeated backslashes before `$` to exactly one Kotlin escape.
- [ ] Extend source/effective audit locale registries to all 41 codes.

### Task 3: Parallel generation workflow

**Files:**
- Modify: `.github/workflows/localization-audit.yml`

**Interfaces:**
- Matrix generation jobs create artifacts containing only generated Kotlin locale files.
- Aggregation job downloads all artifacts with `merge-multiple: true`, runs repair/readiness/audits/compile, then commits deterministic generated files to the PR branch once.

- [ ] Replace single six-locale generation with 8 balanced locale batches.
- [ ] Each matrix job runs audit discovery and `generate_production_locale_catalogs.py --codes ...`, then uploads `*Content.kt` files for its codes.
- [ ] Aggregator checks out PR head, downloads all catalog artifacts, repairs escapes, runs readiness, source audit, effective audit, RTL test and Kotlin compile.
- [ ] Aggregator commits generated catalogs only after all checks pass.

### Task 4: RTL production verification

**Files:**
- Create: `scripts/verify_rtl_locales.py`
- Modify: `app/src/main/java/com/example/ui/Language.kt` if an explicit `isRtl` property is needed.

**Interfaces:**
- `verify_rtl_locales.py` exits non-zero unless ar/he/fa/ur are marked RTL, Manifest has `android:supportsRtl="true"`, and no other new locale is accidentally marked RTL.

- [ ] Write failing RTL verification first.
- [ ] Add explicit RTL metadata to AppLanguage or a stable helper used by UI/tests.
- [ ] Make RTL verification green.

### Task 5: Final verification and merge

**Files:**
- Update PR description with final coverage and CI evidence.

- [ ] Confirm all 41 catalog files contain every current canonical customer key.
- [ ] Confirm readiness, source audit, effective/runtime audit and RTL verification are green.
- [ ] Confirm `gradle :app:compileDebugKotlin --no-daemon` succeeds.
- [ ] Mark PR ready for review.
- [ ] Merge the exact green head SHA into `main`.
