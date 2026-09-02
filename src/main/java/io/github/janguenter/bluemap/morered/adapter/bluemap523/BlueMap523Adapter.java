/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.morered.activation.AddonRuntime;

/** BlueMap 5.23 feature-backport registration boundary for More Red wires. */
public final class BlueMap523Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_morered:wire_parts"),
            (pack, gallery, settings) ->
                    new MoreRedWireRenderer(pack, gallery, settings, RUNTIME)
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_morered:exact_profile"),
                    pack -> new ProfileResourceExtension(pack, RENDERER, RUNTIME)
            );

    private BlueMap523Adapter() {
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
