# German Visible-Copy Inventory Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a deterministic, reviewable German source-of-truth inventory for every Harmony-owned production-visible text outside Developer Studio, publish the real current counts, and make CI fail when new visible copy bypasses that inventory.

**Architecture:** A Python inventory pipeline scans production Kotlin/Compose, product-content sources, Android XML/resources, SVG assets, and a reviewed bitmap/route manifest. Every discovered render occurrence is normalized into a stable `VisibleCopyOccurrence`, deduplicated into canonical German `VisibleCopyUnit` records, and written to a committed JSON report with three headline metrics. Developer Studio is excluded by explicit policy; brands remain visible inventory entries but are marked exempt from translation. This plan intentionally stops after the German baseline is complete and locked; Japanese is implemented in a separate follow-up plan against this generated baseline.

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
- No OCR dependency is introduced; bitmap text is handled through an explicit reviewed asset registry because bitmap OCR cannot be a deterministic CI source of truth.
- The persisted report must publish unique visible translation units, visible render occurrences, and German word count deterministically.
- Do not modify Japanese or other locale content in this plan.

---

## File Structure

**Create**
- `scripts/visible_copy_inventory.py` — inventory engine and CLI; scans all source types and emits deterministic records/metrics.
- `scripts/visible_copy_policy.json` — explicit path exclusions, internal-ID exemptions, brand exemptions, known rendering APIs and ignored technical literals.
- `scripts/visible_copy_asset_registry.json` — reviewed text-bearing bitmap/SVG exceptions and brand-only asset declarations.
- `scripts/visible_copy_routes.json` — normal-user route/feature coverage manifest used to prove all product areas were considered.
- `scripts/test_visible_copy_inventory.py` — unit/regression tests for discovery, normalization, placeholders, exclusions and determinism.
- `localization/visible-copy-inventory.de.json` — generated canonical German inventory and headline metrics committed to the repository.
- `localization/visible-copy-inventory-summary.md` — human-readable counts and breakdown by source/presentation type.

**Modify**
- `scripts/audit_localization.py` — stop treating the English map as the canonical completeness definition; for now read the German inventory for baseline reporting while preserving existing locale checks until the Japanese follow-up plan replaces them.
- `.github/workflows/localization-audit.yml` — generate inventory, verify clean deterministic diff, run inventory tests, then run existing localization/build checks.

**Read-only source coverage during implementation**
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/ui/**/*.kt` excluding explicit Developer Studio paths
- `app/src/main/java/com/example/data/**/*.kt` including `GeneratedHarmonyContent.kt`, excluding explicit Developer Studio-only files
- `app/src/main/java/com/example/widget/**/*.kt`
- `app/src/main/res/values/**/*.xml`, `layout/**/*.xml`, `xml/**/*.xml`, `drawable/**/*.xml`
- `app/src/main/assets/**/*.svg`
- `app/src/main/res/drawable-nodpi/**/*.{png,jpg,jpeg,webp}` through the reviewed asset registry

---

### Task 1: Define the inventory data contract and RED tests

**Files:**
- Create: `scripts/test_visible_copy_inventory.py`
- Create: `scripts/visible_copy_policy.json`
- Create: `scripts/visible_copy_routes.json`
- Create: `scripts/visible_copy_asset_registry.json`

**Interfaces:**
- Consumes: repository-relative source paths and literal/template text.
- Produces: tests expecting `discover_repository(root: Path) -> InventoryReport`, `normalize_visible_text(text: str) -> str`, `extract_placeholders(text: str) -> tuple[str, ...]`, and `write_report(report: InventoryReport, path: Path) -> None` from Task 2.

- [ ] **Step 1: Write failing contract tests**

Create `scripts/test_visible_copy_inventory.py` with `unittest` tests that import the future inventory module and assert the exact contract below:

```python
from pathlib import Path
import json
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

- [ ] **Step 2: Add explicit policy files**

Create `scripts/visible_copy_policy.json` with this initial structure:

