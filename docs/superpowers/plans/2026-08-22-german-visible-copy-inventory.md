# German Visible-Copy Inventory Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a deterministic, reviewable German source-of-truth inventory for every Harmony-owned production-visible text outside Developer Studio, publish the real current counts, and make CI fail when new visible copy bypasses that inventory.

**Architecture:** A Python inventory pipeline scans production Kotlin/Compose, product-content sources, Android XML/resources, SVG assets, and a reviewed bitmap/route manifest. Every discovered render occurrence is normalized into a stable `VisibleCopyOccurrence`, deduplicated into canonical German units, and written to a committed JSON report with exact metrics. Developer Studio is excluded by explicit policy; brands remain visible inventory entries but are marked exempt from translation. This plan intentionally stops after the German baseline is complete and locked; Japanese is implemented in a separate follow-up plan against this generated baseline.

**Tech Stack:** Python 3 standard library, Kotlin/Jetpack Compose source scanning, Android XML, SVG/XML parsing, JSON, existing GitHub Actions localization workflow, Gradle/Kotlin compile gate.

**Spec:** `docs/superpowers/specs/2026-08-22-complete-visible-copy-inventory-japanese-design.md`

## Global Constraints

- Start from production `main` commit `396f8f0623a021ddbe15365c987b2106db8e500a`.
- German is the canonical localization source; `EXACT_ENGLISH_CONTENT` must not define completeness.
- Include every Harmony-owned production-visible text, question, option, game instruction/result, dynamic template, resource string, widget/notification copy, SVG text and reviewed bitmap text.
- Exclude Developer Studio / developer mode and its internal tooling copy explicitly.
- Keep internal IDs/debug/test-only metadata separate from customer copy.
- Brand names and brand-logo text stay unchanged and are recorded as `brand` exemptions, not translated.
- Preserve placeholder signatures exactly in inventory units.
- No OCR dependency is introduced; bitmap text is handled through an explicit reviewed asset registry because OCR cannot be a deterministic CI source of truth.
- The persisted report must publish unique visible units, unique translatable German units, exempt visible units, visible render occurrences, and German word count deterministically.
- Do not modify Japanese or other locale content in this plan.

---

## File Structure

**Create**
- `scripts/visible_copy_inventory.py` — inventory engine and CLI.
- `scripts/visible_copy_policy.json` — explicit Dev/internal/brand policy.
- `scripts/visible_copy_asset_registry.json` — reviewed bitmap/SVG classification.
- `scripts/visible_copy_routes.json` — normal-user route coverage manifest.
- `scripts/test_visible_copy_inventory.py` — TDD/regression tests.
- `localization/visible-copy-inventory.de.json` — generated canonical German inventory.
- `localization/visible-copy-inventory-summary.md` — human-readable exact metrics and source breakdown.

**Modify**
- `scripts/audit_localization.py` — stop deriving the canonical baseline from English.
- `.github/workflows/localization-audit.yml` — regenerate and verify the German inventory in CI.

**Production source coverage**
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/ui/**/*.kt`, excluding Dev Studio by policy
- `app/src/main/java/com/example/data/**/*.kt`, including `GeneratedHarmonyContent.kt`, excluding Dev-only files by policy
- `app/src/main/java/com/example/widget/**/*.kt`
- `app/src/main/res/values/**/*.xml`, `layout/**/*.xml`, `xml/**/*.xml`, `drawable/**/*.xml`
- `app/src/main/assets/**/*.svg`
- production raster assets under `app/src/main/res` and `app/src/main/assets` through the reviewed asset registry

---

### Task 1: Define the inventory contract and RED tests

**Files:**
- Create: `scripts/test_visible_copy_inventory.py`
- Create: `scripts/visible_copy_policy.json`
- Create: `scripts/visible_copy_routes.json`
- Create: `scripts/visible_copy_asset_registry.json`

**Interfaces:**
- Consumes: repository-relative source paths and literal/template text.
- Produces tests expecting:
  - `discover_repository(root: Path) -> InventoryReport`
  - `normalize_visible_text(text: str) -> str`
  - `extract_placeholders(text: str) -> tuple[str, ...]`
  - `write_report(report: InventoryReport, path: Path) -> None`

- [ ] **Step 1: Write the failing contract test**

Create `scripts/test_visible_copy_inventory.py`:

```python
from pathlib import Path
import tempfile
import unittest

