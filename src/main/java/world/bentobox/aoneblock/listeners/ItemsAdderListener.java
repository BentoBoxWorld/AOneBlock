package world.bentobox.aoneblock.listeners;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.aoneblock.AOneBlock;

public class ItemsAdderListener implements Listener {

    private final AOneBlock addon;
    public ItemsAdderListener(AOneBlock addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onLoad(ItemsAdderLoadDataEvent e) {
        addon.loadData(true);
    }

}
