/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class WireTopologyTest {

    private static final String WIRE = "morered:red_alloy_wire";

    @Test
    void emitsOneCoplanarHalfLineForMatchingSameIdFaces() {
        Map<Offset, BlockState> neighbors = new HashMap<>();
        neighbors.put(offset(Direction.NORTH), state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE, Direction.DOWN),
                lookup(neighbors)
        );

        assertTrue(parts.contains(WireTopology.Part.line(
                Direction.DOWN, Direction.NORTH
        )));
        assertEquals(1, parts.size());
    }

    @Test
    void leavesTheSharedJunctionToTheAuthoredElbowVariant() {
        Map<Offset, BlockState> neighbors = new HashMap<>();
        neighbors.put(offset(Direction.NORTH), state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE, Direction.DOWN, Direction.NORTH),
                lookup(neighbors)
        );

        assertFalse(parts.contains(WireTopology.Part.line(
                Direction.DOWN, Direction.NORTH
        )));
    }

    @Test
    void emitsConvexEdgeFromTwoSameIdNeighborFaces() {
        Map<Offset, BlockState> neighbors = new HashMap<>();
        neighbors.put(offset(Direction.DOWN), state(WIRE, Direction.NORTH));
        neighbors.put(offset(Direction.NORTH), state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE),
                lookup(neighbors)
        );

        assertEquals(List.of(WireTopology.Part.edge(
                Direction.DOWN, Direction.NORTH
        )), parts);
    }

    @Test
    void rejectsMalformedSameIdNeighborsInsteadOfGuessing() {
        Map<Offset, BlockState> neighbors = new HashMap<>();
        neighbors.put(offset(Direction.NORTH), new BlockState(Key.parse(WIRE)));

        assertThrows(IllegalArgumentException.class, () -> WireTopology.parts(
                state(WIRE, Direction.DOWN),
                lookup(neighbors)
        ));
    }

    @Test
    void connectsOnlyTheAuditedMixedMediaPairs() {
        Map<Offset, BlockState> compatible = new HashMap<>();
        compatible.put(
                offset(Direction.NORTH),
                state("morered:white_network_cable", Direction.DOWN)
        );
        assertTrue(WireTopology.parts(
                state(WIRE, Direction.DOWN), lookup(compatible)
        ).contains(WireTopology.Part.line(Direction.DOWN, Direction.NORTH)));

        Map<Offset, BlockState> incompatible = new HashMap<>();
        incompatible.put(
                offset(Direction.NORTH),
                state("morered:bundled_network_cable", Direction.DOWN)
        );
        assertFalse(WireTopology.parts(
                state(WIRE, Direction.DOWN), lookup(incompatible)
        ).contains(WireTopology.Part.line(Direction.DOWN, Direction.NORTH)));
    }

    @Test
    void continuesSameBlockLineIntoTheDiagonalConvexLeg() {
        Map<Offset, BlockState> neighbors = new HashMap<>();
        neighbors.put(offset(Direction.UP), state(WIRE));
        neighbors.put(
                offset(Direction.UP, Direction.NORTH),
                state(WIRE, Direction.DOWN)
        );

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE, Direction.NORTH), lookup(neighbors)
        );

        assertTrue(parts.contains(WireTopology.Part.line(
                Direction.NORTH, Direction.UP
        )));
    }

    private static BlockState state(String id, Direction... attached) {
        Map<String, String> properties = new HashMap<>();
        for (Direction direction : Direction.values()) {
            properties.put(direction.name().toLowerCase(Locale.ROOT), "false");
        }
        for (Direction direction : attached) {
            properties.put(direction.name().toLowerCase(Locale.ROOT), "true");
        }
        properties.put("transform", "identity");
        return new BlockState(Key.parse(id), Map.copyOf(properties));
    }

    private static WireTopology.NeighborLookup lookup(
            Map<Offset, BlockState> neighbors
    ) {
        return (x, y, z) -> neighbors.getOrDefault(
                new Offset(x, y, z), BlockState.AIR
        );
    }

    private static Offset offset(Direction... directions) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (Direction direction : directions) {
            x += direction.toVector().getX();
            y += direction.toVector().getY();
            z += direction.toVector().getZ();
        }
        return new Offset(x, y, z);
    }

    private record Offset(int x, int y, int z) {
    }
}
