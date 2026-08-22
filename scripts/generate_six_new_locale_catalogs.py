#!/usr/bin/env python3
"""Generate the six new full offline Harmony locale catalogs from the current German source."""
from __future__ import annotations

import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

import audit_localization as audit

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
MISSING = ROOT / "localization_missing.json"

TARGETS = {
    "nl": ("DutchContent.kt", "EXACT_DUTCH_CONTENT", "localizeDutchDynamicContent", "nl"),
    "sv": ("SwedishContent.kt", "EXACT_SWEDISH_CONTENT", "localizeSwedishDynamicContent", "sv"),
    "is": ("IcelandicContent.kt", "EXACT_ICELANDIC_CONTENT", "localizeIcelandicDynamicContent", "is"),
    "ko": ("KoreanContent.kt", "EXACT_KOREAN_CONTENT", "localizeKoreanDynamicContent", "ko"),
    "zh-CN": ("ChineseSimplifiedContent.kt", "EXACT_CHINESE_SIMPLIFIED_CONTENT", "localizeChineseSimplifiedDynamicContent", "zh-CN"),
    "zh-TW": ("ChineseTraditionalContent.kt", "EXACT_CHINESE_TRADITIONAL_CONTENT", "localizeChineseTraditionalDynamicContent", "zh-TW"),
}

# Human-reviewed high-visibility UI terms. These override machine translation.
REVIEWED = {
    "nl": {
        "Schließen": "Sluiten", "Dein Bild": "Jouw foto", "Partnerbild": "Foto van je partner",
        "Privater Paar-Chat": "Privéchat voor koppels", "Unbeantwortet": "Onbeantwoord",
        "Unbeantwortete Fragen": "Onbeantwoorde vragen", "Entweder oder": "Dit of dat",
        "Frage": "Vraag", "Unterhaltung": "Entertainment", "Hochzeit": "Bruiloft",
        "Burger": "Burger", "Aussehen": "Uiterlijk", "Das erste Treffen": "De eerste ontmoeting",
        "Tauche ins Unterbewusstsein": "Duik in het onderbewustzijn", "Reise beginnen": "Reis beginnen",
        "Handy weitergeben": "Geef de telefoon door", "ODER": "OF",
        "App-Sprache auswählen": "App-taal selecteren",
    },
    "sv": {
        "Schließen": "Stäng", "Dein Bild": "Din bild", "Partnerbild": "Partnerns bild",
        "Privater Paar-Chat": "Privat par-chatt", "Unbeantwortet": "Obesvarad",
        "Unbeantwortete Fragen": "Obesvarade frågor", "Entweder oder": "Antingen eller",
        "Frage": "Fråga", "Unterhaltung": "Underhållning", "Hochzeit": "Bröllop",
        "Burger": "Hamburgare", "Aussehen": "Utseende", "Das erste Treffen": "Första mötet",
        "Tauche ins Unterbewusstsein": "Dyk ner i det undermedvetna", "Reise beginnen": "Börja resan",
        "Handy weitergeben": "Lämna över mobilen", "ODER": "ELLER",
        "App-Sprache auswählen": "Välj appspråk",
    },
    "is": {
        "Schließen": "Loka", "Dein Bild": "Myndin þín", "Partnerbild": "Mynd maka",
        "Privater Paar-Chat": "Einkaspjall para", "Unbeantwortet": "Ósvarað",
        "Unbeantwortete Fragen": "Ósvaraðar spurningar", "Entweder oder": "Annað hvort eða",
        "Frage": "Spurning", "Unterhaltung": "Afþreying", "Hochzeit": "Brúðkaup",
        "Burger": "Hamborgari", "Aussehen": "Útlit", "Das erste Treffen": "Fyrsti fundurinn",
        "Tauche ins Unterbewusstsein": "Kafaðu inn í undirmeðvitundina", "Reise beginnen": "Hefja ferð",
        "Handy weitergeben": "Réttu símann áfram", "ODER": "EÐA",
        "App-Sprache auswählen": "Veldu tungumál apps",
    },
    "ko": {
        "Schließen": "닫기", "Dein Bild": "내 사진", "Partnerbild": "파트너 사진",
        "Privater Paar-Chat": "커플 비공개 채팅", "Unbeantwortet": "미응답",
        "Unbeantwortete Fragen": "미응답 질문", "Entweder oder": "이것 아니면 저것",
        "Frage": "질문", "Unterhaltung": "엔터테인먼트", "Hochzeit": "결혼식",
        "Burger": "버거", "Aussehen": "외모", "Das erste Treffen": "첫 만남",
        "Tauche ins Unterbewusstsein": "무의식 속으로 들어가기", "Reise beginnen": "여정 시작",
        "Handy weitergeben": "휴대폰 넘기기", "ODER": "또는",
        "App-Sprache auswählen": "앱 언어 선택",
    },
    "zh-CN": {
        "Schließen": "关闭", "Dein Bild": "你的照片", "Partnerbild": "伴侣照片",
        "Privater Paar-Chat": "私密情侣聊天", "Unbeantwortet": "未回答",
        "Unbeantwortete Fragen": "未回答的问题", "Entweder oder": "二选一",
        "Frage": "问题", "Unterhaltung": "娱乐", "Hochzeit": "婚礼",
        "Burger": "汉堡", "Aussehen": "外貌", "Das erste Treffen": "第一次见面",
        "Tauche ins Unterbewusstsein": "潜入潜意识", "Reise beginnen": "开始旅程",
        "Handy weitergeben": "把手机交给对方", "ODER": "或",
        "App-Sprache auswählen": "选择应用语言",
    },
    "zh-TW": {
        "Schließen": "關閉", "Dein Bild": "你的照片", "Partnerbild": "伴侶照片",
        "Privater Paar-Chat": "私密情侶聊天", "Unbeantwortet": "未回答",
        "Unbeantwortete Fragen": "未回答的問題", "Entweder oder": "二選一",
        "Frage": "問題", "Unterhaltung": "娛樂", "Hochzeit": "婚禮",
        "Burger": "漢堡", "Aussehen": "外貌", "Das erste Treffen": "第一次見面",
        "Tauche ins Unterbewusstsein": "潛入潛意識", "Reise beginnen": "開始旅程",
        "Handy weitergeben": "把手機交給對方", "ODER": "或",
        "App-Sprache auswählen": "選擇 App 語言",
    },
}

