package world.bentobox.aoneblock.oneblocks;

import org.bukkit.block.Block;
import world.bentobox.aoneblock.AOneBlock;

/**
 * Represents a custom block with custom executable
 *
 * @author HSGamer
 */
public interface OneBlockCustomBlock {
    /**
     * Executes the custom logic
     *
     * @param block the block
     */
    void execute(AOneBlock addon, Block block);
}