```json
{
  "exclude_path_globs": [
    "app/src/main/java/com/example/ui/screens/DevStudioScreen.kt",
    "app/src/main/java/com/example/data/Dev*.kt",
    "app/src/main/java/com/example/data/DeveloperDataManager.kt"
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

Create `scripts/visible_copy_routes.json` listing these required product areas exactly as route IDs: `home`, `games`, `pack-list`, `quiz-runner`, `panda-either-or`, `introspection`, `chat`, `moments`, `profile`, `navigation`, `dialogs`, `widgets-notifications`. Each route entry contains `source_paths` pointing at the concrete screen/component files currently present on `main`.

Create `scripts/visible_copy_asset_registry.json` with arrays `text_bearing_assets`, `brand_only_assets`, and `reviewed_text_free_assets`. Begin with the known Aurora SVG paths and brand-game image assets; every bitmap in `drawable-nodpi` must eventually appear in exactly one of these three buckets before Task 4 can pass.

- [ ] **Step 3: Run tests to verify RED**

Run:

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: FAIL with `ModuleNotFoundError: No module named 'visible_copy_inventory'`.

- [ ] **Step 4: Commit the RED contract**

```bash
git add scripts/test_visible_copy_inventory.py scripts/visible_copy_policy.json scripts/visible_copy_routes.json scripts/visible_copy_asset_registry.json
git commit -m "test: define complete visible-copy inventory contract"
```

---

### Task 2: Implement deterministic Kotlin/XML/content discovery

**Files:**
- Create: `scripts/visible_copy_inventory.py`
- Test: `scripts/test_visible_copy_inventory.py`

**Interfaces:**
- Consumes: policy/route/asset JSON from Task 1 and repository root.
- Produces:
  - `normalize_visible_text(text: str) -> str`
  - `extract_placeholders(text: str) -> tuple[str, ...]`
  - `discover_repository(root: Path) -> InventoryReport`
  - `write_report(report: InventoryReport, path: Path) -> None`
  - `InventoryReport.units: list[dict]`
  - `InventoryReport.occurrences: list[dict]`
  - `InventoryReport.metrics: dict[str, int]`

- [ ] **Step 1: Implement core dataclasses and normalization**

Use this concrete model in `scripts/visible_copy_inventory.py`:

```python
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

`normalize_visible_text` strips surrounding whitespace, converts CRLF to LF, collapses horizontal whitespace outside explicit newlines, and never splits a sentence into individual words. `extract_placeholders` recognizes Kotlin `${...}`, `{name}`, `%s`, `%d`, `%1$s`, `%1$d` and returns them in source order.

- [ ] **Step 2: Implement Kotlin/Compose literal and product-content scanning**

Walk all `*.kt` under `app/src/main/java`, applying `exclude_path_globs`. Extract quoted and triple-quoted literals only when they are associated with customer-visible contexts. The scanner must recognize at minimum these call/property contexts: `Text`, `BasicText`, `label`, `title`, `subtitle`, `description`, `placeholder`, `contentDescription`, `Snackbar`, `Toast`, `AlertDialog`, `Question`, `question`, `prompt`, `answer`, `option`, `options`, `Pack`, `Game`, `Moment`, `Result`, `instruction`, `message`.

For generated/product data such as `GeneratedHarmonyContent.kt`, collect human-language values passed to model constructors/fields while rejecting technical IDs via the policy patterns. Record each occurrence with exact file and 1-based line number.

- [ ] **Step 3: Implement Android XML and SVG scanning**

Parse XML using `xml.etree.ElementTree`:

```python
for string in root.findall(".//string"):
    add_occurrence(path, line=0, presentation="android-string", german="".join(string.itertext()))
```

Also capture literal `android:text`, `android:hint`, `android:contentDescription` values that do not reference `@string/...`. For SVGs, capture `<text>` and `<tspan>` content as `presentation="svg-text"`; brand-logo strings are retained with `exemption="brand"`.

- [ ] **Step 4: Deduplicate into stable German units**

Group occurrences by `(normalized_german, placeholder_signature, exemption)`. Compute a stable ID:

```python
stable_id = "de_" + hashlib.sha256(
    (german + "\0" + "\0".join(placeholders) + "\0" + (exemption or "")).encode("utf-8")
).hexdigest()[:16]
```

Sort units by `stable_id` and occurrences by `(path, line, german)` before serialization.

- [ ] **Step 5: Compute the required metrics**

Produce at least these deterministic metrics:

```python
metrics = {
    "unique_visible_units_total": len(units),
    "unique_translatable_units": sum(1 for u in units if not u["exemption"]),
    "exempt_visible_units": sum(1 for u in units if u["exemption"]),
    "visible_render_occurrences": len(occurrences),
    "german_word_count": sum(word_count(u["german"]) for u in units if not u["exemption"]),
}
```

`word_count` removes placeholders first and counts Unicode letter/number word tokens; punctuation and emoji are not words.

- [ ] **Step 6: Run the contract tests**

Run:

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: all Task 1 tests PASS.

- [ ] **Step 7: Commit the inventory engine**

```bash
git add scripts/visible_copy_inventory.py scripts/test_visible_copy_inventory.py
git commit -m "feat: discover complete German visible copy"
```

---

### Task 3: Add asset completeness and route coverage gates

**Files:**
- Modify: `scripts/visible_copy_inventory.py`
- Modify: `scripts/test_visible_copy_inventory.py`
- Modify: `scripts/visible_copy_asset_registry.json`
- Modify: `scripts/visible_copy_routes.json`

