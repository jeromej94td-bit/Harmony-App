from pathlib import Path
import tempfile
import unittest

from visible_copy_canonical import discover_repository, extract_placeholders
from visible_copy_inventory import normalize_visible_text, write_report
from visible_copy_validation import validate_asset_registry, validate_route_manifest

ROOT = Path(__file__).resolve().parents[1]


class VisibleCopyInventoryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.report = discover_repository(ROOT)
        cls.texts = {unit["german"] for unit in cls.report.units}

    def test_normalization_preserves_visible_sentence(self):
        self.assertEqual(
            normalize_visible_text("  Möchtest du wirklich zurück?  "),
            "Möchtest du wirklich zurück?",
        )

    def test_placeholder_signature_is_stable(self):
        self.assertEqual(
            extract_placeholders("${profile.partnerName}, noch {count} Fragen für $partnerName"),
            ("${profile.partnerName}", "{count}", "$partnerName"),
        )

    def test_repository_discovers_android_strings(self):
        self.assertIn("Überspringen", self.texts)
        self.assertIn("Das oder das?", self.texts)

    def test_repository_discovers_universal_game_content(self):
        self.assertIn("Reiseziele", self.texts)
        self.assertIn("Paris, Frankreich", self.texts)
        self.assertIn("Ich habe noch nie bei einem Disney-Film geweint.", self.texts)
        self.assertTrue(any(occ["path"].endswith("GeneratedHarmonyContent.kt") for occ in self.report.occurrences))

    def test_generated_metadata_and_comments_are_not_customer_copy(self):
        for technical in (
            "reiseziele",
            "reisen",
            "cj_disney_quiz",
            "filme_serien",
            "ichhabenochnie",
        ):
            self.assertNotIn(technical, self.texts)
        self.assertNotIn("Das oder das", self.texts)

    def test_locale_specific_cuisine_installers_do_not_define_german_baseline(self):
        self.assertFalse(any(occ["path"].endswith("CuisinePackInstaller.kt") for occ in self.report.occurrences))

    def test_repository_discovers_introspection_german_source(self):
        self.assertIn("Tauche ins Unterbewusstsein", self.texts)
        self.assertIn("Reise beginnen", self.texts)

    def test_repository_discovers_widget_copy(self):
        self.assertIn("Ein Bild nur für euch 💕", self.texts)
        self.assertIn("Öffne Harmony und füge euer erstes Bild hinzu", self.texts)
        self.assertIn("Harmony PicShare · ${pictures.size} Bilder rotieren", self.texts)

    def test_localization_fallback_arguments_are_not_second_canonical_units(self):
        for german in (
            "Dein Bild",
            "Partnerbild",
            "Profil",
            "Dein Name",
            "Zusammen seit",
        ):
            self.assertIn(german, self.texts)
        for fallback in (
            "Your photo",
            "Partner photo",
            "Profile",
            "Your name",
            "Together since",
            "Invite code: HRM-8731 · everything unlocked",
        ):
            self.assertNotIn(fallback, self.texts)

    def test_bundled_options_are_inventoried_without_generic_duplicates(self):
        coca = [occ for occ in self.report.occurrences if occ["german"] == "Coca-Cola" and occ["path"].endswith("DriveTotAssetInstaller.kt")]
        self.assertEqual(len(coca), 1)
        self.assertEqual(coca[0]["presentation"], "bundled-image-option")
        self.assertEqual(coca[0]["exemption"], "brand")

    def test_technical_literals_are_not_customer_copy(self):
        for technical in (
            "floorAlpha",
            "nebula1Alpha",
            "nebula2Alpha",
            "nebula3Alpha",
            "animated_palette",
            "category_glow_${category.id}",
            "edit_start_date_input",
            "image/*",
            "🛠️ Developer mode",
            "Open Developer Studio",
            "Edit games and destinations, import folders, adjust images",
        ):
            self.assertNotIn(technical, self.texts)

    def test_base64_and_placeholder_only_literals_are_not_customer_copy(self):
        self.assertFalse(any(len(text) > 1000 and " " not in text[:200] for text in self.texts))
        for technical in ("{total}", "${index + 1}", "$percentage%", "${searchResults.size}"):
            self.assertNotIn(technical, self.texts)

    def test_locale_catalogs_are_not_inventory_sources(self):
        self.assertFalse(any(occ["path"].endswith("JapaneseContent.kt") for occ in self.report.occurrences))
        self.assertFalse(any(occ["path"].endswith("EnglishContent.kt") for occ in self.report.occurrences))

    def test_dev_studio_is_explicitly_excluded(self):
        self.assertFalse(any("DevStudioScreen.kt" in occ["path"] for occ in self.report.occurrences))

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
        with tempfile.TemporaryDirectory() as tmp:
            a = Path(tmp) / "a.json"
            b = Path(tmp) / "b.json"
            write_report(self.report, a)
            write_report(self.report, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())


if __name__ == "__main__":
    unittest.main()
