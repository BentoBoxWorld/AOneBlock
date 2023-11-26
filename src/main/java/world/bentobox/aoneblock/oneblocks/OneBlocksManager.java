package world.bentobox.aoneblock.oneblocks;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
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
import world.bentobox.aoneblock.oneblocks.Requirement.ReqType;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.bentobox.util.Util;

/**
 * Provides a manager for all phases
 * 
 * @author tastybento
 *
 */
public class OneBlocksManager {

    private static final String ONE_BLOCKS_YML = "oneblocks.yml";
    private static final String NAME = "name";
    private static final String BIOME = "biome";
    private static final String FIRST_BLOCK = "firstBlock";
    private static final String ICON = "icon";
    private static final String FIXED_BLOCKS = "fixedBlocks";
    private static final String HOLOGRAMS = "holograms";
    private static final String CHESTS = "chests";
    private static final String RARITY = "rarity";
    private static final String CONTENTS = "contents";
    private static final String MOBS = "mobs";
    private static final String BLOCKS = "blocks";
    private static final String PHASES = "phases";
    private static final String GOTO_BLOCK = "gotoBlock";
    private static final String START_COMMANDS = "start-commands";
    private static final String END_COMMANDS = "end-commands";
    private static final String END_COMMANDS_FIRST_TIME = "end-commands-first-time";
    private static final String REQUIREMENTS = "requirements";
    private static final String BLOCK = "Block ";
    private static final String BUT_ALREADY_SET_TO = " but already set to ";
    private static final String DUPLICATE = " Duplicate phase file?";
    private final AOneBlock addon;
    private TreeMap<Integer, OneBlockPhase> blockProbs;

    /**
     * @param addon - addon
     */
    public OneBlocksManager(AOneBlock addon) {
	this.addon = addon;
	// Initialize block probabilities
	blockProbs = new TreeMap<>();
    }

