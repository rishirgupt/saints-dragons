# Saint's Dragons – Minecraft 26.2 port (Fabric), source code only

> **This is an unofficial community port, not the original mod.**
> Saint's Dragons was created by **Leon Saint ([@LilRicefield](https://github.com/LilRicefield))**. The dragons, their design,
> animations, models, textures, sounds and the code are his work. This repository only contains the source code changes needed to
> run the mod on Minecraft 26.2. **Please support the original mod and its author.**

## Original Saint's Dragons

| | |
|---|---|
| Original repository | <https://github.com/LilRicefield/saints-dragons> |
| Modrinth | <https://modrinth.com/mod/saints-dragons> |
| Wiki | <https://raevyx.miraheze.org/wiki/Main_Page> |
| Issues (original mod) | <https://github.com/LilRicefield/saints-dragons/issues> |
| Author | Leon Saint ([@LilRicefield](https://github.com/LilRicefield)), contact `lilricearoni@gmail.com` |

This repository is a GitHub fork of the original project. The port lives on the `26.2-fabric` branch.

## Licensing, and why there is no download

Saint's Dragons uses a dual license (see [`LICENSE.md`](LICENSE.md)):

- **Source code: MIT.** The code in this repository is available under the MIT License. The 26.2 port changes are Copyright (c) 2026
  Rishi Gupta under the same terms; the original code is Copyright (c) 2025 Leon Saint.
- **Art and audio assets: All Rights Reserved** (textures, 3D models, animations, sounds, promotional material). They may only be used as
  part of the *unmodified* original mod and **may not be redistributed or reuploaded**.

For that reason **this repository does not contain any textures, models, animations or sounds, and there is no release jar.**
Everything that is Leon Saint's artwork is left out on purpose, and third party art is listed in [`CREDIT.md`](CREDIT.md)
(Will Tice / unTied Games, under their own license).

If you want to play the mod on 26.2, ask the author for permission or wait for an official release. If you have that permission, copy
the original `assets/` folder from the upstream repository (`common/src/main/resources/assets`) into `src/main/resources/assets` before building.

## What this port changes

- Fabric Loom 1.18 / Java 25 with the unobfuscated Mojang names, Fabric API 0.161.0+26.2 and GeckoLib 5.5.5.
- Rendering, camera, GUI, networking, entity, item and data-component code ported to the 26.2 APIs.
- Dragon animation controller fixes for T-poses while riding, flying and after state changes.
- First person riding camera: the roll fades out when looking straight up and eases in during take-off.

## Building

```
./gradlew build
```

Requires Java 25 and the assets described above.

## Credits

- **Leon Saint** – creator of Saint's Dragons.
- Will Tice / unTied Games – purchased pixel art effects, see [`CREDIT.md`](CREDIT.md).
- The GeckoLib, Fabric, Cloth Config, Mod Menu, JEI and Jade projects for the libraries this mod builds on.

## Reporting problems

Problems that only happen on 26.2 belong in this repository's issues; please do not bother the original author with them.

*Parts of this port were made with the help of Claude (Anthropic).*
