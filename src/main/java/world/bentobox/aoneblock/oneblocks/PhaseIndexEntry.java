package world.bentobox.aoneblock.oneblocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * One entry of the phase index (phases_index.yml). The index defines which
 * phases load, in what order, and how many blocks each phase runs for. Start
 * blocks are computed by summing the lengths of the phases loaded before an
 * entry, so entries can be reordered freely and a skipped phase takes up no
 * blocks.
 *
 * @author tastybento
 */
public class PhaseIndexEntry {

    private static final String FILE = "file";
    private static final String SECTION = "section";
    private static final String NAME = "name";
    private static final String LENGTH = "length";
    private static final String ENABLED = "enabled";
    private static final String REQUIRED_MC_VERSION = "requiredMinecraftVersion";

    private String file;
    private String section;
    private String name;
    private int length;
    private boolean enabled = true;
    private String requiredMinecraftVersion;

    /**
     * Reads an entry from its raw YAML map.
     *
     * @param map raw map from the index file
     * @return entry, or null if the map has no file name
     */
    @Nullable
    public static PhaseIndexEntry fromMap(Map<?, ?> map) {
        String file = Objects.toString(map.get(FILE), null);
        if (file == null) {
            return null;
        }
        PhaseIndexEntry entry = new PhaseIndexEntry();
        entry.file = file;
        entry.section = Objects.toString(map.get(SECTION), null);
        entry.name = Objects.toString(map.get(NAME), file);
        entry.length = map.get(LENGTH) instanceof Number number ? number.intValue() : 0;
        entry.enabled = !Boolean.FALSE.equals(map.get(ENABLED));
        String version = Objects.toString(map.get(REQUIRED_MC_VERSION), "");
        entry.requiredMinecraftVersion = version.isEmpty() ? null : version;
        return entry;
    }

    /**
     * @return this entry as a map for writing to the index file
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(FILE, file);
        if (section != null) {
            map.put(SECTION, section);
        }
        map.put(NAME, name);
        map.put(LENGTH, length);
        if (!enabled) {
            map.put(ENABLED, false);
        }
        if (requiredMinecraftVersion != null) {
            map.put(REQUIRED_MC_VERSION, requiredMinecraftVersion);
        }
        return map;
    }

    /**
     * @return base name of the phase file, without the .yml extension. The chest
     *         file is this plus _chests.yml. This is the phase's identity.
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file base name of the phase file, without the .yml extension
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the top-level key inside the phase file, or null to use the file's
     *         first section
     */
    @Nullable
    public String getSection() {
        return section;
    }

    /**
     * @param section the top-level key inside the phase file
     */
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * @return display name, used in logs and admin tools
     */
    public String getName() {
        return name;
    }

    /**
     * @param name display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return number of blocks this phase runs for
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length number of blocks this phase runs for
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return false if the phase should not be loaded
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled false to leave the phase out
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the minimum Minecraft version this phase needs, or null if it can
     *         run on any version
     */
    @Nullable
    public String getRequiredMinecraftVersion() {
        return requiredMinecraftVersion;
    }

    /**
     * @param requiredMinecraftVersion the minimum Minecraft version this phase
     *                                 needs, e.g. "26.2"
     */
    public void setRequiredMinecraftVersion(String requiredMinecraftVersion) {
        this.requiredMinecraftVersion = requiredMinecraftVersion;
    }
}
