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
    void makeSpace(@NonNull Entity entity, @NonNull Location spawnLocation)
    {
        World world = entity.getWorld();
        List<Block> airBlocks = new ArrayList<>();
        List<Block> waterBlocks = new ArrayList<>();
        // Make space for entity based on the entity's size
        final BoundingBox boundingBox = entity.getBoundingBox();
        final boolean isWaterProtected = this.addon.getSettings().isWaterMobProtection() &&
                WATER_ENTITIES.contains(entity.getType());

        for (double y = boundingBox.getMinY(); y <= Math.min(boundingBox.getMaxY(), world.getMaxHeight()); y++)
        {
            // Start with middle block.
            Block block = world.getBlockAt(new Location(world, spawnLocation.getBlockX(), y, spawnLocation.getBlockZ()));

            // Check if block must be replaced with air or water.
            this.checkBlock(block, boundingBox, isWaterProtected, airBlocks, waterBlocks);

            // If entity requires water protection, then add air block above it. Dolphin protection.
            if (isWaterProtected)
            {
                // Look up only if possible
                if (y + 1 < world.getMaxHeight())
                {
                    airBlocks.add(block.getRelative(BlockFace.UP));
                }
            }

            // Process entity width and depth.
            if (boundingBox.getWidthX() > 1 && boundingBox.getWidthZ() > 1)
            {
                // Entities are spawned in the middle of block. So add extra half block to both dimensions.
                for (double x = boundingBox.getMinX() - 0.5; x < boundingBox.getMaxX() + 0.5; x++)
                {
                    for (double z = boundingBox.getMinZ() - 0.5; z < boundingBox.getMaxZ() + 0.5; z++)
                    {
                        block = world.getBlockAt(new Location(world,
                                x,
                                y,
                                z));

                        // Check if block should be marked.
                        this.checkBlock(block, boundingBox, isWaterProtected, airBlocks, waterBlocks);
                    }
                }
            }
            else if (boundingBox.getWidthX() > 1)
            {
                // If entity is just wider, then check the one dimension.
                for (double x = boundingBox.getMinX() - 0.5; x < boundingBox.getMaxX() + 0.5; x++)
                {
                    block = world.getBlockAt(new Location(world,
                            x,
                            y,
                            spawnLocation.getZ()));

                    // Check if block should be marked.
                    this.checkBlock(block, boundingBox, isWaterProtected, airBlocks, waterBlocks);
                }
            }
            else if (boundingBox.getWidthZ() > 1)
            {
                // If entity is just wider, then check the one dimension.
                for (double z = boundingBox.getMinZ() - 0.5; z < boundingBox.getMaxZ() + 0.5; z++)
                {
                    block = world.getBlockAt(new Location(world,
                            spawnLocation.getX(),
                            y,
                            z));

                    // Check if block should be marked.
                    this.checkBlock(block, boundingBox, isWaterProtected, airBlocks, waterBlocks);
                }
            }
        }

        // Fire event
        BlockClearEvent event = new BlockClearEvent(entity, airBlocks, waterBlocks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
        {
            // Event is cancelled. Blocks cannot be removed.
            return;
        }

        // Break blocks.
        airBlocks.forEach(Block::breakNaturally);
        airBlocks.forEach(b -> b.setType(Material.AIR));
        waterBlocks.forEach(block -> {
            if (block.getBlockData() instanceof Waterlogged waterlogged)
            {
                // If block was not removed and is waterlogged, then it means it can be just waterlogged.
                waterlogged.setWaterlogged(true);
            }
            else
            {
                // Replace block with water.
                block.setType(Material.WATER);
            }
        });
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
