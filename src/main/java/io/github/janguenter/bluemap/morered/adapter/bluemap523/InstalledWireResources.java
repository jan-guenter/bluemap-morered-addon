/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Atomic admission and routing of the exact installed More Red wire resources. */
final class InstalledWireResources {

    private static final int MULTIPART_VARIANTS = 19;
    private static final int NODE_VARIANTS = 6;
    private static final int ELBOW_VARIANTS = 12;

    private InstalledWireResources() {
    }

    static Admission inspect(ResourcePack pack) {
        List<Variant> dynamicParts = new ArrayList<>();
        for (String blockId : MoreRedWireCatalog.BLOCK_IDS) {
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                    pack.getBlockStates().get(MoreRedWireCatalog.blockKey(blockId));
            if (state == null || state.getMultipart() == null || state.getVariants() != null) {
                return null;
            }
            List<Variant> variants = new ArrayList<>();
            state.forEach(variants::add);
            if (variants.size() != MULTIPART_VARIANTS
                    || !validVariants(blockId, variants, dynamicParts)) {
                return null;
            }
            for (String part : MoreRedWireCatalog.MODEL_PARTS) {
                if (pack.getModels().get(MoreRedWireCatalog.modelKey(blockId, part)) == null) {
                    return null;
                }
            }
        }
        return dynamicParts.size() == MoreRedWireCatalog.BLOCK_IDS.size()
                ? new Admission(List.copyOf(dynamicParts)) : null;
    }

    static boolean bakedModelsValid(ResourcePack pack) {
        try {
            for (String blockId : MoreRedWireCatalog.BLOCK_IDS) {
                Key textureKey = MoreRedWireCatalog.textureKey(blockId);
                Texture texture = pack.getTextures().get(textureKey);
                if (!validTexture(texture)) {
                    return false;
                }
                for (String part : List.of("node", "elbow", "line", "edge")) {
                    Model model = pack.getModels().get(
                            MoreRedWireCatalog.modelKey(blockId, part)
                    );
                    if (!validModel(pack, model, textureKey)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    private static boolean validVariants(
            String blockId,
            List<Variant> variants,
            List<Variant> dynamicParts
    ) {
        int nodes = 0;
        int elbows = 0;
        int parts = 0;
        Key partsKey = MoreRedWireCatalog.modelKey(blockId, "parts");
        Key nodeKey = MoreRedWireCatalog.modelKey(blockId, "node");
        Key elbowKey = MoreRedWireCatalog.modelKey(blockId, "elbow");
        for (Variant variant : variants) {
            if (variant.getRenderer() != BlockRendererType.DEFAULT
                    || variant.isUvlock()
                    || Double.compare(variant.getWeight(), 1D) != 0) {
                return false;
            }
            if (partsKey.equals(variant.getModel())) {
                if (variant.isTransformed()) {
                    return false;
                }
                parts++;
                dynamicParts.add(variant);
            } else if (nodeKey.equals(variant.getModel())) {
                nodes++;
            } else if (elbowKey.equals(variant.getModel())) {
                elbows++;
            } else {
                return false;
            }
        }
        return parts == 1 && nodes == NODE_VARIANTS && elbows == ELBOW_VARIANTS;
    }

    private static boolean validTexture(Texture texture) throws IOException {
        if (texture == null) {
            return false;
        }
        BufferedImage image = texture.getTextureImage();
        return image != null && image.getWidth() == 16 && image.getHeight() == 16;
    }

    private static boolean validModel(ResourcePack pack, Model model, Key textureKey) {
        if (model == null || model.getElements() == null || model.getElements().length == 0) {
            return false;
        }
        for (Element element : model.getElements()) {
            if (element == null || element.getFaces().isEmpty()) {
                return false;
            }
            boolean validFaces = element.getFaces().values().stream().allMatch(face -> {
                Key resolved = face.getTexture().getTexturePath(model.getTextures()::get);
                return textureKey.equals(resolved) && pack.getTextures().get(resolved) != null;
            });
            if (!validFaces) {
                return false;
            }
        }
        return true;
    }

    record Admission(List<Variant> dynamicParts) {

        Admission {
            dynamicParts = List.copyOf(dynamicParts);
        }

        boolean route(BlockRendererType renderer) {
            if (dynamicParts.stream().anyMatch(
                    variant -> variant.getRenderer() != BlockRendererType.DEFAULT
            )) {
                return false;
            }
            int routed = 0;
            try {
                for (Variant variant : dynamicParts) {
                    variant.setRenderer(renderer);
                    routed++;
                }
                return true;
            } catch (RuntimeException exception) {
                for (int index = 0; index < routed; index++) {
                    dynamicParts.get(index).setRenderer(BlockRendererType.DEFAULT);
                }
                return false;
            }
        }
    }
}
