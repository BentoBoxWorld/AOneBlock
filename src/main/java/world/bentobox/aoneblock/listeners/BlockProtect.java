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

public class BlockProtect implements Listener {

    public static final Color GREEN = Color.fromBGR(0, 100, 0);
    private static final List<Particle> PARTICLES = new ArrayList<>(List.of(Particle.DUST));
    private Iterator<Particle> particleIterator = Collections.emptyIterator();

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

        if (clickType.equalsIgnoreCase("NONE") || !addon.inWorld(e.getPlayer().getWorld())
                || e.getClickedBlock() == null) {
            return;
        }

        if ((action == Action.LEFT_CLICK_BLOCK && clickType.equalsIgnoreCase("LEFT"))
                || (action == Action.RIGHT_CLICK_BLOCK && clickType.equalsIgnoreCase("RIGHT"))) {

            Location l = e.getClickedBlock().getLocation();
            addon.getIslands().getIslandAt(l).map(Island::getCenter).filter(center -> center.equals(l))
                    .ifPresent(this::showSparkles);
        }
    }

    public void showSparkles(Location location) {
        if (!particleIterator.hasNext()) {
            Collections.shuffle(PARTICLES);
            particleIterator = PARTICLES.iterator();
        }
        Particle p = particleIterator.next();
        for (double x = -0.5; x <= 1.5; x += addon.getSettings().getParticleDensity()) {
            for (double y = 0.0; y <= 1.5; y += addon.getSettings().getParticleDensity()) {
                for (double z = -0.5; z < 1.5; z += addon.getSettings().getParticleDensity()) {
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
        // Dropped blocks do not spawn on integer locations, so we have to check block values independently
        addon.getIslands().getIslandAt(l).filter(i -> l.getBlockX() == i.getCenter().getBlockX()
                && l.getBlockY() == i.getCenter().getBlockY()
                && l.getBlockZ() == i.getCenter().getBlockZ()
                ).ifPresent(i -> e.setCancelled(true));
    }


}
