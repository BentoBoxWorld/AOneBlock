package world.bentobox.oneblock.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.io.Files;

import world.bentobox.oneblock.OneBlock;
import world.bentobox.oneblock.listeners.OneBlockObject.Rarity;

public class OneBlocksManager {

    private static final String ONE_BLOCKS_YML = "oneblocks.yml";
    private static final String NAME = "name";
    private static final String BIOME = "biome";
    private static final String FIRST_BLOCK = "firstBlock";
    private static final String CHESTS = "chests";
    private static final String RARITY = "rarity";
    private static final String CONTENTS = "contents";
    private static final String MOBS = "mobs";
    private static final String BLOCKS = "blocks";
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
        loadBlocks();
    }

    private void loadBlocks() throws IOException, InvalidConfigurationException, NumberFormatException {
        // Clear
        blockProbs = new TreeMap<>();
        // Load the config file
        YamlConfiguration oneBlocks = new YamlConfiguration();
        oneBlocks.load(addon.getDataFolder() + File.separator + ONE_BLOCKS_YML);
        for (String blockNumber : oneBlocks.getKeys(false)) {
            OneBlockPhase obPhase = new OneBlockPhase(blockNumber);
            // Get config Section
            ConfigurationSection phase = oneBlocks.getConfigurationSection(blockNumber);
            // goto
            if (phase.contains("gotoBlock")) {
                obPhase.setGotoBlock(phase.getInt("gotoBlock", 0));
            }
            // name
            obPhase.setPhaseName(phase.getString(NAME, blockNumber));
            // biome
            obPhase.setPhaseBiome(Biome.valueOf(phase.getString(BIOME, "PLAINS").toUpperCase()));
            // First block
            if (phase.contains(FIRST_BLOCK)) {
                addFirstBlock(obPhase, phase.getString(FIRST_BLOCK));
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
        if (phase.isConfigurationSection(CHESTS)) {
            ConfigurationSection chests = phase.getConfigurationSection(CHESTS);
            for (String chestId: chests.getKeys(false)) {
                ConfigurationSection chest = chests.getConfigurationSection(chestId);
                Rarity rarity = OneBlockObject.Rarity.valueOf(chest.getString(RARITY, "COMMON").toUpperCase());
                Map<Integer, ItemStack> items = new HashMap<>();
                ConfigurationSection contents = chest.getConfigurationSection(CONTENTS);
                if (contents != null) {
                    for (String index : contents.getKeys(false)) {
                        int slot = Integer.valueOf(index);
                        ItemStack item = contents.getItemStack(index);
                        if (item != null) items.put(slot, item);
                    }
                }
                obPhase.addChest(items, rarity);
            }
        }

    }

    private void addMobs(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection(MOBS)) {
            ConfigurationSection mobs = phase.getConfigurationSection(MOBS);
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
        if (phase.isConfigurationSection(BLOCKS)) {
            ConfigurationSection blocks = phase.getConfigurationSection(BLOCKS);
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

    /**
     * @return list of phase names with spaces replaced by underscore so they are one word
     */
    public List<String> getPhaseList() {
        return blockProbs.values().stream().map(p -> p.getPhaseName().replace(" ", "_")).collect(Collectors.toList());
    }

    /**
     * Get phase by name. Name should have any spaces converted to underscores. Case insensitive.
     * @param name - name to search
     * @return optional OneBlockPhase
     */
    public Optional<OneBlockPhase> getPhase(String name) {
        return blockProbs.values().stream().filter(p -> p.getPhaseName().replace(" ", "_").equalsIgnoreCase(name)).findFirst();
    }

    public boolean saveOneBlockConfig() {
        // Make the config file
        YamlConfiguration oneBlocks = new YamlConfiguration();
        blockProbs.values().forEach(p -> {
            ConfigurationSection phSec = oneBlocks.createSection(p.getBlockNumber());
            phSec.set(NAME, p.getPhaseName());
            if (p.getFirstBlock() != null) {
                phSec.set(FIRST_BLOCK, p.getFirstBlock().getMaterial().name());
            }
            phSec.set(BIOME, p.getPhaseBiome().name());
            saveBlocks(phSec, p);
            saveEntities(phSec, p);
            saveChests(phSec, p);
        });
        try {
            // Make backup
            File config = new File(addon.getDataFolder() + File.separator + ONE_BLOCKS_YML);
            File configBak = new File(addon.getDataFolder() + File.separator + ONE_BLOCKS_YML + ".bak");
            Files.copy(config, configBak);
            oneBlocks.save(config);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void saveChests(ConfigurationSection phSec, OneBlockPhase phase) {
        ConfigurationSection chests = phSec.createSection(CHESTS);
        int index = 1;
        for (OneBlockObject chest:phase.getChests()) {
            ConfigurationSection c = chests.createSection(String.valueOf(index++));
            c.set(CONTENTS, chest.getChest());
            c.set(RARITY, chest.getRarity().name());
        }

    }

    private void saveEntities(ConfigurationSection phSec, OneBlockPhase phase) {
        ConfigurationSection mobs = phSec.createSection(MOBS);
        phase.getMobs().forEach((k,v) -> mobs.set(k.name(), v));
    }

    private void saveBlocks(ConfigurationSection phSec, OneBlockPhase phase) {
        ConfigurationSection blocks = phSec.createSection(BLOCKS);
        phase.getBlocks().forEach((k,v) -> blocks.set(k.name(), v));

    }
}
