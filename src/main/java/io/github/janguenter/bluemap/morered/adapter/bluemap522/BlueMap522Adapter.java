/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.morered.activation.AddonRuntime;

/** BlueMap 5.22 registration boundary for the exact More Red wire profile. */
public final class BlueMap522Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_morered:wire_parts"),
            (pack, gallery, settings) ->
                    new MoreRedWireRenderer(pack, gallery, settings, RUNTIME)
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ProfileResourceExtensionType(RENDERER, RUNTIME);

    private BlueMap522Adapter() {
    }

    /** Registers the renderer and exact-profile resource extension atomically. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        return true;
    }

    static BlockRendererType renderer() {
        return RENDERER;
    }
}
