# AOneBlock
A OneBlock Minecraft plugin, written by tastybento. 
Credit for the original idea: IJAminecraft.

[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/OneBlock)](https://ci.codemc.org/job/BentoBoxWorld/job/OneBlock/)

## About
AOneBlock puts you on a block in space. There is only one block. What do you do next?

## Development Status

The game is in Alpha stage.

## Commands

The user command is `/ob`. The admin command is `/oba`. 



## FAQ

Q: What phases are there?

A: There are 11 phases: Plains, Underground, Winter, Ocean, Jungle, Swamp, Dungeon, Desert, The Nether, Plenty, Desolation, and The End. Each phase features a set of blocks, items, and mobs appropriate for the setting.

Q: How many blocks are there in the 11 phases?

A: There are currently 11 thousand blocks!

Q: What happens after the last phase?

A: The phases repeat.

Q: Why do I keep falling and dying!

A: There are tricks to surviving, but it might be difficult! You need to build defenses.

Q: I can't catch the blocks when I mine them! How do I do that?

A: Yep. It's tough. You can't catch them all, but it *is* an infinite block!

Q. How can I mine a block if I have no tools?

A: The magic block allows you to mine anything with your hands (it *is* magic!) but it may take a long time.

Q: Why do certain blocks spawn more frequently than others?

A: They just do! You can set the relative probability in the file `oneblock.yml`.

Q. How do I know which is the magic block?

A. Hit it and it will give out green particles.

Q. My magic block is no longer there! How do I get another one?

A. You will have to place a block there. Worse case, kill yourself and one will be generated.

Q. My magic block is liquid! How can I mine it?

A. Use a bucket.

Q: Which mobs can spawn?

A: Each phase has a different set of mobs that can spawn. Be careful because they may push you off! If you listen carefully, you may hear hostile mobs coming.

Q. I have no chance to react to hostile mobs spawning!

A. Be prepared. Listen carefully when you mine a block and you will hear hostile mobs coming before they spawn. If you are in a hostile phase, then expect mobs and build defences to protect yourself. You can mine a block from quite far away.

Q. When mobs spawn, my defences are destroyed! Why?

A. Mobs must have space to spawn. If there's anything in the way, it'll be broken and dropped. You'll have to build accordingly.

Q: Do chests spawn?

A: Yes. Chests spawn with random items in them from the current phase. There are common, uncommon, rare and epic chests. Chests with sparkles are good.

Q: Is it possible to reach the Nether or End in this map?

A: No. Those worlds are not included yet, just the phases.

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

The mob section list mobs that can spawn and their relative probability along with blocks. You can only list entities that are alive and can spawn in this list.

### chests

If CHEST is listed in the blocks section, then it will be randomly filled according to this section. You can define as many chests as you like. The first number is a unique chest number. Then follows the chest contents that includes the slot number and the item stack contents. Finally there is the chest's rarity, which can be COMMON, UNCOMMON, RARE or EPIC. The best way to set chests is to do it in game. Fill a chest with the contents you want and then while looking at it enter the command `/oba setchest <phase> <rarity>` where <phase> is the name of the phase and rarity is the rarity. Use Tab Complete to see the options. The chest will be automatically added to the oneblocks.yml file and be ready to use. Deleting chests must be done by editing the oneblocks.yml file for now and reloading the addon.

### Other Add-ons

OneBlock is an add-on that uses the BentoBox API. Here are some other ones that you may be interested in:

* [**Addons**](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

You can add all the usual addons to OneBlock, like Challeges, Likes, Level, Warps, etc. but it is not required.

Bugs and Feature requests
=========================
File bug and feature requests here: https://github.com/BentoBoxWorld/OneBlock/issues

