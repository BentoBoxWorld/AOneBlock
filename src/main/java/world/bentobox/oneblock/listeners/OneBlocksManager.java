package world.bentobox.oneblock.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.oneblock.OneBlock;

public class OneBlocksManager {

    private static final String ONE_BLOCKS_YML = "oneblocks.yml";
    private final OneBlock addon;
    private TreeMap<Integer, OneBlockPhase> blockProbs;

    /**
     * @param addon - addon
     * @throws InvalidConfigurationException - exception
     * @throws IOException - exception
     * @throws FileNotFoundException - exception
     */
    public OneBlocksManager(OneBlock addon) throws FileNotFoundException, IOException, InvalidConfigurationException {
        this.addon = addon;
        // Save the default oneblocks.yml file
        addon.saveResource(ONE_BLOCKS_YML, false);
        /*
        YamlConfiguration c = new YamlConfiguration();
        c.set("0.chests.1.prob", 5);
        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) newBook.getItemMeta();
        esm.addStoredEnchant(Enchantment.DURABILITY, 3, true);
        newBook.setItemMeta(esm);
        c.set("0.chests.1.contents.3", newBook);
        ItemStack itemStack = new ItemStack(Material.IRON_AXE, 3);
        c.set("0.chests.1.contents.5", itemStack);
        addon.log(c.saveToString());*/
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
            // First block
            if (phase.contains("firstBlock")) {
                addFirstBlock(obPhase, phase.getString("firstBlock"));
            }
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

    private void addFirstBlock(OneBlockPhase obPhase, @Nullable String material) {
        if (material == null) return;
        Material m = Material.matchMaterial(material);
        if (m == null || !m.isBlock()) {
            addon.logError("Bad firstBlock material: " + material);
        } else {
            obPhase.setFirstBlock(new OneBlockObject(m, 0));
        }
    }

    private void addChests(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection("chests")) {
            ConfigurationSection chests = phase.getConfigurationSection("chests");
            for (String chestId: chests.getKeys(false)) {
                ConfigurationSection chest = chests.getConfigurationSection(chestId);
                int prob = chest.getInt("prob", 0);
                if (prob > 0) {
                    Map<Integer, ItemStack> items = new HashMap<>();
                    ConfigurationSection contents = chest.getConfigurationSection("contents");
                    if (contents != null) {
                        for (String index : contents.getKeys(false)) {
                            int slot = Integer.valueOf(index);
                            ItemStack item = contents.getItemStack(index);
                            if (item != null) items.put(slot, item);
                        }
                    }
                    obPhase.addChest(items, prob);
                }
            }
            // Calculate the rare chests
            obPhase.discoverRareChests();
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