from visible_copy_inventory import (
    discover_repository,
    extract_placeholders,
    normalize_visible_text,
    write_report,
)

ROOT = Path(__file__).resolve().parents[1]

class VisibleCopyInventoryTests(unittest.TestCase):
    def test_normalization_preserves_visible_sentence(self):
        self.assertEqual(
            normalize_visible_text("  Möchtest du wirklich zurück?  "),
            "Möchtest du wirklich zurück?",
        )

    def test_placeholder_signature_is_stable(self):
        self.assertEqual(
            extract_placeholders("${profile.partnerName}, noch {count} Fragen"),
            ("${profile.partnerName}", "{count}"),
        )

    def test_repository_discovers_android_strings(self):
        report = discover_repository(ROOT)
        texts = {unit["german"] for unit in report.units}
        self.assertIn("Überspringen", texts)
        self.assertIn("Das oder das?", texts)

    def test_repository_discovers_game_content_beyond_legacy_catalog(self):
        report = discover_repository(ROOT)
        locations = [occ["path"] for occ in report.occurrences]
        self.assertTrue(any(path.endswith("GeneratedHarmonyContent.kt") for path in locations))

    def test_dev_studio_is_explicitly_excluded(self):
        report = discover_repository(ROOT)
        self.assertFalse(any("DevStudioScreen.kt" in occ["path"] for occ in report.occurrences))

    def test_report_is_deterministic(self):
        report = discover_repository(ROOT)
        with tempfile.TemporaryDirectory() as tmp:
            a = Path(tmp) / "a.json"
            b = Path(tmp) / "b.json"
            write_report(report, a)
            write_report(report, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())

if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 2: Add the exact initial policy**

Create `scripts/visible_copy_policy.json`:

```json
{
  "exclude_path_globs": [
    "app/src/main/java/com/example/ui/screens/DevStudioScreen.kt",
    "app/src/main/java/com/example/data/Dev*.kt",
    "app/src/main/java/com/example/data/DeveloperDataManager.kt"
  ],
  "excluded_exact_visible_text": [
    "Entwickler Studio Öffnen",
    "Entwickler-Modus",
    "🛠️ Entwickler-Modus",
    "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen"
  ],
  "internal_exact_literals": [
    "aufwaermen", "dasoderdas", "ichhabenochnie", "werwuerde",
    "tot", "wer", "nie", "tief", "reden", "zeich", "zust", "lieber", "foto"
  ],
  "brand_literals": [
    "Coca-Cola", "Pepsi", "Netflix", "McDonald’s", "Burger King",
    "PlayStation", "Xbox", "iPhone", "Android", "Nike", "Adidas",
    "Spotify", "YouTube Music", "IKEA", "Amazon", "Disney", "Studio Ghibli"
  ],
  "technical_literal_patterns": [
    "^[a-z0-9_.-]+$",
    "^https?://",
    "^[A-Fa-f0-9]{16,}$"
  ]
}
```

- [ ] **Step 3: Add the exact route manifest**

Create `scripts/visible_copy_routes.json`:

