# Simple AFK Room

Players who stay idle for a set time are **automatically sent to an AFK room**. When they move, they **return to the exact spot where they were**.

## Features
- Idle detection: movement, looking around, clicks and commands all count as activity
- In the AFK room, players get a title display and have collision turned off
- If a player logs out while in the AFK room, they return to their original spot
- On server stop or reload, everyone in the AFK room is returned
- The AFK room can be in another world (such as the Nether)
- Use the `afkroom.bypass` permission to exclude specific players

## Usage
1. Stand where you want the AFK room and run `/afkroom set`
2. Change the idle time with `/afkroom time <sec>` (default 300 seconds)

## Commands / Permissions
- `/afkroom set | tp | time [sec] | reload` — `afkroom.admin` (OP)
- `afkroom.bypass` — Never sent to the AFK room

## Requirements
Paper / Purpur / Spigot 1.18 or later (Java 17+). No dependencies.
