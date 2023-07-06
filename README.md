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

A: There are 12 phases: Plains, Underground, Winter, Ocean, Jungle, Swamp, Dungeon, Desert, The Nether, Plenty, Desolation, Deep Dark, and The End. Each phase features a set of blocks, chests, items, and mobs appropriate for the setting.

Q: How many blocks are there in the phases?

A: There are currently 12 thousand blocks!

Q: What happens after the last phase?

A: The phases repeat.

Q: Why do I keep falling and dying!

A: There are tricks to surviving, but it might be difficult! You need to build space so you don't fall.

Q: I can't catch the blocks when I mine them! How do I do that?

A: You can't catch them all, but it *is* an infinite block!

Q: Why do certain blocks spawn more frequently than others?

A: They just do! It's random. You can set the relative probability in the config files in the phases folder. Admins can also set certain blocks to appear at certain times no matter what. Look out for the sponge for example!

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

0. Install BentoBox and run it on the server at least once to create its data folders.
1. Place this jar in the addons folder of the BentoBox plugin.
2. Restart the server.
3. The addon will create worlds and a data folder and inside the folder will be a config.yml and config files in phases folder.
4. Stop the server.
5. Edit config.yml and the .yml config files how you want.
6. Delete any worlds that were created by default if you made changes that would affect them.
7. Restart the server.

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

### Other Add-ons

OneBlock is an add-on that uses the BentoBox API. Here are some other ones that you may be interested in:

* [**Addons**](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

You can add all the usual addons to OneBlock, like Challeges, Likes, Level, Warps, etc. but it is not required.

Bugs and Feature requests
=========================
File bug and feature requests here: https://github.com/BentoBoxWorld/OneBlock/issues

