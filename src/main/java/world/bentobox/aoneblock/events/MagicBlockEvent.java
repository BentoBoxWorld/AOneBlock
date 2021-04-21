package world.bentobox.aoneblock.events;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.database.objects.Island;

/**
 * Event that is fired when the magic block is broken
 * @author tastybento
 *
 */
public class MagicBlockEvent extends AbstractMagicBlockEvent {

    protected final ItemStack tool;
    protected final Material nextBlockMaterial;

    /**
     * @param island - island where the magic block is located
     * @param playerUUID - the player involved
     * @param tool
     * @param block
     * @param nextBlockMaterial
     */
    public MagicBlockEvent(@NonNull Island island, @Nullable UUID playerUUID, @Nullable ItemStack tool, @NonNull Block block, @Nullable Material nextBlockMaterial) {
        super(island, playerUUID, block);
        this.tool = tool;
        this.nextBlockMaterial = nextBlockMaterial;
    }

    /**
     * @return the tool used. May be AIR if hands are used
     */
    public ItemStack getTool() {
        return tool;
    }

    /**
     * @return the next Block Material that will be spawned. May be null if an entity is to be spawned
     */
    @Nullable
    public Material getNextBlockMaterial() {
        return nextBlockMaterial;
    }

}
