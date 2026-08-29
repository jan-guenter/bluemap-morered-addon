# BlueMap More Red Add-on

A Java 21 BlueMap add-on for the exact `morered-1.21.1-6.0.0.3` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: owner-accepted `0.1.0-alpha.1` release candidate. After exact artifact
admission, it renders all 18 face-wire families from More Red's installed
models and textures, including straight runs, co-located elbows, compatible
mixed media, and convex corners.

## Build

Clone with the exact development-tool submodule:

```bash
git clone --recurse-submodules \
  https://github.com/jan-guenter/bluemap-morered-addon.git
```

For an existing clone, initialize it before running Gradle:

```bash
git submodule update --init --recursive -- tooling/bluemap-addon-toolkit
```

The settings preflight accepts only the committed toolkit gitlink at its exact
expected commit and rejects an uninitialized, changed, or dirty submodule.

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
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
