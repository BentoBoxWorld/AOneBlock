package world.bentobox.aoneblock.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 */
public class HoloListener implements Listener {
    private final AOneBlock addon;
    private final Set<Location> activeHolograms;
    private static final Vector DEFAULT_OFFSET = new Vector(0.5, 1.1, 0.5);

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        this.activeHolograms = new HashSet<>();
    }

    /**
     * Handles the event when an island is deleted.
     * Removes the associated hologram.
     *
     * @param e the IslandDeleteEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        removeHologramAt(e.getIsland());
    }

    /**
     * Clears all cached holograms and removes their entities.
     * Called when disabling
     */
    public void onDisable() {
        activeHolograms.forEach(this::cleanupHologram);
        activeHolograms.clear();
    }

    /**
     * Sets up the hologram for a specific island.
     * If it's a new island, sets the starting hologram line.
     *
     * @param island the island to set up
     * @param is the OneBlockIslands data object
     * @param newIsland whether this is a new island
     */
    protected void setUp(@NonNull Island island, @NonNull OneBlockIslands is, boolean newIsland) {
        if (!addon.getSettings().isUseHolograms() || island.getOwner() == null) {
            return;
        }

        if (newIsland) {
            String startingText = User.getInstance(island.getOwner())
                .getTranslation("aoneblock.island.starting-hologram");
            is.setHologram(startingText == null ? "" : startingText);
        }
        updateHologram(island, is.getHologram());
    }

    /**
     * Processes the phase change for an island and updates its hologram.
     *
     * @param i the island
     * @param is the OneBlockIslands data object
     * @param phase the current OneBlockPhase
     */
    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String holoText = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(holoText == null ? "" : Util.translateColorCodes(holoText));
        updateHologram(i, is.getHologram());
    }

    /**
     * Updates the hologram lines for the given island.
     * Handles creation, updating, and scheduled deletion of holograms.
     *
     * @param island the island to update
     * @param text the text to display
     */
    private void updateHologram(Island island, String text) {
        if (!addon.getSettings().isUseHolograms() || text.isBlank()) {
            return;
        }
        removeHologramAt(island);
        Location pos = getHologramLocation(island);
        createHologram(pos, text);

        // Set up auto delete if duration is set
        if (addon.getSettings().getHologramDuration() > 0) {
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), 
                () -> removeHologramAt(island), 
                addon.getSettings().getHologramDuration() * 20L);
        }
    }

    /**
     * Gets the location for the hologram based on the island's center and the configured offset.
     *
     * @param island the island
     * @return the location for the hologram
     */
    private Location getHologramLocation(Island island) {
        Vector offset = parseVector(addon.getSettings().getOffset());
        return island.getCenter().clone().add(offset);
    }

    /**
     * Creates a new hologram (TextDisplay) at the given location.
     * Caches the hologram for future reference.
     *
     * @param pos the location to create the hologram at
     * @param text the text to display
     */
    private void createHologram(Location pos, String text) {
        TextDisplay display = pos.getWorld().spawn(pos, TextDisplay.class);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setBillboard(Billboard.CENTER);
        display.setPersistent(true);
        display.setText(text);
        activeHolograms.add(pos);
    }

    /**
     * Deletes the hologram for the given island and removes any residual holograms nearby.
     *
     * @param island the island whose hologram should be deleted
     */
    private void removeHologramAt(@NonNull Island island) {
        if (island.getWorld() == null) {
            return;
        }
        cleanupHologram(getHologramLocation(island));
    }

    /**
     * Removes any holograms at this location
     * @param pos location
     */
    private void cleanupHologram(Location pos) {
        activeHolograms.remove(pos);
        // Chunks have to be loaded for the entity to exist to be deleted
        Util.getChunkAtAsync(pos).thenRun(() -> 
            pos.getWorld().getNearbyEntities(pos, 1, 1, 1).stream()
                .filter(e -> e.getType() == EntityType.TEXT_DISPLAY)
                .forEach(Entity::remove));
    }

    /**
     * Parses a string in the format "x,y,z" into a Vector.
     * If parsing fails, returns a default vector.
     *
     * @param str the string to parse
     * @return the parsed Vector
     */
    private static Vector parseVector(String str) {
        if (str == null) {
            return DEFAULT_OFFSET;
        }
        try {
            String[] parts = str.split(",");
            return parts.length == 3 
                ? new Vector(
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim()))
                : DEFAULT_OFFSET;
        } catch (NumberFormatException e) {
            return DEFAULT_OFFSET;
        }
    }
}
