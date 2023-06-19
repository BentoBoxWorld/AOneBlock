package world.bentobox.aoneblock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.events.island.IslandInfoEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

public class InfoListener implements Listener {

    final AOneBlock addon;

    /**
     * Add info to the info command
     * @param addon - AOneBlock
     */
    public InfoListener(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * Save island on player quitting
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInfo(IslandInfoEvent e) {
        User user = e.getPlayerUUID() == null ? User.getInstance(Bukkit.getConsoleSender()) : User.getInstance(e.getPlayerUUID());
        @NonNull
        OneBlockIslands is = addon.getOneBlocksIsland(e.getIsland());
        if (is.getBlockNumber() == 0) {
            return;
        }
        user.sendMessage("aoneblock.commands.info.count", TextVariables.NUMBER, String.valueOf(is.getBlockNumber()), 
                TextVariables.NAME, is.getPhaseName(),
                "[lifetime]", String.valueOf(is.getLifetime())
                );
        
    }

}