**Interfaces:**
- Consumes: `discover_repository()` output from Task 2.
- Produces: `validate_asset_registry(root: Path, registry: dict) -> list[str]` and `validate_route_manifest(root: Path, manifest: dict, occurrences: list[dict]) -> list[str]`.

- [ ] **Step 1: Add RED tests for unreviewed assets and missing product areas**

Add tests using temporary fixture directories so failures are deterministic:

```python
def test_unregistered_bitmap_is_rejected(self):
    # fixture contains drawable-nodpi/sample.png but registry lists nothing
    self.assertIn("sample.png", "\n".join(validate_asset_registry(root, registry)))

def test_required_route_source_must_exist(self):
    errors = validate_route_manifest(ROOT, {"routes": [{"id": "broken", "source_paths": ["missing.kt"]}]}, [])
    self.assertTrue(errors)
```

- [ ] **Step 2: Implement registry validation**

Enumerate every production `.png`, `.jpg`, `.jpeg`, `.webp` and `.svg` under app resources/assets. Require every raster asset to be explicitly classified in exactly one registry bucket. For SVGs with `<text>/<tspan>`, require a `text_bearing_assets` or `brand_only_assets` entry. Duplicate classification is an error.

- [ ] **Step 3: Review all current image assets without OCR**

Use repository image inspection for each currently shipped bitmap. Classify it as:
- `text_bearing_assets`: Harmony-owned readable words that must become localization inventory units;
- `brand_only_assets`: visible text is only protected/recognized branding and remains unchanged;
- `reviewed_text_free_assets`: no readable text.

For `text_bearing_assets`, record an explicit `german_text` array and `presentation="bitmap-text"`; these strings are injected into the canonical inventory. Do not infer text from filenames.

- [ ] **Step 4: Complete route manifest**

Every normal-user feature in the spec must have at least one concrete source path. Validate that each path exists and that each non-container route contributes at least one occurrence or is explicitly marked `visual_only: true` with a reason.

- [ ] **Step 5: Run tests**

```bash
cd scripts
python3 -m unittest -v test_visible_copy_inventory.py
```

Expected: PASS, with all current production assets classified and all required product routes represented.

- [ ] **Step 6: Commit asset/route coverage**

```bash
git add scripts/visible_copy_inventory.py scripts/test_visible_copy_inventory.py scripts/visible_copy_asset_registry.json scripts/visible_copy_routes.json
git commit -m "feat: cover visible asset text and product routes"
```

---

### Task 4: Generate and manually review the real German baseline

**Files:**
- Create: `localization/visible-copy-inventory.de.json`
- Create: `localization/visible-copy-inventory-summary.md`
- Modify: `scripts/visible_copy_policy.json` only for evidence-backed false positives/internal IDs found during review.

**Interfaces:**
- Consumes: complete `InventoryReport` from Tasks 2–3.
- Produces: committed canonical German inventory used by the later Japanese plan.

- [ ] **Step 1: Generate the first full report**

Run:

```bash
python3 scripts/visible_copy_inventory.py \
  --root . \
  --output localization/visible-copy-inventory.de.json \
  --summary localization/visible-copy-inventory-summary.md
```

Expected: command exits 0 and prints all five metrics, including `unique_translatable_units`, `visible_render_occurrences`, and `german_word_count`.

- [ ] **Step 2: Review every source-type bucket**

Inspect report breakdowns for at least: `compose-ui`, `dynamic-template`, `game-content`, `android-string`, `android-xml`, `widget-notification`, `svg-text`, `bitmap-text`, `brand`.

For each suspected false positive, prove it cannot reach production UI before adding an exact exclusion/pattern to `visible_copy_policy.json`. Never exclude a German sentence merely because it is inconvenient to translate.

- [ ] **Step 3: Verify known previously missed areas are present**

The generated JSON must contain occurrences from each of these concrete files where applicable:
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
- `widget/` production files
- `res/values/strings.xml`

It must not contain a production occurrence from `screens/DevStudioScreen.kt`.

- [ ] **Step 4: Regenerate after review and prove determinism**

Run the generator twice and compare hashes:

```bash
python3 scripts/visible_copy_inventory.py --root . --output /tmp/inventory-a.json --summary /tmp/summary-a.md
python3 scripts/visible_copy_inventory.py --root . --output /tmp/inventory-b.json --summary /tmp/summary-b.md
sha256sum /tmp/inventory-a.json /tmp/inventory-b.json
cmp /tmp/inventory-a.json /tmp/inventory-b.json
```

Expected: identical SHA-256 values and `cmp` exit 0.

- [ ] **Step 5: Write the final repository report**

Regenerate the committed paths and ensure the summary begins with the exact current values:

```markdown
# Harmony German Visible-Copy Baseline

- Unique visible units total: <generated integer>
- Unique translatable German units: <generated integer>
- Exempt visible units: <generated integer>
- Visible render occurrences: <generated integer>
- German word count: <generated integer>
```

