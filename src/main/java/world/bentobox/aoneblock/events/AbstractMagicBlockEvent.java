package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Event that is fired when the magic block is broken
 * @author tastybento
 *
 */
public abstract class AbstractMagicBlockEvent extends BentoBoxEvent {

    protected final Island island;
    protected final UUID playerUUID;
    protected final Block block;

    /**
     * @param island - island where the magic block is located
     * @param playerUUID - the player involved
     * @param block - block involved in the event
     */
    protected AbstractMagicBlockEvent(@NonNull Island island, @Nullable UUID playerUUID, @NonNull Block block) {
        super();
        this.island = island;
        this.playerUUID = playerUUID;
        this.block = block;
    }

    /**
     * @return the island where this is happening
     */
    @NonNull
    public Island getIsland() {
        return island;
    }

    /**
     * @return the playerUUID if any involved
     */
    @Nullable
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * @return the magic block
     */
    @NonNull
    public Block getBlock() {
        return block;
    }


}
