#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated complete-block More Red gallery."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")

    coordinates = {(placement.x, placement.y, placement.z) for placement in cases.PLACEMENTS}
    if len(coordinates) != len(cases.PLACEMENTS):
        raise ValueError("gallery placement coordinates overlap")
    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = cases.ENVELOPE
    if any(
        not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        )
        for placement in cases.PLACEMENTS
    ):
        raise ValueError("gallery placement escaped its bounded envelope")

    expected_ids = {f"morered:{block_id}" for block_id in cases.ORDINARY_IDS + cases.WIRE_IDS}
    actual_ids = {
        placement.block_state.split("[", 1)[0]
        for placement in cases.PLACEMENTS
        if placement.block_state.startswith("morered:")
    }
    if actual_ids != expected_ids:
        raise ValueError(
            f"registered block coverage differs: missing={sorted(expected_ids - actual_ids)}, "
            f"extra={sorted(actual_ids - expected_ids)}"
        )

    for block_id in cases.WIRE_IDS:
        matching = [
            placement
            for placement in cases.PLACEMENTS
            if placement.block_state.startswith(f"morered:{block_id}[")
        ]
        if len(matching) < 2 or any("down=true" not in row.block_state for row in matching):
            raise ValueError(f"wire pair coverage missing for {block_id}")
        if not any(
            first.y == second.y
            and first.z == second.z
            and abs(first.x - second.x) == 1
            for first in matching
            for second in matching
        ):
            raise ValueError(f"adjacent wire pair missing for {block_id}")

    stock = [row for row in cases.PLACEMENTS if row.case_id == "stock-control"]
    if len(stock) != 1 or stock[0].block_state != "minecraft:stone":
        raise ValueError("gallery needs one honest stone stock control")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    expected_setblocks = len(cases.PLACEMENTS) * 2
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != expected_setblocks:
        raise ValueError("build function must place one support and one anchor per case")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print(
        f"More Red gallery lint passed: {len(cases.PLACEMENTS)} anchors, "
        f"{len(expected_ids)} registered blocks, {len(cases.WIRE_IDS)} wire families"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
