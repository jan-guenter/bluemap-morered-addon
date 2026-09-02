/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MoreRedWireCatalogTest {

    @Test
    void ownsTheExactEighteenInstalledWireFamilies() {
        assertEquals(18, MoreRedWireCatalog.BLOCK_IDS.size());
        assertEquals(18, MoreRedWireCatalog.BLOCK_ID_SET.size());
        assertEquals(18, MoreRedWireCatalog.textureKeys().size());
        assertTrue(MoreRedWireCatalog.owns("morered:red_alloy_wire"));
        assertTrue(MoreRedWireCatalog.owns("morered:bundled_network_cable"));
        assertTrue(MoreRedWireCatalog.owns("morered:black_network_cable"));
        assertEquals(
                "morered:block/light_blue_network_cable_line",
                MoreRedWireCatalog.modelKey(
                        "morered:light_blue_network_cable", "line"
                ).getFormatted()
        );
    }

    @Test
    void appliesTheExactInternalCoplanarCompatibilityMatrix() {
        String redAlloy = "morered:red_alloy_wire";
        String bundled = "morered:bundled_network_cable";
        String white = "morered:white_network_cable";
        String blue = "morered:blue_network_cable";

        assertTrue(MoreRedWireCatalog.coplanarCompatible(redAlloy, white));
        assertTrue(MoreRedWireCatalog.coplanarCompatible(white, redAlloy));
        assertTrue(MoreRedWireCatalog.coplanarCompatible(bundled, white));
        assertTrue(MoreRedWireCatalog.coplanarCompatible(white, bundled));
        assertTrue(MoreRedWireCatalog.coplanarCompatible(white, white));
        assertFalse(MoreRedWireCatalog.coplanarCompatible(redAlloy, bundled));
        assertFalse(MoreRedWireCatalog.coplanarCompatible(white, blue));
    }
}
