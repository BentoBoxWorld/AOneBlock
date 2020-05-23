package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.database.objects.Island;

/**
 * Event that is fired when a new phase is triggered
 * @author tastybento
 *
 */
public class MagicBlockPhaseEvent extends AbstractMagicBlockEvent {

    protected final String phase;
    protected final int blockNumber;
    /**
     * @param island
     * @param playerUUID
     * @param block
     * @param phase
     * @param blockNumber
     */
    public MagicBlockPhaseEvent(Island island, UUID playerUUID, Block block, String phase, int blockNumber) {
        super(island, playerUUID, block);
        this.phase = phase;
        this.blockNumber = blockNumber;
    }
    /**
     * @return the phase
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

}
