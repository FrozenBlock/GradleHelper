# Version Stategies

The `VersionStrategy` that is used defined how the various versions & artifact names are generated from the mod metadata.
It can be customized by setting the `version_strategy` gradle property to a value defined below, or by setting it directly on the `mod` extension.

```kotlin title="neoforge/build.gradle.kts"
mod {
   versionStrategy = MyVersionStrategy()
}
```

## Generated Fields

| Field           | Description                                                                                 |
|-----------------|---------------------------------------------------------------------------------------------|
| modVersion      | replaces `mod_version` in [templates](/general#mod-properties) such as `neoforge.mods.toml` |
| metadataTag     | tag property written to `build/release.json`                                                |
| uploadVersion   | version published to curseforge/modrinth (`version_number` on modrinth, title)              |
| versionName     | display name on modrinth (`name` field, subtitle)                                           |
| displayName     | display name on curseforge (defaults to `versionName`)                                      |
| artifactVersion | version of the maven artifact being published                                               |
| artifactName    | name of the maven artifact being published                                                  |
| baseName        | base name of the JAR file being created                                                     |

## Default Strategy

`version_strategy = frozenblock`

The `FrozenBlockVersionStrategy` is the default. See [FrozenBlock Strategy](#frozenblock-strategy) below for details.

## Simple Strategy

`version_strategy = simple`

The `SimpleStrategy` will strip the version metadata (everything after the plus sign) from everything except the `metadataVersion`.

### Examples

<div class="grid examples" markdown>

- ![](https://raw.githubusercontent.com/TeamGalena/Oreganized/refs/heads/main/1.21.x/.idea/icon.png) [Oreganized](https://github.com/TeamGalena/Oreganized)
- ![](https://raw.githubusercontent.com/PssbleTrngle/DyeTheWorld/refs/heads/main/1.21.x/.idea/icon.png) [DyeTheWorld](https://github.com/PssbleTrngle/DyeTheWorld)

</div>

### Field Outputs

=== "mod.version = "1.0.0""

    | Field           | Value          |
    |-----------------|----------------|
    | modVersion      | `1.0.0`        |
    | metadataTag     | `1.0.0`        |
    | uploadVersion   | `1.0.0`        |
    | artifactVersion | `1.0.0`        |
    | artifactName    | `mod_id`       |
    | baseName        | `mod_id-1.0.0` |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                 |
    |-----------------|-----------------------|
    | modVersion      | `1.0.0`               |
    | metadataTag     | `1.0.0+some.metadata` |
    | uploadVersion   | `1.0.0`               |
    | artifactVersion | `1.0.0`               |
    | artifactName    | `mod_id`              |
    | baseName        | `mod_id-1.0.0`        |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                     |
    |-----------------|---------------------------|
    | modVersion      | `1.0.0-prerelease`        |
    | metadataTag     | `1.0.0-prerelease`        |
    | uploadVersion   | `1.0.0-prerelease`        |
    | artifactVersion | `1.0.0-prerelease`        |
    | artifactName    | `mod_id`                  |
    | baseName        | `mod_id-1.0.0-prerelease` |

## With-Minecraft-Version Strategy

`version_strategy = with_minecraft_version`

Wraps around the default version strategy and adds the minecraft version to the artifact name & some of the versions metadata.

This strategy makes sense for projects that share the same versions between different minecraft version (without for example incrementing the major version).

### Examples

<div class="grid examples" markdown>

- ![](https://raw.githubusercontent.com/PssbleTrngle/Atmosphere/refs/heads/main/1.21.x/.idea/icon.png) [Atmosphere](https://github.com/PssbleTrngle/Atmosphere)
- ![](https://raw.githubusercontent.com/PssbleTrngle/FlightLib/refs/heads/main/1.21.x/.idea/icon.png) [FlightLib](https://github.com/PssbleTrngle/FlightLib)

</div>

### Field Outputs

=== "mod.version = "1.0.0""

    | Field           | Value            |
    |-----------------|------------------|
    | modVersion      | `1.0.0`          |
    | metadataTag     | `1.0.0+mc1.21.1` |
    | uploadVersion   | `1.0.0`          |
    | artifactVersion | `1.0.0`          |
    | artifactName    | `mod_id-1.21.1`  |
    | baseName        | `mod_id-1.0.0`   |

=== "mod.version = "1.0.0+mc1.21.1""

    | Field           | Value            |
    |-----------------|------------------|
    | modVersion      | `1.0.0`          |
    | metadataTag     | `1.0.0+mc1.21.1` |
    | uploadVersion   | `1.0.0`          |
    | artifactVersion | `1.0.0`          |
    | artifactName    | `mod_id-1.21.1`  |
    | baseName        | `mod_id-1.0.0`   |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                          |
    |-----------------|--------------------------------|
    | modVersion      | `1.0.0`                        |
    | metadataTag     | `1.0.0+some.metadata.mc1.21.1` |
    | uploadVersion   | `1.0.0`                        |
    | artifactVersion | `1.0.0`                        |
    | artifactName    | `mod_id-1.21.1`                |
    | baseName        | `mod_id-1.0.0`                 |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                       |
    |-----------------|-----------------------------|
    | modVersion      | `1.0.0-prerelease`          |
    | metadataTag     | `1.0.0-prerelease+mc1.21.1` |
    | uploadVersion   | `1.0.0-prerelease`          |
    | artifactVersion | `1.0.0-prerelease`          |
    | artifactName    | `mod_id-1.21.1`             |
    | baseName        | `mod_id-1.0.0-prerelease`   |

## With-Loader Strategy

`version_strategy = with_loader`

Adds the loader to the artifact name & the version metadata of the generated `build/release.json` tag property.

This strategy makes sense for projects that share the same versions between different loaders, but share the same mod id.

### Examples

<div class="grid examples" markdown>

- ![](https://raw.githubusercontent.com/PssbleTrngle/SliceAndDice/refs/heads/main/neoforge/1.21.x/.idea/icon.png) [Slice & Dice Fabric](https://github.com/PssbleTrngle/SliceAndDice/tree/main/fabric/1.20.x)
- ![](https://raw.githubusercontent.com/PssbleTrngle/dye_depot/refs/heads/main/neoforge/1.21/.idea/icon.png) [Dye Depot](https://github.com/PssbleTrngle/dye_depot)

</div>

### Field Outputs

=== "mod.version = "1.0.0""

    | Field           | Value                   |
    |-----------------|-------------------------|
    | modVersion      | `1.0.0`                 |
    | metadataTag     | `1.0.0+neoforge`        |
    | uploadVersion   | `1.0.0`                 |
    | artifactVersion | `1.0.0`                 |
    | artifactName    | `mod_id-neoforge`       |
    | baseName        | `mod_id-neoforge-1.0.0` |

=== "mod.version = "1.0.0+neoforge""

    | Field           | Value                   |
    |-----------------|-------------------------|
    | modVersion      | `1.0.0`                 |
    | metadataTag     | `1.0.0+neoforge`        |
    | uploadVersion   | `1.0.0`                 |
    | artifactVersion | `1.0.0`                 |
    | artifactName    | `mod_id-neoforge`       |
    | baseName        | `mod_id-neoforge-1.0.0` |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                          |
    |-----------------|--------------------------------|
    | modVersion      | `1.0.0`                        |
    | metadataTag     | `1.0.0+some.metadata.neoforge` |
    | uploadVersion   | `1.0.0`                        |
    | artifactVersion | `1.0.0`                        |
    | artifactName    | `mod_id-neoforge`              |
    | baseName        | `mod_id-neoforge-1.0.0`        |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                              |
    |-----------------|------------------------------------|
    | modVersion      | `1.0.0-prerelease`                 |
    | metadataTag     | `1.0.0-prerelease+neoforge`        |
    | uploadVersion   | `1.0.0-prerelease`                 |
    | artifactVersion | `1.0.0-prerelease`                 |
    | artifactName    | `mod_id-neoforge`                  |
    | baseName        | `mod_id-neoforge-1.0.0-prerelease` |

## FrozenBlock Strategy

`version_strategy = frozenblock`

Used by FrozenBlock mods. The modrinth `version_number` (title) and the curseforge display name
use the full `[mod_version]-mc[minecraft_version]-[loader]` format so that fabric & neoforge
uploads are distinguishable, while the modrinth `name` (subtitle) stays as the plain mod version.

### Field Outputs

=== "mod.version = "3.0", minecraft = "26.2", loader = "fabric""

    | Field           | Value                  |
    |-----------------|------------------------|
    | modVersion      | `3.0`                  |
    | metadataTag     | `3.0`                  |
    | uploadVersion   | `3.0-mc26.2-fabric`    |
    | versionName     | `3.0`                  |
    | displayName     | `3.0-mc26.2-fabric`    |
    | artifactVersion | `3.0-mc26.2`           |
    | artifactName    | `mod_id`               |
    | baseName        | `mod_id-3.0-mc26.2`    |