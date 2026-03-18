package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.nexomc.nexo.api.NexoBlocks;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

public class NexoCustomBlock implements OneBlockCustomBlock {
    private final String itemId;

    public NexoCustomBlock(String itemId) {
        this.itemId = itemId;
    }

    public static Optional<NexoCustomBlock> fromId(String id) {
        if (NexoBlocks.isCustomBlock(id)) {
            return Optional.of(new NexoCustomBlock(id));
        }
        return Optional.empty();
    }

    public static Optional<NexoCustomBlock> fromMap(Map<?, ?> map) {
        return Optional
                .ofNullable(Objects.toString(map.get("id"), null))
                .flatMap(NexoCustomBlock::fromId);
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Material.AIR);
            NexoBlocks.place(itemId, block.getLocation());
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not place Nexo block " + itemId + " at " + block.getLocation());
            block.setType(Material.STONE);
        }
    }
}
