package world.bentobox.aoneblock.listeners;

import org.bukkit.Bukkit;
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
        deleteOldHolograms(e.getIsland());
    }

    /**
     * Delete old holograms
     * @param island island
     */
    private void deleteOldHolograms(@NonNull Island island) {
        for (Hologram hologram : HologramsAPI.getHolograms(BentoBox.getInstance())) {
            if (!addon.inWorld(hologram.getWorld())) continue;
            if (island.getBoundingBox().contains(hologram.getLocation().toVector())) hologram.delete();
        }
    }

    protected void setUp(@NonNull Island island, @NonNull OneBlockIslands is) {
        // Delete Old Holograms
        deleteOldHolograms(island);
        // Manage New Hologram
        if (island.getOwner() != null) {
            User owner = User.getInstance(island.getOwner());
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
            if (i.getBoundingBox().contains(hologram.getLocation().toVector())) hologram.delete();
        }
        String hololine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(hololine == null ? "" : hololine);
        Location center = i.getCenter();
        if (hololine != null && center != null) {
            final Hologram hologram = HologramsAPI.createHologram(BentoBox.getInstance(), center.add(0.5, 2.6, 0.5));            
            for (String line : hololine.split("\\n")) {
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
            }
            // Set up auto delete
            if (addon.getSettings().getHologramDuration() > 0) {
                Bukkit.getScheduler().runTaskLater(BentoBox.getInstance(), () -> hologram.delete(), addon.getSettings().getHologramDuration() * 20L);
            }
        }
    }
}
