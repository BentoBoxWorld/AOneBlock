package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.util.Key;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

public class CraftEngineCustomBlock implements OneBlockCustomBlock {
    private final String blockId;

    public CraftEngineCustomBlock(String blockId) {
        this.blockId = blockId;
    }

    public static Optional<CraftEngineCustomBlock> fromId(String id) {
        CustomBlock block = CraftEngineBlocks.byId(Key.of(id));
        if (block != null) {
            return Optional.of(new CraftEngineCustomBlock(id));
        }
        return Optional.empty();
    }

    public static Optional<CraftEngineCustomBlock> fromMap(Map<?, ?> map) {
        return Optional
                .ofNullable(Objects.toString(map.get("id"), null))
                .flatMap(CraftEngineCustomBlock::fromId);
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Material.AIR);
            CraftEngineBlocks.place(block.getLocation(), Key.of(blockId), false);
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not place CraftEngine block " + blockId + ": " + e.getMessage());
            block.setType(Material.STONE);
        }
    }
}
