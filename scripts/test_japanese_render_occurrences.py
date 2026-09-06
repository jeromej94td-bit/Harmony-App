from pathlib import Path
import unittest

from audit_japanese_render_occurrences import (
    build_render_report,
    compose_occurrence_is_localized,
)

ROOT = Path(__file__).resolve().parents[1]


class JapaneseRenderOccurrenceTests(unittest.TestCase):
    def test_raw_compose_literal_is_not_considered_localized(self):
        source = 'Text(text = "Zurück")\n'
        self.assertFalse(compose_occurrence_is_localized(source, 1, "Zurück"))

    def test_content_text_compose_literal_is_considered_localized(self):
        source = 'Text(text = contentText("Zurück"))\n'
        self.assertTrue(compose_occurrence_is_localized(source, 1, "Zurück"))

    def test_tr_compose_literal_is_considered_localized(self):
        source = 'Text(text = tr("Zurück", "Back"))\n'
        self.assertTrue(compose_occurrence_is_localized(source, 1, "Zurück"))

    def test_every_current_render_occurrence_is_audited_individually(self):
        report = build_render_report(ROOT)
        self.assertEqual(report["total_render_occurrences"], 1367)
        self.assertEqual(len(report["occurrences"]), 1367)


if __name__ == "__main__":
    unittest.main()
