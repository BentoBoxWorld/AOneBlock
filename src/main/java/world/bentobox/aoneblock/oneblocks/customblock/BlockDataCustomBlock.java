package world.bentobox.aoneblock.oneblocks.customblock;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        if (map.containsKey("data")) {
            return Optional.of(new BlockDataCustomBlock(Objects.toString(map.get("data"))));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setBlock(Block block) {
        try {
            block.setBlockData(Bukkit.createBlockData(blockData));
        } catch (IllegalArgumentException e) {
            BentoBox.getInstance().logError("Could not set block data " + blockData + " for block " + block.getType());
        }
    }
}
