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
