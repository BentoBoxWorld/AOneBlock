package world.bentobox.aoneblock.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.events.BlockClearEvent;

/**
 * This class creates a space for entities to spawn. Avoids block damage.
 * @author tastybento
 *
 */
public class MakeSpace {

    /**
     * Water entities
     */
    static final List<EntityType> WATER_ENTITIES = List.of(
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH,
            EntityType.TROPICAL_FISH, EntityType.DROWNED, EntityType.DOLPHIN, EntityType.TADPOLE,
            EntityType.SQUID, EntityType.AXOLOTL, EntityType.GLOW_SQUID);

    /**
     * Main addon class.
     */
    private final AOneBlock addon;


    /**
     * @param addon
     */
    public MakeSpace(AOneBlock addon) {
        this.addon = addon;
    }


    /**
     * This method creates a space for entities to spawn. Avoids block damage.
     * @param entity Entity that is spawned.
     * @param spawnLocation Location where entity is spawned.
     */
    public void makeSpace(@NonNull Entity entity, @NonNull Location spawnLocation)
    {
        World world = entity.getWorld();
        List<Block> airBlocks = new ArrayList<>();
        List<Block> waterBlocks = new ArrayList<>();
        final BoundingBox boundingBox = entity.getBoundingBox();
        final boolean isWaterProtected = this.addon.getSettings().isWaterMobProtection() &&
                WATER_ENTITIES.contains(entity.getType());

        int yStart = (int) Math.floor(boundingBox.getMinY());
        int yEnd = yStart + (int) Math.floor(Math.min(boundingBox.getMaxY(), world.getMaxHeight()) - boundingBox.getMinY());

        for (int y = yStart; y <= yEnd; y++)
        {
            Block block = world.getBlockAt(spawnLocation.getBlockX(), y, spawnLocation.getBlockZ());
            this.checkBlock(block, boundingBox, isWaterProtected, airBlocks, waterBlocks);

            // If entity requires water protection, add an air block above it (dolphin protection).
            if (isWaterProtected && y + 1 < world.getMaxHeight())
            {
                airBlocks.add(block.getRelative(BlockFace.UP));
            }

            processEntityWidth(world, spawnLocation, boundingBox, y, isWaterProtected, airBlocks, waterBlocks);
        }

        BlockClearEvent event = new BlockClearEvent(entity, airBlocks, waterBlocks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
        {
            return;
        }

        airBlocks.forEach(Block::breakNaturally);
        airBlocks.forEach(b -> b.setType(Material.AIR));
        waterBlocks.forEach(this::applyWaterBlock);
    }

    /**
     * Checks blocks in the X and Z dimensions around the entity bounding box.
     * Only runs when the entity is wider than one block in at least one horizontal axis.
     * Block coordinates are derived from the bounding box using integer arithmetic to
     * avoid floating-point increment issues.
     * @param world The world.
     * @param spawnLocation The entity spawn location (used as the fixed axis when only one dimension is wide).
     * @param boundingBox The entity bounding box.
     * @param y The current Y block coordinate.
     * @param isWaterProtected Whether the entity needs water protection.
     * @param airBlocks Accumulator list for blocks to clear with air.
     * @param waterBlocks Accumulator list for blocks to fill with water.
     */
    private void processEntityWidth(World world, Location spawnLocation, BoundingBox boundingBox, int y,
            boolean isWaterProtected, List<Block> airBlocks, List<Block> waterBlocks)
    {
        // Convert the expanded bounding box range (±0.5) to integer block coordinates.
        // floor(min - 0.5) gives the first block; ceil(width + 1) gives the iteration count.
        int xStart = (int) Math.floor(boundingBox.getMinX() - 0.5);
        int xCount = (int) Math.ceil(boundingBox.getMaxX() - boundingBox.getMinX() + 1);
        int zStart = (int) Math.floor(boundingBox.getMinZ() - 0.5);
        int zCount = (int) Math.ceil(boundingBox.getMaxZ() - boundingBox.getMinZ() + 1);

        if (boundingBox.getWidthX() > 1 && boundingBox.getWidthZ() > 1)
        {
            for (int x = xStart; x < xStart + xCount; x++)
            {
                for (int z = zStart; z < zStart + zCount; z++)
                {
                    this.checkBlock(world.getBlockAt(x, y, z), boundingBox, isWaterProtected, airBlocks, waterBlocks);
                }
            }
        }
        else if (boundingBox.getWidthX() > 1)
        {
            for (int x = xStart; x < xStart + xCount; x++)
            {
                this.checkBlock(world.getBlockAt(x, y, spawnLocation.getBlockZ()), boundingBox, isWaterProtected, airBlocks, waterBlocks);
            }
        }
        else if (boundingBox.getWidthZ() > 1)
        {
            for (int z = zStart; z < zStart + zCount; z++)
            {
                this.checkBlock(world.getBlockAt(spawnLocation.getBlockX(), y, z), boundingBox, isWaterProtected, airBlocks, waterBlocks);
            }
        }
    }

    /**
     * Applies water to a block: sets a waterlogged block to waterlogged state,
     * or replaces a non-water block with water.
     * @param block The block to apply water to.
     */
    private void applyWaterBlock(Block block)
    {
        if (block.getBlockData() instanceof Waterlogged waterlogged)
        {
            waterlogged.setWaterlogged(true);
        }
        else
        {
            block.setType(Material.WATER);
        }
    }

    /**
     * This method checks if block bounding box overlaps with entity bounding box and populates lists accordingly.
     * @param block Block that need to be checked.
     * @param boundingBox The bounding box of entity.
     * @param isWaterEntity Boolean that indicate that entity must be water-protected.
     * @param airBlocks List of air blocks.
     * @param waterBlocks List of water blocks.
     */
    private void checkBlock(Block block,
            BoundingBox boundingBox,
            boolean isWaterEntity,
            List<Block> airBlocks,
            List<Block> waterBlocks)
    {
        // Check if block should be marked for destroying.
        if (block.getBoundingBox().overlaps(boundingBox))
        {
            // Only if entity collides with the block.
            airBlocks.add(block);
        }

        // Check if block should be marked for replacing with water.
        if (isWaterEntity)
        {
            if (block.getBlockData() instanceof Waterlogged waterlogged)
            {
                // Check if waterlogged block collides.
                if (block.getBoundingBox().overlaps(boundingBox) || !waterlogged.isWaterlogged())
                {
                    // if block overlaps with entity, it will be replaced with air.
                    // if block is not waterlogged, put water in it.
                    waterBlocks.add(block);
                }
            }
            else if (block.getType() != Material.WATER)
            {
                // Well, unfortunately block must go.
                waterBlocks.add(block);
            }
        }
    }
}