PLACEHOLDER_RE = re.compile(r'(\\?\$\{[^}]+\}|\{[^}]+\}|%\d*\$?[a-zA-Z])')


def protect(text: str) -> tuple[str, dict[str, str]]:
    placeholders: dict[str, str] = {}
    def repl(match: re.Match[str]) -> str:
        token = f"HARMONYPLACEHOLDER{len(placeholders):02d}"
        placeholders[token] = match.group(0)
        return token
    return PLACEHOLDER_RE.sub(repl, text), placeholders


def restore(text: str, placeholders: dict[str, str]) -> str:
    for token, value in placeholders.items():
        text = re.sub(re.escape(token), lambda _: value, text, flags=re.IGNORECASE)
    return text


def google_request(text: str, target: str) -> str:
    params = urllib.parse.urlencode({"client": "gtx", "sl": "de", "tl": target, "dt": "t", "q": text})
    url = "https://translate.googleapis.com/translate_a/single?" + params
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 HarmonyLocalization/2.0"})
    last_error: Exception | None = None
    for attempt in range(6):
        try:
            with urllib.request.urlopen(request, timeout=35) as response:
                data = json.loads(response.read().decode("utf-8"))
            return "".join(segment[0] for segment in data[0] if segment and segment[0]).strip()
        except Exception as exc:  # noqa: BLE001
            last_error = exc
            time.sleep(1.5 * (attempt + 1))
    raise RuntimeError(f"Translation failed for {target}: {last_error}")


def request_single(source: str, target: str) -> str:
    protected, placeholders = protect(source)
    return restore(google_request(protected, target), placeholders)


def request_batch(items: list[str], target: str) -> dict[str, str]:
    rows: list[tuple[str, str, dict[str, str], str]] = []
    for index, source in enumerate(items):
        protected, placeholders = protect(source)
        marker = f"[[HARMONY{index:03d}]]"
        rows.append((source, marker, placeholders, protected))
    payload = "\n".join(f"{marker} {protected}" for _, marker, _, protected in rows)
    translated = google_request(payload, target)
    parsed: dict[int, str] = {}
    marker_re = re.compile(r'^\[\[HARMONY(\d{3})\]\]\s*(.*)$', re.IGNORECASE)
    for line in translated.splitlines():
        match = marker_re.match(line.strip())
        if match:
            parsed[int(match.group(1))] = match.group(2).strip()
    if len(parsed) != len(items):
        return {source: request_single(source, target) for source in items}
    result: dict[str, str] = {}
    for index, (source, _, placeholders, _) in enumerate(rows):
        result[source] = restore(parsed[index], placeholders)
    return result


def kotlin_escape(value: str) -> str:
    # audit_localization keeps canonical Kotlin \n escapes as stable key text.
    # Normalize them through a real newline so we write exactly one Kotlin \n escape,
    # rather than doubling the backslash and changing the catalog key.
    value = value.replace(r"\n", "\n")
    return (value.replace("\\", "\\\\")
                 .replace('"', '\\"')
                 .replace("\n", "\\n")
                 .replace("$", "\\$"))


def write_catalog(path: Path, map_name: str, dynamic_name: str, values: dict[str, str]) -> None:
    lines = [
        "package com.example.ui",
        "",
        "/** Generated production locale catalog aligned to the current Harmony customer catalog. */",
        f"internal val {map_name}: Map<String, String> = mapOf(",
    ]
    for source in sorted(values):
        lines.append(f'    "{kotlin_escape(source)}" to "{kotlin_escape(values[source])}",')
    lines += [
        ")",
        "",
        f"internal fun {dynamic_name}(text: String): String? =",
        f"    localizeGeneratedLocaleDynamicContent(text, {map_name})",
        "",
    ]
    path.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    if not MISSING.exists():
        raise RuntimeError("localization_missing.json missing; run audit_localization.py first")
    missing: dict[str, list[str]] = json.loads(MISSING.read_text(encoding="utf-8"))

    for code, (filename, map_name, dynamic_name, target) in TARGETS.items():
        path = UI / filename
        existing = audit.extract_map(path, map_name) if path.exists() else {}
        todo = sorted(set(missing.get(code, [])) - set(existing))
        values = dict(existing)
        if todo:
            print(f"Translating {len(todo)} strings for {code}", flush=True)
            for offset in range(0, len(todo), 8):
                chunk = todo[offset:offset + 8]
                values.update(request_batch(chunk, target))
                if offset and offset % 80 == 0:
                    print(f"  {code}: {min(offset + 8, len(todo))}/{len(todo)}", flush=True)
        values.update({k: v for k, v in REVIEWED[code].items() if k in values or k in todo})
        if todo or not existing:
            write_catalog(path, map_name, dynamic_name, values)
            print(f"Wrote {filename}: {len(values)} entries", flush=True)
        else:
            print(f"{code}: already complete; keeping {filename}", flush=True)


if __name__ == "__main__":
    main()
