#!/usr/bin/env python3
"""Regression checks for enabling Brazilian Portuguese in production."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

catalog = (UI / "TranslationCatalog.kt").read_text(encoding="utf-8")
updates = (UI / "LocalizationUpdates.kt").read_text(encoding="utf-8")
ptbr_source = (UI / "PortugueseBrazilContent.kt").read_text(encoding="utf-8")


def fail(message: str) -> None:
    print(f"::error::{message}")


failed = False

# pt-BR must no longer be a fake-complete bypass.
for forbidden in [
    "if (language == AppLanguage.PORTUGUESE_BRAZIL) return true",
    "if (language == AppLanguage.PORTUGUESE_BRAZIL) return baseExact(german, language)",
]:
    if forbidden in catalog:
        fail(f"pt-BR runtime bypass still present: {forbidden}")
        failed = True

# Current repair map must be wired into the normal runtime lookup.
if "AppLanguage.PORTUGUESE_BRAZIL to LOCALIZATION_UPDATES_PORTUGUESE_BRAZIL" not in updates:
    fail("pt-BR repair map is not registered in LOCALIZATION_UPDATES")
    failed = True
if "LOCALIZATION_UPDATES[language]?.get(german)" not in catalog:
    fail("central localization overrides are not used by TranslationCatalog")
    failed = True

# The native Brazilian catalog must remain valid Kotlin and preserve stable placeholders.
if r"\\$" in ptbr_source:
    fail("pt-BR still contains a double-escaped Kotlin dollar placeholder")
    failed = True
if "parceiroName" in ptbr_source:
    fail("pt-BR translated the stable partnerName placeholder identifier")
    failed = True
if "localizeBrazilianPortugueseDynamicContent" not in ptbr_source:
    fail("native Brazilian Portuguese dynamic localization helper is missing")
    failed = True

# Reviewed high-visibility corrections must win over machine/legacy translations.
overrides_path = UI / "PortugueseBrazilOverrides.kt"
if not overrides_path.exists():
    fail("PortugueseBrazilOverrides.kt is missing")
    failed = True
else:
    text = overrides_path.read_text(encoding="utf-8")
    expected = {
        '"Amazon" to "Amazon"': "brand Amazon",
        '"Auto" to "Carro"': "Auto context",
        '"Direkt ansprechen" to "Falar diretamente"': "direct communication",
        '"Schließen" to "Fechar"': "close action",
        '"Privater Paar-Chat" to "Chat privado do casal"': "private couple chat",
        '"Unbeantwortet" to "Não respondido"': "unanswered label",
        '"Entweder oder" to "Isso ou aquilo"': "either-or title",
        '"Burger" to "Hambúrguer"': "burger label",
        '"Aussehen" to "Aparência"': "appearance label",
        '"Das erste Treffen" to "O primeiro encontro"': "first meeting label",
        '"Tauche ins Unterbewusstsein" to "Mergulhe no subconsciente"': "introspection title",
        '"Reise beginnen" to "Iniciar jornada"': "introspection CTA",
    }
    for needle, label in expected.items():
        if needle not in text:
            fail(f"reviewed pt-BR correction missing: {label}")
            failed = True
    if "PT_BR_REVIEWED_OVERRIDES[german]?.let { return it }" not in catalog:
        fail("reviewed pt-BR overrides do not have runtime priority")
        failed = True

if failed:
    print("pt-BR readiness FAILED")
    sys.exit(1)
print("pt-BR readiness PASSED")
