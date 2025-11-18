# AGENTS.md

This file tracks significant changes and updates to the Banishment Mod project, particularly those made through AI agent assistance.

---

## Project Overview

**Banishment Mod** is a Minecraft NeoForge mod designed for hardcore multiplayer servers. Instead of players becoming spectators when they die, they can be "banished" to a remote location, keeping them in survival mode.

**Version:** 0.0.17  
**Minecraft Version:** 1.21.8  
**Mod Loader:** NeoForge 21.8.51+  
**License:** MIT  
**Author:** Escape From Sean!

---

## Initial Release (2025-11-15)

### Features Implemented

#### Core Functionality
- **Banish Command** (`/banish <player>`)
  - Requires operator permission level 2
  - Initiates a configurable countdown before teleportation
  - Plays alternating high/low pitch note sounds during countdown
  - Broadcasts countdown messages to all players
  - Plays thunder sound effect upon banishment
  - Teleports player and sets them to survival mode

#### Configuration System
- **Config Commands** (`/banishconfig`)
  - `set location <x> <y> <z>` - Set fixed teleport destination
  - `set <option> <value>` - Configure various settings
  - `view` - Display current configuration
  - `reload` - Reload config from file
  
- **Config Options:**
  - `countdown` - Seconds before teleportation (default: 5)
  - `highnote` - High pitch for note sound (default: 1.5)
  - `lownote` - Low pitch for note sound (default: 0.5)
  - `noteinterval` - Ticks between notes (default: 5)
  - `userandom` - Enable random teleport location (default: true)
  - `mindistance` - Minimum teleport distance (default: 50)
  - `maxdistance` - Maximum teleport distance (default: 200)

- **Persistent Storage:** Configuration saved to `config/banishmentmod.json`

#### Advanced Features
- **Random Position Finder**
  - Searches for safe teleport locations within configurable radius
  - Validates ground is solid (not air, lava, or cactus)
  - Uses heightmap for accurate surface detection
  - Attempts up to 30 positions before failing
  
- **Countdown Manager**
  - Server tick-based countdown system
  - Alternating high/low pitch audio cues
  - Per-second countdown broadcasts with formatting
  - Prevents overlapping countdowns
  - Automatic cleanup after execution

#### Technical Implementation
- Uses Brigadier command system with tab completion
- Event-driven architecture with NeoForge event bus
- JSON-based configuration with Gson
- Thread-safe countdown management
- Proper resource cleanup and event unregistration

### File Structure
```
src/main/java/com/escape/banishmentmod/
├── BanishmentMod.java       - Main mod class and command registration
├── BanishCommand.java        - Command implementation and logic
├── CountdownManager.java     - Countdown timer and audio system
├── ModConfig.java            - Configuration management
└── PositionFinder.java       - Safe location finding algorithm

src/main/resources/META-INF/
└── neoforge.mods.toml        - Mod metadata and dependencies
```

---

## Notes

This AGENTS.md file documents the evolution of the Banishment Mod. Future updates by agents or contributors should add new sections above this line, maintaining chronological order (newest first after Project Overview).

---

*Last Updated: 2025-11-18*
