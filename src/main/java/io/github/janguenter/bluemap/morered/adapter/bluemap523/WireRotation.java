/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.VectorM3f;

/** Derives cube rotations from canonical installed-model axes without coordinate tables. */
final class WireRotation {

    private static final int[] QUARTER_TURNS = {0, 90, 180, 270};
    private static final ResourcePath<Model> PROBE_MODEL =
            new ResourcePath<>("bluemap_morered:block/rotation_probe");

    private WireRotation() {
    }

    static Variant line(Key model, Direction face, Direction toward) {
        Rotation rotation = find(Direction.DOWN, face, Direction.NORTH, toward);
        return rotation.variant(model);
    }

    static Variant edge(Key model, Direction sideA, Direction sideB) {
        Rotation rotation = find(Direction.DOWN, sideA, Direction.WEST, sideB);
        return rotation.variant(model);
    }

    static Rotation find(
            Direction sourceA,
            Direction targetA,
            Direction sourceB,
            Direction targetB
    ) {
        if (sourceA.getAxis() == sourceB.getAxis()
                || targetA.getAxis() == targetB.getAxis()) {
            throw new IllegalArgumentException("rotation basis is not orthogonal");
        }
        for (int x : QUARTER_TURNS) {
            for (int y : QUARTER_TURNS) {
                for (int z : QUARTER_TURNS) {
                    Rotation candidate = new Rotation(x, y, z);
                    if (candidate.maps(sourceA, targetA)
                            && candidate.maps(sourceB, targetB)) {
                        return candidate;
                    }
                }
            }
        }
        throw new IllegalArgumentException("no cube rotation maps the requested basis");
    }

    record Rotation(int x, int y, int z) {

        Variant variant(Key model) {
            return new Variant(new ResourcePath<Model>(model), x, y, z);
        }

        boolean maps(Direction source, Direction target) {
            Variant probe = new Variant(PROBE_MODEL, x, y, z);
            VectorM3f transformed = new VectorM3f(0F, 0F, 0F)
                    .set(source.toVector())
                    .rotateAndScale(probe.getTransformMatrix());
            return Math.abs(transformed.x - target.toVector().getX()) < 0.01F
                    && Math.abs(transformed.y - target.toVector().getY()) < 0.01F
                    && Math.abs(transformed.z - target.toVector().getZ()) < 0.01F;
        }
    }
}
