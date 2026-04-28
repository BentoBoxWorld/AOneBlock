package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.hooks.CraftEngineHook;

public class CraftEngineCustomBlock implements OneBlockCustomBlock {
    private final String blockId;

    public CraftEngineCustomBlock(String blockId) {
        this.blockId = blockId;
    }

    public static Optional<CraftEngineCustomBlock> fromId(String id) {
        if (CraftEngineHook.exists(id)) {
            return Optional.of(new CraftEngineCustomBlock(id));
        }
        return Optional.empty();
    }

    public static Optional<CraftEngineCustomBlock> fromMap(Map<?, ?> map) {
        String id = Objects.toString(map.get("id"), null);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(new CraftEngineCustomBlock(id));
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Material.AIR);
            if (!CraftEngineHook.placeBlock(block.getLocation(), blockId)) {
                BentoBox.getInstance().logError("Could not place CraftEngine block " + blockId);
                block.setType(Material.STONE);
            }
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not place CraftEngine block " + blockId + ": " + e.getMessage());
            block.setType(Material.STONE);
        }
    }
}
