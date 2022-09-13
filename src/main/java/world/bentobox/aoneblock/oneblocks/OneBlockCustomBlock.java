package world.bentobox.aoneblock.oneblocks;

import org.bukkit.block.Block;

import java.util.Map;

public interface OneBlockCustomBlock {
    boolean isValid(Map<String, Object> map);

    void setBlock(Block block);
}
