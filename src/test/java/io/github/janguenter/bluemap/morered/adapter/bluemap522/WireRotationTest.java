/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.util.Direction;
import org.junit.jupiter.api.Test;

class WireRotationTest {

    @Test
    void derivesEveryFaceLineOrientationFromTheCanonicalAxes() {
        for (Direction face : Direction.values()) {
            for (Direction toward : Direction.values()) {
                if (face.getAxis() == toward.getAxis()) {
                    continue;
                }
                WireRotation.Rotation rotation = WireRotation.find(
                        Direction.DOWN, face, Direction.NORTH, toward
                );
                assertTrue(rotation.maps(Direction.DOWN, face));
                assertTrue(rotation.maps(Direction.NORTH, toward));
            }
        }
    }

    @Test
    void derivesEveryConvexEdgeOrientationFromTheCanonicalAxes() {
        Direction[] directions = Direction.values();
        for (int first = 0; first < directions.length; first++) {
            for (int second = first + 1; second < directions.length; second++) {
                Direction sideA = directions[first];
                Direction sideB = directions[second];
                if (sideA.getAxis() == sideB.getAxis()) {
                    continue;
                }
                WireRotation.Rotation rotation = WireRotation.find(
                        Direction.DOWN, sideA, Direction.WEST, sideB
                );
                assertTrue(rotation.maps(Direction.DOWN, sideA));
                assertTrue(rotation.maps(Direction.WEST, sideB));
            }
        }
    }
}
