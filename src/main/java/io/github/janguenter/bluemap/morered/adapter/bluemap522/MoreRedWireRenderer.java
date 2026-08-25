/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.morered.activation.AddonRuntime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Emits installed More Red line/edge models for one exact admitted profile. */
final class MoreRedWireRenderer implements BlockRenderer {

    private final AddonRuntime runtime;
    private final ResourceModelRenderer resources;
    private final Map<ModelPartKey, Variant> variants = new ConcurrentHashMap<>();
    private final Color partColor = new Color();

    MoreRedWireRenderer(
            ResourcePack pack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.runtime = runtime;
        this.resources = new ResourceModelRenderer(pack, textures, settings);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant fallback,
            TileModelView target,
            Color mapColor
    ) {
        if (!runtime.active()) {
            resources.render(block, fallback, target, mapColor);
            return;
        }

        int start = target.getStart();
        Color initialMapColor = new Color().set(mapColor);
        try {
            BlockState state = block.getBlockState();
            String blockId = state.getId().getFormatted();
            if (!MoreRedWireCatalog.owns(blockId) || !isPartsVariant(blockId, fallback)) {
                resources.render(block, fallback, target, mapColor);
                return;
            }

            mapColor.set(0F, 0F, 0F, 0F, true);
            float opacity = 0F;
            for (WireTopology.Part part : WireTopology.parts(
                    state, (x, y, z) -> neighbor(block, x, y, z)
            )) {
                partColor.set(0F, 0F, 0F, 0F, true);
                resources.render(
                        block,
                        variant(blockId, part),
                        target.initialize(),
                        partColor
                );
                opacity = Math.max(opacity, partColor.a);
                mapColor.add(partColor.premultiplied());
            }
            if (mapColor.a > 0F) {
                mapColor.flatten().straight();
                mapColor.a = opacity;
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            target.getTileModel().reset(start);
            target.initialize(start);
            mapColor.set(initialMapColor);
            runtime.inactive("wire-renderer-" + exception.getClass().getSimpleName());
            resources.render(block, fallback, target, mapColor);
        }
    }

    private Variant variant(String blockId, WireTopology.Part part) {
        return variants.computeIfAbsent(new ModelPartKey(blockId, part), key -> {
            Key model = MoreRedWireCatalog.modelKey(
                    blockId,
                    part.kind() == WireTopology.Kind.LINE ? "line" : "edge"
            );
            return part.kind() == WireTopology.Kind.LINE
                    ? WireRotation.line(model, part.sideA(), part.sideB())
                    : WireRotation.edge(model, part.sideA(), part.sideB());
        });
    }

    private static BlockState neighbor(BlockNeighborhood block, int x, int y, int z) {
        return block.getNeighborBlock(x, y, z).getBlockState();
    }

    private static boolean isPartsVariant(String blockId, Variant variant) {
        return variant.getRenderer() == BlueMap522Adapter.renderer()
                && MoreRedWireCatalog.modelKey(blockId, "parts").equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock();
    }

    private record ModelPartKey(String blockId, WireTopology.Part part) {
    }
}
