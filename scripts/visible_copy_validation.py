#!/usr/bin/env python3
"""Validation gates for Harmony visible-copy assets and route coverage."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET

RASTER_SUFFIXES = {".png", ".jpg", ".jpeg", ".webp"}


def _load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def _strip_namespace(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def _svg_has_text(path: Path) -> bool:
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        return False
    for elem in tree.getroot().iter():
        if _strip_namespace(elem.tag) in {"text", "tspan"} and "".join(elem.itertext()).strip():
            return True
    return False


def _expand_reviewed_globs(root: Path, registry: dict) -> dict:
    expanded = dict(registry)
    reviewed = list(registry.get("reviewed_text_free_assets", []))
    known = {entry["path"] for entry in reviewed}
    for pattern in registry.get("reviewed_text_free_globs", []):
        matches = [path for path in root.glob(pattern) if path.is_file()]
        if not matches:
            continue
        for path in sorted(matches):
            rel = path.relative_to(root).as_posix()
            if rel not in known:
                reviewed.append({"path": rel})
                known.add(rel)
    expanded["reviewed_text_free_assets"] = reviewed
    return expanded


def validate_asset_registry(root: Path, registry: dict) -> list[str]:
    root = root.resolve()
    registry = _expand_reviewed_globs(root, registry)
    errors: list[str] = []
    classified: dict[str, str] = {}

    for bucket in ("text_bearing_assets", "brand_only_assets", "reviewed_text_free_assets"):
        for entry in registry.get(bucket, []):
            rel = entry["path"]
            if rel in classified:
                errors.append(f"asset classified more than once: {rel}")
            classified[rel] = bucket

    candidates: list[Path] = []
    for base in (root / "app/src/main/res", root / "app/src/main/assets"):
        if not base.exists():
            continue
        for path in base.rglob("*"):
            if path.is_file() and (path.suffix.lower() in RASTER_SUFFIXES or path.suffix.lower() == ".svg"):
                candidates.append(path)

    for path in sorted(candidates):
        rel = path.relative_to(root).as_posix()
        suffix = path.suffix.lower()
        must_classify = suffix in RASTER_SUFFIXES or (suffix == ".svg" and _svg_has_text(path))
        if must_classify and rel not in classified:
            errors.append(f"unclassified visual asset: {rel}")

    for rel, bucket in sorted(classified.items()):
        path = root / rel
        if not path.exists():
            errors.append(f"registered asset missing: {rel}")
            continue
        entry = next(item for item in registry[bucket] if item["path"] == rel)
        if bucket == "text_bearing_assets" and not entry.get("german_text"):
            errors.append(f"text-bearing asset has no german_text: {rel}")
        if bucket == "brand_only_assets" and not entry.get("visible_text"):
            errors.append(f"brand-only asset has no visible_text: {rel}")

    return sorted(set(errors))


def validate_route_manifest(root: Path, manifest: dict, occurrences: list[dict]) -> list[str]:
    root = root.resolve()
    errors: list[str] = []
    occurrence_paths = {occ["path"] for occ in occurrences}
    seen_ids: set[str] = set()

    for route in manifest.get("routes", []):
        route_id = route.get("id", "")
        if not route_id:
            errors.append("route missing id")
            continue
        if route_id in seen_ids:
            errors.append(f"duplicate route id: {route_id}")
        seen_ids.add(route_id)
        source_paths = route.get("source_paths", [])
        if not source_paths:
            errors.append(f"route has no source_paths: {route_id}")
            continue
        existing: list[str] = []
        for rel in source_paths:
            if not (root / rel).exists():
                errors.append(f"route source missing: {route_id}: {rel}")
            else:
                existing.append(rel)
        if existing and not route.get("visual_only", False):
            if not any(rel in occurrence_paths for rel in existing):
                errors.append(f"route has no inventoried visible copy: {route_id}")
        elif route.get("visual_only", False) and not route.get("reason"):
            errors.append(f"visual_only route missing reason: {route_id}")

    return sorted(set(errors))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--asset-only", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()

    from visible_copy_complete import discover_repository

    report = discover_repository(root)
    errors = validate_asset_registry(root, _load_json(root / "scripts/visible_copy_asset_registry.json"))
    if not args.asset_only:
        errors.extend(
            validate_route_manifest(
                root,
                _load_json(root / "scripts/visible_copy_routes.json"),
                report.occurrences,
            )
        )
    if errors:
        for error in sorted(set(errors)):
            print(f"::error::{error}")
        return 1
    print("Visible-copy asset and route validation PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
