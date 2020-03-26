package world.bentobox.oneblock.listeners;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.events.island.IslandEvent.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.oneblock.OneBlock;

/**
 * @author tastybento
 *
 */
public class BlockListener implements Listener {

    private OneBlock addon;
    private BlockProbability blockProbs;
    private Random r = new Random();

    /**
     * @param addon - OneBlock
     */
    public BlockListener(OneBlock addon) {
        this.addon = addon;
        // Create a random list of blocks
        blockProbs = new BlockProbability();
        Arrays.stream(Material.values()).filter(Material::isBlock)
        .filter(Material::isSolid)
        .filter(m -> !m.equals(Material.BEDROCK))
        .filter(m -> !m.name().contains("BANNER"))
        .filter(m -> !m.name().contains("FENCE"))
        .filter(m -> !m.name().contains("BED"))
        .filter(m -> !m.name().contains("PANE"))
        .filter(m -> !m.name().contains("GATE"))
        .filter(m -> !m.name().contains("BELL"))
        .filter(m -> !m.name().contains("DOOR"))
        .filter(m -> !m.name().contains("ICE"))
        .filter(m -> !m.equals(Material.AIR))
        .forEach(m -> blockProbs.addBlock(m, r.nextInt(100)));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandCreatedEvent e) {
        setUp(e.getIsland());
    }

    private void setUp(Island island) {
        // Set the bedrock to the initial block
        island.getCenter().getBlock().setType(Material.GRASS_BLOCK);
        Player p = Bukkit.getPlayer(island.getOwner());
        if (p.isOnline()) p.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandResettedEvent e) {
        setUp(e.getIsland());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        Location pl = e.getPlayer().getLocation();
        addon.getIslands().getIslandAt(e.getBlock().getLocation()).filter(i -> l.equals(i.getCenter())).ifPresent(i -> {
            e.setCancelled(true);
            e.getBlock().getWorld().dropItem(e.getPlayer().getLocation(), new ItemStack(e.getBlock().getType()));
            e.getBlock().setType(blockProbs.getBlock(r, true, true), false);
            if (pl.getBlockX() == l.getBlockX() && pl.getBlockY() == l.getBlockY() && pl.getBlockZ() == l.getBlockZ()) {
                e.getPlayer().teleport(e.getBlock().getRelative(BlockFace.UP).getLocation());
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e) {
        if (!addon.inWorld(e.getBlock().getWorld())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        Location pl = e.getPlayer().getLocation();
        addon.getIslands().getIslandAt(e.getBlock().getLocation()).filter(i -> l.equals(i.getCenter())).ifPresent(i -> {
            e.getBlock().getWorld().dropItem(e.getPlayer().getLocation(), new ItemStack(e.getBlock().getType()));
            e.getBlock().setType(blockProbs.getBlock(r, true, true), false);
            if (pl.getBlockX() == l.getBlockX() && pl.getBlockY() == l.getBlockY() && pl.getBlockZ() == l.getBlockZ()) {
                e.getPlayer().teleport(e.getBlock().getRelative(BlockFace.UP).getLocation());
            }
        });
    }
}
