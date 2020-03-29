package world.bentobox.oneblock.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.oneblock.OneBlock;

class OneBlocks {

    private static final String ONE_BLOCKS_YML = "oneblocks.yml";
    private final OneBlock addon;
    private TreeMap<Integer, OneBlockPhase> blockProbs;

    /**
     * @param addon - addon
     * @throws InvalidConfigurationException - exception
     * @throws IOException - exception
     * @throws FileNotFoundException - exception
     */
    public OneBlocks(OneBlock addon) throws FileNotFoundException, IOException, InvalidConfigurationException {
        this.addon = addon;
        // Save the default oneblocks.yml file
        addon.saveResource(ONE_BLOCKS_YML, false);
        loadBlocks();
    }

    private void loadBlocks() throws IOException, InvalidConfigurationException, NumberFormatException {
        // Clear
        blockProbs = new TreeMap<>();
        // Load the config file
        YamlConfiguration oneBlocks = new YamlConfiguration();
        oneBlocks.load(addon.getDataFolder() + File.separator + ONE_BLOCKS_YML);
        for (String blockNumber : oneBlocks.getKeys(false)) {
            OneBlockPhase obPhase = new OneBlockPhase();
            // Get config Section
            ConfigurationSection phase = oneBlocks.getConfigurationSection(blockNumber);
            // name
            obPhase.setPhaseName(phase.getString("name", blockNumber));
            // biome
            obPhase.setPhaseBiome(Biome.valueOf(phase.getString("biome", "PLAINS").toUpperCase()));
            // Blocks
            addBlocks(obPhase, phase);
            // Mobs
            addMobs(obPhase, phase);
            // Chests
            addChests(obPhase, phase);
            // Add to the map
            Integer blockNum = Integer.valueOf(blockNumber);
            blockProbs.put(blockNum, obPhase);
        }
    }

    private void addChests(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection("chests")) {
            ConfigurationSection chests = phase.getConfigurationSection("chests");
            for (String chestProb : chests.getKeys(false)) {
                int prob = Integer.parseInt(chestProb);
                // Get the itemstacks
                List<ItemStack> chestContents = new ArrayList<>();
                ConfigurationSection chest = chests.getConfigurationSection(chestProb);
                for (String material : chest.getKeys(false)) {
                    Material m = Material.matchMaterial(material);
                    if (m == null) {
                        addon.logError("Bad chest item material in " + ONE_BLOCKS_YML + ": " + material);
                    } else {
                        ItemStack item = new ItemStack(m, chest.getInt(material, 1));
                        chestContents.add(item);
                    }
                }
                obPhase.addChest(chestContents, prob);
            }
        }

    }

    private void addMobs(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection("mobs")) {
            ConfigurationSection mobs = phase.getConfigurationSection("mobs");
            for (String entity : mobs.getKeys(false)) {
                try {
                    EntityType et = EntityType.valueOf(entity.toUpperCase());
                    if (et.isSpawnable() && et.isAlive()) {
                        obPhase.addMob(et, mobs.getInt(entity));
                    } else {
                        throw new IOException("Entity type is not alive or spawnable");
                    }
                } catch (Exception e) {
                    addon.logError("Bad entity type in " + ONE_BLOCKS_YML + ": " + entity);
                    addon.logError(e.getMessage());
                }
            }
        }


    }

    private void addBlocks(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection("blocks")) {
            ConfigurationSection blocks = phase.getConfigurationSection("blocks");
            for (String material : blocks.getKeys(false)) {
                Material m = Material.matchMaterial(material);
                if (m == null || !m.isBlock()) {
                    addon.logError("Bad block material in " + ONE_BLOCKS_YML + ": " + material);
                } else {
                    obPhase.addBlock(m, blocks.getInt(material));
                }

            }
        }

    }

    /**
     * Return the current phase for the block count
     * @param blockCount - number of blocks mined
     * @return the one block phase currently in action
     */
    public OneBlockPhase getPhase(int blockCount) {
        return blockProbs.floorEntry(blockCount).getValue();
    }

}
