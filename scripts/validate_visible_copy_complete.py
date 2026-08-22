#!/usr/bin/env python3
"""Run repository-wide asset and route validation against the complete inventory."""
from __future__ import annotations

import json
from pathlib import Path

from visible_copy_complete import discover_repository
from visible_copy_validation import validate_asset_registry, validate_route_manifest


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    report = discover_repository(root)
    asset_registry = json.loads((root / "scripts/visible_copy_asset_registry.json").read_text(encoding="utf-8"))
    route_manifest = json.loads((root / "scripts/visible_copy_routes.json").read_text(encoding="utf-8"))
    errors = validate_asset_registry(root, asset_registry)
    errors.extend(validate_route_manifest(root, route_manifest, report.occurrences))
    if errors:
        for error in sorted(set(errors)):
            print(f"::error::{error}")
        return 1
    print("Complete visible-copy asset and route validation PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
