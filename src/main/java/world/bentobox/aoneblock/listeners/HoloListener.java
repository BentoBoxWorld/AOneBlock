package world.bentobox.aoneblock.listeners;

import java.util.HashSet;
import java.util.Set;
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
    private final Set<Location> holograms;

    /**
     * @param addon - OneBlock
     */
    public HoloListener(@NonNull AOneBlock addon) {
        this.addon = addon;
        this.holograms = new HashSet<>();
    }

    /**
     * Handles the event when an island is deleted.
     * Removes the associated hologram.
     *
     * @param e the IslandDeleteEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeletedIsland(IslandDeleteEvent e) {
        deleteHologram(e.getIsland());
    }

    /**
     * Creates a new hologram (TextDisplay) at the island's center plus offset.
     * Caches the hologram for future reference.
     *
     * @param island the island to create the hologram for
     * @return the created TextDisplay
     */
    private TextDisplay createHologram(Island island) {
        Location pos = island.getCenter().clone().add(parseVector(addon.getSettings().getOffset()));
        World world = pos.getWorld();
        assert world != null;

        TextDisplay newDisplay = world.spawn(pos, TextDisplay.class);
        newDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        newDisplay.setBillboard(Billboard.CENTER);
        // Save location so it can be deleted
        holograms.add(pos);

        return newDisplay;
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

    /**
     * Updates the hologram lines for the given island.
     * Handles creation, updating, and scheduled deletion of holograms.
     *
     * @param island the island to update
     * @param oneBlockIsland the OneBlockIslands data object
     */
    private void updateLines(Island island, OneBlockIslands oneBlockIsland) {
        // Ignore if holograms are disabled
        if (!addon.getSettings().isUseHolograms()) {
            return;
        }
        // Delete the old hologram, if any
        this.deleteHologram(island);
        // Get the new hologram
        String holoLine = oneBlockIsland.getHologram();

        // Return hologram if empty
        if (holoLine.isBlank()) {
            return;
        }

        // Get or create hologram if needed
        TextDisplay hologram = createHologram(island);

        // Doesn't seem to do much but can't harm
        hologram.setPersistent(true);

        // Set lines to hologram
        hologram.setText(holoLine);

        // Set up auto delete if duration is set
        if (addon.getSettings().getHologramDuration() > 0) {
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> deleteHologram(island), addon.getSettings().getHologramDuration() * 20L);
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

    /**
     * Clears all cached holograms and removes their entities.
     * Called when disabling
     */
    public void clear() {
        holograms.forEach(this::removeHologram);
        holograms.clear();
    }

    /**
     * Deletes the hologram for the given island and removes any residual holograms nearby.
     *
     * @param island the island whose hologram should be deleted
     */
    private void deleteHologram(@NonNull Island island) {
        if (island.getWorld() == null || island.getCenter() == null) {
            return;
        }
        Location pos = island.getCenter().clone().add(parseVector(addon.getSettings().getOffset()));
        removeHologram(pos);
    }

    /**
     * Removes any holograms at this location
     * @param pos location
     */
    private void removeHologram(Location pos) {
        holograms.remove(pos);
        // Chunks have to be loaded for the entity to exist to be deleted
        Util.getChunkAtAsync(pos).thenRun(() -> {
            // Search for entities in a small radius (e.g., 1 block around)
            for (Entity entity : pos.getWorld().getNearbyEntities(pos, 1, 1, 1)) {
                if (entity.getType() == EntityType.TEXT_DISPLAY) {
                    ((TextDisplay) entity).remove();
                }
            }
        });    
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

    /**
     * Processes the phase change for an island and updates its hologram.
     *
     * @param i the island
     * @param is the OneBlockIslands data object
     * @param phase the current OneBlockPhase
     */
    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String holoLine = phase.getHologramLine(is.getBlockNumber());
        is.setHologram(holoLine == null ? "" : Util.translateColorCodes(holoLine));
        updateLines(i, is);
    }
}
