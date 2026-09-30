# simple_AFK_Room

A lightweight Paper/Spigot plugin. It sends players to an AFK room after they have been idle for a set time, and returns them to where they were as soon as they move.

Supported: Paper / Purpur / Spigot 1.18 – 26.3 (Java 17+)

## Commands
Permission: `afkroom.admin` (OP by default)

| Command | Description |
| --- | --- |
| `/afkroom` | Show help |
| `/afkroom set` | Set the AFK room to your current position (world, position and facing are saved) |
| `/afkroom tp` | Teleport to the AFK room |
| `/afkroom time [sec]` | Show or change how long a player can be idle before being sent to the AFK room |
| `/afkroom reload` | Reload config.yml |

## Permissions
| Permission | Default | Description |
| --- | --- | --- |
| `afkroom.admin` | OP | Use `/afkroom` |
| `afkroom.bypass` | none | This player is never sent to the AFK room |

## config.yml
```yaml
afk-time: 300          # Idle seconds
afk-room: {}           # Set with /afkroom set
title: '&c&l-*&6&lAFK-Room&c&l*-'
subtitle: '&eMove to go back'
```
Nobody is moved until the AFK room has been set. A config.yml from 1.0 (`afktime`) is migrated automatically.

## Build
```
./gradlew build
```
The output is `build/libs/Simple_AFK_Room-<version>.jar`. On first run, Gradle downloads JDK 25 automatically.
