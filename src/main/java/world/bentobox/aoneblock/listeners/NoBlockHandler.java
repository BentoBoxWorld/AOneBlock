package world.bentobox.aoneblock.listeners;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Handles the situation when there is no block to go back to after death respawn
 * @author tastybento
 *
 */
public class NoBlockHandler implements Listener {

    private AOneBlock addon;

    public NoBlockHandler(AOneBlock oneBlock) {
        this.addon = oneBlock;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!addon.inWorld(e.getRespawnLocation().getWorld())) {
            return;
        }
        // Check if block is air
        Island island = addon.getIslands().getIsland(e.getRespawnLocation().getWorld(), e.getPlayer().getUniqueId());
        if (island != null && Objects.requireNonNull(island.getCenter()).getBlock().isEmpty()) {
            Objects.requireNonNull(island).getCenter().getBlock().setType(Material.COBBLESTONE);
        }
    }

}
