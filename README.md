# AOneBlock
A OneBlock Minecraft plugin, written by tastybento. 
Credit for the original idea: IJAminecraft.

[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/AOneBlock)](https://ci.codemc.org/job/BentoBoxWorld/job/AOneBlock/)[
![Bugs](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_AOneBlock&metric=bugs)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_AOneBlock)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_AOneBlock&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_AOneBlock)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_AOneBlock&metric=ncloc)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_AOneBlock)

## About
AOneBlock puts you on a block in space. There is only one block. What do you do next?

## Documentation

See BentoBox Docs [docs.bentobox.world](https://docs.bentobox.world/en/latest/gamemodes/AOneBlock/Permissions/) for documentation.

## Commands

The user command is `/ob`. The admin command is `/oba`. 



## FAQ

Q: What phases are there?

A: There are 18 phases: Plains, Underground, Winter, Ocean, Jungle, Swamp, Dungeon, Desert, The Nether, Plenty, Desolation, Deep Dark, The End, Lush Caves, Dripstone Caves, Mangrove Swamp, Meadow, Cherry Grove, and Jagged Peaks. Each phase features a set of blocks, chests, items, and mobs appropriate for the setting.

Q: How many blocks are there in the phases?

A: There are currently 15 thousand blocks!

Q: What happens after the last phase?

A: The phases repeat.

Q: Why do I keep falling and dying!

A: There are tricks to surviving, but it might be difficult! You need to build space so you don't fall.

Q: I can't catch the blocks when I mine them! How do I do that?

A: You can't catch them all, but it *is* an infinite block!

Q: Why do certain blocks spawn more frequently than others?

A: They just do! It's random(ish). You can set the relative probability in the config files in the phases folder. Admins can also set certain blocks to appear at certain times no matter what. Look out for the sponge for example!

Q. How do I know which is the magic block?

A. Hit it and it will give out green particles. It's also at the center of your island.

Q. My magic block is no longer there! How do I get another one?

A. You will have to place a block there. Worse case, kill yourself and one will be generated.

Q. My magic block is liquid! How can I mine it?

A. Use a bucket.

Q: Which mobs can spawn?

A: Each phase has a different set of mobs that can spawn. Be careful because they may push you off! If you listen carefully, you may hear hostile mobs coming.

Q. I have no chance to react to hostile mobs spawning!

A. Be prepared. Listen carefully when you mine a block and you will hear hostile mobs coming before they spawn. If you are in a hostile phase, then expect mobs and build defenses to protect yourself. You can mine a block from quite far away.

Q. When mobs spawn, my defenses are destroyed! Why?

A. Mobs make space to spawn. If there's anything in the way, it'll be broken and dropped. You'll have to build accordingly. This is to prevent suffocation exploits.

Q: Do chests spawn?

A: Yes. Chests spawn with random items in them from the current phase. There are common, uncommon, rare and epic chests. Chests with sparkles are good.

Q: Is it possible to reach the Nether or End in this map?

A: The vanilla Nether exists by default but there is no End world, just an End Phase.

Q: What is the end goal?

A: It's whatever you want it to be! 

## Installation

1. Install BentoBox and run it on the server at least once to create its data folders.
2. Place this jar in the addons folder of the BentoBox plugin.
3. Restart the server.
4. The addon will create worlds and a data folder and inside the folder will be a config.yml and config files in phases folder.
5. Stop the server.
6. Edit config.yml and the .yml config files how you want.
7. Delete any worlds that were created by default if you made changes that would affect them.
8. Restart the server.

## Phase config files

The config files to make the phases are in the phases folder.

There are two files per phase - a file that contains the blocks and mobs, and a file that contains the chests.

The first number of any file is how many blocks need to be mined to reach that phase. This is the phase's key number.
Each phase also has a name, an icon, a biome and the following sections:

- name
- icon
- fixedBlocks
- holograms
- biome
- start-commands
- end-commands
- end-commands-first-time
- requirements
- blocks
- mobs

In the chests file, it just has the phase number and a chests section.

### name
Name of the phase

### icon
The material for an icon to show

### fixedBlocks
List of blocks that will generate at these specific block counts. The numbers are relative to the phase and not the overall player's count.
If you define 0 here, then firstBlock is not required and firstBlock will be replaced with this block.

### holograms

Hologram Lines to Display. The key number is the block of the phase to show the hologram. Chat color codes can be used. A hologram plugin is not required for these holograms.
The First (Before Phase 1) Hologram is Located in your Locale.

### Biome
The biomes for this phase.

### Commands
A list of commands can be run at the start and end of a phase. Commands are run as the Console
unless the command is prefixed with [SUDO], then the command is run as the player
triggering the commands.

These placeholders in the command string will be replaced with the appropriate value:
* [island] - Island name
* [owner] - Island owner's name
* [player] - The name of the player who broke the block triggering the commands
* [phase] - the name of this phase
* [blocks] - the number of blocks broken
* [level] - your island level (Requires Levels Addon)
* [bank-balance] - your island bank balance (Requires Bank Addon)
* [eco-balance] - player's economy balance (Requires Vault and an economy plugin)

Examples:
```
  start-commands:
  - 'give [player] WOODEN_AXE 1'
  - 'broadcast [player] just started OneBlock!'
  end-commands:
  - '[SUDO]summon minecraft:wither'
  These are run only the first time a phase is completed
  end-commands-first-time:
  - 'broadcast &c&l[!] &b[player] &fhas completed the &d&n[phase]&f phase for the first time.'
```
### Requirements

You can stipulate a set of requirements to start the phase:
  * economy-balance - the minimum player's economy balance (Requires Vault and an economy plugin)
  * bank-balance - the minimum island bank balance (requires Bank Addon)
  * level - the island level (Requires Levels Addon)
  * permission - a permission string
  Example:
  ```
   requirements:
     bank-balance: 10000
     level: 10
     permission: ready.for.battle
  ```
  
### blocks

The blocks section list Bukkit Materials followed by a relative probability. All the probability values are added up for the whole phase and the chance of the block being placed is the relative probability divided by the total of all the probabilities.

### mobs

The mob section list mobs that can spawn and their relative probability along with blocks. You can only list entities that are alive and can spawn in this list.

### chests

If CHEST is listed in the blocks section, then it will be randomly filled according to this section. You can define as many chests as you like. The first number is a unique chest number. Then follows the chest contents that includes the slot number and the item stack contents. Finally there is the chest's rarity, which can be COMMON, UNCOMMON, RARE or EPIC. The best way to set chests is to do it in game. Fill a chest with the contents you want and then while looking at it enter the command `/oba setchest <phase> <rarity>` where <phase> is the name of the phase and rarity is the rarity. Use Tab Complete to see the options. The chest will be automatically added to the oneblocks.yml file and be ready to use. Deleting chests must be done by editing the oneblocks.yml file for now and reloading the addon.

Be very careful when editing the chest items and check that the material is a true Bukkit material and spelled correctly.

### Custom block entries

Phase `blocks:` sections can also be written as a YAML list, which unlocks
custom entry types:

```yaml
blocks:
  - type: block-data
    data: redstone_wire[power=15]
    probability: 20

  # `type: block` runs the vanilla /setblock command at the magic-block
  # position. The data string may include block states `[…]`, NBT `{…}`,
  # and a trailing destroy|keep|replace mode — anything valid after
  # `setblock <x> <y> <z>`. Use single quotes around the data so any
  # double quotes inside the NBT don't clash with YAML string delimiters.
  - type: block
    data: 'spawner{Delay:0,MinSpawnDelay:200,MaxSpawnDelay:800,SpawnCount:1,SpawnRange:4,MaxNearbyEntities:6,RequiredPlayerRange:16,SpawnData:{entity:{id:breeze,CustomName:[{text:"Breezy Generator",color:"#f90606"}],CustomNameVisible:1b,Glowing:1b,active_effects:[{id:unluck,duration:200,ambient:1b,show_particles:1b}],attributes:[{id:scale,base:2f}]}}} replace'
    probability: 10

  # #488: summon an entity with vanilla NBT/component data, same syntax as /summon.
  # After spawning, blocks inside the mob's (scaled) bounding box are cleared.
  - type: mob-data
    data: breeze{CustomName:[{text:Breezy,color:"#f90606"}],CustomNameVisible:1b,Glowing:1b,attributes:[{id:scale,base:2f}]}
    underlying-block: STONE
    probability: 15

  # #303: spawn a MythicMob via BentoBox's MythicMobs hook. Requires the MythicMobs
  # plugin to be installed; otherwise the entry is logged and skipped at runtime.
  - type: mythic-mob
    mob: SkeletalKnight        # MythicMob internal type ID (required)
    level: 3                   # mob level (optional, default: 1)
    power: 1.0                 # mob power multiplier (optional, default: 0)
    display-name: "Boss"       # override display name (optional)
    stance: ""                 # MythicMobs stance string (optional)
    underlying-block: STONE    # block placed under the mob (optional, default: STONE)
    probability: 5
```

`type: block` is an alias for `type: block-data` — both route to the same
handler. Use `block-data` when you only need simple block states
(`redstone_wire[power=15]`); use `block` when the data contains NBT or a
setblock mode flag so the intent is obvious at a glance.

> **Spawner gotcha:** placing a `spawner` via `/setblock` only sets the fields
> you provide — everything else defaults to `0`/`-1`, which leaves the spawner
> **inactive** (`Delay:-1` means "never tick"). If you want it to actually spawn
> mobs, set `Delay`, `MinSpawnDelay`, `MaxSpawnDelay`, `SpawnCount`, `SpawnRange`,
> `MaxNearbyEntities`, and `RequiredPlayerRange` explicitly, as shown in the
> example above. `Delay:0` spawns on the very next tick (visually instant),
> `Delay:N` waits N ticks before the first spawn, and `Delay:-1` never ticks.
> Test your full data string in-game with `/setblock ~ ~ ~ <data>` first — if
> the spawner ticks there, it will tick from `custom-blocks:` too.

> **Note:** the `mob-data` string is passed straight to the vanilla `/summon`
> command, so it must be valid NBT for your server version. A few 1.21 gotchas:
> attribute ids no longer use the `generic.`/`player.` prefix (`scale`, not
> `generic.scale`), numeric attribute bases need a float suffix (`base:2f`),
> and `CustomName` must be a text-component list (`[{text:Breezy}]`), not a
> plain string. Test the command in-game first with `/summon <your data>` —
> if it works there it will work here. Bad NBT is logged and the spawn is
> skipped.

#### MythicMobs configuration

To use `type: mythic-mob` you need:
1. [MythicMobs](https://www.spigotmc.org/resources/mythicmobs.5702/) (free or premium) installed on the server.
2. BentoBox's built-in MythicMobs hook active (it registers automatically when MythicMobs is detected).

**Fields:**

| Field | Required | Default | Description |
|---|---|---|---|
| `mob` | ✅ | — | The internal MythicMobs mob type ID as defined in your MythicMobs config (case-sensitive). |
| `level` | ❌ | `1` | The level at which to spawn the mob. |
| `power` | ❌ | `0` | The power multiplier for the mob. |
| `display-name` | ❌ | mob ID | Override the mob's display name. |
| `stance` | ❌ | `""` | An optional MythicMobs stance string. |
| `underlying-block` | ❌ | `STONE` | The vanilla block placed at the magic-block position before the mob spawns. |
| `probability` | ❌ | — | The relative spawn weight in the phase pool. |

**Alternative approach using commands:**

If you experience any issues with MythicMobs skills or abilities when spawning
via `type: mythic-mob`, you can use the MythicMobs `/mm mobs spawn` command
inside the phase's `start-commands` or `end-commands` instead. This spawns the
mob through MythicMobs directly, which ensures all skills and behaviours work
exactly as configured:

```yaml
start-commands:
  # Spawn mob at the island's magic-block position using MythicMobs command.
  # Replace <world>, <x>, <y>, <z> with the actual coordinates, or use a
  # console command that targets the player's location.
  - 'mm mobs spawn MY_MYTHIC_MOB 1 [world],[x],[y],[z]'
```

> **Tip:** When spawning MythicMobs via commands you have full control over
> the exact spawn location and can still use all MythicMobs features without
> any compatibility limitations.

#### ItemsAdder custom blocks

Requires the [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/)
plugin to be installed. ItemsAdder blocks can be referenced in two ways:

**Map form** (inside a `blocks:` or `custom-blocks:` list):
```yaml
blocks:
  - type: itemsadder
    id: namespace:block_id   # ItemsAdder block ID (required)
    probability: 20
```

**Short form** — place the ItemsAdder block ID directly as a map key (same as
vanilla materials):
```yaml
blocks:
  namespace:block_id: 20
```

#### Nexo custom blocks

Requires the [Nexo](https://www.spigotmc.org/resources/nexo.112709/) plugin to
be installed. Nexo blocks can be referenced in two ways:

**Map form** (inside a `blocks:` or `custom-blocks:` list):
```yaml
blocks:
  - type: nexo
    id: nexo_block_id   # Nexo block ID (required)
    probability: 20
```

**Short form** — place the Nexo block ID directly as a map key:
```yaml
blocks:
  nexo_block_id: 20
```

If you'd rather leave your existing `blocks:` map-form section untouched, you
can put custom entries in a sibling `custom-blocks:` list. Both sections are
read and their entries merged into the same weighted pool, so probabilities in
the two sections are directly comparable:

```yaml
blocks:
  PODZOL: 40
  DIRT: 1000
  OAK_LOG: 2000

custom-blocks:
  - type: mob-data
    data: breeze{CustomName:[{text:Breezy,color:"#f90606"}],CustomNameVisible:1b,Glowing:1b,attributes:[{id:scale,base:2f}]}
    underlying-block: STONE
    probability: 50

  - type: block
    data: 'spawner{Delay:0,MinSpawnDelay:200,MaxSpawnDelay:800,SpawnCount:1,SpawnRange:4,MaxNearbyEntities:6,RequiredPlayerRange:16,SpawnData:{entity:{id:breeze,CustomName:[{text:"Breezy Generator",color:"#f90606"}],CustomNameVisible:1b,Glowing:1b,active_effects:[{id:unluck,duration:200,ambient:1b,show_particles:1b}],attributes:[{id:scale,base:2f}]}}} replace'
    probability: 10

  - type: mythic-mob
    mob: SkeletalKnight
    level: 3
    probability: 5
```

### Other Add-ons

OneBlock is an add-on that uses the BentoBox API. Here are some other ones that you may be interested in:

* [**Addons**](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

You can add all the usual addons to OneBlock, like Challeges, Likes, Level, Warps, etc. but it is not required.

Bugs and Feature requests
=========================
File bug and feature requests here: https://github.com/BentoBoxWorld/OneBlock/issues

