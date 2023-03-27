package world.bentobox.aoneblock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAligment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.NonNull;

import net.md_5.bungee.api.ChatColor;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
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
        island.getWorld().getEntities().stream().filter(e -> e instanceof TextDisplay).filter(e -> island.onIsland(e.getLocation())).forEach(Entity::remove);
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
            showHologram(hololine, center);            
        }
    }

    private void showHologram(String hololine, Location center) {      
        if (hololine != null && center != null) {
            Location pos = center.clone().add(0.5, 1.1, 0.5);
            center.getWorld().getEntities().stream().filter(e -> e instanceof TextDisplay).filter(e -> e.getLocation().getBlockX() == pos.getBlockX()
                    && e.getLocation().getBlockY() == pos.getBlockY()
                    && e.getLocation().getBlockZ() == pos.getBlockZ()).forEach(Entity::remove);
            TextDisplay td = (TextDisplay) center.getWorld().spawnEntity(pos, EntityType.TEXT_DISPLAY);
            td.setText(hololine);
            td.setAlignment(TextAligment.CENTER);
            td.setBillboard(Billboard.CENTER);
            // Kill hologram
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                if (td != null) td.remove();
            }, addon.getSettings().getHologramDuration() * 20L);
        }        
    }

    protected void process(@NonNull Island i, @NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        String text = phase.getHologramLine(is.getBlockNumber());
        if (text != null) {
            String hololine = ChatColor.translateAlternateColorCodes('&', text);
            is.setHologram(hololine == null ? "" : hololine);
            Location center = i.getCenter();
            showHologram(hololine, center);
        }
    }
}
