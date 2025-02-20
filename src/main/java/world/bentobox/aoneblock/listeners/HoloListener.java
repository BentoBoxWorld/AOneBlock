package world.bentobox.aoneblock.listeners;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * Handles Holographic elements
 *
 * @author tastybento, HSGamer
 */
public class HoloListener implements Listener {
    private final AOneBlock addon;
    private final Map<Island, TextDisplay> cachedHolograms;

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        this.cachedHolograms = new IdentityHashMap<>();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        deleteHologram(e.getIsland());
    }

    private Optional<TextDisplay> getHologram(Island island) {
        return Optional.ofNullable(cachedHolograms.get(island)).filter(TextDisplay::isValid);
    }

    private TextDisplay createHologram(Island island) {
        Location pos = island.getCenter().clone().add(parseVector(addon.getSettings().getOffset()));
        World world = pos.getWorld();
        assert world != null;

        TextDisplay newDisplay = world.spawn(pos, TextDisplay.class);
        newDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        newDisplay.setBillboard(Billboard.CENTER);

        cachedHolograms.put(island, newDisplay);

        return newDisplay;
    }

    private static Vector parseVector(String str) {
        if (str == null) {
            return new Vector(0.5, 1.1, 0.5);
        }
        String[] parts = str.split(",");
        if (parts.length != 3) {
            return new Vector(0.5, 1.1, 0.5);
        }

        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            return new Vector(x, y, z);
        } catch (NumberFormatException e) {
            return new Vector(0.5, 1.1, 0.5);
        }
    }

    private void clearIfInitialized(TextDisplay hologram) {
        if (hologram.isValid()) {
            hologram.remove();
        }
    }

    private void updateLines(Island island, OneBlockIslands oneBlockIsland) {
        // Ignore if holograms are disabled
        if (!addon.getSettings().isUseHolograms()) {
            return;
        }

        Optional<TextDisplay> optionalHologram = getHologram(island);
        String holoLine = oneBlockIsland.getHologram();

        // Clear hologram if empty
        if (holoLine.isBlank() && optionalHologram.isPresent()) {
            clearIfInitialized(optionalHologram.get());
            return;
        }

        // Get or create hologram if needed
        TextDisplay hologram = optionalHologram.orElseGet(() -> createHologram(island));

        // Convert and set lines to hologram
        hologram.setText(holoLine);

        // Set up auto delete
        if (addon.getSettings().getHologramDuration() > 0) {
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> clearIfInitialized(hologram), addon.getSettings().getHologramDuration() * 20L);
        }
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
        cachedHolograms.values().forEach(this::clearIfInitialized);
        cachedHolograms.clear();
    }

    /**
     * Delete hologram
     *
     * @param island island
     */
    private void deleteHologram(@NonNull Island island) {
        TextDisplay hologram = cachedHolograms.remove(island);
        if (hologram != null) {
            clearIfInitialized(hologram);
        }
        // Clear any residual ones that are not cached for some reason
        clearTextDisplayNearBlock(island);
    }

    private void clearTextDisplayNearBlock(Island island) {
        World world = island.getWorld();
        if (world == null)
            return;
        Location pos = island.getCenter().clone().add(parseVector(addon.getSettings().getOffset()));
        // Search for entities in a small radius (e.g., 1 block around)
        for (Entity entity : world.getNearbyEntities(pos, 1, 1, 1)) {
            if (entity.getType() == EntityType.TEXT_DISPLAY) {
                ((TextDisplay) entity).remove();
            }
        }
    }

    protected void setUp(@NonNull Island island, @NonNull OneBlockIslands is, boolean newIsland) {
        UUID ownerUUID = island.getOwner();
        if (ownerUUID == null) {
            return;
        }

        User owner = User.getInstance(ownerUUID);
        if (newIsland) {
            String holoLine = owner.getTranslation("aoneblock.island.starting-hologram");
            is.setHologram(holoLine == null ? "" : holoLine);
        }
        updateLines(island, is);
    }

    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String holoLine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(holoLine == null ? "" : Util.translateColorCodes(holoLine));
        updateLines(i, is);
    }
}
