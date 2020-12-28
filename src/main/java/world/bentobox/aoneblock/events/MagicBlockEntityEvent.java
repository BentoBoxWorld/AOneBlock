package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.database.objects.Island;

/**
 * Event that is fired when the magic block spawns an entity
 * @author tastybento
 *
 */
public class MagicBlockEntityEvent extends AbstractMagicBlockEvent {

    protected final EntityType entityType;
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
     * @param entityType
     */
    public MagicBlockEntityEvent(Island island, UUID playerUUID, Block block, EntityType entityType) {
        super(island, playerUUID, block);
        this.entityType = entityType;
    }

    /**
     * @return the entityType spawned on the magic block
     */
    @NonNull
    public EntityType getEntityType() {
        return entityType;
    }


}
