/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.addon.runtime.artifact.ExactArtifactDetector;
import io.github.janguenter.bluemap.morered.activation.AddonRuntime;
import io.github.janguenter.bluemap.morered.profile.MoreRed6003Profile;

import java.nio.file.Path;
import java.util.Set;

/** Exact-artifact admission and atomic installed-resource routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private InstalledWireResources.Admission admission;

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.morered.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, MoreRed6003Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        admission = InstalledWireResources.inspect(resourcePack);
        if (admission == null) {
            runtime.inactive("installed-wire-schema-invalid");
            return;
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return admission == null ? Set.of() : MoreRedWireCatalog.textureKeys();
    }

    @Override
    public void bake() {
        if (admission == null) {
            return;
        }
        if (!InstalledWireResources.bakedModelsValid(resourcePack)) {
            admission = null;
            runtime.inactive("installed-wire-model-invalid");
            return;
        }
        if (!admission.route(renderer)) {
            admission = null;
            runtime.inactive("wire-routing-collision");
            return;
        }
        runtime.activate();
        System.out.println("BlueMap More Red add-on active: 18 installed-resource wires.");
    }

    @Override
    public void getBlockProperties(BlockState blockState, BlockProperties.Builder builder) {
        if (runtime.active() && MoreRedWireCatalog.owns(
                blockState.getId().getFormatted()
        )) {
            builder.culling(false).occluding(false).cullingIdentical(false);
        }
    }
}
