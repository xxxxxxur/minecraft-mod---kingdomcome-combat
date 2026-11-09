# Kingdom Come Combat 1.7.4

All currently publishable targets built successfully on 2026-08-25.

## Recommended package set

| Loader | Minecraft | Artifact |
| --- | --- | --- |
| Fabric | 1.21 | `kingdom_come_combat-1.7.4+fabric-mc1.21.jar` |
| Fabric | 1.21.1 | `kingdom_come_combat-1.7.4+fabric-mc1.21.1.jar` |
| Fabric | 1.21.6–1.21.8 | `kingdom_come_combat-1.7.4+fabric-mc1.21.6-1.21.8.jar` |
| Fabric | 1.21.10 | `kingdom_come_combat-1.7.4+fabric-mc1.21.10.jar` |
| Fabric | 1.21.11 | `kingdom_come_combat-1.7.4+fabric-mc1.21.11.jar` |
| NeoForge | 1.21.1 | `kingdom_come_combat-1.7.4+neoforge-mc1.21.1.jar` |

The individual 1.21.6, 1.21.7, and 1.21.8 Fabric artifacts are retained as
verification/fallback builds. Their JAR entry lists and contents are identical
apart from `fabric.mod.json` and the informational Minecraft version in the
manifest. The merged artifact uses the verified dependency range
`>=1.21.6 <=1.21.8` and has zero class/resource differences from the 1.21.7
verification build.

The 1.21/1.21.1 pair cannot be merged safely because the hardship damage event
uses a different Fabric API callback. The 1.21.10/1.21.11 pair cannot be merged
safely because the command-renderer, game-rule, spear, and entity accessor
adapters produce version-specific classes and Mixin lists.

## Changes verified

- Added the client-side third-party mob renderer compatibility switch. It moves
  the PAL whole-body transform to the shared `setupTransforms` hook so rewritten
  render methods keep armor and attachment passes aligned with the animated mob.
- Lightweight damage now restores pre-damage velocity and applies KCC attack or
  configured-mob knockback instead of retaining damage-source/vanilla knockback.
- The lightweight damage return injection no longer captures version-sensitive
  `damage`/`hurt` arguments. It checks the result and obtains `ServerWorld` from
  the target safely, including non-humanoid targets such as silverfish.
- Lightweight vanilla damage runs inside an explicit internal replacement
  scope, preventing the nested `damage` call from re-entering KCC external or
  configured-mob attack replacement (and preventing recursive stack overflow).

## Verification performed

- Full Gradle `build` for every artifact in the recommended package set and for
  the three retained individual 1.21.6–1.21.8 verification artifacts.
- Embedded mod version and exact/ranged Minecraft dependency checked.
- ZIP/JAR integrity checked for every artifact.
- Lightweight callback bytecode checked on legacy Fabric, modern Fabric,
  command-renderer Fabric, and NeoForge; the callback captures only
  `CallbackInfoReturnable<Boolean>` on all four paths.
- Legacy and command-renderer compatibility hooks checked in the remapped JARs.
- NeoForge artifact checked as a separately built native loader artifact.
- SHA-256 hashes recorded in `SHA256SUMS`.

## Not published

Minecraft 1.21.2–1.21.5 and 1.21.9 remain outside the current source adapter.
The separate 26.2 prototype was compiled as an audit but still fails before
client compilation because it directly feeds Yarn-named shared sources into an
official-name toolchain. It is not included as a misleading or unsafe package.
