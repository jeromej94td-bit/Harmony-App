from pathlib import Path
import tempfile
import unittest

from visible_copy_complete import discover_repository
from visible_copy_inventory import (
    extract_placeholders,
    normalize_visible_text,
    write_report,
)
from visible_copy_validation import (
    validate_asset_registry,
    validate_route_manifest,
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

    def test_repository_discovers_introspection_german_source(self):
        report = discover_repository(ROOT)
        texts = {unit["german"] for unit in report.units}
        self.assertIn("Tauche ins Unterbewusstsein", texts)
        self.assertIn("Reise beginnen", texts)

    def test_repository_discovers_widget_copy(self):
        report = discover_repository(ROOT)
        texts = {unit["german"] for unit in report.units}
        self.assertIn("Ein Bild nur für euch 💕", texts)
        self.assertIn("Öffne Harmony und füge euer erstes Bild hinzu", texts)
        self.assertIn("Harmony PicShare · ${pictures.size} Bilder rotieren", texts)

    def test_cuisine_metadata_is_not_customer_copy(self):
        report = discover_repository(ROOT)
        texts = {unit["german"] for unit in report.units}
        for technical in (
            "harmony_settings_prefs",
            "app_language",
            "tot_italian_cuisine_mixed",
            "tot_polish_cuisine_traditional",
            "cucina",
            "italia",
            "kuchnia",
            "polska",
        ):
            self.assertNotIn(technical, texts)
        self.assertIn("🍝 Cucina italiana — scelte regionali", texts)
        self.assertIn("Pizza napoletana", texts)
        self.assertIn("🇵🇱 Tradycyjna kuchnia polska", texts)
        self.assertIn("Pierogi ruskie", texts)

    def test_locale_catalogs_are_not_inventory_sources(self):
        report = discover_repository(ROOT)
        self.assertFalse(any(occ["path"].endswith("JapaneseContent.kt") for occ in report.occurrences))
        self.assertFalse(any(occ["path"].endswith("EnglishContent.kt") for occ in report.occurrences))

    def test_dev_studio_is_explicitly_excluded(self):
        report = discover_repository(ROOT)
        self.assertFalse(any("DevStudioScreen.kt" in occ["path"] for occ in report.occurrences))

    def test_unregistered_bitmap_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            image = root / "app/src/main/res/drawable-nodpi/sample.png"
            image.parent.mkdir(parents=True)
            image.write_bytes(b"not-an-image")
            errors = validate_asset_registry(
                root,
                {
                    "text_bearing_assets": [],
                    "brand_only_assets": [],
                    "reviewed_text_free_assets": [],
                },
            )
            self.assertIn(
                "unclassified visual asset: app/src/main/res/drawable-nodpi/sample.png",
                errors,
            )

    def test_required_route_source_must_exist(self):
        errors = validate_route_manifest(
            ROOT,
            {"routes": [{"id": "broken", "source_paths": ["missing.kt"]}]},
            [],
        )
        self.assertIn("route source missing: broken: missing.kt", errors)

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