The implementation must substitute actual generated integers; the committed file must not contain angle-bracket placeholders.

- [ ] **Step 6: Commit the locked German baseline**

```bash
git add localization/visible-copy-inventory.de.json localization/visible-copy-inventory-summary.md scripts/visible_copy_policy.json
git commit -m "data: lock complete German visible-copy baseline"
```

---

### Task 5: Replace the 952 completeness assumption and gate future copy in CI

**Files:**
- Modify: `scripts/audit_localization.py`
- Modify: `.github/workflows/localization-audit.yml`
- Test: `scripts/test_visible_copy_inventory.py`

**Interfaces:**
- Consumes: `localization/visible-copy-inventory.de.json`.
- Produces: CI invariant that the committed German inventory is regenerated from source and remains current; exposes the canonical count to follow-up locale audits.

- [ ] **Step 1: Write a RED regression proving English cannot define the canonical count**

Add a source-level regression test:

```python
def test_audit_no_longer_defines_canonical_from_english_content(self):
    audit = (ROOT / "scripts/audit_localization.py").read_text(encoding="utf-8")
    self.assertNotIn('canonical_all = extract_map(UI / "EnglishContent.kt"', audit)
    self.assertIn("visible-copy-inventory.de.json", audit)
```

Run it now; expected FAIL because the old English canonical expression is still present.

- [ ] **Step 2: Change `audit_localization.py` baseline loading**

Add:

```python
INVENTORY_PATH = ROOT / "localization/visible-copy-inventory.de.json"

def load_canonical_inventory() -> dict:
    return json.loads(INVENTORY_PATH.read_text(encoding="utf-8"))
```

Replace the old English-derived `canonical` definition with the inventory's non-exempt German units. Preserve existing locale-specific checks temporarily, but label their coverage as `legacy-locale coverage` until the Japanese follow-up plan migrates locale catalogs to the new baseline. Do not claim other languages are fully complete against the new baseline yet.

- [ ] **Step 3: Add CI inventory generation and clean-diff check**

Before locale coverage steps in `.github/workflows/localization-audit.yml`, add:

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

- [ ] **Step 4: Run the full static test suite**

```bash
python3 -m unittest -v scripts/test_visible_copy_inventory.py
python3 scripts/audit_localization.py
```

Expected: inventory tests PASS. Localization audit may report other locales as legacy/incomplete against the new baseline but must not redefine or shrink the German canonical set.

- [ ] **Step 5: Compile Android**

```bash
gradle :app:compileDebugKotlin --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit CI migration**

```bash
git add scripts/audit_localization.py scripts/test_visible_copy_inventory.py .github/workflows/localization-audit.yml
git commit -m "ci: make German visible-copy inventory canonical"
```

---

### Task 6: Final verification and handoff to Japanese plan

**Files:**
- Verify: `localization/visible-copy-inventory.de.json`
- Verify: `localization/visible-copy-inventory-summary.md`
- Verify: all Task 1–5 code and workflow files.

**Interfaces:**
- Consumes: final German inventory.
- Produces: exact baseline metrics and a locked artifact that the separate Japanese implementation plan can compare against.

- [ ] **Step 1: Run all inventory validations from a clean checkout**

```bash
python3 -m unittest -v scripts/test_visible_copy_inventory.py
python3 scripts/visible_copy_inventory.py --root . --output /tmp/final-de.json --summary /tmp/final-de.md
cmp localization/visible-copy-inventory.de.json /tmp/final-de.json
python3 scripts/audit_localization.py
gradle :app:compileDebugKotlin --no-daemon
```

Expected: tests PASS, `cmp` exit 0, and Gradle `BUILD SUCCESSFUL`.

- [ ] **Step 2: Verify the old 952 number is not used as a completeness threshold**

Run:

```bash
grep -R "952" scripts localization .github/workflows || true
```

Any remaining occurrence must be historical documentation/data only; no executable completeness check may compare against 952.

- [ ] **Step 3: Report the exact real metrics**

Read the committed summary and report these values verbatim to the user:
- unique visible units total;
- unique translatable German units;
- exempt visible units;
- visible render occurrences;
- German word count.

Do not estimate or round them.

- [ ] **Step 4: Commit any final evidence-only correction, otherwise do not create an empty commit**

If verification required no change, leave the branch head unchanged. If a proven false positive/exemption correction was necessary, regenerate both inventory outputs, rerun Step 1, then commit only that correction.

- [ ] **Step 5: Create the Japanese implementation plan only after this baseline is reviewed**

The follow-up plan must use `localization/visible-copy-inventory.de.json` as its canonical input and must not use `EXACT_ENGLISH_CONTENT` or a fixed historical number as its coverage target.
