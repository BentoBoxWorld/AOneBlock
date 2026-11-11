package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.database.objects.Island;

/**
 * Fired when a phase finale ends, either because the boss was defeated or
 * because it left (everyone died or went away, or the boss was removed).
 * @author tastybento
 */
public class PhaseFinaleEndEvent extends AbstractMagicBlockEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String phase;
    private final boolean defeated;
    private final boolean firstTime;

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @param island - island
     * @param playerUUID - player who killed the boss, if any
     * @param block - the magic block
     * @param phase - name of the phase that is ending
     * @param defeated - true if the boss was defeated
     * @param firstTime - true if this is the first time this island defeated this boss
     */
    public PhaseFinaleEndEvent(@NonNull Island island, @Nullable UUID playerUUID, @NonNull Block block,
            @NonNull String phase, boolean defeated, boolean firstTime) {
        super(island, playerUUID, block);
        this.phase = phase;
        this.defeated = defeated;
        this.firstTime = firstTime;
    }

    /**
     * @return the name of the phase that is ending
     */
    @NonNull
    public String getPhase() {
        return phase;
    }

    /**
     * @return true if the boss was defeated, false if the finale was abandoned
     */
    public boolean isDefeated() {
        return defeated;
    }

    /**
     * @return true if this is the first time the island defeated this boss
     */
    public boolean isFirstTime() {
        return firstTime;
    }
}
