package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.database.objects.Island;

/**
 * Event that is fired when a new phase is triggered
 * @author tastybento
 *
 */
public class MagicBlockPhaseEvent extends AbstractMagicBlockEvent {

    protected final String phase;
    protected final String oldPhase;
    protected final int blockNumber;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @param island
     * @param playerUUID
     * @param block
     * @param phase
     * @param blockNumber
     */
    public MagicBlockPhaseEvent(Island island, UUID playerUUID, Block block, String phase, String oldPhase, int blockNumber) {
        super(island, playerUUID, block);
        this.phase = phase;
        this.oldPhase = oldPhase;
        this.blockNumber = blockNumber;
    }
    /**
     * @return the new phase
     */
    @NonNull
    public String getPhase() {
        return phase;
    }
    /**
     * @return the blockNumber
     */
    public int getBlockNumber() {
        return blockNumber;
    }
    /**
     * @return the original phase
     */
    public String getOldPhase() {
        return oldPhase;
    }
}