```json
{
  "routes": [
    {"id": "home", "source_paths": ["app/src/main/java/com/example/ui/screens/HomeScreen.kt"]},
    {"id": "games", "source_paths": ["app/src/main/java/com/example/ui/screens/GamesScreen.kt"]},
    {"id": "pack-list", "source_paths": ["app/src/main/java/com/example/ui/screens/PackListScreen.kt"]},
    {"id": "quiz-runner", "source_paths": ["app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"]},
    {"id": "panda-either-or", "source_paths": ["app/src/main/java/com/example/ui/screens/PandaEitherOrScreen.kt"]},
    {"id": "introspection", "source_paths": ["app/src/main/java/com/example/ui/screens/IntrospectionGameScreen.kt", "app/src/main/java/com/example/ui/introspection/IntrospectionStrings.kt"]},
    {"id": "chat", "source_paths": ["app/src/main/java/com/example/ui/screens/ChatScreen.kt"]},
    {"id": "moments", "source_paths": ["app/src/main/java/com/example/ui/screens/MomentsScreen.kt"]},
    {"id": "profile", "source_paths": ["app/src/main/java/com/example/ui/screens/ProfileSheet.kt"]},
    {"id": "navigation", "source_paths": ["app/src/main/java/com/example/MainActivity.kt", "app/src/main/java/com/example/ui/components/CommonUI.kt"]},
    {"id": "dialogs", "source_paths": ["app/src/main/java/com/example/ui/components/CommonUI.kt", "app/src/main/java/com/example/ui/screens/GamesScreen.kt", "app/src/main/java/com/example/ui/screens/ProfileSheet.kt"]},
    {"id": "widgets-notifications", "source_paths": ["app/src/main/java/com/example/widget/PicShareWidgetProvider.kt", "app/src/main/java/com/example/widget/PicShareWidgetPreferences.kt"]}
  ]
}
```

- [ ] **Step 4: Add the exact initial asset-registry schema**

Create `scripts/visible_copy_asset_registry.json`:

```json
{
  "text_bearing_assets": [],
  "brand_only_assets": [],
  "reviewed_text_free_assets": []
}
```

The empty registry is intentional at RED stage. Task 3 classifies every shipped raster asset and every SVG that contains readable text.

- [ ] **Step 5: Run RED**

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: FAIL with `ModuleNotFoundError: No module named 'visible_copy_inventory'`.

- [ ] **Step 6: Commit**

```bash
git add scripts/test_visible_copy_inventory.py scripts/visible_copy_policy.json scripts/visible_copy_routes.json scripts/visible_copy_asset_registry.json
git commit -m "test: define complete visible-copy inventory contract"
```

---

### Task 2: Implement deterministic Kotlin/XML/content discovery

**Files:**
- Create: `scripts/visible_copy_inventory.py`
- Modify: `scripts/test_visible_copy_inventory.py`

**Interfaces:**
- Consumes: Task 1 policy/route/asset JSON and repository root.
- Produces:
  - `normalize_visible_text(text: str) -> str`
  - `extract_placeholders(text: str) -> tuple[str, ...]`
  - `discover_repository(root: Path) -> InventoryReport`
  - `write_report(report: InventoryReport, path: Path) -> None`
  - `InventoryReport.units: list[dict]`
  - `InventoryReport.occurrences: list[dict]`
  - `InventoryReport.metrics: dict[str, int]`

- [ ] **Step 1: Implement the core data model**

```python
from dataclasses import dataclass

@dataclass(frozen=True)
class VisibleCopyOccurrence:
    path: str
    line: int
    presentation: str
    german: str
    placeholders: tuple[str, ...]
    exemption: str | None = None

@dataclass
class InventoryReport:
    units: list[dict]
    occurrences: list[dict]
    metrics: dict[str, int]
```

`normalize_visible_text` strips surrounding whitespace, converts CRLF to LF, collapses horizontal whitespace outside explicit newlines, and never splits a sentence into individual words. `extract_placeholders` recognizes Kotlin `${...}`, `{name}`, `%s`, `%d`, `%1$s`, `%1$d` in source order.

- [ ] **Step 2: Implement Kotlin/Compose and product-content scanning**

Walk all production `*.kt` under `app/src/main/java`, applying the policy. Detect literal or template text used in at least these visible contexts: `Text`, `BasicText`, `label`, `title`, `subtitle`, `description`, `placeholder`, `contentDescription`, `Snackbar`, `Toast`, `AlertDialog`, `Question`, `question`, `prompt`, `answer`, `option`, `options`, `Pack`, `Game`, `Moment`, `Result`, `instruction`, `message`.

For data-heavy sources such as `GeneratedHarmonyContent.kt`, collect human-language constructor/property values and reject technical IDs only when policy proves them internal. Record exact repository path and 1-based line number.

