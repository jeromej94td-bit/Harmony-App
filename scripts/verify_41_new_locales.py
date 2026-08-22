#!/usr/bin/env python3
"""Readiness regression for the 41 approved production locales.

This is intentionally independent from the generator so RED failures describe missing
production wiring instead of failing to import implementation code.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

LOCALES = {
    "HUNGARIAN": ("hu", "HungarianContent.kt", "EXACT_HUNGARIAN_CONTENT", "localizeHungarianDynamicContent"),
    "ROMANIAN": ("ro", "RomanianContent.kt", "EXACT_ROMANIAN_CONTENT", "localizeRomanianDynamicContent"),
    "BULGARIAN": ("bg", "BulgarianContent.kt", "EXACT_BULGARIAN_CONTENT", "localizeBulgarianDynamicContent"),
    "UKRAINIAN": ("uk", "UkrainianContent.kt", "EXACT_UKRAINIAN_CONTENT", "localizeUkrainianDynamicContent"),
    "RUSSIAN": ("ru", "RussianContent.kt", "EXACT_RUSSIAN_CONTENT", "localizeRussianDynamicContent"),
    "GREEK": ("el", "GreekContent.kt", "EXACT_GREEK_CONTENT", "localizeGreekDynamicContent"),
    "TURKISH": ("tr", "TurkishContent.kt", "EXACT_TURKISH_CONTENT", "localizeTurkishDynamicContent"),
    "ARABIC": ("ar", "ArabicContent.kt", "EXACT_ARABIC_CONTENT", "localizeArabicDynamicContent"),
    "HEBREW": ("he", "HebrewContent.kt", "EXACT_HEBREW_CONTENT", "localizeHebrewDynamicContent"),
    "PERSIAN": ("fa", "PersianContent.kt", "EXACT_PERSIAN_CONTENT", "localizePersianDynamicContent"),
    "HINDI": ("hi", "HindiContent.kt", "EXACT_HINDI_CONTENT", "localizeHindiDynamicContent"),
    "BENGALI": ("bn", "BengaliContent.kt", "EXACT_BENGALI_CONTENT", "localizeBengaliDynamicContent"),
    "URDU": ("ur", "UrduContent.kt", "EXACT_URDU_CONTENT", "localizeUrduDynamicContent"),
    "TAMIL": ("ta", "TamilContent.kt", "EXACT_TAMIL_CONTENT", "localizeTamilDynamicContent"),
    "TELUGU": ("te", "TeluguContent.kt", "EXACT_TELUGU_CONTENT", "localizeTeluguDynamicContent"),
    "MARATHI": ("mr", "MarathiContent.kt", "EXACT_MARATHI_CONTENT", "localizeMarathiDynamicContent"),
    "GUJARATI": ("gu", "GujaratiContent.kt", "EXACT_GUJARATI_CONTENT", "localizeGujaratiDynamicContent"),
    "KANNADA": ("kn", "KannadaContent.kt", "EXACT_KANNADA_CONTENT", "localizeKannadaDynamicContent"),
    "MALAYALAM": ("ml", "MalayalamContent.kt", "EXACT_MALAYALAM_CONTENT", "localizeMalayalamDynamicContent"),
    "THAI": ("th", "ThaiContent.kt", "EXACT_THAI_CONTENT", "localizeThaiDynamicContent"),
    "VIETNAMESE": ("vi", "VietnameseContent.kt", "EXACT_VIETNAMESE_CONTENT", "localizeVietnameseDynamicContent"),
    "INDONESIAN": ("id", "IndonesianContent.kt", "EXACT_INDONESIAN_CONTENT", "localizeIndonesianDynamicContent"),
    "MALAY": ("ms", "MalayContent.kt", "EXACT_MALAY_CONTENT", "localizeMalayDynamicContent"),
    "FILIPINO": ("fil", "FilipinoContent.kt", "EXACT_FILIPINO_CONTENT", "localizeFilipinoDynamicContent"),
    "BURMESE": ("my", "BurmeseContent.kt", "EXACT_BURMESE_CONTENT", "localizeBurmeseDynamicContent"),
    "KHMER": ("km", "KhmerContent.kt", "EXACT_KHMER_CONTENT", "localizeKhmerDynamicContent"),
    "LAO": ("lo", "LaoContent.kt", "EXACT_LAO_CONTENT", "localizeLaoDynamicContent"),
    "SWAHILI": ("sw", "SwahiliContent.kt", "EXACT_SWAHILI_CONTENT", "localizeSwahiliDynamicContent"),
    "AFRIKAANS": ("af", "AfrikaansContent.kt", "EXACT_AFRIKAANS_CONTENT", "localizeAfrikaansDynamicContent"),
    "AMHARIC": ("am", "AmharicContent.kt", "EXACT_AMHARIC_CONTENT", "localizeAmharicDynamicContent"),
    "YORUBA": ("yo", "YorubaContent.kt", "EXACT_YORUBA_CONTENT", "localizeYorubaDynamicContent"),
    "IGBO": ("ig", "IgboContent.kt", "EXACT_IGBO_CONTENT", "localizeIgboDynamicContent"),
    "HAUSA": ("ha", "HausaContent.kt", "EXACT_HAUSA_CONTENT", "localizeHausaDynamicContent"),
    "ZULU": ("zu", "ZuluContent.kt", "EXACT_ZULU_CONTENT", "localizeZuluDynamicContent"),
    "XHOSA": ("xh", "XhosaContent.kt", "EXACT_XHOSA_CONTENT", "localizeXhosaDynamicContent"),
    "SOMALI": ("so", "SomaliContent.kt", "EXACT_SOMALI_CONTENT", "localizeSomaliDynamicContent"),
    "ESTONIAN": ("et", "EstonianContent.kt", "EXACT_ESTONIAN_CONTENT", "localizeEstonianDynamicContent"),
    "LATVIAN": ("lv", "LatvianContent.kt", "EXACT_LATVIAN_CONTENT", "localizeLatvianDynamicContent"),
    "LITHUANIAN": ("lt", "LithuanianContent.kt", "EXACT_LITHUANIAN_CONTENT", "localizeLithuanianDynamicContent"),
    "SLOVENIAN": ("sl", "SlovenianContent.kt", "EXACT_SLOVENIAN_CONTENT", "localizeSlovenianDynamicContent"),
    "SERBIAN": ("sr", "SerbianContent.kt", "EXACT_SERBIAN_CONTENT", "localizeSerbianDynamicContent"),
}
RTL_CODES = {"ar", "he", "fa", "ur"}

language = (UI / "Language.kt").read_text(encoding="utf-8")
catalog = (UI / "TranslationCatalog.kt").read_text(encoding="utf-8")
introspection = (UI / "introspection/IntrospectionStrings.kt").read_text(encoding="utf-8")

failed = False

def fail(message: str) -> None:
    global failed
    failed = True
    print(f"::error::{message}")

for enum_name, (code, filename, exact_name, dynamic_name) in LOCALES.items():
    if f'{enum_name}("{code}"' not in language:
        fail(f"AppLanguage entry missing: {enum_name} ({code})")
    path = UI / filename
    if not path.exists():
        fail(f"locale source missing: {filename}")
    else:
        text = path.read_text(encoding="utf-8")
        if exact_name not in text:
            fail(f"exact catalog missing in {filename}: {exact_name}")
        if dynamic_name not in text:
            fail(f"dynamic localizer missing in {filename}: {dynamic_name}")
        if "mapOf()" in text:
            fail(f"locale catalog is still empty: {filename}")
        if r"\\$" in text:
            fail(f"double-escaped Kotlin dollar placeholder in {filename}")
    if f"AppLanguage.{enum_name} -> {exact_name}[german]" not in catalog:
        fail(f"exact TranslationCatalog route missing: {enum_name}")
    if f"AppLanguage.{enum_name} -> {dynamic_name}(text)" not in catalog:
        fail(f"dynamic TranslationCatalog route missing: {enum_name}")
    if f"AppLanguage.{enum_name}" not in introspection:
        fail(f"Introspection route missing: {enum_name}")

if "val isRtl: Boolean" not in language:
    fail("AppLanguage RTL metadata property missing")
else:
    for code in RTL_CODES:
        enum_name = next(name for name, data in LOCALES.items() if data[0] == code)
        if enum_name not in language:
            continue

if failed:
    print("41-locale readiness FAILED")
    sys.exit(1)
print("41-locale readiness PASSED")
