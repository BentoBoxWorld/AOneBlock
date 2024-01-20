package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;

import dev.lone.itemsadder.api.CustomBlock;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

public class ItemsAdderCustomBlock implements OneBlockCustomBlock {
    private final CustomBlock customBlock;

    public ItemsAdderCustomBlock(CustomBlock customBlock) {
        this.customBlock = customBlock;
    }

    public static Optional<ItemsAdderCustomBlock> fromId(String id) {
        return Optional.ofNullable(CustomBlock.getInstance(id)).map(ItemsAdderCustomBlock::new);
    }

    public static Optional<ItemsAdderCustomBlock> fromMap(Map<?, ?> map) {
        return Optional
                .ofNullable(Objects.toString(map.get("id"), null))
                .flatMap(ItemsAdderCustomBlock::fromId);
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Material.AIR);
            customBlock.place(block.getLocation());
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not place custom block " + customBlock.getId() + " for block " + block.getType());
            block.setType(Material.STONE);
        }
    }
}
