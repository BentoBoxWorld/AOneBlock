package world.bentobox.aoneblock.oneblocks.customblock;

import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.listeners.MakeSpace;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

import java.util.*;

/**
 * A custom block that spawns mob on an underlying block
 *
 * @author Baterka
 */
public class MobCustomBlock implements OneBlockCustomBlock {
    private final EntityType mob;
    private final Material underlyingBlock;

    public MobCustomBlock(EntityType mob, Material underlyingBlock) {
        this.mob = mob;
        this.underlyingBlock = underlyingBlock;
    }

    public static Optional<MobCustomBlock> fromMap(Map<?, ?> map) {
        String entityTypeValue = Objects.toString(map.get("mob"), null);
        String underlyingBlockValue = Objects.toString(map.get("underlying-block"), null);

        EntityType entityType = maybeEntity(entityTypeValue);
        Material underlyingBlock = Material.getMaterial(underlyingBlockValue);

        if(underlyingBlock == null){
            BentoBox.getInstance().logWarning("Underlying block " + underlyingBlockValue + " does not exist and will be replaced with STONE.");
        }

        return Optional.of(new MobCustomBlock(entityType, underlyingBlock));
    }

    private static EntityType maybeEntity(String entityTypeValue) {
        String name = entityTypeValue.toUpperCase(Locale.ENGLISH);
        EntityType et;

        // Pig zombie handling
        if (name.equals("PIG_ZOMBIE") || name.equals("ZOMBIFIED_PIGLIN")) {
            et = Enums.getIfPresent(EntityType.class, "ZOMBIFIED_PIGLIN")
                    .or(Enums.getIfPresent(EntityType.class, "PIG_ZOMBIE").or(EntityType.PIG));
        } else {
            et = Enums.getIfPresent(EntityType.class, name).orNull();
        }

        if (et == null) {
            // Does not exist
            BentoBox.getInstance().logWarning("Entity " + name + " does not exist and will not spawn when block is shown.");
            return null;
        }
        if (et.isSpawnable() && et.isAlive()) {
            return et;
        } else {
            // Not spawnable
            BentoBox.getInstance().logWarning("Entity " + et.name() + " is not spawnable and will not spawn when block is shown.");
            return null;
        }
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Objects.requireNonNullElse(underlyingBlock, Material.STONE));
            spawnEntity(addon, block, mob);
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not spawn entity " + mob.name() + " on block " + block.getType());
        }
    }

    private void spawnEntity(AOneBlock addon, @NonNull Block block, @NonNull EntityType mob) {
        Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));
        Entity entity = block.getWorld().spawnEntity(spawnLoc, mob);
        // Make space for entity - this will blot out blocks
        if (addon.getSettings().isClearBlocks()) {
            new MakeSpace(addon).makeSpace(entity, spawnLoc);
        }
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
    }

    public EntityType getMob() {
        return mob;
    }
}
