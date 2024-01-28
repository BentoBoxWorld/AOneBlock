package world.bentobox.aoneblock.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * Listener to provide protection for players in the first minute of their island. Prevents movement.
 */
public class StartSafetyListener implements Listener {

    private final AOneBlock addon;
    private final Map<UUID, Long> newIslands = new HashMap<>();

    public StartSafetyListener(AOneBlock addon) {
        super();
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewIsland(IslandCreatedEvent e) {
        store(e.getIsland().getWorld(), e.getPlayerUUID());
    }

    private void store(World world, UUID playerUUID) {
        if (addon.inWorld(world) && addon.START_SAFETY.isSetForWorld(world) && !newIslands.containsKey(playerUUID)) {
            long time = addon.getSettings().getStartingSafetyDuration();
            if (time < 0) {
                time = 10; // 10 seconds
            }
            newIslands.put(playerUUID, System.currentTimeMillis() + (time * 1000));
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                newIslands.remove(playerUUID);
                User.getInstance(playerUUID).sendMessage("protection.flags.START_SAFETY.free-to-move");
            }, time);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResetIsland(IslandResetEvent e) {
        store(e.getIsland().getWorld(), e.getPlayerUUID());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (addon.inWorld(e.getPlayer().getWorld()) && newIslands.containsKey(e.getPlayer().getUniqueId())
                && e.getTo() != null && !e.getPlayer().isSneaking()
                && (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ())) {
            // Do not allow x or z movement
            e.setTo(new Location(e.getFrom().getWorld(), e.getFrom().getX(), e.getTo().getY(), e.getFrom().getZ(),
                    e.getTo().getYaw(), e.getTo().getPitch()));
            String waitTime = String
                    .valueOf((int) ((newIslands.get(e.getPlayer().getUniqueId()) - System.currentTimeMillis()) / 1000));
            User.getInstance(e.getPlayer()).notify(addon.START_SAFETY.getHintReference(), TextVariables.NUMBER,
                    waitTime);
        }
    }

}
