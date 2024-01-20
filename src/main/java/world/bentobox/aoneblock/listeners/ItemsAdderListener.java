package world.bentobox.aoneblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import world.bentobox.aoneblock.AOneBlock;

/**
 * Handles ItemsAdderLoadDataEvent which fired when ItemsAdder loaded it's data or reload it's data
 *
 * @author Teenkung123
 */
public class ItemsAdderListener implements Listener {

    private final AOneBlock addon;
    public ItemsAdderListener(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * handle ItemsAdderLoadDataEvent then reload the addon if it's get triggered
     * @param e - ItemsAdderLoadDataEvent
     */
    @EventHandler
    public void onLoad(ItemsAdderLoadDataEvent e) {
        addon.loadData();
    }

}
