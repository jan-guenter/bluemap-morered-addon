/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Exact installed-resource keys owned by the More Red wire renderer. */
final class MoreRedWireCatalog {

    static final List<String> BLOCK_IDS = List.of(
            "morered:red_alloy_wire",
            "morered:bundled_network_cable",
            "morered:white_network_cable",
            "morered:orange_network_cable",
            "morered:magenta_network_cable",
            "morered:light_blue_network_cable",
            "morered:yellow_network_cable",
            "morered:lime_network_cable",
            "morered:pink_network_cable",
            "morered:gray_network_cable",
            "morered:light_gray_network_cable",
            "morered:cyan_network_cable",
            "morered:purple_network_cable",
            "morered:blue_network_cable",
            "morered:brown_network_cable",
            "morered:green_network_cable",
            "morered:red_network_cable",
            "morered:black_network_cable"
    );
    static final List<String> MODEL_PARTS = List.of(
            "parts", "node", "elbow", "line", "edge"
    );
    static final Set<String> BLOCK_ID_SET = Set.copyOf(BLOCK_IDS);

    private MoreRedWireCatalog() {
    }

    static Key blockKey(String blockId) {
        return Key.parse(blockId);
    }

    static Key modelKey(String blockId, String part) {
        return Key.parse("morered:block/" + path(blockId) + '_' + part);
    }

    static Key textureKey(String blockId) {
        return Key.parse("morered:block/" + path(blockId));
    }

    static Set<Key> textureKeys() {
        LinkedHashSet<Key> textures = new LinkedHashSet<>();
        for (String blockId : BLOCK_IDS) {
            textures.add(textureKey(blockId));
        }
        return Set.copyOf(textures);
    }

    static boolean owns(String blockId) {
        return BLOCK_ID_SET.contains(blockId);
    }

    private static String path(String blockId) {
        int separator = blockId.indexOf(':');
        if (separator < 0 || separator == blockId.length() - 1) {
            throw new IllegalArgumentException("block id has no path");
        }
        return blockId.substring(separator + 1);
    }
}
