# Particle Control

A Fabric client mod for Minecraft 1.21.11. All particles are disabled by
default. Press **P** in-game to open a searchable GUI listing every
registered particle type, and toggle any of them back on individually.

## How it works

- `ParticleManagerMixin` injects into `ParticleManager#addParticle(...)` and
  cancels the spawn for any particle type not explicitly enabled. This is a
  single interception point that catches every particle in the game,
  vanilla or modded, rather than hardcoding a list of types.
- `ParticleConfig` stores the enabled set as JSON in
  `config/particlecontrol/particles.json`, loaded on client start and saved
  whenever you close the GUI or hit Enable/Disable all.
- `ParticleControlScreen` builds its list straight from the
  `Registries.PARTICLE_TYPE` registry, so it will always include every
  particle in the current game version without manual updates.

## Building

You need a JDK 21 install.

```bash
git clone <your repo url>
cd particle-control
./gradlew build
```

The compiled jar will be in `build/libs/`. Drop it into your `mods/`
folder alongside Fabric Loader 0.18.1+ and Fabric API 0.141.4+1.21.11.

This project doesn't ship a Gradle wrapper jar (binary files aren't
practical to hand you directly). Before your first build, generate one:

```bash
gradle wrapper --gradle-version 8.11
```

If you don't have Gradle installed locally, the easiest fix is to open the
project in **IntelliJ IDEA with the Minecraft Development plugin** — it can
generate the wrapper for you, or you can start from the
[official Fabric template mod generator](https://fabricmc.net/develop/template/)
and copy these source files into it.

## A note on accuracy

I mapped this against Yarn `1.21.11+build.4`, the last version to ship Yarn
mappings before Mojang mappings become the default. Minecraft's client
internals shift around release to release (particularly package paths like
`InputUtil`), so if you hit a compile error on an import, it's almost
always a one-line fix: right-click the broken symbol in your IDE and let it
suggest the correct import from your local Minecraft sources, or check the
class in the Yarn javadocs at
`https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/`.

## Ideas for later

- Category tabs (weather / potion / block / entity / redstone) like the
  mockup — group by particle id prefix or a manually curated map.
- Mod Menu integration for a settings-screen entry point instead of only a
  keybind.
- Keep the GUI open while the world renders (already the case —
  `shouldPause()` returns false) and preview a particle live when hovered.
