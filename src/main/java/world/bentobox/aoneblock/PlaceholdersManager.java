package world.bentobox.aoneblock;

import java.util.Objects;
import java.util.TreeMap;

import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

public class PlaceholdersManager {

    private static final TreeMap<Double, String> SCALE;
    static {
        SCALE = new TreeMap<>();
        SCALE.put(0D,     "&c╍╍╍╍╍╍╍╍");
        SCALE.put(12.5,   "&a╍&c╍╍╍╍╍╍╍");
        SCALE.put(25.0,   "&a╍╍&c╍╍╍╍╍╍");
        SCALE.put(37.5, "&a╍╍╍&c╍╍╍╍╍");
        SCALE.put(50D,    "&a╍╍╍╍&c╍╍╍╍");
        SCALE.put(62.5,  "&a╍╍╍╍╍&c╍╍╍");
        SCALE.put(75.0,   "&a╍╍╍╍╍&c╍╍╍");
        SCALE.put(87.5, "&a╍╍╍╍╍╍╍&c╍");
        SCALE.put(100D,  "&a╍╍╍╍╍╍╍╍");
    }

    private final AOneBlock addon;

    public PlaceholdersManager(AOneBlock addon) {
        this.addon = addon;
    }


    /**
     * Get phase by location of user
     * @param user - user
     * @return Phase name
     */
    public String getPhaseByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(OneBlockIslands::getPhaseName)
                .orElse("");
    }

    /**
     * Get block count by user location
     * @param user - user
     * @return String of count
     */
    public String getCountByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(OneBlockIslands::getBlockNumber)
                .map(String::valueOf)
                .orElse("");
    }

    /**
     * Get user's island phase
     * @param user - island owner or team member
     * @return phase name
     */
    public String getPhase(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        return i == null ? "" : addon.getOneBlocksIsland(i).getPhaseName();
    }

    /**
     * Get island block count
     * @param user island owner or team member
     * @return string of block count
     */
    public String getCount(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        return i == null ? "" : String.valueOf(addon.getOneBlocksIsland(i).getBlockNumber());
    }

    /**
     * Get the next phase based on user's location
     * @param user - user
     * @return next phase
     */
    public String getNextPhaseByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(addon.getOneBlockManager()::getNextPhase)
                .orElse("");
    }

    /**
     * Get next island phase
     * @param user island owner or team member
     * @return next island phase
     */
    public String getNextPhase(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        return i == null ? "" : addon.getOneBlockManager().getNextPhase(addon.getOneBlocksIsland(i));
    }

    /**
     * Get how many blocks until next phase based on user's location
     * @param user user
     * @return string number of blocks
     */
    public String getNextPhaseBlocksByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(addon.getOneBlockManager()::getNextPhaseBlocks)
                .map(num -> num < 0 ? user.getTranslation("aoneblock.placeholders.infinite") : String.valueOf(num))
                .orElse("");
    }

    /**
     * Get how many blocks until the next island phase
     * @param user owner or team member
     * @return string number of blocks
     */
    public String getNextPhaseBlocks(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        if (i == null) {
            return "";
        }
        int num = addon.getOneBlockManager().getNextPhaseBlocks(addon.getOneBlocksIsland(i));
        return num < 0 ? user.getTranslation("aoneblock.placeholders.infinite") : String.valueOf(num);
    }

    /**
     * Get percentage done of current phase by user's location
     * @param user - user
     * @return string percentage
     */
    public String getPercentDoneByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(addon.getOneBlockManager()::getPercentageDone)
                .map(num -> Math.round(num) + "%")
                .orElse("");
    }

    /**
     * Get percentage done of user's island phase
     * @param user owner or team member
     * @return string percentage
     */
    public String getPercentDone(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        if (i == null) {
            return "";
        }
        double num = addon.getOneBlockManager().getPercentageDone(addon.getOneBlocksIsland(i));
        return Math.round(num) + "%";
    }

    /**
     * Get percentage done of phase as colored scale based on user's location
     * @param user user
     * @return colored scale
     */
    public String getDoneScaleByLocation(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        return addon.getIslands().getProtectedIslandAt(Objects.requireNonNull(user.getLocation()))
                .map(addon::getOneBlocksIsland)
                .map(addon.getOneBlockManager()::getPercentageDone)
                .map(num -> SCALE.floorEntry(num).getValue())
                .orElse("");
    }

    /**
     * Get percentage done of phase as colored scale
     * @param user owner or team member
     * @return colored scale
     */
    public String getDoneScale(User user) {
        if (user == null || user.getUniqueId() == null) return "";
        Island i = addon.getIslands().getIsland(addon.getOverWorld(), user);
        if (i == null) {
            return "";
        }
        double num = addon.getOneBlockManager().getPercentageDone(addon.getOneBlocksIsland(i));
        return SCALE.floorEntry(num).getValue();
    }


}
