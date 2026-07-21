package world.bentobox.aoneblock.oneblocks;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

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
    private static final String FIXED_BLOCK_KEY = "Fixed block key ";
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
    private static final String CUSTOM_BLOCKS = "custom-blocks";
    private static final String PHASES = "phases";
    private static final String GOTO_BLOCK = "gotoBlock";
    private static final String CHEST_WITH_PREFIX = "CHEST_WITH_";
    private static final String START_COMMANDS = "start-commands";
    private static final String END_COMMANDS = "end-commands";
    private static final String END_COMMANDS_FIRST_TIME = "end-commands-first-time";
    private static final String REQUIREMENTS = "requirements";
    private static final String REQUIRED_MC_VERSION = "requiredMinecraftVersion";
    // Possessive quantifiers - version strings need no backtracking and this
    // keeps pathological inputs from recursing deeply in the regex engine
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d++(?:\\.\\d++)*+)");
    private static final String PHASES_INDEX_YML = "phases_index.yml";
    private static final String INDEX_PHASES = "phases";
    private static final String GOTO_AT_END = "gotoAtEnd";
    private static final String ADMIN_LENGTHS = "adminLengths";
    private static final String CHESTS_YML_SUFFIX = "_chests.yml";
    private static final String WEIGHT = "weight";
    /**
     * Length used for a phase whose index entry has no valid length.
     */
    public static final int DEFAULT_PHASE_LENGTH = 500;
    private static final FilenameFilter YML_FILTER = (dir, name) -> name.toLowerCase(Locale.ENGLISH)
            .endsWith(".yml");
    private static final String BLOCK = "Block ";
    private static final String BUT_ALREADY_SET_TO = " but already set to ";
    private static final String DUPLICATE = " Duplicate phase file?";
    private final AOneBlock addon;
    private TreeMap<Integer, OneBlockPhase> blockProbs;
    private List<PhaseIndexEntry> phaseIndex = new ArrayList<>();
    private Integer gotoAtEnd;
    /**
     * True once an admin has set phase lengths through a tool. Reconciliation
     * then never overwrites lengths from the files' legacy start-block keys.
     */
    private boolean adminLengths;

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
        // Clear block probabilities and the index model
        blockProbs = new TreeMap<>();
        phaseIndex = new ArrayList<>();
        gotoAtEnd = null;
        adminLengths = false;
        // Check for folder
        File check = new File(addon.getDataFolder(), PHASES);
        if (check.mkdirs()) {
            setUpNewFolder(check);
        }
        if (loadUsingIndex(check)) {
            return;
        }
        // Fallback - load every phase file directly as before the index existed
        gotoAtEnd = null;
        File[] phaseFiles = Objects.requireNonNull(check.listFiles(YML_FILTER));
        if (phaseFiles.length > 0) {
            addon.logWarning("Phase index could not be used - loading phase files directly.");
        }
        for (File phaseFile : phaseFiles) {
            loadPhase(phaseFile);
        }
    }

    /**
     * Fills a freshly made phases folder: migrates a legacy single-file setup if
     * one exists, otherwise copies the shipped phase files from the jar.
     */
    private void setUpNewFolder(File check) throws IOException {
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

    /**
     * Loads the phases via the phase index. The index is read before any phase
     * file so that phases needing a newer server version are skipped without
     * their files ever being parsed, and it is reconciled with the phases folder
     * first - the folder is the source of truth for which phases exist, so
     * custom or renamed phase files always load and the admin phases panel shows
     * this server's reality, not a preset.
     *
     * @param check the phases folder
     * @return true if the phases were loaded, false to fall back to loading the
     *         phase files directly
     */
    private boolean loadUsingIndex(File check) {
        File indexFile = new File(addon.getDataFolder(), PHASES_INDEX_YML);
        boolean freshIndex = !indexFile.exists();
        if (freshIndex) {
            copyIndexFromAddonJar();
        }
        List<PhaseIndexEntry> entries = indexFile.exists() ? readIndex(indexFile) : new ArrayList<>();
        if (entries == null) {
            return false;
        }
        boolean changed = reconcileIndex(entries, check, freshIndex);
        if (entries.isEmpty()) {
            return false;
        }
        phaseIndex = entries;
        if (changed && saveIndex()) {
            addon.log("Updated " + PHASES_INDEX_YML + " to match the files in the phases folder.");
        }
        int startBlock = 0;
        for (PhaseIndexEntry entry : entries) {
            startBlock = loadIndexedPhase(check, entry, startBlock);
        }
        if (gotoAtEnd != null) {
            OneBlockPhase gotoPhase = new OneBlockPhase(String.valueOf(startBlock));
            gotoPhase.setGotoBlock(gotoAtEnd);
            blockProbs.put(startBlock, gotoPhase);
        }
        return true;
    }

    /**
     * Copies the shipped phase index from the addon jar, if it has one.
     */
    private void copyIndexFromAddonJar() {
        try {
            addon.saveResource(PHASES_INDEX_YML, false);
        } catch (Exception e) {
            // Not in the jar - the index will be generated from the phase files
        }
    }

    /**
     * Reads the phase index file and sets {@link #gotoAtEnd} from it.
     *
     * @param indexFile index file
     * @return the entries, or null if the file cannot be parsed
     */
    @Nullable
    private List<PhaseIndexEntry> readIndex(File indexFile) {
        YamlConfiguration index = new YamlConfiguration();
        try {
            index.load(indexFile);
        } catch (Exception e) {
            addon.logError("Could not load " + PHASES_INDEX_YML + ": " + e.getMessage());
            return null;
        }
        List<PhaseIndexEntry> entries = new ArrayList<>();
        for (Map<?, ?> map : index.getMapList(INDEX_PHASES)) {
            PhaseIndexEntry entry = PhaseIndexEntry.fromMap(map);
            if (entry == null) {
                addon.logError(PHASES_INDEX_YML + " entry is missing the file name. Skipping it.");
            } else {
                entries.add(entry);
            }
        }
        gotoAtEnd = index.contains(GOTO_AT_END) ? index.getInt(GOTO_AT_END, 0) : null;
        adminLengths = index.getBoolean(ADMIN_LENGTHS, false);
        return entries;
    }

    /**
     * Result of scanning the main phase files in the phases folder: every phase
     * section with a numeric (legacy start-block) key, phase sections with any
     * other key, plus any goto found. Section keys only need to be numbers for
     * the legacy fallback loader - for indexed phases they are just identifiers,
     * so custom files may use keys like 'my_phase'.
     */
    private static class DiskScan {
        final TreeMap<Integer, PhaseIndexEntry> phases = new TreeMap<>();
        final List<PhaseIndexEntry> unkeyed = new ArrayList<>();
        Integer gotoTarget;
        int gotoStart = -1;

        /**
         * Length of the phase at this legacy start-block key, from the gap to the
         * next scanned key (or the goto), or -1 if there is nothing after it.
         */
        int lengthAt(int start) {
            Integer next = phases.higherKey(start);
            int end = next != null ? next : gotoStart;
            return end > start ? end - start : -1;
        }
    }

    /**
     * Scans the main phase files on disk. Only main files are read - they contain
     * plain scalars, so this is safe on any server version. Chest files, which
     * hold serialized items, are never touched.
     *
     * @param phaseFolder folder holding the phase files
     * @return the scan
     */
    private DiskScan scanPhaseFolder(File phaseFolder) {
        DiskScan scan = new DiskScan();
        FilenameFilter mainYmlFilter = (dir, name) -> name.toLowerCase(Locale.ENGLISH).endsWith(".yml")
                && !name.toLowerCase(Locale.ENGLISH).endsWith(CHESTS_YML_SUFFIX);
        File[] files = phaseFolder.listFiles(mainYmlFilter);
        if (files != null) {
            for (File phaseFile : files) {
                scanPhaseFile(phaseFile, scan);
            }
        }
        return scan;
    }

    /**
     * Adds every phase section in one main phase file to the scan.
     */
    private void scanPhaseFile(File phaseFile, DiskScan scan) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(phaseFile);
        } catch (Exception e) {
            addon.logError("Could not scan " + phaseFile.getName() + " for the phase index: " + e.getMessage());
            return;
        }
        String base = phaseFile.getName().substring(0, phaseFile.getName().length() - ".yml".length());
        for (String key : cfg.getKeys(false)) {
            ConfigurationSection section = cfg.getConfigurationSection(key);
            if (section != null) {
                scanPhaseSection(base, key, section, scan);
            }
        }
    }

    /**
     * Adds one top-level section of a phase file to the scan - as the goto, a
     * numeric-keyed phase, or an unkeyed phase.
     */
    private void scanPhaseSection(String base, String key, ConfigurationSection section, DiskScan scan) {
        boolean numericKey = NumberUtils.isDigits(key);
        if (section.contains(GOTO_BLOCK)) {
            scan.gotoTarget = section.getInt(GOTO_BLOCK, 0);
            if (numericKey) {
                scan.gotoStart = Integer.parseInt(key);
            }
            return;
        }
        // Non-numeric keys are fine for indexed phases, but ask for some
        // evidence that the section really is a phase so stray YAML in the
        // folder does not become one
        if (!numericKey && !looksLikePhase(section)) {
            return;
        }
        PhaseIndexEntry entry = new PhaseIndexEntry();
        entry.setFile(base);
        entry.setSection(key);
        entry.setName(section.getString(NAME, base));
        String requiredVersion = Objects.toString(section.get(REQUIRED_MC_VERSION), "");
        if (!requiredVersion.isEmpty()) {
            entry.setRequiredMinecraftVersion(requiredVersion);
        }
        if (numericKey) {
            scan.phases.put(Integer.parseInt(key), entry);
        } else {
            scan.unkeyed.add(entry);
        }
    }

    private boolean looksLikePhase(ConfigurationSection section) {
        return section.contains(NAME) || section.contains(BLOCKS) || section.contains(FIXED_BLOCKS)
                || section.contains(MOBS);
    }

    /**
     * Reconciles the index entries with the phase files actually in the phases
     * folder, so the index - and everything driven by it, like the admin phases
     * panel - reflects this server's reality rather than a shipped preset:
     * <ul>
     * <li>An entry whose file and section exist on disk keeps its place.</li>
     * <li>An entry whose file is missing is re-pointed at a folder file holding a
     * phase with the same name - this follows shipped file renames across addon
     * versions (e.g. 11000_deep_dark to 10500_deep_dark).</li>
     * <li>Failing that, a shipped file is restored from the addon jar - this
     * recovers phases added by an addon upgrade, whose files are never copied
     * into an existing folder.</li>
     * <li>Failing that, the entry is removed.</li>
     * <li>Folder files not in the index at all are added. Numeric section keys
     * position an entry among the others by legacy start block and imply its
     * length; non-numeric keys (fine for custom phases - keys are only start
     * blocks for the legacy non-index loader) go to the end with the default
     * length, to be arranged in the admin GUI.</li>
     * </ul>
     * When any of that repair work happened - or the index was only just copied
     * from the jar over an existing folder - the index clearly did not describe
     * this server, so entry lengths are also refreshed from the gaps between the
     * files' legacy start-block keys, preserving the layout the server actually
     * ran before the index existed. Once an admin has set lengths through a tool
     * ({@link #setAdminLengths()}), lengths are never refreshed.
     *
     * @param entries        index entries, reconciled in place
     * @param phaseFolder    folder holding the phase files
     * @param refreshLengths true to refresh entry lengths from the folder even if
     *                       no other repair was needed
     * @return true if the entries or goto changed and the index should be saved
     */
    boolean reconcileIndex(List<PhaseIndexEntry> entries, File phaseFolder, boolean refreshLengths) {
        DiskScan disk = scanPhaseFolder(phaseFolder);
        boolean fromScratch = entries.isEmpty();
        Reconciliation rec = new Reconciliation();
        for (PhaseIndexEntry entry : entries) {
            reconcileEntry(entry, disk, phaseFolder, rec);
        }
        addKeyedDiscoveries(disk, rec, fromScratch);
        addUnkeyedDiscoveries(disk, rec);
        if (gotoAtEnd == null && disk.gotoTarget != null) {
            gotoAtEnd = disk.gotoTarget;
            rec.changed = true;
        }
        if (refreshLengths || rec.repaired) {
            refreshLengthsFromFolder(disk, rec);
        }
        entries.clear();
        entries.addAll(rec.result);
        return rec.changed;
    }

    /**
     * Working state of one reconciliation run.
     */
    private static class Reconciliation {
        final List<PhaseIndexEntry> result = new ArrayList<>();
        /**
         * Legacy start-block key of the folder section backing each kept entry.
         * Null when an entry is not backed by a scanned numeric-keyed section.
         */
        final List<Integer> orderKeys = new ArrayList<>();
        final Set<PhaseIndexEntry> claimedScans = new HashSet<>();
        final Map<PhaseIndexEntry, Integer> claims = new HashMap<>();
        boolean changed;
        boolean repaired;

        void keep(PhaseIndexEntry entry, @Nullable Integer orderKey) {
            result.add(entry);
            orderKeys.add(orderKey);
        }
    }

    /**
     * Reconciles one index entry: claims its backing folder section (re-pointing
     * the entry if the phase moved), restores its file from the jar, or drops it.
     */
    private void reconcileEntry(PhaseIndexEntry entry, DiskScan disk, File phaseFolder, Reconciliation rec) {
        PhaseIndexEntry scanned = matchOnDisk(entry, disk, phaseFolder, rec);
        if (scanned != null) {
            rec.claimedScans.add(scanned);
            Integer start = sectionKeyOf(scanned);
            if (start != null) {
                rec.claims.put(entry, start);
            }
            rec.keep(entry, start);
            return;
        }
        if (!mainFile(phaseFolder, entry.getFile()).exists()) {
            restorePhaseFileFromJar(entry.getFile());
            if (!mainFile(phaseFolder, entry.getFile()).exists()) {
                addon.logWarning("Phase index: removed " + entry.getName() + " - " + entry.getFile()
                        + ".yml is not in the phases folder or the addon jar.");
                rec.repaired = true;
                rec.changed = true;
                return;
            }
            addon.log("Phase index: restored missing " + entry.getFile() + ".yml from the addon jar.");
            rec.repaired = true;
        }
        // On disk but without a free scanned section - keep it as-is and let
        // the loader's section fallback deal with it
        rec.keep(entry, sectionKeyOf(entry));
    }

    /**
     * Finds the scanned folder section backing this entry: its own file and
     * section, else a free section of its file, else - only when its file is
     * gone - a section holding a phase with the same name. In the latter two
     * cases the entry is re-pointed at what was found.
     */
    @Nullable
    private PhaseIndexEntry matchOnDisk(PhaseIndexEntry entry, DiskScan disk, File phaseFolder, Reconciliation rec) {
        PhaseIndexEntry scanned = findScanned(disk, rec.claimedScans, en -> en.getFile().equals(entry.getFile())
                && (entry.getSection() == null || Objects.equals(entry.getSection(), en.getSection())));
        if (scanned != null) {
            return scanned;
        }
        scanned = findScanned(disk, rec.claimedScans, en -> en.getFile().equals(entry.getFile()));
        if (scanned == null && !mainFile(phaseFolder, entry.getFile()).exists()) {
            scanned = findScanned(disk, rec.claimedScans,
                    en -> en.getName() != null && en.getName().equalsIgnoreCase(entry.getName()));
        }
        if (scanned == null) {
            return null;
        }
        // The same phase lives in a different file or section on disk
        addon.log("Phase index: " + entry.getName() + " is " + scanned.getFile()
                + ".yml in the phases folder. Using that file.");
        entry.setFile(scanned.getFile());
        entry.setSection(scanned.getSection());
        entry.setName(scanned.getName());
        entry.setRequiredMinecraftVersion(scanned.getRequiredMinecraftVersion());
        rec.repaired = true;
        rec.changed = true;
        return scanned;
    }

    /**
     * Adds numeric-keyed folder sections the index does not know about,
     * positioned by legacy start block with the length its key gap implies.
     */
    private void addKeyedDiscoveries(DiskScan disk, Reconciliation rec, boolean fromScratch) {
        for (Entry<Integer, PhaseIndexEntry> en : disk.phases.entrySet()) {
            if (rec.claimedScans.contains(en.getValue())) {
                continue;
            }
            PhaseIndexEntry entry = en.getValue();
            int length = disk.lengthAt(en.getKey());
            entry.setLength(length > 0 ? length : DEFAULT_PHASE_LENGTH);
            int pos = insertPosition(rec.orderKeys, en.getKey());
            rec.result.add(pos, entry);
            rec.orderKeys.add(pos, en.getKey());
            rec.claims.put(entry, en.getKey());
            if (!fromScratch) {
                addon.log("Phase index: added " + entry.getName() + " from " + entry.getFile()
                        + ".yml found in the phases folder.");
            }
            rec.repaired = true;
            rec.changed = true;
        }
    }

    /**
     * Adds folder sections without numeric keys to the end of the phase order
     * with the default length, for the admin to arrange in the GUI.
     */
    private void addUnkeyedDiscoveries(DiskScan disk, Reconciliation rec) {
        for (PhaseIndexEntry entry : disk.unkeyed) {
            if (rec.claimedScans.contains(entry)) {
                continue;
            }
            entry.setLength(DEFAULT_PHASE_LENGTH);
            rec.keep(entry, null);
            addon.log("Phase index: added " + entry.getName() + " from " + entry.getFile()
                    + ".yml at the end of the phase order. Move it with the admin phases GUI.");
            rec.repaired = true;
            rec.changed = true;
        }
    }

    /**
     * Refreshes claimed entries' lengths from the gaps between the folder
     * files' legacy start-block keys, unless an admin owns the lengths.
     */
    private void refreshLengthsFromFolder(DiskScan disk, Reconciliation rec) {
        if (adminLengths) {
            return;
        }
        for (Entry<PhaseIndexEntry, Integer> claim : rec.claims.entrySet()) {
            int length = disk.lengthAt(claim.getValue());
            if (length > 0 && length != claim.getKey().getLength()) {
                claim.getKey().setLength(length);
                rec.changed = true;
            }
        }
    }

    /**
     * Finds the first unclaimed scanned phase section that matches - numeric-keyed
     * sections in start-block order first, then the unkeyed ones.
     *
     * @param disk    folder scan
     * @param claimed scanned sections already claimed by an index entry
     * @param test    match condition
     * @return matching scanned section, or null
     */
    @Nullable
    private PhaseIndexEntry findScanned(DiskScan disk, Set<PhaseIndexEntry> claimed,
            Predicate<PhaseIndexEntry> test) {
        return Stream.concat(disk.phases.values().stream(), disk.unkeyed.stream())
                .filter(en -> !claimed.contains(en) && test.test(en)).findFirst().orElse(null);
    }

    /**
     * @return the entry's section key as a legacy start block, or null if the
     *         key is not numeric
     */
    @Nullable
    private Integer sectionKeyOf(PhaseIndexEntry entry) {
        return NumberUtils.isDigits(entry.getSection()) ? Integer.valueOf(entry.getSection()) : null;
    }

    /**
     * @return the position in the reconciled list where a phase with this legacy
     *         start-block key belongs - before the first entry with a higher key
     */
    private int insertPosition(List<Integer> orderKeys, int start) {
        for (int i = 0; i < orderKeys.size(); i++) {
            Integer key = orderKeys.get(i);
            if (key != null && key > start) {
                return i;
            }
        }
        return orderKeys.size();
    }

    private File mainFile(File phaseFolder, String fileBase) {
        return new File(phaseFolder, fileBase + ".yml");
    }

    /**
     * Copies a phase's main and chest files from the addon jar, if it ships them.
     */
    private void restorePhaseFileFromJar(String fileBase) {
        saveJarResource(PHASES + "/" + fileBase + ".yml");
        saveJarResource(PHASES + "/" + fileBase + CHESTS_YML_SUFFIX);
    }

    private void saveJarResource(String jarPath) {
        try {
            addon.saveResource(jarPath, false);
        } catch (Exception e) {
            // Not shipped in the jar
        }
    }

    /**
     * Writes a phase index file.
     *
     * @param indexFile  file to write
     * @param entries    index entries, in phase order
     * @param gotoTarget block count to jump to after the last phase, or null
     * @return true if written
     */
    private boolean writeIndex(File indexFile, List<PhaseIndexEntry> entries, @Nullable Integer gotoTarget) {
        YamlConfiguration index = new YamlConfiguration();
        index.set(INDEX_PHASES, entries.stream().map(PhaseIndexEntry::toMap).toList());
        if (gotoTarget != null) {
            index.set(GOTO_AT_END, gotoTarget);
        }
        if (adminLengths) {
            index.set(ADMIN_LENGTHS, true);
        }
        try {
            index.save(indexFile);
            return true;
        } catch (IOException e) {
            addon.logError("Could not save " + PHASES_INDEX_YML + " " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves the in-memory phase index to disk. Call after changing the order,
     * lengths, or enabled state of entries from {@link #getPhaseIndex()}, then
     * call {@link #loadPhases()} to apply the changes.
     *
     * @return true if written
     */
    public boolean saveIndex() {
        return writeIndex(new File(addon.getDataFolder(), PHASES_INDEX_YML), phaseIndex, gotoAtEnd);
    }

    /**
     * @return the live phase index, in phase order, including entries that are
     *         disabled or skipped on this server version. Mutate it and call
     *         {@link #saveIndex()} then {@link #loadPhases()} to apply changes.
     *         Empty when phases were loaded without an index.
     */
    public List<PhaseIndexEntry> getPhaseIndex() {
        return phaseIndex;
    }

    /**
     * Checks whether an index entry would load on this server - it is enabled and
     * the server meets its required Minecraft version.
     *
     * @param entry index entry
     * @return true if the phase would load
     */
    public boolean isPhaseAvailable(PhaseIndexEntry entry) {
        String requiredVersion = Objects.toString(entry.getRequiredMinecraftVersion(), "");
        return entry.isEnabled()
                && (requiredVersion.isEmpty() || isVersionAtLeast(Bukkit.getMinecraftVersion(), requiredVersion));
    }

    /**
     * @return block count the game jumps to after the last phase, or null if
     *         there is no jump
     */
    @Nullable
    public Integer getGotoAtEnd() {
        return gotoAtEnd;
    }

    /**
     * @param gotoAtEnd block count to jump to after the last phase, or null for
     *                  no jump
     */
    public void setGotoAtEnd(@Nullable Integer gotoAtEnd) {
        this.gotoAtEnd = gotoAtEnd;
    }

    /**
     * Marks the index as holding admin-set phase lengths. Call before
     * {@link #saveIndex()} when a tool changes an entry's length. From then on
     * reconciliation never overwrites lengths from the phase files' legacy
     * start-block keys, so the admin's values stick even when files are later
     * added, renamed, or removed.
     */
    public void setAdminLengths() {
        adminLengths = true;
    }

    /**
     * Loads one indexed phase.
     *
     * @param phaseFolder folder holding the phase files
     * @param entry       index entry
     * @param startBlock  start block for this phase
     * @return the start block for the next phase - unchanged if this one was
     *         skipped
     */
    private int loadIndexedPhase(File phaseFolder, PhaseIndexEntry entry, int startBlock) {
        String name = entry.getName();
        if (!entry.isEnabled()) {
            addon.log("Skipping phase " + name + ": disabled in " + PHASES_INDEX_YML + ".");
            return startBlock;
        }
        String requiredVersion = Objects.toString(entry.getRequiredMinecraftVersion(), "");
        if (!requiredVersion.isEmpty() && !isVersionAtLeast(Bukkit.getMinecraftVersion(), requiredVersion)) {
            addon.log("Skipping phase " + name + ": it requires Minecraft " + requiredVersion + " or later.");
            return startBlock;
        }
        File mainFile = new File(phaseFolder, entry.getFile() + ".yml");
        if (!mainFile.exists()) {
            addon.logError(PHASES_INDEX_YML + ": " + mainFile.getName() + " does not exist. Skipping phase " + name
                    + ".");
            return startBlock;
        }
        String blockNumber = String.valueOf(startBlock);
        OneBlockPhase obPhase = new OneBlockPhase(blockNumber);
        obPhase.setIndexEntry(entry);
        if (!requiredVersion.isEmpty()) {
            obPhase.setRequiredMinecraftVersion(requiredVersion);
        }
        try {
            addon.log("Loading " + mainFile.getName());
            ConfigurationSection phaseConfig = getPhaseSection(mainFile, entry.getSection());
            if (phaseConfig == null) {
                addon.logError(mainFile.getName() + " has no phase section. Skipping phase " + name + ".");
                return startBlock;
            }
            parsePhaseSection(obPhase, phaseConfig, blockNumber);
        } catch (Exception e) {
            addon.logError("Could not load phase " + name + ": " + e.getMessage());
            return startBlock;
        }
        loadIndexedChests(phaseFolder, entry.getFile(), entry.getSection(), obPhase, name);
        blockProbs.put(startBlock, obPhase);
        return startBlock + phaseLength(entry);
    }

    /**
     * Loads the chest file for an indexed phase, if there is one. The file is read
     * with plain SnakeYAML - not YamlConfiguration - so serialized items are never
     * eagerly deserialized. Each item is built individually and an item that this
     * server version does not know is skipped with a log line instead of a stack
     * trace. A broken chest file loses its chests but does not lose the phase.
     */
    private void loadIndexedChests(File phaseFolder, String fileBase, String section, OneBlockPhase obPhase,
            String name) {
        File chestFile = new File(phaseFolder, fileBase + CHESTS_YML_SUFFIX);
        if (!chestFile.exists()) {
            return;
        }
        try (Reader reader = java.nio.file.Files.newBufferedReader(chestFile.toPath(), StandardCharsets.UTF_8)) {
            addon.log("Loading " + chestFile.getName());
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            Object phaseMap = getRawSection(yaml.load(reader), section);
            if (phaseMap instanceof Map<?, ?> map && map.get(CHESTS) instanceof Map<?, ?> chests) {
                addChestsFromRaw(obPhase, chests, chestFile.getName());
            }
        } catch (Exception e) {
            addon.logError("Could not load chests for phase " + name + ": " + e.getMessage());
        }
    }

    /**
     * Gets the named section of a raw YAML map, or its first map value if the
     * named one is absent.
     */
    private Object getRawSection(Object root, String section) {
        if (!(root instanceof Map<?, ?> rootMap)) {
            return null;
        }
        if (section != null) {
            for (Entry<?, ?> en : rootMap.entrySet()) {
                if (section.equals(Objects.toString(en.getKey()))) {
                    return en.getValue();
                }
            }
        }
        return rootMap.values().stream().filter(Map.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Adds the chests defined in a raw chests map to the phase.
     *
     * @param obPhase  phase to add chests to
     * @param chests   raw map of chest id to chest definition
     * @param fileName file the chests came from, for log messages
     */
    private void addChestsFromRaw(OneBlockPhase obPhase, Map<?, ?> chests, String fileName) {
        for (Object chestDef : chests.values()) {
            if (!(chestDef instanceof Map<?, ?> chest)) {
                continue;
            }
            Rarity rarity = Enums.getIfPresent(Rarity.class,
                    Objects.toString(chest.get(RARITY), "COMMON").toUpperCase(Locale.ENGLISH)).or(Rarity.COMMON);
            Map<Integer, ItemStack> items = new HashMap<>();
            if (chest.get(CONTENTS) instanceof Map<?, ?> contents) {
                for (Entry<?, ?> slotEntry : contents.entrySet()) {
                    if (!NumberUtils.isCreatable(Objects.toString(slotEntry.getKey()))
                            || !(slotEntry.getValue() instanceof Map<?, ?> rawItem)) {
                        continue;
                    }
                    ItemStack item = chestItem(rawItem, fileName);
                    if (item != null) {
                        items.put(Integer.parseInt(Objects.toString(slotEntry.getKey())), item);
                    }
                }
            }
            obPhase.addChest(items, rarity);
        }
    }

    /**
     * Builds one chest item from its raw serialized map. Items whose id is not in
     * this server's registry are skipped with a log line. Everything else is
     * handed to Bukkit's deserializer, with failures contained to the one item.
     *
     * @param raw      raw serialized item map
     * @param fileName file the item came from, for log messages
     * @return the item, or null if it cannot exist on this server
     */
    private @Nullable ItemStack chestItem(Map<?, ?> raw, String fileName) {
        String id = Objects.toString(raw.get("id"), Objects.toString(raw.get("type"), null));
        if (id != null && Material.matchMaterial(id) == null) {
            addon.log("Skipping item " + id + " in " + fileName + ": it does not exist on this server version.");
            return null;
        }
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            raw.forEach((k, v) -> map.put(Objects.toString(k), v));
            map.remove("==");
            return ItemStack.deserialize(map);
        } catch (Exception e) {
            addon.log("Skipping item " + id + " in " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private int phaseLength(PhaseIndexEntry entry) {
        if (entry.getLength() > 0) {
            return entry.getLength();
        }
        addon.logWarning("Phase " + entry.getName() + " has no valid length in " + PHASES_INDEX_YML + ". Using "
                + DEFAULT_PHASE_LENGTH + ".");
        return DEFAULT_PHASE_LENGTH;
    }

    /**
     * Gets the named section of a phase file, or its first section if the named
     * one is absent.
     */
    private ConfigurationSection getPhaseSection(File file, String section) throws IOException, InvalidConfigurationException {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.load(file);
        if (section != null && cfg.isConfigurationSection(section)) {
            return cfg.getConfigurationSection(section);
        }
        return cfg.getKeys(false).stream().map(cfg::getConfigurationSection).filter(Objects::nonNull).findFirst()
                .orElse(null);
    }

    /**
     * Check that a server Minecraft version is the same as or newer than a required
     * version. Only the leading dotted-numeric part of each string is compared, so
     * "1.21.11-R0.1-SNAPSHOT" is read as 1.21.11; missing components count as 0.
     *
     * @param serverVersion   version the server is running, e.g. "1.21.11" or "26.2"
     * @param requiredVersion minimum version needed
     * @return true if serverVersion is at least requiredVersion; false if it is
     *         older or if either version cannot be parsed
     */
    static boolean isVersionAtLeast(String serverVersion, String requiredVersion) {
        int[] server = parseVersion(serverVersion);
        int[] required = parseVersion(requiredVersion);
        if (server.length == 0 || required.length == 0) {
            return false;
        }
        for (int i = 0; i < Math.max(server.length, required.length); i++) {
            int s = i < server.length ? server[i] : 0;
            int r = i < required.length ? required[i] : 0;
            if (s != r) {
                return s > r;
            }
        }
        return true;
    }

    private static int[] parseVersion(String version) {
        if (version == null) {
            return new int[0];
        }
        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.find()) {
            return new int[0];
        }
        return Arrays.stream(matcher.group(1).split("\\.")).mapToInt(Integer::parseInt).toArray();
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
            // Get config Section
            ConfigurationSection phaseConfig = oneBlocks.getConfigurationSection(phaseStartBlockNumKey);
            // Skip phases that need a newer Minecraft version than this server runs
            String requiredVersion = Objects.toString(phaseConfig.get(REQUIRED_MC_VERSION), "");
            if (!requiredVersion.isEmpty() && !isVersionAtLeast(Bukkit.getMinecraftVersion(), requiredVersion)) {
                addon.log("Skipping phase " + phaseStartBlockNumKey + " in " + phaseFile.getName()
                        + ": it requires Minecraft " + requiredVersion + " or later.");
                continue;
            }
            OneBlockPhase obPhase = blockProbs.computeIfAbsent(phaseStartBlockNum,
                    k -> new OneBlockPhase(phaseStartBlockNumKey));
            if (!requiredVersion.isEmpty()) {
                obPhase.setRequiredMinecraftVersion(requiredVersion);
            }
            // goto
            if (phaseConfig.contains(GOTO_BLOCK)) {
                obPhase.setGotoBlock(phaseConfig.getInt(GOTO_BLOCK, 0));
                continue;
            }
            parsePhaseSection(obPhase, phaseConfig, phaseStartBlockNumKey);
            // Add to the map
            blockProbs.put(phaseStartBlockNum, obPhase);
        }
    }

    /**
     * Parses everything in one phase section into the phase.
     *
     * @param obPhase     phase to fill
     * @param phaseConfig configuration section being read
     * @param blockNumber string representation of this phase's start block
     * @throws IOException if there's an error in the config file
     */
    void parsePhaseSection(OneBlockPhase obPhase, ConfigurationSection phaseConfig, String blockNumber)
            throws IOException {
        initBlock(blockNumber, obPhase, phaseConfig);
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
            checkNotDuplicate(obPhase.getPhaseName() != null, blockNumber, "Phase name",
                    phaseConfig.getString(NAME), obPhase.getPhaseName(), ". Duplicate phase file?");
            obPhase.setPhaseName(phaseConfig.getString(NAME, blockNumber));
        }

        // Set biome
        if (phaseConfig.contains(BIOME, true)) {
            checkNotDuplicate(obPhase.getPhaseBiome() != null, blockNumber, "Biome",
                    phaseConfig.getString(BIOME), obPhase.getPhaseBiome(), DUPLICATE);
            obPhase.setPhaseBiome(getBiome(phaseConfig.getString(BIOME)));
        }

        // Set first block
        if (phaseConfig.contains(FIRST_BLOCK)) {
            checkNotDuplicate(obPhase.getFirstBlock() != null, blockNumber, "First block",
                    phaseConfig.getString(FIRST_BLOCK), obPhase.getFirstBlock(), DUPLICATE);
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
            checkNotDuplicate(!obPhase.getFixedBlocks().isEmpty(), blockNumber, "Fixed blocks",
                    phaseConfig.getString(FIXED_BLOCKS), obPhase.getFixedBlocks(), DUPLICATE);
            addFixedBlocks(obPhase, phaseConfig.getConfigurationSection(FIXED_BLOCKS));
        }

        // Add holograms
        if (phaseConfig.contains(HOLOGRAMS)) {
            checkNotDuplicate(!obPhase.getHologramLines().isEmpty(), blockNumber, "Hologram Lines",
                    phaseConfig.getString(HOLOGRAMS), obPhase.getHologramLines(), DUPLICATE);
            addHologramLines(obPhase, phaseConfig.getConfigurationSection(HOLOGRAMS));
        }
    }

    private void checkNotDuplicate(boolean alreadySet, String blockNumber, String field,
            Object newValue, Object existingValue, String suffix) throws IOException {
        if (alreadySet) {
            throw new IOException(BLOCK + blockNumber + ": " + field + " trying to be set to "
                    + newValue + BUT_ALREADY_SET_TO + existingValue + suffix);
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
            if (!NumberUtils.isCreatable(key)) {
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
            addon.logError(FIXED_BLOCK_KEY + key + " material is not a valid custom block. Ignoring.");
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

        // Check for CHEST_WITH_X notation
        String matUpper = mat.toUpperCase(Locale.ENGLISH);
        if (matUpper.startsWith(CHEST_WITH_PREFIX)) {
            parseChestWithItem(result, key, k, matUpper.substring(CHEST_WITH_PREFIX.length()));
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
                addon.logError(FIXED_BLOCK_KEY + key + " material is invalid or not a block. Ignoring.");
            }
        }
    }

    /**
     * Parses a {@code CHEST_WITH_X} shorthand entry and adds it to the result map.
     * The produced chest block will contain a single item of the specified material
     * in slot 0.
     *
     * @param result   the resulting fixed-blocks map
     * @param key      the raw YAML key (used in error messages)
     * @param k        the integer value of the key
     * @param itemName the material name of the item to place in the chest
     */
    private void parseChestWithItem(Map<Integer, OneBlockObject> result, String key, int k, String itemName) {
        Material item = Material.matchMaterial(itemName);
        if (item == null) {
            addon.logError(FIXED_BLOCK_KEY + key + " CHEST_WITH item is invalid: " + itemName + ". Ignoring.");
            return;
        }
        Map<Integer, ItemStack> chestContents = new HashMap<>();
        chestContents.put(0, new ItemStack(item));
        result.put(k, new OneBlockObject(chestContents, Rarity.COMMON));
    }

    private void addHologramLines(OneBlockPhase obPhase, ConfigurationSection fb) {
        if (fb == null)
            return;
        Map<Integer, String> result = new HashMap<>();
        for (String key : fb.getKeys(false)) {
            if (!NumberUtils.isCreatable(key)) {
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
        NamespacedKey key = NamespacedKey.fromString(string.toLowerCase(Locale.ENGLISH));
        var biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Biome result = biomeRegistry.get(key);
        if (result == null) {
            addon.logError("Biome " + string + " is invalid! Use one of these...");
            biomeRegistry.stream().sorted(Comparator.comparing(biome -> biome.getKey().getKey()))
                    .forEach(biome -> addon.logError(biome.getKey().getKey()));
            return Biome.PLAINS;
        }
        return result;
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
            int weight;
            if (mobs.isConfigurationSection(entity)) {
                // Object form - a weight plus an optional required version
                ConfigurationSection def = Objects.requireNonNull(mobs.getConfigurationSection(entity));
                if (entryIsForNewerServer(def, "mob " + entity, obPhase)) {
                    continue;
                }
                weight = def.getInt(WEIGHT, 0);
            } else {
                weight = mobs.getInt(entity, 0);
            }
            EntityType et = resolveEntityType(entity.toUpperCase(Locale.ENGLISH));
            if (et == null) {
                addon.logError("Bad entity type in " + obPhase.getPhaseName() + ": " + entity);
                addon.logError("Try one of these...");
                addon.logError(Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable)
                        .filter(EntityType::isAlive).map(EntityType::name).collect(Collectors.joining(",")));
                return;
            }
            processMobEntry(obPhase, entity, et, weight);
        }
    }

    /**
     * Checks the optional required version of an object-form block or mob entry.
     * A gated entry is skipped with a log line so phases can mix content from
     * different Minecraft versions without errors on older servers.
     *
     * @param def     the entry's configuration section
     * @param what    description of the entry for the log message
     * @param obPhase phase being loaded
     * @return true if the entry needs a newer server than this one
     */
    private boolean entryIsForNewerServer(ConfigurationSection def, String what, OneBlockPhase obPhase) {
        String requiredVersion = Objects.toString(def.get(REQUIRED_MC_VERSION), "");
        if (!requiredVersion.isEmpty() && !isVersionAtLeast(Bukkit.getMinecraftVersion(), requiredVersion)) {
            addon.log("Skipping " + what + " in " + obPhase.getPhaseName() + ": it requires Minecraft "
                    + requiredVersion + " or later.");
            return true;
        }
        return false;
    }

    private EntityType resolveEntityType(String name) {
        // Pig zombie handling: accept both legacy and current name
        if (name.equals("PIG_ZOMBIE") || name.equals("ZOMBIFIED_PIGLIN")) {
            return Enums.getIfPresent(EntityType.class, "ZOMBIFIED_PIGLIN")
                    .or(Enums.getIfPresent(EntityType.class, "PIG_ZOMBIE").or(EntityType.PIG));
        }
        return Enums.getIfPresent(EntityType.class, name).orNull();
    }

    private void processMobEntry(OneBlockPhase obPhase, String entity, EntityType et, int weight) {
        if (!et.isSpawnable() || !et.isAlive()) {
            addon.logError("Entity type is not spawnable " + obPhase.getPhaseName() + ": " + entity);
            return;
        }
        if (weight > 0) {
            obPhase.addMob(et, weight);
        } else {
            addon.logWarning("Bad entity weight for " + obPhase.getPhaseName() + ": " + entity
                    + ". Must be positive number above 1.");
        }
    }

    void addBlocks(OneBlockPhase obPhase, ConfigurationSection phase) {
        if (phase.isConfigurationSection(BLOCKS)) {
            ConfigurationSection blocks = phase.getConfigurationSection(BLOCKS);
            for (String material : blocks.getKeys(false)) {
                if (blocks.isConfigurationSection(material)) {
                    // Object form - a weight plus an optional required version
                    ConfigurationSection def = Objects.requireNonNull(blocks.getConfigurationSection(material));
                    if (entryIsForNewerServer(def, "block " + material, obPhase)) {
                        continue;
                    }
                    addMaterial(obPhase, material, Objects.toString(def.get(WEIGHT)));
                } else {
                    addMaterial(obPhase, material, Objects.toString(blocks.get(material)));
                }
            }
        } else if (phase.isList(BLOCKS)) {
            for (Map<?, ?> map : phase.getMapList(BLOCKS)) {
                processBlockMapEntry(obPhase, map);
            }
        }

        // Optional sibling list holding only custom entries. Lets admins keep the
        // existing map-form `blocks:` section untouched while still registering
        // custom block types like `mob-data` or `mythic-mob`.
        if (phase.isList(CUSTOM_BLOCKS)) {
            for (Map<?, ?> map : phase.getMapList(CUSTOM_BLOCKS)) {
                addCustomBlockFromMap(obPhase, map);
            }
        }
    }

    private void processBlockMapEntry(OneBlockPhase obPhase, Map<?, ?> map) {
        if (map.size() == 1) {
            Map.Entry<?, ?> entry = map.entrySet().iterator().next();
            if (addMaterial(obPhase, Objects.toString(entry.getKey()), Objects.toString(entry.getValue()))) {
                return;
            }
        }
        addCustomBlockFromMap(obPhase, map);
    }

    private void addCustomBlockFromMap(OneBlockPhase obPhase, Map<?, ?> map) {
        int probability = Integer.parseInt(Objects.toString(map.get("probability"), "0"));
        Optional<OneBlockCustomBlock> customBlock = OneBlockCustomBlockCreator.create(map);
        if (customBlock.isPresent()) {
            obPhase.addCustomBlock(customBlock.get(), probability);
        } else {
            addon.logError("Bad custom block in " + obPhase.getPhaseName() + ": " + map);
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
                .map(n -> n.replace(" ", "_")).toList();
    }

    /**
     * @return the blockProbs
     */
    public NavigableMap<Integer, OneBlockPhase> getBlockProbs() {
        return blockProbs;
    }

    /**
     * Get phase by name. Name should have any spaces converted to underscores. Case-
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
            if (p.isGotoPhase() && !phaseIndex.isEmpty()) {
                // Indexed goto phases are synthetic - gotoAtEnd in the index covers them
                continue;
            }
            success = savePhase(p);
        }
        if (!phaseIndex.isEmpty()) {
            success = saveIndex() && success;
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
        ConfigurationSection phSec = oneBlocks.createSection(getPhaseSectionKey(p));
        if (p.getRequiredMinecraftVersion() != null) {
            phSec.set(REQUIRED_MC_VERSION, p.getRequiredMinecraftVersion());
        }
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
                phSec.set(BIOME, p.getPhaseBiome().getKey().getKey());
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
        ConfigurationSection phSec = oneBlocks.createSection(getPhaseSectionKey(p));
        if (p.getRequiredMinecraftVersion() != null) {
            phSec.set(REQUIRED_MC_VERSION, p.getRequiredMinecraftVersion());
        }
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
        if (p.getIndexEntry() != null) {
            // The file base name is the phase's identity and stays stable no matter
            // where the phase currently starts
            return p.getIndexEntry().getFile();
        }
        if (p.isGotoPhase()) {
            return p.getBlockNumber() + "_goto_" + p.getGotoBlock();
        }
        return p.getBlockNumber() + "_"
                + (p.getPhaseName() == null ? "" : p.getPhaseName().toLowerCase().replace(' ', '_'));
    }

    /**
     * @return the top-level YAML key to save a phase under - the stable section
     *         key from the index when there is one, otherwise the start block
     */
    private String getPhaseSectionKey(OneBlockPhase p) {
        PhaseIndexEntry indexEntry = p.getIndexEntry();
        String section = indexEntry == null ? null : indexEntry.getSection();
        if (section != null) {
            return section;
        }
        // Block number can be null when the phase came from the database via GSON
        String blockNumber = p.getBlockNumber();
        return blockNumber == null ? "0" : blockNumber;
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
        phase.getFixedBlocks().forEach((k, v) -> {
            String value;
            if (v.getChest() != null && v.getChest().size() == 1 && v.getChest().containsKey(0)) {
                // Serialize as CHEST_WITH_X when there is exactly one item in slot 0
                value = CHEST_WITH_PREFIX + v.getChest().get(0).getType().name();
            } else if (v.getMaterial() != null) {
                value = v.getMaterial().name();
            } else {
                value = Material.CHEST.name();
            }
            fixedBlocks.set(String.valueOf(k), value);
        });
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
        Integer blockNum = Integer.valueOf(phase.getBlockNumber());
        Integer nextKey = blockProbs.ceilingKey(blockNum + 1);
        if (nextKey == null) {
            return;
        }
        int phaseSize = nextKey - blockNum;
        int likelyChestTotal = logBlockProbs(phase, phaseSize);
        if (likelyChestTotal == 0) {
            addon.logWarning("No chests will be generated");
            return;
        }
        addon.log("**** A total of " + likelyChestTotal + " chests will be generated ****");
        logChestProbs(phase, likelyChestTotal);
        logMobProbs(phase, phaseSize);
    }

    /**
     * Logs the probability report for each block type in the phase.
     *
     * @param phase     - the phase to report on
     * @param phaseSize - total number of blocks in the phase
     * @return the likely number of CHEST blocks that will be generated
     */
    private int logBlockProbs(OneBlockPhase phase, int phaseSize) {
        int blockTotal = phase.getBlockTotal();
        int likelyChestTotal = 0;
        double totalBlocks = 0;
        for (Entry<Material, Integer> en : phase.getBlocks().entrySet()) {
            double likelyNumberGenerated = (double) en.getValue() / blockTotal * phaseSize;
            totalBlocks += likelyNumberGenerated;
            logReport(en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = "
                    + Math.round(likelyNumberGenerated * 100 / phaseSize) + "%", likelyNumberGenerated);
            if (en.getKey().equals(Material.CHEST)) {
                likelyChestTotal = (int) Math.round(likelyNumberGenerated);
            }
        }
        addon.log("Total blocks generated = " + totalBlocks);
        return likelyChestTotal;
    }

    /**
     * Logs the probability report for each chest rarity in the phase.
     *
     * @param phase            - the phase to report on
     * @param likelyChestTotal - the likely total number of chests generated
     */
    private void logChestProbs(OneBlockPhase phase, int likelyChestTotal) {
        double lastChance = 0;
        for (Entry<Double, Rarity> en : OneBlockPhase.CHEST_CHANCES.entrySet()) {
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
    }

    /**
     * Logs the probability report for each mob type in the phase.
     *
     * @param phase     - the phase to report on
     * @param phaseSize - total number of blocks in the phase
     */
    private void logMobProbs(OneBlockPhase phase, int phaseSize) {
        addon.log("-=-=-=-= Mobs -=-=-=-=-");
        double totalMobs = 0;
        for (Entry<EntityType, Integer> en : phase.getMobs().entrySet()) {
            double likelyNumberGenerated = (double) en.getValue() / phase.getTotal() * phaseSize;
            totalMobs += likelyNumberGenerated;
            logReport(en.getKey() + " likely generated = " + Math.round(likelyNumberGenerated) + " = "
                    + Math.round(likelyNumberGenerated * 100 / phaseSize) + "%", likelyNumberGenerated);
        }
        addon.log("**** A total of " + Math.round(totalMobs) + " mobs will likely be generated ****");
    }

    /**
     * Logs a report line as a warning if the likely count is below 1, otherwise as
     * a normal log entry.
     *
     * @param report                - the message to log
     * @param likelyNumberGenerated - the computed likelihood
     */
    private void logReport(String report, double likelyNumberGenerated) {
        if (likelyNumberGenerated < 1) {
            addon.logWarning(report);
        } else {
            addon.log(report);
        }
    }

    /**
     * Get all the probs for each phase and log to console
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
