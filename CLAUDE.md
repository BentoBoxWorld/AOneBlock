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

## Dependency Source Lookup

When you need to inspect source code for a dependency (e.g., BentoBox, addons):

1. **Check local Maven repo first**: `~/.m2/repository/` — sources jars are named `*-sources.jar`
2. **Check the workspace**: Look for sibling directories or Git submodules that may contain the dependency as a local project (e.g., `../bentoBox`, `../addon-*`)
3. **Check Maven local cache for already-extracted sources** before downloading anything
4. Only download a jar or fetch from the internet if the above steps yield nothing useful

Prefer reading `.java` source files directly from a local Git clone over decompiling or extracting a jar.

In general, the latest version of BentoBox should be targeted.

## Project Layout

Related projects are checked out as siblings under `~/git/`:

**Core:**
- `bentobox/` — core BentoBox framework

**Game modes:**
- `addon-acidisland/` — AcidIsland game mode
- `addon-bskyblock/` — BSkyBlock game mode
- `Boxed/` — Boxed game mode (expandable box area)
- `CaveBlock/` — CaveBlock game mode
- `OneBlock/` — AOneBlock game mode
- `SkyGrid/` — SkyGrid game mode
- `RaftMode/` — Raft survival game mode
- `StrangerRealms/` — StrangerRealms game mode
- `Brix/` — plot game mode
- `parkour/` — Parkour game mode
- `poseidon/` — Poseidon game mode
- `gg/` — gg game mode

**Addons:**
- `addon-level/` — island level calculation
- `addon-challenges/` — challenges system
- `addon-welcomewarpsigns/` — warp signs
- `addon-limits/` — block/entity limits
- `addon-invSwitcher/` / `invSwitcher/` — inventory switcher
- `addon-biomes/` / `Biomes/` — biomes management
- `Bank/` — island bank
- `Border/` — world border for islands
- `Chat/` — island chat
- `CheckMeOut/` — island submission/voting
- `ControlPanel/` — game mode control panel
- `Converter/` — ASkyBlock to BSkyBlock converter
- `DimensionalTrees/` — dimension-specific trees
- `discordwebhook/` — Discord integration
- `Downloads/` — BentoBox downloads site
- `DragonFights/` — per-island ender dragon fights
- `ExtraMobs/` — additional mob spawning rules
- `FarmersDance/` — twerking crop growth
- `GravityFlux/` — gravity addon
- `Greenhouses-addon/` — greenhouse biomes
- `IslandFly/` — island flight permission
- `IslandRankup/` — island rankup system
- `Likes/` — island likes/dislikes
- `Limits/` — block/entity limits
- `lost-sheep/` — lost sheep adventure
- `MagicCobblestoneGenerator/` — custom cobblestone generator
- `PortalStart/` — portal-based island start
- `pp/` — pp addon
- `Regionerator/` — region management
- `Residence/` — residence addon
- `TopBlock/` — top ten for OneBlock
- `TwerkingForTrees/` — twerking tree growth
- `Upgrades/` — island upgrades (Vault)
- `Visit/` — island visiting
- `weblink/` — web link addon
- `CrowdBound/` — CrowdBound addon

**Data packs:**
- `BoxedDataPack/` — advancement datapack for Boxed

**Documentation & tools:**
- `docs/` — main documentation site
- `docs-chinese/` — Chinese documentation
- `docs-french/` — French documentation
- `BentoBoxWorld.github.io/` — GitHub Pages site
- `website/` — website
- `translation-tool/` — translation tool

Check these for source before any network fetch.

## Key Dependencies (source locations)

- `world.bentobox:bentobox` → `~/git/bentobox/src/`
