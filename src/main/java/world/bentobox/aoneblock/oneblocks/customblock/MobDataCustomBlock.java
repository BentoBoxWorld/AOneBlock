package world.bentobox.aoneblock.oneblocks.customblock;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.listeners.MakeSpace;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;

/**
 * A custom block that summons an entity using a Minecraft-format NBT data
 * string (i.e. the same syntax as the vanilla {@code /summon} command).
 * <p>
 * Example YAML entry:
 *
 * <pre>
 * - type: mob-data
 *   data: minecraft:breeze{Glowing:1b,attributes:[{id:"minecraft:generic.scale",base:2}]}
 *   underlying-block: STONE
 *   probability: 15
 * </pre>
 *
 * After summoning, the spawned entity's bounding box is measured one tick
 * later and {@link MakeSpace} is invoked so that scaled/oversized hitboxes
 * clear the surrounding blocks. See BentoBoxWorld/AOneBlock#488.
 */
public class MobDataCustomBlock implements OneBlockCustomBlock {

    private final String data;
    private final Material underlyingBlock;

    public MobDataCustomBlock(@NonNull String data, @Nullable Material underlyingBlock) {
        this.data = data;
        this.underlyingBlock = underlyingBlock;
    }

    public static Optional<MobDataCustomBlock> fromMap(Map<?, ?> map) {
        String data = Objects.toString(map.get("data"), null);
        if (data == null) {
            return Optional.empty();
        }

        String underlyingBlockValue = Objects.toString(map.get("underlying-block"), null);
        Material underlyingBlock = underlyingBlockValue == null ? null : Material.getMaterial(underlyingBlockValue);
        if (underlyingBlockValue != null && underlyingBlock == null) {
            BentoBox.getInstance().logWarning("Underlying block " + underlyingBlockValue
                    + " does not exist and will be replaced with STONE.");
        }

        return Optional.of(new MobDataCustomBlock(data, underlyingBlock));
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Objects.requireNonNullElse(underlyingBlock, Material.STONE));

            Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));
            String world = "minecraft:" + spawnLoc.getWorld().getName();
            // Vanilla summon coordinates accept decimals.
            String x = String.valueOf(spawnLoc.getX());
            String y = String.valueOf(spawnLoc.getY());
            String z = String.valueOf(spawnLoc.getZ());
            String command = "execute in " + world + " run summon " + data + " " + x + " " + y + " " + z;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);

            // Defer MakeSpace by one tick so NBT-driven attributes (e.g.
            // minecraft:generic.scale) have applied before we measure the bounding box.
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                Entity spawned = findRecentlySpawned(spawnLoc);
                if (spawned != null && addon.getSettings().isClearBlocks()) {
                    new MakeSpace(addon).makeSpace(spawned, spawnLoc);
                }
            }, 1L);
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not summon mob-data entity '" + data + "': " + e.getMessage());
        }
    }

    /**
     * Finds the most-recently-spawned living entity near the given location.
     * Skips players, then picks the entity with the lowest {@code getTicksLived()}
     * (i.e. the newest).
     */
    @Nullable
    private Entity findRecentlySpawned(Location spawnLoc) {
        return spawnLoc.getWorld().getNearbyEntities(spawnLoc, 1.5D, 2D, 1.5D).stream()
                .filter(e -> !(e instanceof Player))
                .filter(LivingEntity.class::isInstance)
                .min(Comparator.comparingInt(Entity::getTicksLived))
                .orElse(null);
    }

    public String getData() {
        return data;
    }

    public Material getUnderlyingBlock() {
        return underlyingBlock;
    }
}
