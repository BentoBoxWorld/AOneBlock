package world.bentobox.aoneblock.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This listener class provides protection for the OneBlock,
 * preventing it from being destroyed or moved by various means.
 */
public class BlockProtect implements Listener {

    /** The color green for particles. */
    public static final Color GREEN = Color.fromBGR(0, 100, 0);
    /** The list of particles to be used for sparkles. */
    private static final List<Particle> PARTICLES = new ArrayList<>(List.of(Particle.DUST));
    /** An iterator for the particle list to cycle through them. */
    private Iterator<Particle> particleIterator = Collections.emptyIterator();

    /** The AOneBlock addon instance. */
    private final AOneBlock addon;

    /**
     * @param addon - AOneBlock addon
     */
    public BlockProtect(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * Show particles when block is hit
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(PlayerInteractEvent e) {
        Action action = e.getAction();
        String clickType = addon.getSettings().getClickType();

        // Exit if click type is NONE, player is not in a OneBlock world, or no block was clicked.
        if (clickType.equalsIgnoreCase("NONE") || !addon.inWorld(e.getPlayer().getWorld())
                || e.getClickedBlock() == null) {
            return;
        }

        // Check if the action matches the configured click type.
        if ((action == Action.LEFT_CLICK_BLOCK && clickType.equalsIgnoreCase("LEFT"))
                || (action == Action.RIGHT_CLICK_BLOCK && clickType.equalsIgnoreCase("RIGHT"))) {

            Location l = e.getClickedBlock().getLocation();
            // If the clicked block is a OneBlock, show sparkles.
            addon.getIslands().getIslandAt(l).map(Island::getCenter).filter(center -> center.equals(l))
                    .ifPresent(this::showSparkles);
        }
    }

    /**
     * Spawns particles around a given location to create a sparkle effect.
     * @param location The location to spawn particles at.
     */
    public void showSparkles(Location location) {
        // Reset the particle iterator if it has been exhausted.
        if (!particleIterator.hasNext()) {
            Collections.shuffle(PARTICLES);
            particleIterator = PARTICLES.iterator();
        }
        Particle p = particleIterator.next();
        // Iterate over a 2x1.5x2 box around the block to spawn particles.
        for (double x = -0.5; x <= 1.5; x += addon.getSettings().getParticleDensity()) {
            for (double y = 0.0; y <= 1.5; y += addon.getSettings().getParticleDensity()) {
                for (double z = -0.5; z < 1.5; z += addon.getSettings().getParticleDensity()) {
                        // Spawn a dust particle with the configured color and size.
                        location.getWorld().spawnParticle(p, location.clone().add(new Vector(x, y, z)), 5,
                                0.1, 0, 0.1, 1, new Particle.DustOptions(addon.getSettings().getParticleColor(),
                                    addon.getSettings().getParticleSize().floatValue()));

                }
            }
        }
    }

    /**
     * Prevent entities other than players changing the magic block
     * @param e - EntityChangeBlockEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockChange(final EntityChangeBlockEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        // If the block being changed is a OneBlock, cancel the event.
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> e.setCancelled(true));
    }

    /**
     * Blocks oneblocks from being blown up
     * @param e - EntityExplodeEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
        if (!addon.inWorld(e.getLocation().getWorld())) {
            return;
        }
        // Remove any OneBlock from the list of blocks to be exploded.
        e.blockList().removeIf(b -> addon.getIslands().getIslandAt(b.getLocation()).filter(i -> b.getLocation().equals(i.getCenter())).isPresent());
    }

    /**
     * Prevent block from being pushed by a piston
     * @param e - BlockPistonExtendEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        checkPiston(e, e.getBlock(), e.getBlocks());
    }
    /**
     * Prevent block from being pulled by a sticky piston
     * @param e - BlockPistonRetractEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        checkPiston(e, e.getBlock(), e.getBlocks());
    }

    /**
     * Checks if a piston action (extend or retract) would move a OneBlock and cancels it if so.
     * @param e The cancellable piston event.
     * @param block The piston block.
     * @param blocks The list of blocks that would be moved.
     */
    private void checkPiston(Cancellable e, Block block, List<Block> blocks) {
        if (!addon.inWorld(block.getWorld())) {
            return;
        }
        Location l = block.getLocation();
        addon.getIslands().getIslandAt(l).map(Island::getCenter).ifPresent(c -> e.setCancelled(
                // Run through the location of all the relative blocks and see if they match the oneblock location
                blocks.stream().map(Block::getLocation).anyMatch(loc -> loc.equals(c)))
                );
    }

    /**
     * Prevent falling blocks from happening
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFallingBlockSpawn(EntitySpawnEvent e) {
        if (!e.getEntityType().equals(EntityType.FALLING_BLOCK) || !addon.inWorld(e.getEntity().getWorld())) {
            return;
        }
        Location l = e.getLocation();
        // Dropped blocks do not spawn on integer locations, so we have to check block values independently.
        // If the falling block is at the location of a OneBlock, cancel the spawn.
        addon.getIslands().getIslandAt(l).filter(i -> l.getBlockX() == i.getCenter().getBlockX()
                && l.getBlockY() == i.getCenter().getBlockY()
                && l.getBlockZ() == i.getCenter().getBlockZ()
                ).ifPresent(i -> e.setCancelled(true));
    }


}
