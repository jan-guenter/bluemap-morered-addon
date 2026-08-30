/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.profile;

import io.github.janguenter.bluemap.addon.runtime.artifact.ArtifactPin;

import java.util.List;

/** Exact All the Mons 1.2.0 profile `morered-1.21.1-6.0.0.3`. */
public final class MoreRed6003Profile {

    public static final String PROFILE_ID = "morered-1.21.1-6.0.0.3";
    public static final List<ArtifactPin> ARTIFACTS = List.of(
            new ArtifactPin(
                    "morered",
                    "morered",
                    "1.21.1-6.0.0.3",
                    "morered-1.21.1-6.0.0.3.jar",
                    535_669L,
                    "8075126184f540c6b35b92127088f6cc4c9544627acac9f2287c62a0dfbde74e"
            )
    );

    private MoreRed6003Profile() {
    }
}
