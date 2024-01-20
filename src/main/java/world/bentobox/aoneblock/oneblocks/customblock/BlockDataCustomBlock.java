package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

/**
 * A custom block that is defined by a block data value.
 *
 * @author HSGamer
 */
public class BlockDataCustomBlock implements OneBlockCustomBlock {
    private final String blockData;

    public BlockDataCustomBlock(String blockData) {
        this.blockData = blockData;
    }

    public static Optional<BlockDataCustomBlock> fromMap(Map<?, ?> map) {
        String type = Objects.toString(map.get("data"), null);
        if (type == null) {
            return Optional.empty();
        }
        return Optional.of(new BlockDataCustomBlock(type));
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setBlockData(Bukkit.createBlockData(blockData));
        } catch (IllegalArgumentException e) {
            BentoBox.getInstance().logError("Could not set block data " + blockData + " for block " + block.getType());
        }
    }
}
