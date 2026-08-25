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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class WireTopologyTest {

    private static final String WIRE = "morered:red_alloy_wire";

    @Test
    void emitsOneCoplanarHalfLineForMatchingSameIdFaces() {
        Map<Direction, BlockState> neighbors = new EnumMap<>(Direction.class);
        neighbors.put(Direction.NORTH, state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE, Direction.DOWN),
                direction -> neighbors.getOrDefault(direction, BlockState.AIR)
        );

        assertTrue(parts.contains(WireTopology.Part.line(
                Direction.DOWN, Direction.NORTH
        )));
        assertEquals(1, parts.size());
    }

    @Test
    void leavesTheSharedJunctionToTheAuthoredElbowVariant() {
        Map<Direction, BlockState> neighbors = new EnumMap<>(Direction.class);
        neighbors.put(Direction.NORTH, state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE, Direction.DOWN, Direction.NORTH),
                direction -> neighbors.getOrDefault(direction, BlockState.AIR)
        );

        assertFalse(parts.contains(WireTopology.Part.line(
                Direction.DOWN, Direction.NORTH
        )));
    }

    @Test
    void emitsConvexEdgeFromTwoSameIdNeighborFaces() {
        Map<Direction, BlockState> neighbors = new EnumMap<>(Direction.class);
        neighbors.put(Direction.DOWN, state(WIRE, Direction.NORTH));
        neighbors.put(Direction.NORTH, state(WIRE, Direction.DOWN));

        List<WireTopology.Part> parts = WireTopology.parts(
                state(WIRE),
                direction -> neighbors.getOrDefault(direction, BlockState.AIR)
        );

        assertEquals(List.of(WireTopology.Part.edge(
                Direction.DOWN, Direction.NORTH
        )), parts);
    }

    @Test
    void rejectsMalformedSameIdNeighborsInsteadOfGuessing() {
        Map<Direction, BlockState> neighbors = new EnumMap<>(Direction.class);
        neighbors.put(Direction.NORTH, new BlockState(Key.parse(WIRE)));

        assertThrows(IllegalArgumentException.class, () -> WireTopology.parts(
                state(WIRE, Direction.DOWN),
                direction -> neighbors.getOrDefault(direction, BlockState.AIR)
        ));
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
}
