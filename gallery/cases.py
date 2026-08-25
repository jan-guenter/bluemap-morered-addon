#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Bounded complete-block More Red comparison gallery."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "morered_gallery"
ENVELOPE = (172, 99, 172, 205, 102, 205)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


WIRE_IDS = (
    "red_alloy_wire",
    "bundled_network_cable",
    "white_network_cable",
    "orange_network_cable",
    "magenta_network_cable",
    "light_blue_network_cable",
    "yellow_network_cable",
    "lime_network_cable",
    "pink_network_cable",
    "gray_network_cable",
    "light_gray_network_cable",
    "cyan_network_cable",
    "purple_network_cable",
    "blue_network_cable",
    "brown_network_cable",
    "green_network_cable",
    "red_network_cable",
    "black_network_cable",
)

ORDINARY_IDS = (
    "and_2_gate",
    "and_gate",
    "bitwise_and_gate",
    "bitwise_diode",
    "bitwise_not_gate",
    "bitwise_or_gate",
    "bitwise_xnor_gate",
    "bitwise_xor_gate",
    "bundled_cable_post",
    "bundled_cable_relay_plate",
    "diode",
    "hexidecrubrometer",
    "latch",
    "multiplexer",
    "nand_2_gate",
    "nand_gate",
    "nor_gate",
    "not_gate",
    "or_gate",
    "pulse_gate",
    "redwire_post",
    "redwire_post_plate",
    "redwire_post_relay_plate",
    "soldering_table",
    "stone_plate",
    "xnor_gate",
    "xor_gate",
)

POST_IDS = {
    "bundled_cable_post",
    "bundled_cable_relay_plate",
    "redwire_post",
    "redwire_post_plate",
    "redwire_post_relay_plate",
}


def ordinary_state(block_id: str, index: int) -> str:
    if block_id == "soldering_table":
        properties = ""
    elif block_id == "hexidecrubrometer":
        properties = "[face=floor,facing=north,power=0]"
    elif block_id in POST_IDS:
        properties = "[facing=down]"
    elif block_id in {"multiplexer", "pulse_gate"}:
        properties = f"[facing=down,rotation={index % 4},input_b=false]"
    else:
        properties = f"[facing=down,rotation={index % 4}]"
    return f"morered:{block_id}{properties}"


