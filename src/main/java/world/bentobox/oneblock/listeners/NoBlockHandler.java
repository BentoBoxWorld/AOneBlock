package world.bentobox.oneblock.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.oneblock.OneBlock;

/**
 * Handles the situation when there is no block to go back to after death respawn
 * @author tastybento
 *
 */
public class NoBlockHandler implements Listener {

    private OneBlock addon;

    public NoBlockHandler(OneBlock oneBlock) {
        this.addon = oneBlock;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!addon.inWorld(e.getRespawnLocation().getWorld())) {
            return;
        }
        // Check if block is air
        Island island = addon.getIslands().getIsland(e.getRespawnLocation().getWorld(), e.getPlayer().getUniqueId());
        if (island != null && island.getCenter().getBlock().isEmpty()) {
            island.getCenter().getBlock().setType(Material.COBBLESTONE);
        }
    }

}
