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
 *   data: breeze{Glowing:1b,attributes:[{id:scale,base:2f}]}
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

            String command = buildSummonCommand(data, world, x, y, z);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);

            // Defer MakeSpace by one tick so NBT-driven attributes (e.g.
            // minecraft:scale) have applied before we measure the bounding box.
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                Entity spawned = findRecentlySpawned(spawnLoc);
                if (spawned != null && addon.getSettings().isClearBlocks()) {
                    new MakeSpace(addon).makeSpace(spawned, spawnLoc);
                }
            }, 1L);
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not summon mob-data entity '" + data + "': " + e.getMessage()
                    + " — check that the entity id, NBT keys, and attribute ids are valid for this Minecraft"
                    + " version (1.21 renamed 'minecraft:generic.*' attributes to drop the 'generic.' prefix).");
        }
    }

    /**
     * Builds the {@code execute in <world> run summon <entity> <x> <y> <z> [nbt]}
     * command for the given data string and coordinates.
     * <p>
     * Vanilla summon grammar is {@code summon <entity> <x> <y> <z> [nbt]} — the
     * NBT must come AFTER the coordinates. Gluing it to the entity id (e.g.
     * {@code summon breeze{...} x y z}) makes the command parser throw an
     * "Unhandled exception" in {@code VanillaCommandWrapper}, so we split the
     * data string at the first NBT/component delimiter (<code>{</code> or
     * <code>[</code>) and interleave the coordinates.
     * <p>
     * Package-private so unit tests can verify the resulting command without
     * needing a live server.
     */
    static String buildSummonCommand(String data, String world, String x, String y, String z) {
        int nbtStart = indexOfFirst(data, '{', '[');
        String entityId = nbtStart < 0 ? data : data.substring(0, nbtStart);
        String nbt = nbtStart < 0 ? "" : " " + data.substring(nbtStart);
        return "execute in " + world + " run summon " + entityId + " " + x + " " + y + " " + z + nbt;
    }

    /**
     * Returns the index of the first occurrence of any of the given characters
     * in {@code s}, or -1 if none are present. Used to locate where the NBT
     * portion of a {@code /summon}-style data string begins.
     */
    private static int indexOfFirst(String s, char a, char b) {
        int ai = s.indexOf(a);
        int bi = s.indexOf(b);
        if (ai < 0) return bi;
        if (bi < 0) return ai;
        return Math.min(ai, bi);
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
