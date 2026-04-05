# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=TestClassName

# Run a single test method
mvn test -Dtest=TestClassName#methodName

# Full verify with coverage (JaCoCo)
mvn verify
```

## Project Overview

AOneBlock is a BentoBox GameModeAddon for Minecraft. Players start on a single magic block in the sky; mining that block cycles through 18+ sequential phases (Plains, Underground, Ocean, Jungle, etc.), each with its own blocks, mobs, and chest loot. The plugin extends `GameModeAddon` from BentoBox and hooks into the Bukkit event system.

## Key Architecture

**Entry point:** `AOneBlock.java` — registers listeners, commands, generators, and the `OneBlocksManager`.

**Phase system (`oneblocks/`):**
- `OneBlocksManager` — loads phase YAML files from `src/main/resources/phases/`, tracks per-island block counts, handles phase transitions, and resolves what block/mob/chest appears next.
- `OneBlockPhase` — represents a single phase with its block pool, mob pool, and chest tables.
- `OneBlockObject` — a single block-pool entry (can be a vanilla block, custom block, or mob spawn).
- `customblock/` — adapters for custom block types (ItemsAdder, Mob, raw BlockData).

**Data persistence:** `OneBlockIslands` (in `dataobjects/`) is stored via BentoBox's database abstraction. It tracks block count, phase name, and hologram state per island.

**Listeners (`listeners/`):** Handle the magic block break event (core gameplay loop), boss bar updates, hologram placement, island join/leave hooks, and protection flags.

**Events (`events/`):** `MagicBlockEvent`, `MagicBlockPhaseEvent`, and `BlockClearEvent` are fired during gameplay so other plugins can react.

**Commands:**
- Player: `/ob` (`PlayerCommand`) — sub-commands: count, phases, actionbar, bossbar, respawnblock, setcount
- Admin: `/oba` (`AdminCommand`) — sub-commands: setchest, sanitycheck, setcount

**World generation:** `ChunkGeneratorWorld` generates the empty void world with the single starter block.

**GUI:** `PhasesPanel` provides a clickable phase browser.

## Testing

All tests extend `CommonTestSetup`, which sets up a MockBukkit server, mocks BentoBox and its managers (islands, players, worlds), and tears everything down after each test. Use Mockito and the repository's `world.bentobox.aoneblock.WhiteBox` helper for injecting state into private fields.

Phase YAML files under `src/main/resources/phases/` are loaded at startup; tests that exercise `OneBlocksManager` need those resources on the classpath (they are by default via Maven's test resource path).
