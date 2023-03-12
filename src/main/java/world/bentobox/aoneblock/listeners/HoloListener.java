package world.bentobox.aoneblock.listeners;

import me.hsgamer.unihologram.common.api.Hologram;
import me.hsgamer.unihologram.common.api.HologramLine;
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

import java.util.*;

/**
 * Handles Holographic elements. Relies on UniHologram Plugin
 * @author tastybento, HSGamer
 */
public class HoloListener implements Listener {

    private final AOneBlock addon;
    private final SpigotHologramProvider hologramProvider;
    private final Map<Island, Hologram<Location>> cachedHolograms;

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        this.hologramProvider = JavaPlugin.getPlugin(UniHologramPlugin.class).getProvider();
        this.cachedHolograms = new IdentityHashMap<>();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        deleteHologram(e.getIsland());
    }

    private Hologram<Location> getHologram(Island island) {
        return cachedHolograms.compute(island, (is, hologram) -> {
            if (hologram == null) {
                Location center = island.getCenter();
                hologram = hologramProvider.createHologram(UUID.randomUUID().toString(), center.add(0.5, 2.6, 0.5));
            }
            if (!hologram.isInitialized()) {
                hologram.init();
            }
            return hologram;
        });
    }

    private void setLines(Hologram<Location> hologram, String lines) {
        List<HologramLine> hologramLines = Arrays.stream(lines.split("\\n")).<HologramLine>map(TextHologramLine::new).toList();
        hologram.setLines(hologramLines);
    }

    private void clearIfInitialized(Hologram<Location> hologram) {
        if (hologram.isInitialized()) {
            hologram.clear();
        }
    }

    private void setLines(Island island, String lines) {
        setLines(getHologram(island), lines);
    }

    /**
     * Setup holograms on startup
     */
    public void setUp() {
        addon.getIslands().getIslands().stream()
                .filter(i -> addon.inWorld(i.getWorld()))
                .forEach(island -> setUp(island, addon.getOneBlocksIsland(island), false));
    }

    public void clear() {
        cachedHolograms.values().forEach(Hologram::clear);
        cachedHolograms.clear();
    }

    /**
     * Delete hologram
     * @param island island
     */
    private void deleteHologram(@NonNull Island island) {
        Hologram<Location> hologram = cachedHolograms.remove(island);
        if (hologram != null) {
            clearIfInitialized(hologram);
        }
    }

    protected void setUp(@NonNull Island island, @NonNull OneBlockIslands is, boolean newIsland) {
        UUID ownerUUID = island.getOwner();
        if (ownerUUID != null) {
            User owner = User.getInstance(ownerUUID);
            String holoLine;
            if (newIsland) {
                holoLine = owner.getTranslation("aoneblock.island.starting-hologram");
            } else {
                holoLine = is.getHologram();
            }
            is.setHologram(holoLine == null ? "" : holoLine);
            if (holoLine != null) {
                setLines(island, holoLine);
            }
        }
    }


    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String holoLine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(holoLine == null ? "" : holoLine);
        if (holoLine != null) {
            final Hologram<Location> hologram = getHologram(i);
            setLines(hologram, holoLine);
            // Set up auto delete
            if (addon.getSettings().getHologramDuration() > 0) {
                Bukkit.getScheduler().runTaskLater(BentoBox.getInstance(), () -> clearIfInitialized(hologram), addon.getSettings().getHologramDuration() * 20L);
            }
        } else {
            deleteHologram(i);
        }
    }
}