def build_placements() -> tuple[Placement, ...]:
    placements: list[Placement] = []
    for index, block_id in enumerate(ORDINARY_IDS):
        placements.append(
            Placement(
                f"block-{block_id}",
                f"{block_id} representative state",
                174 + 3 * (index % 7),
                100,
                174 + 3 * (index // 7),
                ordinary_state(block_id, index),
                "stock-resource-visible",
            )
        )

    for index, block_id in enumerate(WIRE_IDS):
        start_x = 174 + 4 * (index % 6)
        z = 190 + 4 * (index // 6)
        for offset, suffix in ((0, "west"), (1, "east")):
            placements.append(
                Placement(
                    f"wire-{block_id}-{suffix}",
                    f"{block_id} connected floor pair",
                    start_x + offset,
                    100,
                    z,
                    f"morered:{block_id}[down=true]",
                    "connected-wire-visible",
                )
            )

    for block_id, center_x in (("red_alloy_wire", 176), ("bundled_network_cable", 187)):
        for suffix, dx, dz in (
            ("center", 0, 0),
            ("west", -1, 0),
            ("east", 1, 0),
            ("north", 0, -1),
            ("south", 0, 1),
        ):
            placements.append(
                Placement(
                    f"cross-{block_id}-{suffix}",
                    f"{block_id} floor cross",
                    center_x + dx,
                    100,
                    203 + dz,
                    f"morered:{block_id}[down=true]",
                    "connected-wire-visible",
                )
            )

    placements.extend(
        (
            Placement(
                "hexidecrubrometer-power-15",
                "hexidecrubrometer maximum power state",
                194,
                100,
                203,
                "morered:hexidecrubrometer[face=floor,facing=north,power=15]",
                "stock-resource-visible",
            ),
            Placement(
                "multiplexer-input-b",
                "multiplexer switched input state",
                197,
                100,
                203,
                "morered:multiplexer[facing=down,rotation=0,input_b=true]",
                "stock-resource-visible",
            ),
            Placement(
                "pulse-gate-input-b",
                "pulse gate switched input state",
                200,
                100,
                203,
                "morered:pulse_gate[facing=down,rotation=0,input_b=true]",
                "stock-resource-visible",
            ),
            Placement(
                "stock-control",
                "stone stock rendering control",
                203,
                100,
                203,
                "minecraft:stone",
                "stock-visible",
            ),
        )
    )
    return tuple(placements)


PLACEMENTS = build_placements()


# Exact multi-cell topology cases whose supports are themselves wires or whose
# placement order materially changes the derived connection state.
SPECIAL_BUILD_COMMANDS = (
    "# topology-convex-red-alloy: down-to-north convex edge",
    "setblock 174 99 185 minecraft:stone",
    "setblock 174 99 186 morered:red_alloy_wire[north=true]",
    "setblock 174 100 185 morered:red_alloy_wire[down=true]",
    "setblock 174 100 186 morered:red_alloy_wire",
    "# topology-convex-bundled: down-to-north convex edge",
    "setblock 180 99 185 minecraft:stone",
    "setblock 180 99 186 morered:bundled_network_cable[north=true]",
    "setblock 180 100 185 morered:bundled_network_cable[down=true]",
    "setblock 180 100 186 morered:bundled_network_cable",
    "# topology-concave-red-alloy: co-located down+north elbow",
    "setblock 186 99 186 minecraft:stone",
    "setblock 186 100 185 minecraft:stone",
    "setblock 186 100 186 morered:red_alloy_wire[down=true,north=true]",
    "# topology-unlike-colors: adjacent nodes must not form a line",
    "setblock 192 99 186 minecraft:stone",
    "setblock 193 99 186 minecraft:stone",
    "setblock 192 100 186 morered:red_network_cable[down=true]",
    "setblock 193 100 186 morered:blue_network_cable[down=true]",
    "# topology-red-colored: unlike media must form a coplanar line",
    "setblock 198 99 182 minecraft:stone",
    "setblock 199 99 182 minecraft:stone",
    "setblock 198 100 182 morered:red_alloy_wire[down=true]",
    "setblock 199 100 182 morered:red_network_cable[down=true]",
    "# topology-bundled-colored: unlike media must form a coplanar line",
    "setblock 198 99 186 minecraft:stone",
    "setblock 199 99 186 minecraft:stone",
    "setblock 198 100 186 morered:bundled_network_cable[down=true]",
    "setblock 199 100 186 morered:blue_network_cable[down=true]",
)

SPECIAL_VERIFY_COMMANDS = (
    "execute unless block 174 100 186 morered:red_alloy_wire run tellraw @a {\"text\":\"gallery mismatch: topology-convex-red-alloy\",\"color\":\"red\"}",
    "execute unless block 180 100 186 morered:bundled_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-convex-bundled\",\"color\":\"red\"}",
    "execute unless block 186 100 186 morered:red_alloy_wire run tellraw @a {\"text\":\"gallery mismatch: topology-concave-red-alloy\",\"color\":\"red\"}",
    "execute unless block 192 100 186 morered:red_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-unlike-red\",\"color\":\"red\"}",
    "execute unless block 193 100 186 morered:blue_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-unlike-blue\",\"color\":\"red\"}",
    "execute unless block 198 100 182 morered:red_alloy_wire run tellraw @a {\"text\":\"gallery mismatch: topology-red-colored-red\",\"color\":\"red\"}",
    "execute unless block 199 100 182 morered:red_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-red-colored-cable\",\"color\":\"red\"}",
    "execute unless block 198 100 186 morered:bundled_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-bundled-colored-bundled\",\"color\":\"red\"}",
    "execute unless block 199 100 186 morered:blue_network_cable run tellraw @a {\"text\":\"gallery mismatch: topology-bundled-colored-cable\",\"color\":\"red\"}",
)
