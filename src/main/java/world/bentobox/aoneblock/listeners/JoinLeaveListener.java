package world.bentobox.aoneblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

public class JoinLeaveListener implements Listener {

    AOneBlock addon;

    /**
     * @param addon - AOneBlock
     */
    public JoinLeaveListener(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * Show particles when block is hit
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Island island = addon.getIslands().getIsland(addon.getOverWorld(), e.getPlayer().getUniqueId());
        if (island != null) {
            addon.getBlockListener().saveIsland(island).thenAccept(r -> {
                if (Boolean.FALSE.equals(r)) {
                    addon.logError("Could not save AOneBlock island at " + Util.xyz(island.getCenter().toVector()) + " to database " + island.getUniqueId());
                }
            });
        }
    }

}
