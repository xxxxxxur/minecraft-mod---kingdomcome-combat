# Kingdom Come Combat 1.7.3

All currently publishable targets built successfully on 2026-08-04.

| Loader | Minecraft | Result |
| --- | --- | --- |
| Fabric | 1.21 | Success |
| Fabric | 1.21.1 | Success |
| Fabric | 1.21.6 | Success |
| Fabric | 1.21.7 | Success |
| Fabric | 1.21.8 | Success |
| Fabric | 1.21.10 | Success |
| Fabric | 1.21.11 | Success |
| NeoForge | 1.21.1 | Success |

Verification performed:

- Full Gradle `clean build` for every target.
- Embedded mod version checked as `1.7.3`.
- Embedded Minecraft dependency checked against the artifact name.
- ZIP/JAR archive integrity checked for all artifacts.
- SHA-256 hashes recorded in `SHA256SUMS`.
- NeoForge artifact contains only NeoForge metadata, declares
  `forgified_fabric_api`, and uses the native 1.21.1 `LivingEntity#hurt`
  Mixin descriptor.
- Fabric 1.21.1 uses the legacy `LivingEntity#damage(DamageSource, float)`
  descriptor for all three projectile/injury callbacks, including lightweight
  damage interruption.

Not published by the current compatibility matrix: Minecraft 1.21.2–1.21.5,
1.21.9, and 26.2.
