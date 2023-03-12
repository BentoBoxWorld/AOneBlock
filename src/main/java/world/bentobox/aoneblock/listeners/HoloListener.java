package world.bentobox.aoneblock.listeners;

import me.hsgamer.unihologram.common.api.Hologram;
import me.hsgamer.unihologram.common.line.TextHologramLine;
import me.hsgamer.unihologram.spigot.SpigotHologramProvider;
import me.hsgamer.unihologram.spigot.plugin.UniHologramPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Handles Holographic elements. Relies on UniHologram Plugin
 * @author tastybento, HSGamer
 */
public class HoloListener implements Listener {

    private final AOneBlock addon;
    private final SpigotHologramProvider hologramProvider;

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        this.hologramProvider = JavaPlugin.getPlugin(UniHologramPlugin.class).getProvider();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        deleteOldHolograms(e.getIsland());
    }

    /**
     * Setup holograms on startup
     */
    public void setup() {
        addon.getIslands().getIslands().stream()
                .filter(i -> addon.inWorld(i.getWorld()))
                .forEach(island -> {
                    OneBlockIslands oneBlockIsland = addon.getOneBlocksIsland(island);
                    String hololine = oneBlockIsland.getHologram();
                    Location center = island.getCenter();
                    if (!hololine.isEmpty()) {
                        final Hologram<Location> hologram = hologramProvider.createHologram(island.getName() + "-" + UUID.randomUUID(), center.add(0.5, 2.6, 0.5));
                        for (String line : hololine.split("\\n")) {
                            hologram.addLine(new TextHologramLine(line));
                        }
                    }
                });
    }

    /**
     * Delete old holograms
     * @param island island
     */
    private void deleteOldHolograms(@NonNull Island island) {
        for (Hologram<Location> hologram : hologramProvider.getAllHolograms()) {
            if (!addon.inWorld(hologram.getLocation())) continue;
            if (island.getBoundingBox().contains(hologram.getLocation().toVector())) hologram.clear();
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
                final Hologram<Location> hologram = hologramProvider.createHologram(island.getName() + "-" + UUID.randomUUID(), center.add(0.5, 2.6, 0.5));
                for (String line : hololine.split("\\n")) {
                    hologram.addLine(new TextHologramLine(line));
                }
            }
        }
    }


    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        // Manage Holograms
        for (Hologram<Location> hologram : hologramProvider.getAllHolograms()) {
            if (!addon.inWorld(hologram.getLocation())) continue;
            if (i.getBoundingBox().contains(hologram.getLocation().toVector())) hologram.clear();
        }
        String hololine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(hololine == null ? "" : hololine);
        Location center = i.getCenter();
        if (hololine != null && center != null) {
            final Hologram<Location> hologram = hologramProvider.createHologram(i.getName() + "-" + UUID.randomUUID(), center.add(0.5, 2.6, 0.5));
            for (String line : hololine.split("\\n")) {
                hologram.addLine(new TextHologramLine(line));
            }
            // Set up auto delete
            if (addon.getSettings().getHologramDuration() > 0) {
                Bukkit.getScheduler().runTaskLater(BentoBox.getInstance(), hologram::clear, addon.getSettings().getHologramDuration() * 20L);
            }
        }
    }
}
