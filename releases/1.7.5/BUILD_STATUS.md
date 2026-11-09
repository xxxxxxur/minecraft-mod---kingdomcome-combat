# Kingdom Come Combat 1.7.5

Build date: 2026-10-27 (Asia/Shanghai).

Built from the current workspace, including English localization fallback fixes.

## Build results

| Target | Result | Artifact / Log |
| --- | --- | --- |
| fabric-1.21.7 | Built and verified | [kingdom_come_combat-1.7.5+fabric-mc1.21.7.jar](kingdom_come_combat-1.7.5+fabric-mc1.21.7.jar) |
| fabric-1.21 | Built and verified | [kingdom_come_combat-1.7.5+fabric-mc1.21.jar](kingdom_come_combat-1.7.5+fabric-mc1.21.jar) |
| fabric-1.21.1 | Built and verified | [kingdom_come_combat-1.7.5+fabric-mc1.21.1.jar](kingdom_come_combat-1.7.5+fabric-mc1.21.1.jar) |
| fabric-1.21.6 | running | [Build log](logs/fabric-1.21.6.log) |

## Verification

- Full Gradle `build` succeeded for each artifact listed as built.
- Embedded 1.7.5 version and exact/ranged Minecraft dependencies checked during packaging.
- JAR integrity, translation keys, packaged localization fallback behavior, and SHA-256 verified.
- Source localization regression suite passed: 360 translation keys, 22 skill books, passive skills, and hardships.
- Game/client/server launch testing was not performed in this build task.

## Targets without release artifacts

`fabric-1.21.6`.

These targets were attempted. Their logs record the remaining source/API or dependency failures; no older binaries were renamed as 1.7.5.
