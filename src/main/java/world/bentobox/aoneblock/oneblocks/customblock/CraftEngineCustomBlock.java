package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Map;
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

    /**
     * Checks whether {@code id} is a syntactically valid namespaced key
     * ({@code namespace:key} with both parts non-blank).
     */
    static boolean isValidNamespacedKey(String id) {
        int colon = id.indexOf(':');
        if (colon <= 0 || colon == id.length() - 1) {
            return false;
        }
        String namespace = id.substring(0, colon);
        String key = id.substring(colon + 1);
        return !namespace.isBlank() && !key.isBlank();
    }

    public static Optional<CraftEngineCustomBlock> fromMap(Map<?, ?> map) {
        Object raw = map.get("id");
        if (!(raw instanceof String id)) {
            return Optional.empty();
        }
        if (id.isBlank() || !isValidNamespacedKey(id)) {
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
