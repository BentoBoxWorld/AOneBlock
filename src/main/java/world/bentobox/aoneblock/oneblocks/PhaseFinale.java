package world.bentobox.aoneblock.oneblocks;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The boss encounter that ends a phase. Defined by the optional {@code finale:}
 * section of a phase file.
 *
 * @param boss              entity type of the boss. If {@code mythicBoss} is set this
 *                          is the fallback used when the MythicMob cannot be spawned,
 *                          and may be null for no fallback
 * @param name              display name of the boss, MiniMessage or legacy codes
 * @param health            base maximum health, before scaling for players, or 0 or
 *                          less to keep the mob's own maximum health
 * @param scale             size multiplier, 1 is normal size, or 0 or less to keep the
 *                          mob's own size
 * @param damage            attack damage, or 0 or less to keep the mob's default
 * @param speed             movement speed, or 0 or less to keep the mob's default
 * @param voidRespawn       if true a boss that falls into the void returns to the
 *                          magic block; if false the fall kills it and counts as a win
 * @param warning           how many blocks before the end of the phase the omen starts
 * @param minions           mobs that spawn alongside the boss, and how many of each
 * @param drops             items dropped at the magic block when the boss is defeated
 * @param commands          commands run every time the boss is defeated
 * @param firstTimeCommands commands run only the first time the island defeats this boss
 * @param mythicBoss        MythicMobs mob type id to spawn as the boss, or empty for a
 *                          vanilla boss
 * @param mythicLevel       level of the MythicMobs boss and minions
 * @param mythicMinions     MythicMobs mobs, by type id, that spawn alongside the boss,
 *                          and how many of each
 * @author tastybento
 */
public record PhaseFinale(@Nullable EntityType boss, String name, double health, double scale, double damage,
        double speed, boolean voidRespawn, int warning, Map<EntityType, Integer> minions,
        Map<Material, Integer> drops, List<String> commands, List<String> firstTimeCommands, String mythicBoss,
        double mythicLevel, Map<String, Integer> mythicMinions) {

    /**
     * Default number of blocks of forewarning.
     */
    public static final int DEFAULT_WARNING = 20;

    /**
     * Defensive copies so the record is immutable.
     */
    public PhaseFinale {
        minions = Map.copyOf(minions);
        drops = Map.copyOf(drops);
        commands = List.copyOf(commands);
        firstTimeCommands = List.copyOf(firstTimeCommands);
        mythicBoss = mythicBoss == null ? "" : mythicBoss.trim();
        mythicMinions = mythicMinions == null ? Map.of() : Map.copyOf(mythicMinions);
        if (boss == null && mythicBoss.isEmpty()) {
            throw new IllegalArgumentException("A finale needs a boss or a mythic-boss");
        }
    }

    /**
     * A finale with a vanilla boss.
     */
    public PhaseFinale(EntityType boss, String name, double health, double scale, double damage, double speed,
            boolean voidRespawn, int warning, Map<EntityType, Integer> minions, Map<Material, Integer> drops,
            List<String> commands, List<String> firstTimeCommands) {
        this(boss, name, health, scale, damage, speed, voidRespawn, warning, minions, drops, commands,
                firstTimeCommands, "", 1D, Map.of());
    }

    /**
     * @return true if the boss is a MythicMobs mob
     */
    public boolean isMythic() {
        return !mythicBoss.isEmpty();
    }
}
