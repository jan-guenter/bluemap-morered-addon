# BlueMap More Red Add-on

A Java 21 BlueMap add-on for the exact `morered-1.21.1-6.0.0.3` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.3` is the owner-accepted native BlueMap 5.23 release
candidate. It preserves the owner-accepted `0.1.0-alpha.2` rendering and
sources exact-artifact admission from the pinned `bluemap-addon-runtime`
module. After admission, it renders all
18 face-wire families from More Red's installed models and textures, including
straight runs, co-located elbows, compatible mixed media, and convex corners.

## Build

Clone with the exact development-tool submodule:

```bash
git clone --recurse-submodules \
  https://github.com/jan-guenter/bluemap-morered-addon.git
```

For an existing clone, initialize it before running Gradle:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-runtime \
  modules/bluemap-addon-adapter-api
```

The settings preflight accepts only the committed toolkit, runtime, and
Adapter API gitlinks at their exact expected commits and rejects an
uninitialized, changed, or dirty submodule. The modules' main Java sources are
compiled into this add-on; their standalone JARs are never installed or nested.

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PmoreRedJar=/path/to/morered-1.21.1-6.0.0.3.jar \
  -PreleaseTag=v0.1.0-alpha.3 clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the generated
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

Place the production JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.morered.disabled=true` to leave the exact profile inactive.

## Scope boundary

The implemented pass covers `red_alloy_wire`, `bundled_network_cable`, and all
16 colored network cables. It preserves the installed node and elbow models
and reconstructs their dynamic line and convex-edge topology. Persisted power
tint, free-span post cables, particles, and animation remain outside this pass.
Malformed resources or unsupported artifact profiles leave the add-on inactive.

No More Red binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
