/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.morered.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ExactWireResourceContractTest {

    @Test
    void exactCandidateProvidesTheAdmittedInstalledResourceProgram() throws IOException {
        String configured = System.getProperty("moreRedJar");
        Assumptions.assumeTrue(configured != null && !configured.isBlank());

        try (ZipFile jar = new ZipFile(Path.of(configured).toFile())) {
            for (String blockId : MoreRedWireCatalog.BLOCK_IDS) {
                String path = blockId.substring(blockId.indexOf(':') + 1);
                JsonObject state = json(jar, "assets/morered/blockstates/" + path + ".json");
                JsonArray multipart = state.getAsJsonArray("multipart");
                assertEquals(19, multipart.size());
                assertEquals(
                        "morered:block/" + path + "_parts",
                        multipart.get(0).getAsJsonObject()
                                .getAsJsonObject("apply").get("model").getAsString()
                );

                for (String part : MoreRedWireCatalog.MODEL_PARTS) {
                    assertNotNull(jar.getEntry(
                            "assets/morered/models/block/" + path + '_' + part + ".json"
                    ));
                }
                assertNotNull(jar.getEntry(
                        "assets/morered/textures/block/" + path + ".png"
                ));

                JsonObject dynamic = json(
                        jar, "assets/morered/models/block/" + path + "_parts.json"
                );
                assertEquals("morered:wire_parts", dynamic.get("loader").getAsString());
                assertTrue(dynamic.getAsJsonObject("line").has("parent"));
                assertTrue(dynamic.getAsJsonObject("edge").has("parent"));
            }

            for (String part : new String[]{"node", "elbow", "line", "edge"}) {
                JsonObject redModel = json(
                        jar, "assets/morered/models/block/red_alloy_wire_" + part + ".json"
                );
                assertEquals("morered:rotate_tints", redModel.get("loader").getAsString());
                assertTrue(redModel.has("elements"));
            }
        }
    }

    private static JsonObject json(ZipFile jar, String path) throws IOException {
        ZipEntry entry = jar.getEntry(path);
        assertNotNull(entry, path);
        try (InputStreamReader reader = new InputStreamReader(
                jar.getInputStream(entry), StandardCharsets.UTF_8
        )) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
