package world.bentobox.aoneblock.listeners;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.MagicBlockEvent;
import world.bentobox.bentobox.api.events.flags.FlagSettingChangeEvent;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

public class BossBarListener implements Listener {

    public BossBarListener(AOneBlock addon) {
        super();
        this.addon = addon;
    }

    private AOneBlock addon;

    // Store a boss bar for each player (using their UUID)
    private final Map<Island, BossBar> islandBossBars = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreakBlockEvent(MagicBlockEvent e) {
        // Update boss bar
        tryToShowBossBar(e.getPlayerUUID(), e.getIsland());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnterIsland(IslandEnterEvent event) {
        if (addon.inWorld(event.getIsland().getWorld())) {
            tryToShowBossBar(event.getPlayerUUID(), event.getIsland());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFlagChange(FlagSettingChangeEvent e) {
        if (e.getEditedFlag() == addon.ONEBLOCK_BOSSBAR) {
            // Show to players on island. If it isn't allowed then this will clean up the boss bar too
            e.getIsland().getPlayersOnIsland().stream().map(Player::getUniqueId)
                    .forEach(uuid -> this.tryToShowBossBar(uuid, e.getIsland()));
        }
    }

    /**
     * Try to show the bossbar to the player
     * @param uuid player's UUID
     * @param island island they are on
     */
    private void tryToShowBossBar(UUID uuid, Island island) {
        User user = User.getInstance(uuid);

        // Only show if enabled for island
        if (!island.isAllowed(addon.ONEBLOCK_BOSSBAR)) {
            BossBar removed = islandBossBars.remove(island);
            if (removed != null) {
                // Remove all players from the boss bar
                removed.removeAll();
            }
            return;
        }
        // Default to showing boss bar unless it is explicitly turned off
        if (!user.getMetaData("aoneblock.bossbar").map(MetaDataValue::asBoolean).orElse(true)) {
            // Remove any boss bar from user if they are in the world
            removeBar(user, island);
            // Do not show a boss bar
            return;
        }
        // Prepare boss bar
        String title = user.getTranslationOrNothing("aoneblock.bossbar.title");
        BarColor c;
        try {
            c = BarColor.valueOf(user.getTranslation("aoneblock.bossbar.color").toUpperCase(Locale.ENGLISH));
        } catch (Exception e) {
            c = BarColor.RED;
            addon.logError("Bossbar color unknown. Pick from RED, WHITE, PINK, BLUE, GREEN, YELLOW, or PURPLE");
        }
        BarStyle s = BarStyle.SOLID;
        try {
            s = BarStyle.valueOf(user.getTranslation("aoneblock.bossbar.style").toUpperCase(Locale.ENGLISH));
        } catch (Exception e) {
            s = BarStyle.SOLID;
            addon.logError(
                    "Bossbar style unknow. Pick from SOLID, SEGMENTED_6,  SEGMENTED_10, SEGMENTED_12, SEGMENTED_20");
        }
        // Get it or make it
        BossBar bar = this.islandBossBars.getOrDefault(island,
                Bukkit.createBossBar(title, c, s));
        // Get the progress
        @NonNull
        OneBlockIslands obi = addon.getOneBlocksIsland(island);

        // Set progress
        bar.setProgress(addon.getOneBlockManager().getPercentageDone(obi) / 100);
        int numBlocksToGo = addon.getOneBlockManager().getNextPhaseBlocks(obi);
        int phaseBlocks = addon.getOneBlockManager().getPhaseBlocks(obi);
        int done = phaseBlocks - numBlocksToGo;
        String translation = user.getTranslationOrNothing("aoneblock.bossbar.status", "[togo]",
                String.valueOf(numBlocksToGo), "[total]", String.valueOf(phaseBlocks), "[done]", String.valueOf(done));
        bar.setTitle(translation);
        // Add to user if they don't have it already
        Player player = Bukkit.getPlayer(uuid);
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        // Save the boss bar for later reference (e.g., when updating or removing)
        islandBossBars.put(island, bar);

    }

    private void removeBar(User user, Island island) {
        if (!addon.inWorld(island.getWorld()) || !user.isPlayer() || !user.isOnline()) {
            return;
        }
        BossBar bossBar = islandBossBars.get(island);
        if (bossBar != null) {
            bossBar.removePlayer(user.getPlayer());
            if (bossBar.getPlayers().isEmpty()) {
                // Clean up
                islandBossBars.remove(island);
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExitIsland(IslandExitEvent event) {
        User user = User.getInstance(event.getPlayerUUID());
        removeBar(user, event.getIsland());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        // If the player is on an island then show the bar
        if (!addon.inWorld(e.getPlayer().getLocation())) {
            return;
        }
        addon.getIslands().getIslandAt(e.getPlayer().getLocation())
                .ifPresent(is -> this.tryToShowBossBar(e.getPlayer().getUniqueId(), is));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        // Clean up boss bars
        islandBossBars.values().stream().forEach(bb -> bb.removePlayer(e.getPlayer()));
        islandBossBars.values().removeIf(bb -> bb.getPlayers().isEmpty());
    }

    /**
     * User-level boss bar control.
     * @param user user to toggle
     */
    public void toggleUser(User user) {
        boolean newState = !user.getMetaData("aoneblock.bossbar").map(MetaDataValue::asBoolean).orElse(true);
        user.putMetaData("aoneblock.bossbar", new MetaDataValue(newState));
        if (newState) {
            // If the player is on an island then show the bar
            addon.getIslands().getIslandAt(user.getLocation()).filter(is -> addon.inWorld(is.getWorld()))
                    .ifPresent(is -> this.tryToShowBossBar(user.getUniqueId(), is));
            user.sendMessage("aoneblock.commands.island.bossbar.status_on");
        } else {
            // Remove player from any boss bars. Adding happens automatically
            islandBossBars.forEach((k, v) -> v.removePlayer(user.getPlayer()));
            user.sendMessage("aoneblock.commands.island.bossbar.status_off");
        }
    }

}
