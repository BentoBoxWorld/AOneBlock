package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.oneblocks.PhaseFinale;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Fired when a phase finale is about to start, before the boss spawns.
 * Cancelling it waives the finale: no boss spawns and the island may move on
 * to the next phase.
 * @author tastybento
 */
public class PhaseFinaleStartEvent extends AbstractMagicBlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final String phase;
    private final PhaseFinale finale;
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @param island - island
     * @param playerUUID - player who triggered the finale
     * @param block - the magic block
     * @param phase - name of the phase that is ending
     * @param finale - the finale definition
     */
    public PhaseFinaleStartEvent(@NonNull Island island, @Nullable UUID playerUUID, @NonNull Block block,
            @NonNull String phase, @NonNull PhaseFinale finale) {
        super(island, playerUUID, block);
        this.phase = phase;
        this.finale = finale;
    }

    /**
     * @return the name of the phase that is ending
     */
    @NonNull
    public String getPhase() {
        return phase;
    }

    /**
     * @return the finale definition
     */
    @NonNull
    public PhaseFinale getFinale() {
        return finale;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