    /**
     * Loads the game phases
     *
     * @throws IOException - if config file has bad syntax or migration fails
     */
    public void loadPhases() throws IOException {
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
		java.nio.file.Files.delete(oneblockFile.toPath());
		java.nio.file.Files.delete(renamedFile.toPath());
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
     *
     * @param file - the file to copy
     */
    void copyPhasesFromAddonJar(File file) {
	try (JarFile jar = new JarFile(addon.getFile())) {
	    // Obtain any locale files, save them and update
	    Util.listJarFiles(jar, PHASES, ".yml").forEach(lf -> addon.saveResource(lf, file, false, true));
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
	for (String phaseStartBlockNumKey : oneBlocks.getKeys(false)) {
	    Integer phaseStartBlockNum = Integer.valueOf(phaseStartBlockNumKey);
	    OneBlockPhase obPhase = blockProbs.computeIfAbsent(phaseStartBlockNum,
		    k -> new OneBlockPhase(phaseStartBlockNumKey));
	    // Get config Section
	    ConfigurationSection phaseConfig = oneBlocks.getConfigurationSection(phaseStartBlockNumKey);
	    // goto
	    if (phaseConfig.contains(GOTO_BLOCK)) {
		obPhase.setGotoBlock(phaseConfig.getInt(GOTO_BLOCK, 0));
		continue;
	    }
	    initBlock(phaseStartBlockNumKey, obPhase, phaseConfig);
	    // Blocks
	    addBlocks(obPhase, phaseConfig);
	    // Mobs
	    addMobs(obPhase, phaseConfig);
	    // Chests
	    addChests(obPhase, phaseConfig);
	    // Commands
	    addCommands(obPhase, phaseConfig);
	    // Requirements
	    addRequirements(obPhase, phaseConfig);
	    // Add to the map
	    blockProbs.put(phaseStartBlockNum, obPhase);
	}
    }

    /**
     * Load in the phase's init
     *
     * @param blockNumber string representation of this phase's block number
     * @param obPhase     OneBlockPhase
     * @param phaseConfig configuration section being read
     * @throws IOException if there's an error in the config file
     */
    void initBlock(String blockNumber, OneBlockPhase obPhase, ConfigurationSection phaseConfig) throws IOException {
	// Set name
	if (phaseConfig.contains(NAME, true)) {
	    if (obPhase.getPhaseName() != null) {
		throw new IOException(
			BLOCK + blockNumber + ": Phase name trying to be set to " + phaseConfig.getString(NAME)
				+ BUT_ALREADY_SET_TO + obPhase.getPhaseName() + ". Duplicate phase file?");
	    }
	    obPhase.setPhaseName(phaseConfig.getString(NAME, blockNumber));
	}

	// Set biome
	if (phaseConfig.contains(BIOME, true)) {
	    if (obPhase.getPhaseBiome() != null) {
		throw new IOException(BLOCK + blockNumber + ": Biome trying to be set to "
			+ phaseConfig.getString(BIOME) + BUT_ALREADY_SET_TO + obPhase.getPhaseBiome() + DUPLICATE);
	    }
	    obPhase.setPhaseBiome(getBiome(phaseConfig.getString(BIOME)));
	}

	// Set first block
	if (phaseConfig.contains(FIRST_BLOCK)) {
	    if (obPhase.getFirstBlock() != null) {
		throw new IOException(
			BLOCK + blockNumber + ": First block trying to be set to " + phaseConfig.getString(FIRST_BLOCK)
				+ BUT_ALREADY_SET_TO + obPhase.getFirstBlock() + DUPLICATE);
	    }
	    addFirstBlock(obPhase, phaseConfig.getString(FIRST_BLOCK));
	}

	// Set icon
	if (phaseConfig.contains(ICON)) {
	    ItemStack icon = ItemParser.parse(phaseConfig.getString(ICON));

	    if (icon == null) {
		throw new IOException("ItemParser failed to parse icon: '" + phaseConfig.getString(ICON)
			+ "' for phase " + obPhase.getFirstBlock() + ". Can you check if it is correct?");
	    }

	    obPhase.setIconBlock(icon);
	}

	// Add fixed blocks
	if (phaseConfig.contains(FIXED_BLOCKS)) {
	    if (!obPhase.getFixedBlocks().isEmpty()) {
		throw new IOException(BLOCK + blockNumber + ": Fixed blocks trying to be set to "
			+ phaseConfig.getString(FIXED_BLOCKS) + BUT_ALREADY_SET_TO + obPhase.getFixedBlocks()
			+ DUPLICATE);
	    }
	    addFixedBlocks(obPhase, phaseConfig.getConfigurationSection(FIXED_BLOCKS));
	}

	// Add holograms
	if (phaseConfig.contains(HOLOGRAMS)) {
	    if (!obPhase.getHologramLines().isEmpty()) {
		throw new IOException(
			BLOCK + blockNumber + ": Hologram Lines trying to be set to " + phaseConfig.getString(HOLOGRAMS)
				+ BUT_ALREADY_SET_TO + obPhase.getHologramLines() + DUPLICATE);
	    }
	    addHologramLines(obPhase, phaseConfig.getConfigurationSection(HOLOGRAMS));
	}
    }

    private void addFixedBlocks(OneBlockPhase obPhase, ConfigurationSection firstBlocksConfig) {
	if (firstBlocksConfig == null) {
	    return;
	}

	Map<Integer, OneBlockObject> result = parseFirstBlocksConfig(firstBlocksConfig);

	// Set the first block if it exists
	if (result.containsKey(0)) {
	    addon.log("Found firstBlock in fixedBlocks.");
	    obPhase.setFirstBlock(result.get(0));
	}
	// Store the remainder
	obPhase.setFixedBlocks(result);
    }

    private Map<Integer, OneBlockObject> parseFirstBlocksConfig(ConfigurationSection firstBlocksConfig) {
	Map<Integer, OneBlockObject> result = new HashMap<>();

	for (String key : firstBlocksConfig.getKeys(false)) {
	    if (!NumberUtils.isNumber(key)) {
		addon.logError("Fixed block key must be an integer. " + key);
		continue;
	    }
	    int k = Integer.parseInt(key);
	    parseBlock(result, firstBlocksConfig, key, k);
	}
	return result;
    }

    private void parseBlock(Map<Integer, OneBlockObject> result, ConfigurationSection firstBlocksConfig, String key,
	    int k) {
	if (firstBlocksConfig.isConfigurationSection(key)) {
	    parseObjectBlock(result, firstBlocksConfig, key, k);
	} else {
	    parseStringBlock(result, firstBlocksConfig, key, k);
	}
    }

    /**
     * This method handles the case where the block's value is a configuration
     * section, indicating that the block is defined as an object.
     * 
     * @param result            the resulting map
     * @param firstBlocksConfig config
     * @param key               key
     * @param k                 integer value of key
     */
    private void parseObjectBlock(Map<Integer, OneBlockObject> result, ConfigurationSection firstBlocksConfig,
	    String key, int k) {
	// Object block parsing logic
	Map<String, Object> map = firstBlocksConfig.getConfigurationSection(key).getValues(false);
	Optional<OneBlockCustomBlock> customBlock = OneBlockCustomBlockCreator.create(map);
	if (customBlock.isPresent()) {
	    result.put(k, new OneBlockObject(customBlock.get(), 0));
	} else {
	    addon.logError("Fixed block key " + key + " material is not a valid custom block. Ignoring.");
	}
    }

    /**
     * This method handles the case where the block's value is a string, which could
     * either be a custom block or a standard Minecraft material.
     * 
     * @param result            the resulting map
     * @param firstBlocksConfig config
     * @param key               key
     * @param k                 integer value of key
     */
    private void parseStringBlock(Map<Integer, OneBlockObject> result, ConfigurationSection firstBlocksConfig,
	    String key, int k) {
	// String block parsing logic
	String mat = firstBlocksConfig.getString(key);
	if (mat == null) {
	    return;
	}

	Optional<OneBlockCustomBlock> customBlock = OneBlockCustomBlockCreator.create(mat);
	if (customBlock.isPresent()) {
	    result.put(k, new OneBlockObject(customBlock.get(), 0));
	} else {
	    Material m = Material.matchMaterial(mat);
	    if (m != null && m.isBlock()) {
		result.put(k, new OneBlockObject(m, 0));
	    } else {
		addon.logError("Fixed block key " + key + " material is invalid or not a block. Ignoring.");
	    }
	}
    }

    private void addHologramLines(OneBlockPhase obPhase, ConfigurationSection fb) {
	if (fb == null)
	    return;
	Map<Integer, String> result = new HashMap<>();
	for (String key : fb.getKeys(false)) {
	    if (!NumberUtils.isNumber(key)) {
		addon.logError("Fixed block key must be an integer. " + key);
		continue;
	    }
	    int k = Integer.parseInt(key);
	    String line = fb.getString(key);
	    if (line != null) {
		result.put(k, line);
	    }
	}
	// Set Hologram Lines
	obPhase.setHologramLines(result);

    }

    private Biome getBiome(String string) {
	if (string == null) {
	    return Biome.PLAINS;
	}
	if (Enums.getIfPresent(Biome.class, string).isPresent()) {
	    return Biome.valueOf(string);
	}
	// Special case for nether
	if (string.equals("NETHER") || string.equals("NETHER_WASTES")) {
	    return Enums.getIfPresent(Biome.class, "NETHER")
		    .or(Enums.getIfPresent(Biome.class, "NETHER_WASTES").or(Biome.PLAINS));
	}
	addon.logError("Biome " + string.toUpperCase() + " is invalid! Use one of these...");
	addon.logError(Arrays.stream(Biome.values()).map(Biome::name).collect(Collectors.joining(",")));
	return Biome.PLAINS;
    }

    void addFirstBlock(OneBlockPhase obPhase, @Nullable String material) {
	if (material == null) {
	    return;
	}
	Material m = Material.matchMaterial(material);
	if (m == null || !m.isBlock()) {
	    addon.logError("Bad firstBlock material: " + material);
	} else {
	    obPhase.setFirstBlock(new OneBlockObject(m, 0));
	}
    }

    void addCommands(OneBlockPhase obPhase, ConfigurationSection phase) {
	if (phase.contains(START_COMMANDS)) {
	    obPhase.setStartCommands(phase.getStringList(START_COMMANDS));
	}
	if (phase.contains(END_COMMANDS)) {
	    obPhase.setEndCommands(phase.getStringList(END_COMMANDS));
	}
	if (phase.contains(END_COMMANDS_FIRST_TIME)) {
	    obPhase.setFirstTimeEndCommands(phase.getStringList(END_COMMANDS_FIRST_TIME));
	}
    }

    void addRequirements(OneBlockPhase obPhase, ConfigurationSection phase) {
	List<Requirement> reqList = new ArrayList<>();
	if (!phase.isConfigurationSection(REQUIREMENTS)) {
	    return;
	}
	ConfigurationSection reqs = phase.getConfigurationSection(REQUIREMENTS);
	for (ReqType key : Requirement.ReqType.values()) {
	    if (reqs.contains(key.getKey())) {
		Requirement r;
		if (key.getClazz().equals(Double.class)) {
		    r = new Requirement(key, reqs.getDouble(key.getKey()));
		} else if (key.getClazz().equals(Long.class)) {
		    r = new Requirement(key, reqs.getLong(key.getKey()));
		} else {
		    r = new Requirement(key, reqs.getString(key.getKey()));
		}
		reqList.add(r);
	    }
	}
	obPhase.setRequirements(reqList);
    }

    void addChests(OneBlockPhase obPhase, ConfigurationSection phase) throws IOException {
	if (!phase.isConfigurationSection(CHESTS)) {
	    return;
	}
	if (!obPhase.getChests().isEmpty()) {
	    throw new IOException(obPhase.getPhaseName() + ": Chests cannot be set more than once. Duplicate file?");
	}
	ConfigurationSection chests = phase.getConfigurationSection(CHESTS);
	for (String chestId : chests.getKeys(false)) {
	    ConfigurationSection chest = chests.getConfigurationSection(chestId);
	    Rarity rarity = Rarity.COMMON;
	    try {
		rarity = OneBlockObject.Rarity.valueOf(chest.getString(RARITY, "COMMON").toUpperCase());
	    } catch (Exception e) {
		addon.logError(
			"Rarity value of " + chest.getString(RARITY, "UNKNOWN") + " is invalid! Use one of these...");
		addon.logError(Arrays.stream(Rarity.values()).map(Rarity::name).collect(Collectors.joining(",")));
		rarity = Rarity.COMMON;
	    }
	    Map<Integer, ItemStack> items = new HashMap<>();
	    ConfigurationSection contents = chest.getConfigurationSection(CONTENTS);
	    if (contents != null) {
		for (String index : contents.getKeys(false)) {
		    int slot = Integer.parseInt(index);
		    ItemStack item = contents.getItemStack(index);
		    if (item != null) {
			items.put(slot, item);
		    }
		}
	    }
	    obPhase.addChest(items, rarity);
	}
    }

    void addMobs(OneBlockPhase obPhase, ConfigurationSection phase) throws IOException {
	if (!phase.isConfigurationSection(MOBS)) {
	    return;
	}
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
		addon.logError(Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable)
			.filter(EntityType::isAlive).map(EntityType::name).collect(Collectors.joining(",")));
		return;
	    }
	    if (et.isSpawnable() && et.isAlive()) {
		if (mobs.getInt(entity) > 0) {
		    obPhase.addMob(et, mobs.getInt(entity));
		} else {
		    addon.logWarning("Bad entity weight for " + obPhase.getPhaseName() + ": " + entity
			    + ". Must be positive number above 1.");
		}
	    } else {
		addon.logError("Entity type is not spawnable " + obPhase.getPhaseName() + ": " + entity);
	    }
	}
    }

    void addBlocks(OneBlockPhase obPhase, ConfigurationSection phase) {
	if (phase.isConfigurationSection(BLOCKS)) {
	    ConfigurationSection blocks = phase.getConfigurationSection(BLOCKS);
	    for (String material : blocks.getKeys(false)) {
		if (Material.getMaterial(material) != null) {
		    addMaterial(obPhase, material, Objects.toString(blocks.get(material)));
		} else {
		    if (addon.hasItemsAdder()) {
			CustomBlock block = CustomBlock.getInstance(material);
			if (block != null) {
			    addItemsAdderBlock(obPhase, material, Objects.toString(blocks.get(material)));
			} else if (ItemsAdder.getAllItems() != null) {
			    if (ItemsAdder.getAllItems().size() != 0) {
				addon.logError("Bad block material in " + obPhase.getPhaseName() + ": " + material);
			    }
			}
		    } else {
			addon.logError("Bad block material in " + obPhase.getPhaseName() + ": " + material);
		    }
		}
	    }
	} else if (phase.isList(BLOCKS)) {
	    List<Map<?, ?>> blocks = phase.getMapList(BLOCKS);
	    for (Map<?, ?> map : blocks) {
		if (map.size() == 1) {
		    Map.Entry<?, ?> entry = map.entrySet().iterator().next();
		    if (addMaterial(obPhase, Objects.toString(entry.getKey()), Objects.toString(entry.getValue()))) {
			continue;
		    }
		}

		int probability = Integer.parseInt(Objects.toString(map.get("probability"), "0"));
		Optional<OneBlockCustomBlock> customBlock = OneBlockCustomBlockCreator.create(map);
		if (customBlock.isPresent()) {
		    obPhase.addCustomBlock(customBlock.get(), probability);
		} else {
		    addon.logError("Bad custom block in " + obPhase.getPhaseName() + ": " + map);
		}
	    }
	}
    }

    private boolean addMaterial(OneBlockPhase obPhase, String material, String probability) {
	int prob;
	try {
	    prob = Integer.parseInt(probability);
	} catch (Exception e) {
	    return false;
	}

	if (prob < 1) {
	    addon.logWarning("Bad item weight for " + obPhase.getPhaseName() + ": " + material
		    + ". Must be positive number above 1.");
	    return false;
	}

	// Register if the material is a valid custom block and can be created from the
	// short creator from OneBlockCustomBlockCreator
	Optional<OneBlockCustomBlock> optionalCustomBlock = OneBlockCustomBlockCreator.create(material);
	if (optionalCustomBlock.isPresent()) {
	    obPhase.addCustomBlock(optionalCustomBlock.get(), prob);
	    return true;
	}

	// Otherwise, register the material as a block
	Material m = Material.matchMaterial(material);
	if (m == null || !m.isBlock()) {
	    addon.logError("Bad block material in " + obPhase.getPhaseName() + ": " + material);
	    return false;
	}
	obPhase.addBlock(m, prob);
	return true;
    }

    private void addItemsAdderBlock(OneBlockPhase obPhase, String block, String probability) {
	int prob;
	try {
	    prob = Integer.parseInt(probability);
	    if (prob < 1) {
		addon.logWarning("Bad item weight for " + obPhase.getPhaseName() + ": " + block
			+ ". Must be positive number above 1.");
	    } else {
		obPhase.addItemsAdderCustomBlock(block, prob);
	    }
	} catch (Exception e) {
	    addon.logError("Bad item weight for " + obPhase.getPhaseName() + ": " + block + ". Must be a number.");
	}

    }

    /**
     * Return the current phase for the block count
     *
     * @param blockCount - number of blocks mined
     * @return the one block phase based on blockCount or null if there is none
     */
    @Nullable
    public OneBlockPhase getPhase(int blockCount) {
	Entry<Integer, OneBlockPhase> en = blockProbs.floorEntry(blockCount);
	return en != null ? en.getValue() : null;
    }

    /**
     * @return list of phase names with spaces replaced by underscore so they are
     *         one word
     */
    public List<String> getPhaseList() {
	return blockProbs.values().stream().map(OneBlockPhase::getPhaseName).filter(Objects::nonNull)
		.map(n -> n.replace(" ", "_")).collect(Collectors.toList());
    }

    /**
     * @return the blockProbs
     */
    public NavigableMap<Integer, OneBlockPhase> getBlockProbs() {
	return blockProbs;
    }

    /**
     * Get phase by name. Name should have any spaces converted to underscores. Case
     * insensitive.
     *
     * @param name - name to search
     * @return optional OneBlockPhase
     */
    public Optional<OneBlockPhase> getPhase(String name) {
	return blockProbs.values().stream()
		.filter(p -> p.getPhaseName() != null && (p.getPhaseName().equalsIgnoreCase(name)
			|| p.getPhaseName().replace(" ", "_").equalsIgnoreCase(name)))
		.findFirst();
    }

    /**
     * Save all the phases in memory to disk.
     *
     * @return true if saved
     */

    public boolean saveOneBlockConfig() {
	// Go through each phase
	boolean success = true;
	for (OneBlockPhase p : blockProbs.values()) {
	    success = savePhase(p);
	}
	return success;
    }

    /**
     * Save a phase
     * 
     * @param p OneBlockPhase
     * @return true if successfully saved
     */
    public boolean savePhase(OneBlockPhase p) {
	if (!saveMainPhase(p)) {
	    // Failure
	    return false;
	}
	// No chests in goto phases
	if (p.isGotoPhase()) {
	    // Done
	    return true;
	}
	return saveChestPhase(p);
    }

    private boolean saveMainPhase(OneBlockPhase p) {
	YamlConfiguration oneBlocks = new YamlConfiguration();
	ConfigurationSection phSec = oneBlocks.createSection(p.getBlockNumber());
	// Check for a goto block
	if (p.isGotoPhase()) {
	    phSec.set(GOTO_BLOCK, p.getGotoBlock());
	} else {
	    phSec.set(NAME, p.getPhaseName());
	    if (p.getIconBlock() != null) {
		phSec.set(ICON, p.getIconBlock().getType().name());
	    }
	    if (p.getFirstBlock() != null) {
		phSec.set(FIRST_BLOCK, p.getFirstBlock().getMaterial().name());
	    }
	    if (p.getPhaseBiome() != null) {
		phSec.set(BIOME, p.getPhaseBiome().name());
	    }
	    saveBlocks(phSec, p);
	    saveEntities(phSec, p);
	    saveHolos(phSec, p);
	    saveCommands(phSec, p);
	}
	try {
	    // Save
	    File phaseFile = new File(addon.getDataFolder() + File.separator + PHASES, getPhaseFileName(p) + ".yml");
	    oneBlocks.save(phaseFile);
	} catch (IOException e) {
	    addon.logError("Could not save phase " + p.getPhaseName() + " " + e.getMessage());
	    return false;
	}
	return true;
    }

    private void saveCommands(ConfigurationSection phSec, OneBlockPhase p) {
	phSec.set(START_COMMANDS, p.getStartCommands());
	phSec.set(END_COMMANDS, p.getEndCommands());

    }

    private void saveHolos(ConfigurationSection phSec, OneBlockPhase p) {
	if (p.getHologramLines() == null)
	    return;
	ConfigurationSection holos = phSec.createSection(HOLOGRAMS);
	p.getHologramLines().forEach((k, v) -> holos.set(String.valueOf(k), v));
    }

    private boolean saveChestPhase(OneBlockPhase p) {
	YamlConfiguration oneBlocks = new YamlConfiguration();
	ConfigurationSection phSec = oneBlocks.createSection(p.getBlockNumber());
	saveChests(phSec, p);
	try {
	    // Save
	    File phaseFile = new File(addon.getDataFolder() + File.separator + PHASES,
		    getPhaseFileName(p) + "_chests.yml");
	    oneBlocks.save(phaseFile);
	} catch (IOException e) {
	    addon.logError("Could not save chest phase " + p.getPhaseName() + " " + e.getMessage());
	    return false;
	}
	return true;
    }

    private String getPhaseFileName(OneBlockPhase p) {
	if (p.isGotoPhase()) {
	    return p.getBlockNumber() + "_goto_" + p.getGotoBlock();
	}
	return p.getBlockNumber() + "_"
		+ (p.getPhaseName() == null ? "" : p.getPhaseName().toLowerCase().replace(' ', '_'));
    }

    private void saveChests(ConfigurationSection phSec, OneBlockPhase phase) {
	ConfigurationSection chests = phSec.createSection(CHESTS);
	int index = 1;
	for (OneBlockObject chest : phase.getChests()) {
	    ConfigurationSection c = chests.createSection(String.valueOf(index++));
	    c.set(CONTENTS, chest.getChest());
	    c.set(RARITY, chest.getRarity().name());
	}

    }

    private void saveEntities(ConfigurationSection phSec, OneBlockPhase phase) {
	ConfigurationSection mobs = phSec.createSection(MOBS);
	phase.getMobs().forEach((k, v) -> mobs.set(k.name(), v));
    }

    private void saveBlocks(ConfigurationSection phSec, OneBlockPhase phase) {
	ConfigurationSection fixedBlocks = phSec.createSection(FIXED_BLOCKS);
	phase.getFixedBlocks().forEach((k, v) -> fixedBlocks.set(String.valueOf(k), v.getMaterial().name()));
	ConfigurationSection blocks = phSec.createSection(BLOCKS);
	phase.getBlocks().forEach((k, v) -> blocks.set(k.name(), v));

    }

    /**
     * Get the phase after this one
     *
     * @param phase - one block phase
     * @return next phase or null if there isn't one
     */
    @SuppressWarnings("WrapperTypeMayBePrimitive")
    @Nullable
    public OneBlockPhase getNextPhase(@NonNull OneBlockPhase phase) {
	// These are Integer objects because GSON can yield nulls if they do not exist
	Integer blockNum = phase.getBlockNumberValue();
	Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
	return nextKey != null ? this.getPhase(nextKey) : null;
    }

    /**
     * Get the number of blocks until the next phase after this one
     *
     * @param obi - one block island
     * @return number of blocks to the next phase. If there is no phase after -1 is
     *         returned.
     */
    public int getNextPhaseBlocks(@NonNull OneBlockIslands obi) {
	Integer blockNum = obi.getBlockNumber();
	Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
	if (nextKey == null) {
	    return -1;
	}
	OneBlockPhase nextPhase = this.getPhase(nextKey);
	return nextPhase == null ? -1 : (nextPhase.getBlockNumberValue() - obi.getBlockNumber());
    }

    /**
     * Get the number of blocks for this phase
     *
     * @param obi - one block island
     * @return number of blocks for this current phase. If there is no phase after
     *         -1 is returned.
     */
    public int getPhaseBlocks(@NonNull OneBlockIslands obi) {
	Integer blockNum = obi.getBlockNumber();
	Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
	if (nextKey == null) {
	    return -1;
	}
	OneBlockPhase thisPhase = this.getPhase(blockNum);
	if (thisPhase == null)
	    return -1;
	OneBlockPhase nextPhase = this.getPhase(nextKey);
	return nextPhase == null ? -1 : (nextPhase.getBlockNumberValue() - thisPhase.getBlockNumberValue());
    }

    /**
     * Get the percentage done of this phase
     *
     * @param obi - one block island
     * @return percentage done. If there is no next phase then return 0
     */
    public double getPercentageDone(@NonNull OneBlockIslands obi) {
	int blockNum = obi.getBlockNumber();
	OneBlockPhase thisPhase = this.getPhase(blockNum);
	if (thisPhase == null) {
	    return 0;
	}
	Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
	if (nextKey == null) {
	    return 0;
	}
	OneBlockPhase nextPhase = this.getPhase(nextKey);
	if (nextPhase == null) {
	    return 0;
	}
	int phaseSize = nextPhase.getBlockNumberValue() - thisPhase.getBlockNumberValue();
	return 100 - (100 * (double) (nextPhase.getBlockNumberValue() - obi.getBlockNumber()) / phaseSize);
    }

    public void getProbs(OneBlockPhase phase) {
	// Find the phase after this one
	Integer blockNum = Integer.valueOf(phase.getBlockNumber());
	Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
	if (nextKey != null) {
	    // This is the size of the phase in blocks
	    int phaseSize = nextKey - blockNum;
	    int blockTotal = phase.getBlockTotal();
	    int likelyChestTotal = 0;
	    double totalBlocks = 0;
	    // Now calculate the relative block probability
	    for (Entry<Material, Integer> en : phase.getBlocks().entrySet()) {
		double chance = (double) en.getValue() / blockTotal;
		double likelyNumberGenerated = chance * phaseSize;
		totalBlocks += likelyNumberGenerated;
		String report = en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = "
			+ Math.round(likelyNumberGenerated * 100 / phaseSize) + "%";
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
		String report = num + " " + en.getValue() + " chests in phase. Likely number generated = "
			+ Math.round(likelyNumberGenerated);
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
		double chance = (double) en.getValue() / phase.getTotal();
		double likelyNumberGenerated = chance * phaseSize;
		totalMobs += likelyNumberGenerated;
		String report = en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = "
			+ Math.round(likelyNumberGenerated * 100 / phaseSize) + "%";
		if (likelyNumberGenerated < 1) {
		    addon.logWarning(report);
		} else {
		    addon.log(report);
		}
	    }
	    addon.log("**** A total of " + Math.round(totalMobs) + " mobs will likely be generated ****");
	}
    }

    /**
     * Get all the probs for each phases and log to console
     */
    public void getAllProbs() {
	blockProbs.values().forEach(this::getProbs);
    }

    /**
     * Get the next phase name
     *
     * @param obi - one block island
     * @return next phase name or an empty string
     */
    public String getNextPhase(@NonNull OneBlockIslands obi) {
	return getPhase(obi.getPhaseName()).map(this::getNextPhase) // Next phase or null
		.filter(Objects::nonNull).map(OneBlockPhase::getPhaseName).orElse("");
    }
}
