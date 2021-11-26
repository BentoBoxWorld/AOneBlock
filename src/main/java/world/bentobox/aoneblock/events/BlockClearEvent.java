package world.bentobox.aoneblock.events;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import world.bentobox.bentobox.api.events.BentoBoxEvent;

/**
 * Event that is fired when the addon clears blocks when the magic block spawns an entity.
 * @author tastybento
 *
 */
public class BlockClearEvent extends BentoBoxEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    protected final Entity entity;
    protected final List<Block> airBlocks;
    protected final List<Block> waterBlocks;
    protected boolean cancel;

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Event that is fired when the addon clears blocks when the magic block spawns an entity.
     * Blocks cleared are the size of the entity's hitbox, so can be any size depending on the
     * entity. Blocks are cleared by calling the Bukkit breakNaturally block method, then set to air.
     * If the entity is a water mob then after breaking the blocks, the blocks are set to water
     * and a single block layer of air above will be created (dolphins need air).
     * Canceling the event will prevent all of this. Removing a block from the block list will
     * prevent it happening to that block.
     * @param entity entity spawning
     * @param airBlocks air blocks that will be created around it.
     * @param waterBlocks water blocks that will be created, if entity is a water mob
     */
    public BlockClearEvent(Entity entity, List<Block> airBlocks, List<Block> waterBlocks) {
        this.entity = entity;
        this.airBlocks = airBlocks;
        this.waterBlocks = waterBlocks;
    }

    /**
     * This is the entity that is spawning
     * @return the entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * These blocks will be set to air
     * @return the airBlocks
     */
    public List<Block> getAirBlocks() {
        return airBlocks;
    }

    /**
     * These blocks will be set to water
     * @return the waterBlocks
     */
    public List<Block> getWaterBlocks() {
        return waterBlocks;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;

    }


}
