package world.bentobox.aoneblock.oneblocks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Enums;
import com.google.common.io.Files;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockObject.Rarity;
import world.bentobox.bentobox.util.Util;

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
    private static final String PHASES = "phases";
    private final AOneBlock addon;
    private TreeMap<Integer, OneBlockPhase> blockProbs;

    /**
     * @param addon - addon
     * @throws InvalidConfigurationException - exception
     * @throws IOException - exception
     * @throws FileNotFoundException - exception
     */
    public OneBlocksManager(AOneBlock addon) {
        this.addon = addon;
        // Initialize block probabilities
        blockProbs = new TreeMap<>();
    }

    /**
     * Loads the game phases
     */
    public void loadPhases() throws IOException, InvalidConfigurationException, NumberFormatException {
        // Clear block probabilities
        blockProbs = new TreeMap<>();
        // Check for folder
        File check = new File(addon.getDataFolder(), PHASES);
        if (check.mkdirs()) {
            addon.log(check.getAbsolutePath() + " does not exist, made folder.");
            // Check for oneblock.yml
            File oneblockFile = new File(addon.getDataFolder(), ONE_BLOCKS_YML);
            if (oneblockFile.exists()) {
                // Migrate to new folders
                File renamedFile = new File(check, ONE_BLOCKS_YML);
                Files.move(oneblockFile, renamedFile);
                loadPhase(renamedFile);
                this.saveOneBlockConfig();
                oneblockFile.delete();
                renamedFile.delete();
                blockProbs.clear();
            } else {
                // Copy files from JAR
                copyPhasesFromAddonJar(check);
            }
        }
        // Get files in folder
        // Filter for files ending with .yml
        FilenameFilter ymlFilter = (dir, name) -> name.toLowerCase(java.util.Locale.ENGLISH).endsWith(".yml");
        for (File phaseFile : Objects.requireNonNull(check.listFiles(ymlFilter))) {
            loadPhase(phaseFile);
        }
    }

    /**
     * Copies phase files from the addon jar to the file system
     * @param check
     * @param addon - addon
     */
    void copyPhasesFromAddonJar(File check) {
        try (JarFile jar = new JarFile(addon.getFile())) {
            // Obtain any locale files, save them and update
            Util.listJarFiles(jar, PHASES, ".yml").forEach(lf -> addon.saveResource(lf, check, false, true));
        } catch (Exception e) {
            addon.logError(e.getMessage());
        }
    }

    private void loadPhase(File phaseFile) throws IOException {
        addon.log("Loading " + phaseFile.getName());
        // Load the config file
        YamlConfiguration oneBlocks = new YamlConfiguration();
        try {
            oneBlocks.load(phaseFile);
        } catch (Exception e) {
            addon.logError(e.getMessage());
            return;
        }
        for (String blockNumber : oneBlocks.getKeys(false)) {
            Integer blockNum = Integer.valueOf(blockNumber);
            OneBlockPhase obPhase = blockProbs.computeIfAbsent(blockNum, k -> new OneBlockPhase(blockNumber));
            // Get config Section
            ConfigurationSection phase = oneBlocks.getConfigurationSection(blockNumber);
            // goto
            if (phase.contains("gotoBlock")) {
                obPhase.setGotoBlock(phase.getInt("gotoBlock", 0));
                continue;
            }
            initBlock(blockNumber, obPhase, phase);
            // Blocks
            addBlocks(obPhase, phase);
            // Mobs
            addMobs(obPhase, phase);
            // Chests
            addChests(obPhase, phase);
            // Add to the map
            blockProbs.put(blockNum, obPhase);
        }

    }

    void initBlock(String blockNumber, OneBlockPhase obPhase, ConfigurationSection phase) throws IOException {
        if (phase.contains(NAME, true)) {
            if (obPhase.getPhaseName() != null) {
                throw new IOException("Block " + blockNumber + ": Phase name trying to be set to " + phase.getString(NAME) + " but already set to " + obPhase.getPhaseName() + ". Duplicate phase file?");
            }
            // name
            obPhase.setPhaseName(phase.getString(NAME, blockNumber));
        }
        // biome
        if (phase.contains(BIOME, true)) {
            if (obPhase.getPhaseBiome() != null) {
                throw new IOException("Block " + blockNumber + ": Biome trying to be set to " + phase.getString(BIOME) + " but already set to " + obPhase.getPhaseBiome() + " Duplicate phase file?");
            }
            obPhase.setPhaseBiome(getBiome(phase.getString(BIOME)));
        }
        // First block
        if (phase.contains(FIRST_BLOCK)) {
            if (obPhase.getFirstBlock() != null) {
                throw new IOException("Block " + blockNumber + ": First block trying to be set to " + phase.getString(FIRST_BLOCK) + " but already set to " + obPhase.getFirstBlock() + " Duplicate phase file?");
            }
            addFirstBlock(obPhase, phase.getString(FIRST_BLOCK));
        }
    }

    private Biome getBiome(String string) {
        if (string == null) return Biome.PLAINS;
        if (Enums.getIfPresent(Biome.class, string).isPresent()) {
            return Biome.valueOf(string);
        }
        // Special case for nether
        if (string.equals("NETHER") || string.equals("NETHER_WASTES")) {
            return Enums.getIfPresent(Biome.class, "NETHER").or(Enums.getIfPresent(Biome.class, "NETHER_WASTES").or(Biome.PLAINS));
        }
        addon.logError("Biome " + string.toUpperCase() + " is invalid! Use one of these...");
        addon.logError(Arrays.stream(Biome.values()).map(Biome::name).collect(Collectors.joining(",")));
        return Biome.PLAINS;
    }

    void addFirstBlock(OneBlockPhase obPhase, @Nullable String material) {
        if (material == null) return;
        Material m = Material.matchMaterial(material);
        if (m == null || !m.isBlock()) {
            addon.logError("Bad firstBlock material: " + material);
        } else {
            obPhase.setFirstBlock(new OneBlockObject(m, 0));
        }
    }

    void addChests(OneBlockPhase obPhase, ConfigurationSection phase) throws IOException {
        if (phase.isConfigurationSection(CHESTS)) {
            if (!obPhase.getChests().isEmpty()) {
                throw new IOException(obPhase.getPhaseName() + ": Chests cannot be set more than once. Duplicate file?");
            }
            ConfigurationSection chests = phase.getConfigurationSection(CHESTS);
            for (String chestId: chests.getKeys(false)) {
                ConfigurationSection chest = chests.getConfigurationSection(chestId);
                Rarity rarity = Rarity.COMMON;
                try {
                    rarity = OneBlockObject.Rarity.valueOf(chest.getString(RARITY, "COMMON").toUpperCase());
                } catch (Exception e) {
                    addon.logError("Rarity value of " + chest.getString(RARITY, "UNKNOWN") + " is invalid! Use one of these...");
                    addon.logError(Arrays.stream(Rarity.values()).map(Rarity::name).collect(Collectors.joining(",")));
                    rarity = Rarity.COMMON;
                }
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

    void addMobs(OneBlockPhase obPhase, ConfigurationSection phase) throws IOException {
        if (phase.isConfigurationSection(MOBS)) {
            if (!obPhase.getMobs().isEmpty()) {
                throw new IOException(obPhase.getPhaseName() + ": Mobs cannot be set more than once. Duplicate file?");
            }
            ConfigurationSection mobs = phase.getConfigurationSection(MOBS);
            for (String entity : mobs.getKeys(false)) {
                String name = entity.toUpperCase(Locale.ENGLISH);
                EntityType et = null;
                // Pig zombie handling
                if (name.equals("PIG_ZOMBIE") || name.equals("ZOMBIFIED_PIGLIN")) {
                    et = Enums.getIfPresent(EntityType.class, "ZOMBIFIED_PIGLIN")
                            .or(Enums.getIfPresent(EntityType.class, "PIG_ZOMBIE").or(EntityType.PIG));
                } else {
                    et = Enums.getIfPresent(EntityType.class, name).orNull();
                }
                if (et == null) {
                    // Does not exist
                    addon.logError("Bad entity type in " + obPhase.getPhaseName() + ": " + entity);
                    addon.logError("Try one of these...");
                    addon.logError(Arrays.stream(EntityType.values())
                            .filter(EntityType::isSpawnable)
                            .filter(EntityType::isAlive)
                            .map(EntityType::name).collect(Collectors.joining(",")));
                    return;
                }
                if (et.isSpawnable() && et.isAlive()) {
                    obPhase.addMob(et, mobs.getInt(entity));
                } else {
                    addon.logError("Entity type is not spawnable " + obPhase.getPhaseName() + ": " + entity);
                }
            }

        }
    }

    void addBlocks(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection(BLOCKS)) {
            ConfigurationSection blocks = phase.getConfigurationSection(BLOCKS);
            for (String material : blocks.getKeys(false)) {
                Material m = Material.matchMaterial(material);
                if (m == null || !m.isBlock()) {
                    addon.logError("Bad block material in " + obPhase.getPhaseName() + ": " + material);
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
        return blockProbs.values()
                .stream()
                .map(p ->
                p.getPhaseName())
                .filter(n -> n != null)
                .map(n ->
                n.replace(" ", "_"))
                .collect(Collectors.toList());
    }

    /**
     * @return the blockProbs
     */
    public TreeMap<Integer, OneBlockPhase> getBlockProbs() {
        return blockProbs;
    }

    /**
     * Get phase by name. Name should have any spaces converted to underscores. Case insensitive.
     * @param name - name to search
     * @return optional OneBlockPhase
     */
    public Optional<OneBlockPhase> getPhase(String name) {
        return blockProbs.values().stream().filter(p -> p.getPhaseName() != null && p.getPhaseName().replace(" ", "_").equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Save the oneblock.yml file in memory to disk. Makes a backup.
     * @return true if saved
     */
    public boolean saveOneBlockConfig() {
        // Go through each phase
        blockProbs.values().forEach(p -> {
            YamlConfiguration oneBlocks = new YamlConfiguration();
            ConfigurationSection phSec = oneBlocks.createSection(p.getBlockNumber());
            // Check for a goto block
            if (p.isGotoPhase()) {
                phSec.set("gotoBlock", p.getGotoBlock());
            } else {
                phSec.set(NAME, p.getPhaseName());
                if (p.getFirstBlock() != null) {
                    phSec.set(FIRST_BLOCK, p.getFirstBlock().getMaterial().name());
                }
                if (p.getPhaseBiome() != null) {
                    phSec.set(BIOME, p.getPhaseBiome().name());
                }
                saveBlocks(phSec, p);
                saveEntities(phSec, p);
            }
            try {
                // Save
                File phaseFile = new File(addon.getDataFolder() + File.separator + PHASES, getPhaseFileName(p) + ".yml");
                oneBlocks.save(phaseFile);
            } catch (IOException e) {
                addon.logError("Could not save phase " + p.getPhaseName() + " " + e.getMessage());
            }
            // No chests in goto phases
            if (p.isGotoPhase()) return;
            // Save chests separately
            oneBlocks = new YamlConfiguration();
            phSec = oneBlocks.createSection(p.getBlockNumber());
            saveChests(phSec, p);
            try {
                // Save
                File phaseFile = new File(addon.getDataFolder() + File.separator + PHASES, getPhaseFileName(p) + "_chests.yml");
                oneBlocks.save(phaseFile);
            } catch (IOException e) {
                addon.logError("Could not save phase " + p.getPhaseName() + " " + e.getMessage());
            }

        });

        return true;
    }

    private String getPhaseFileName(OneBlockPhase p) {
        if (p.isGotoPhase()) {
            return p.getBlockNumber() + "_goto_" + p.getGotoBlock();
        }
        return p.getBlockNumber() + "_" + p.getPhaseName().toLowerCase();
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

    /**
     * Get the phase after this one
     * @param phase - one block phase
     * @return next phase or null if there isn't one
     */
    @Nullable
    public OneBlockPhase getNextPhase(@NonNull OneBlockPhase phase) {
        Integer blockNum = Integer.valueOf(phase.getBlockNumber());
        Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
        addon.logWarning(phase.getPhaseName() + " starts at " + blockNum + (nextKey != null ? " and ends " + nextKey : ""));
        return nextKey != null ? this.getPhase(nextKey) : null;
    }

    public void getProbs(OneBlockPhase phase) {
        // Find the phase after this one
        Integer blockNum = Integer.valueOf(phase.getBlockNumber());
        Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
        addon.logWarning(phase.getPhaseName() + " starts at " + blockNum + (nextKey != null ? " and ends " + nextKey : ""));
        if (nextKey != null) {
            // This is the size of the phase in blocks
            int phaseSize = nextKey - blockNum;
            int blockTotal = phase.getBlockTotal();
            int likelyChestTotal = 0;
            double totalBlocks = 0;
            // Now calculate the relative block probability
            for (Entry<Material, Integer> en : phase.getBlocks().entrySet()) {
                double chance = (double)en.getValue()/blockTotal;
                double likelyNumberGenerated = chance * phaseSize;
                totalBlocks += likelyNumberGenerated;
                String report = en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = " + Math.round(likelyNumberGenerated*100/phaseSize) + "%";
                if (likelyNumberGenerated < 1) {
                    addon.logWarning(report);
                } else {
                    addon.log(report);
                }
                if (en.getKey().equals(Material.CHEST)) {
                    likelyChestTotal = (int) Math.round(likelyNumberGenerated);
                }
            }
            addon.log("Total blocks generated = " + totalBlocks);
            // Get the specific chest probability
            if (likelyChestTotal == 0) {
                addon.logWarning("No chests will be generated");
                return;
            }
            addon.log("**** A total of " + likelyChestTotal + " chests will be generated ****");
            // Now calculate chest chances
            double lastChance = 0;
            for (Entry<Double, Rarity> en : OneBlockPhase.CHEST_CHANCES.entrySet()) {
                // Get the number of chests in this rarity group
                int num = phase.getChestsMap().getOrDefault(en.getValue(), Collections.emptyList()).size();
                double likelyNumberGenerated = (en.getKey() - lastChance) * likelyChestTotal;
                lastChance = en.getKey();
                String report = num + " " + en.getValue() + " chests in phase. Likely number generated = " + Math.round(likelyNumberGenerated);
                if (num > 0 && likelyNumberGenerated < 1) {
                    addon.logWarning(report);
                } else {
                    addon.log(report);
                }

            }
            // Mobs
            addon.log("-=-=-=-= Mobs -=-=-=-=-");
            double totalMobs = 0;
            // Now calculate the relative block probability
            for (Entry<EntityType, Integer> en : phase.getMobs().entrySet()) {
                double chance = (double)en.getValue()/phase.getTotal();
                double likelyNumberGenerated = chance * phaseSize;
                totalMobs += likelyNumberGenerated;
                String report = en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = " + Math.round(likelyNumberGenerated*100/phaseSize) + "%";
                if (likelyNumberGenerated < 1) {
                    addon.logWarning(report);
                } else {
                    addon.log(report);
                }
            }
            addon.log("**** A total of " + Math.round(totalMobs) + " mobs will likely be generated ****");
        }
    }

    public void getAllProbs() {
        blockProbs.values().forEach(this::getProbs);
    }

    /**
     * Get the next phase name
     * @param obi - one block island
     * @return next phase name or an empty string
     */
    public String getNextPhase(@NonNull OneBlockIslands obi) {
        return getPhase(obi.getPhaseName())
                .map(this::getNextPhase) // Next phase or null
                .filter(Objects::nonNull)
                .map(OneBlockPhase::getPhaseName).orElse("");
    }
}