- [ ] **Step 3: Implement Android XML and SVG scanning**

Use `xml.etree.ElementTree` to collect `<string>` values and literal `android:text`, `android:hint`, `android:contentDescription` attributes that are not resource references. For SVG, collect `<text>` and `<tspan>` values; brand values remain occurrences with `exemption="brand"`.

- [ ] **Step 4: Deduplicate into stable German units**

Group by `(normalized_german, placeholder_signature, exemption)` and compute:

```python
stable_id = "de_" + hashlib.sha256(
    (german + "\0" + "\0".join(placeholders) + "\0" + (exemption or "")).encode("utf-8")
).hexdigest()[:16]
```

Sort units by `stable_id` and occurrences by `(path, line, german)` before serialization.

- [ ] **Step 5: Compute exact metrics**

```python
metrics = {
    "unique_visible_units_total": len(units),
    "unique_translatable_units": sum(1 for u in units if not u["exemption"]),
    "exempt_visible_units": sum(1 for u in units if u["exemption"]),
    "visible_render_occurrences": len(occurrences),
    "german_word_count": sum(word_count(u["german"]) for u in units if not u["exemption"]),
}
```

`word_count` removes placeholders and counts Unicode letter/number tokens; punctuation and emoji do not count as words.

- [ ] **Step 6: Run GREEN tests**

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: Task 1 tests PASS.

- [ ] **Step 7: Commit**

```bash
git add scripts/visible_copy_inventory.py scripts/test_visible_copy_inventory.py
git commit -m "feat: discover complete German visible copy"
```

---

### Task 3: Add asset-completeness and route-coverage gates

**Files:**
- Modify: `scripts/visible_copy_inventory.py`
- Modify: `scripts/test_visible_copy_inventory.py`
- Modify: `scripts/visible_copy_asset_registry.json`
- Verify: `scripts/visible_copy_routes.json`

**Interfaces:**
- Consumes: `discover_repository()` output.
- Produces:
  - `validate_asset_registry(root: Path, registry: dict) -> list[str]`
  - `validate_route_manifest(root: Path, manifest: dict, occurrences: list[dict]) -> list[str]`

- [ ] **Step 1: Extend imports and write RED tests**

Extend the test import with `validate_asset_registry` and `validate_route_manifest`. Add fixture-based tests where an unregistered `sample.png` produces an error and a route referencing `missing.kt` produces an error.

```python
def test_required_route_source_must_exist(self):
    errors = validate_route_manifest(
        ROOT,
        {"routes": [{"id": "broken", "source_paths": ["missing.kt"]}]},
        [],
    )
    self.assertTrue(errors)
```

Run the tests; expected FAIL because the validation functions do not exist yet.

- [ ] **Step 2: Implement asset-registry validation**

Enumerate every production `.png`, `.jpg`, `.jpeg`, `.webp`, and `.svg` under `app/src/main/res` and `app/src/main/assets`. Require every raster asset to be classified in exactly one registry bucket. For SVGs containing `<text>` or `<tspan>`, require classification in `text_bearing_assets` or `brand_only_assets`. Duplicate classification is an error.

- [ ] **Step 3: Review every currently shipped raster asset without OCR**

Visually inspect each asset and classify it as exactly one of:
- `text_bearing_assets`: Harmony-owned readable text, stored with explicit `german_text` array;
- `brand_only_assets`: only brand/logo wording, stored with `exemption: "brand"`;
- `reviewed_text_free_assets`: no readable text.

`text_bearing_assets` are injected as `presentation="bitmap-text"` occurrences. Never infer embedded text from filenames.

- [ ] **Step 4: Implement route validation**

Require every `source_paths` entry in `visible_copy_routes.json` to exist. Require each route to contribute at least one discovered occurrence unless it has an explicit `visual_only: true` and non-empty `reason` field. The committed current route manifest should need no `visual_only` exemptions unless source review proves one.

- [ ] **Step 5: Run tests and commit**

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: PASS after every current production asset is classified.

```bash
git add scripts/visible_copy_inventory.py scripts/test_visible_copy_inventory.py scripts/visible_copy_asset_registry.json scripts/visible_copy_routes.json
git commit -m "feat: cover visible asset text and product routes"
```

