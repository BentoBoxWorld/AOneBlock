# OneBlock
A variation on the OneBlock Minecraft map, written by tastybento. Credit: The OneBock concept originally by IJAminecraft.

[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/OneBlock)](https://ci.codemc.org/job/BentoBoxWorld/job/OneBlock/)

## About
OneBlock puts you on a block in space. There is only one block. What do you do next?

## Development Status

tastybento is tuning the game right now.

## Commands

The user command is `/ob`. The admin command is `/oba`. 



## FAQ

Q: What phases are there?

A: Like the original, there are 10 phases: Plains, Underground, Snow Biome, Ocean, Jungle Dungeon, Desert, The Nether, Idyll, Desolation, and The End. Each phase features a set of blocks, items, and mobs appropriate for the setting.

Q: How many blocks are there in the 10 phases?

A: TBD - now tuning.

Q: What happens after the last phase?

A: Right now, the 10th phase goes on forever. It's still in development.

Q: Why do I keep falling and dying!

A: There are tricks to surviving, but it might be difficult!

Q: I can't catch the blocks when I mine them! How do I do that?

A: Yep. It's tough. You can't catch them all, but it *is* an infinite block!

Q: Why do certain blocks spawn more frequently than others?

A: They just do! You can set the relative probability in the file `oneblock.yml`.

Q: Which mobs can spawn?

A: Each phase has a different set of mobs that can spawn. Be careful because they may push you off!

Q: Do chests spawn?

A: Yes. Chests spawn with random items in them from the current phase. 

Q: Is it possible to reach the Nether in this map?

A: Yes, it is. Nether blocks and obsidian can be obtained in the Nether phase. Lava buckets may be available earlier in certain chests, but you'd need to be insanely lucky to get enough to create a nether portal before reaching phase 7.

Q. If I get to the Nether, what's there?

A. You'll have to find out!

Q. Is it possible to reach the End on this map?

A. Phew, you really ask a lot of questions! Err, yes, but I haven't worked out how to make that happen yet!

Q: What is the end goal?

A: It's whatever you want it to be! 

## Installation

0. Install BentoBox and run it on the server at least once to create its data folders.
1. Place this jar in the addons folder of the BentoBox plugin.
2. Restart the server.
3. The addon will create worlds and a data folder and inside the folder will be a config.yml and oneblock.yml files.
4. Stop the server.
5. Edit config.yml and oneblock.yml files how you want.
6. Delete any worlds that were created by default if you made changes that would affect them.
7. Restart the server.

## oneblock.yml

The first number is how many blocks need to be mined to reach that phase.
Each phase has a name, a biome and the following sections:

- blocks
- mobs
- chests

### blocks

The blocks section list Bukkit Materials followed by a relative probability. All the probability values are added up for the whole phase and the chance of the block being placed is the relative probability divided by the total of all the probabilities.

### mobs

The mob section list mobs that can spawn and their relative probability. You can only list entities that are alive and can spawn in this list.

### chests

If a chest is listed in the blocks section, then it will be randomly filled according to this section. You can define as many chests as you like. The first number is the relative probability (within chests) of a chest being used. Inside each chest section, list the item's material and the number of items to place.

### Other Add-ons

OneBlock is an add-on that uses the BentoBox API. Here are some other ones that you may be interested in:

* [**Addons**](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

You can add all the usual addons to OneBlock, like Challeges, Likes, Level, Warps, etc.

Bugs and Feature requests
=========================
File bug and feature requests here: https://github.com/BentoBoxWorld/OneBlock/issues

