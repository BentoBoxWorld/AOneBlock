package world.bentobox.aoneblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import world.bentobox.aoneblock.AOneBlock;

/**
 * Handles CraftEngineReloadEvent which is fired when CraftEngine loads or reloads its data
 */
public class CraftEngineListener implements Listener {

    private final AOneBlock addon;

    public CraftEngineListener(AOneBlock addon) {
        this.addon = addon;
    }

    /**
     * Handle CraftEngineReloadEvent then reload the addon if it gets triggered
     * @param e - CraftEngineReloadEvent
     */
    @EventHandler
    public void onReload(CraftEngineReloadEvent e) {
        addon.loadData();
    }
}