---

### Task 4: Generate and manually review the real German baseline

**Files:**
- Create: `localization/visible-copy-inventory.de.json`
- Create: `localization/visible-copy-inventory-summary.md`
- Modify: `scripts/visible_copy_policy.json` only for evidence-backed false positives/internal IDs.

**Interfaces:**
- Consumes: complete `InventoryReport` from Tasks 2–3.
- Produces: canonical German inventory for the later Japanese plan.

- [ ] **Step 1: Generate the first full report**

```bash
python3 scripts/visible_copy_inventory.py \
  --root . \
  --output localization/visible-copy-inventory.de.json \
  --summary localization/visible-copy-inventory-summary.md
```

Expected: exit 0 and print all five metrics.

- [ ] **Step 2: Review every source bucket**

Inspect breakdowns for `compose-ui`, `dynamic-template`, `game-content`, `android-string`, `android-xml`, `widget-notification`, `svg-text`, `bitmap-text`, and `brand`. A suspected false positive may be excluded only after confirming it cannot reach normal production UI. Never exclude a German sentence because it is inconvenient to translate.

- [ ] **Step 3: Verify previously missed areas explicitly**

The report must include occurrences from every applicable file below:
- `screens/HomeScreen.kt`
- `screens/GamesScreen.kt`
- `screens/PackListScreen.kt`
- `screens/QuizRunnerScreen.kt`
- `screens/PandaEitherOrScreen.kt`
- `screens/IntrospectionGameScreen.kt`
- `screens/ChatScreen.kt`
- `screens/MomentsScreen.kt`
- `screens/ProfileSheet.kt`
- `data/GeneratedHarmonyContent.kt`
- `widget/PicShareWidgetProvider.kt`
- `res/values/strings.xml`

It must contain no occurrence from `screens/DevStudioScreen.kt`.

- [ ] **Step 4: Prove deterministic output**

```bash
python3 scripts/visible_copy_inventory.py --root . --output /tmp/inventory-a.json --summary /tmp/summary-a.md
python3 scripts/visible_copy_inventory.py --root . --output /tmp/inventory-b.json --summary /tmp/summary-b.md
sha256sum /tmp/inventory-a.json /tmp/inventory-b.json
cmp /tmp/inventory-a.json /tmp/inventory-b.json
```

Expected: identical SHA-256 values and `cmp` exit 0.

- [ ] **Step 5: Generate the committed summary with real integers**

The summary renderer must write the five values directly from `report.metrics`; it must not contain literal template placeholders or estimates. Verify with:

```bash
python3 - <<'PY'
import json, re
from pathlib import Path
report = json.loads(Path('localization/visible-copy-inventory.de.json').read_text(encoding='utf-8'))
summary = Path('localization/visible-copy-inventory-summary.md').read_text(encoding='utf-8')
for key in (
    'unique_visible_units_total',
    'unique_translatable_units',
    'exempt_visible_units',
    'visible_render_occurrences',
    'german_word_count',
):
    value = str(report['metrics'][key])
    assert re.search(rf'\b{re.escape(value)}\b', summary), (key, value)
print(report['metrics'])
PY
```

- [ ] **Step 6: Commit the locked baseline**

```bash
git add localization/visible-copy-inventory.de.json localization/visible-copy-inventory-summary.md scripts/visible_copy_policy.json
git commit -m "data: lock complete German visible-copy baseline"
```

---

### Task 5: Replace the 952/English completeness assumption and gate CI

**Files:**
- Modify: `scripts/audit_localization.py`
- Modify: `.github/workflows/localization-audit.yml`
- Modify: `scripts/test_visible_copy_inventory.py`

**Interfaces:**
- Consumes: `localization/visible-copy-inventory.de.json`.
- Produces: CI invariant that the German baseline comes from source, not English or a historical fixed number.

- [ ] **Step 1: Write the RED regression**

Add:

