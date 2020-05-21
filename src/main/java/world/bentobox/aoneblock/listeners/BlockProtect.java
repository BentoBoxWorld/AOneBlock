package world.bentobox.aoneblock.listeners;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.database.objects.Island;

public class BlockProtect implements Listener {

    AOneBlock addon;

    /**
     * @param addon
     */
    public BlockProtect(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * Show particles when block is hit
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Block block = e.getBlock();
        Location l = block.getLocation();
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i ->
        block.getWorld().spawnParticle(Particle.REDSTONE, l.add(new Vector(0.5, 1.0, 0.5)), 5, 0.1, 0, 0.1, 1, new Particle.DustOptions(Color.fromBGR(0,100,0), 1)));
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
        addon.getIslands().getIslandAt(l).filter(i -> l.equals(i.getCenter())).ifPresent(i -> e.setCancelled(true));
    }

    /**
     * Blocks oneblocks from being blown up
     * @param e - EntityExplodeEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
        if (!addon.inWorld(e.getEntity().getWorld())) {
            return;
        }
        e.blockList().removeIf(b -> addon.getIslands().getIslandAt(b.getLocation()).filter(i -> b.getLocation().equals(i.getCenter())).isPresent());
    }

    /**
     * Prevent block from being pushed by a piston
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        addon.getIslands().getIslandAt(l).map(Island::getCenter).ifPresent(c -> e.setCancelled(
                // Run through the location of all the relative blocks and see if they match the oneblock location
                e.getBlocks().stream().map(Block::getLocation).anyMatch(loc -> loc.equals(c)))
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
        // Dropped blocks do not spawn on integer locations, so we have to check block values independently
        addon.getIslands().getIslandAt(l).filter(i -> l.getBlockX() == i.getCenter().getBlockX()
                && l.getBlockY() == i.getCenter().getBlockY()
                && l.getBlockZ() == i.getCenter().getBlockZ()
                ).ifPresent(i -> e.setCancelled(true));
    }


}
