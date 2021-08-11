package world.bentobox.aoneblock.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.NonNull;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Handles Holographic elements. Relies on Holographic Plugin
 * @author tastybento
 */
public class HoloListener implements Listener {

    private final AOneBlock addon;

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        deleteOldHolograms(e.getIsland().getUniqueId());
    }

    /**
     * Delete old holograms
     * @param islandId unique island id
     */
    private void deleteOldHolograms(String islandId) {
        for (Hologram hologram : HologramsAPI.getHolograms(BentoBox.getInstance())) {
            if (!addon.inWorld(hologram.getWorld())) continue;
            addon.getIslands().getIslandAt(hologram.getLocation()).filter(island -> island.getUniqueId().equals(islandId)).ifPresent(h -> hologram.delete());
        }
    }

    protected void setUp(@NonNull Island island, @NonNull OneBlockIslands is) {
        // Delete Old Holograms
        deleteOldHolograms(island.getUniqueId());
        // Manage New Hologram
        User owner = User.getInstance(island.getOwner());
        if (owner != null) {
            String hololine = owner.getTranslation("aoneblock.island.starting-hologram");
            is.setHologram(hololine == null ? "" : hololine);
            Location center = island.getCenter();
            if (hololine != null && center != null) {
                final Hologram hologram = HologramsAPI.createHologram(BentoBox.getInstance(), center.add(0.5, 2.6, 0.5));
                for (String line : hololine.split("\\n")) {
                    hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
    }


    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        // Manage Holograms
        for (Hologram hologram : HologramsAPI.getHolograms(BentoBox.getInstance())) {
            if (!addon.inWorld(hologram.getWorld())) continue;
            addon.getIslands().getIslandAt(hologram.getLocation()).filter(island -> island.getUniqueId().equals(is.getUniqueId())).ifPresent(h -> hologram.delete());
        }
        String hololine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(hololine == null ? "" : hololine);
        Location center = i.getCenter();
        if (hololine != null && center != null) {
            final Hologram hologram = HologramsAPI.createHologram(BentoBox.getInstance(), center.add(0.5, 2.6, 0.5));
            for (String line : hololine.split("\\n")) {
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }



}
