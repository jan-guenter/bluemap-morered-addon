/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.world.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Bounded neighborhood interpretation for installed line and convex-edge models. */
final class WireTopology {

    private WireTopology() {
    }

    static List<Part> parts(BlockState state, NeighborLookup neighbors) {
        String blockId = state.getId().getFormatted();
        if (!MoreRedWireCatalog.owns(blockId) || !validFaces(state)) {
            throw new IllegalArgumentException("unsupported More Red wire state");
        }

        List<Part> result = new ArrayList<>();
        for (Direction face : Direction.values()) {
            if (!attached(state, face)) {
                continue;
            }
            for (Direction toward : Direction.values()) {
                if (face.getAxis() == toward.getAxis() || attached(state, toward)) {
                    continue;
                }
                BlockState neighbor = neighbors.at(toward);
                if (sameWire(blockId, neighbor) && attachedChecked(neighbor, face)) {
                    result.add(Part.line(face, toward));
                }
            }
        }

        Direction[] directions = Direction.values();
        for (int first = 0; first < directions.length; first++) {
            Direction sideA = directions[first];
            for (int second = first + 1; second < directions.length; second++) {
                Direction sideB = directions[second];
                if (sideA.getAxis() == sideB.getAxis()) {
                    continue;
                }
                BlockState neighborA = neighbors.at(sideA);
                BlockState neighborB = neighbors.at(sideB);
                if (sameWire(blockId, neighborA)
                        && sameWire(blockId, neighborB)
                        && attachedChecked(neighborA, sideB)
                        && attachedChecked(neighborB, sideA)) {
                    result.add(Part.edge(sideA, sideB));
                }
            }
        }
        return List.copyOf(result);
    }

    static boolean validFaces(BlockState state) {
        Map<String, String> properties = state.getProperties();
        for (Direction direction : Direction.values()) {
            String value = properties.get(property(direction));
            if (!"true".equals(value) && !"false".equals(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameWire(String blockId, BlockState state) {
        return state != null && blockId.equals(state.getId().getFormatted());
    }

    private static boolean attachedChecked(BlockState state, Direction direction) {
        if (!validFaces(state)) {
            throw new IllegalArgumentException("malformed adjacent More Red wire state");
        }
        return attached(state, direction);
    }

    private static boolean attached(BlockState state, Direction direction) {
        return "true".equals(state.getProperties().get(property(direction)));
    }

    private static String property(Direction direction) {
        return direction.name().toLowerCase(Locale.ROOT);
    }

    @FunctionalInterface
    interface NeighborLookup {
        BlockState at(Direction direction);
    }

    record Part(Kind kind, Direction sideA, Direction sideB) {

        Part {
            if (sideA == null || sideB == null || sideA.getAxis() == sideB.getAxis()) {
                throw new IllegalArgumentException("wire part directions are invalid");
            }
        }

        static Part line(Direction face, Direction toward) {
            return new Part(Kind.LINE, face, toward);
        }

        static Part edge(Direction sideA, Direction sideB) {
            return new Part(Kind.EDGE, sideA, sideB);
        }
    }

    enum Kind {
        LINE,
        EDGE
    }
}