```python
def test_audit_no_longer_defines_canonical_from_english_content(self):
    audit = (ROOT / "scripts/audit_localization.py").read_text(encoding="utf-8")
    self.assertNotIn('canonical_all = extract_map(UI / "EnglishContent.kt"', audit)
    self.assertIn("visible-copy-inventory.de.json", audit)
```

Run the test. Expected: FAIL because the old English-derived canonical expression is still present.

- [ ] **Step 2: Load the German inventory in `audit_localization.py`**

Add:

```python
INVENTORY_PATH = ROOT / "localization/visible-copy-inventory.de.json"

def load_canonical_inventory() -> dict:
    return json.loads(INVENTORY_PATH.read_text(encoding="utf-8"))
```

Replace the English-derived canonical definition with non-exempt German units from the inventory. Preserve old locale-specific checks only as explicitly labeled `legacy-locale coverage` until the Japanese follow-up plan migrates locale coverage. Do not call the other languages complete against the new baseline.

- [ ] **Step 3: Add the CI inventory gate before locale checks**

Add to `.github/workflows/localization-audit.yml`:

```yaml
- name: Test visible-copy inventory
  run: python3 -m unittest -v scripts/test_visible_copy_inventory.py

- name: Regenerate German visible-copy inventory
  run: |
    python3 scripts/visible_copy_inventory.py \
      --root . \
      --output localization/visible-copy-inventory.de.json \
      --summary localization/visible-copy-inventory-summary.md

- name: Verify German inventory is committed and current
  run: git diff --exit-code -- localization/visible-copy-inventory.de.json localization/visible-copy-inventory-summary.md
```

- [ ] **Step 4: Run static checks and Android compile**

```bash
python3 -m unittest -v scripts/test_visible_copy_inventory.py
python3 scripts/audit_localization.py
gradle :app:compileDebugKotlin --no-daemon
```

Expected: inventory tests PASS and Gradle prints `BUILD SUCCESSFUL`. The audit may describe other locales as legacy/incomplete, but it must not shrink the German canonical set.

- [ ] **Step 5: Commit**

```bash
git add scripts/audit_localization.py scripts/test_visible_copy_inventory.py .github/workflows/localization-audit.yml
git commit -m "ci: make German visible-copy inventory canonical"
```

---

### Task 6: Final verification and Japanese handoff

**Files:**
- Verify: `localization/visible-copy-inventory.de.json`
- Verify: `localization/visible-copy-inventory-summary.md`
- Verify: all Task 1–5 implementation files.

**Interfaces:**
- Consumes: final German inventory.
- Produces: exact baseline metrics and a locked artifact for the Japanese implementation plan.

- [ ] **Step 1: Run all validation from the final branch state**

```bash
python3 -m unittest -v scripts/test_visible_copy_inventory.py
python3 scripts/visible_copy_inventory.py --root . --output /tmp/final-de.json --summary /tmp/final-de.md
cmp localization/visible-copy-inventory.de.json /tmp/final-de.json
python3 scripts/audit_localization.py
gradle :app:compileDebugKotlin --no-daemon
```

Expected: tests PASS, `cmp` exit 0, and Gradle `BUILD SUCCESSFUL`.

- [ ] **Step 2: Verify 952 is no longer an executable completeness threshold**

```bash
grep -R "952" scripts localization .github/workflows || true
```

Any remaining occurrence may be historical documentation/data only. No executable completeness check may compare a locale against 952.

- [ ] **Step 3: Report the exact real metrics**

Read `localization/visible-copy-inventory-summary.md` and report verbatim:
- unique visible units total;
- unique translatable German units;
- exempt visible units;
- visible render occurrences;
- German word count.

Do not estimate or round.

- [ ] **Step 4: Commit only if final verification required an evidence-backed correction**

If source review finds a false positive or missing exemption, update policy/registry, regenerate both output files, rerun Step 1, and commit the correction. Otherwise do not create an empty commit.

- [ ] **Step 5: Create the separate Japanese implementation plan after the German baseline is reviewed**

That plan must consume `localization/visible-copy-inventory.de.json` as its canonical source and must not use `EXACT_ENGLISH_CONTENT` or a fixed historical number as its coverage target.
