package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 */
public class StartSafetyListenerTest extends CommonTestSetup {

    private StartSafetyListener ssl;
    @Mock
    private Location location2;
    @Mock
    private Flag flag;

    private final @NonNull WSettings ws = new WSettings();
    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        AOneBlock addon = new AOneBlock();
        addon.setIslandWorld(world);
        addon.setSettings(new Settings());

        // Player
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.getWorld()).thenReturn(world);
        User.getInstance(mockPlayer);

        when(world.getName()).thenReturn("world");

        when(flag.isSetForWorld(world)).thenReturn(true);

        when(iwm.inWorld(world)).thenReturn(true);
        when(iwm.getWorldSettings(world)).thenReturn(ws);

        when(location.getWorld()).thenReturn(world);
        when(location2.getWorld()).thenReturn(world);
        when(island.getWorld()).thenReturn(world);
        when(island.getCenter()).thenReturn(location);

        when(location2.getX()).thenReturn(0.5D);

        addon.START_SAFETY.setSetting(world, true);

        ssl = new StartSafetyListener(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#StartSafetyListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testStartSafetyListener() {
        assertNotNull(ssl);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#onNewIsland(world.bentobox.bentobox.api.events.island.IslandCreatedEvent)}.
     */
    @Test
    public void testOnNewIsland() {
        IslandCreatedEvent e = new IslandCreatedEvent(island, uuid, false, location);
        ssl.onNewIsland(e);
        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), anyLong());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#onResetIsland(world.bentobox.bentobox.api.events.island.IslandResetEvent)}.
     */
    @Test
    public void testOnResetIsland() {
        IslandResetEvent e = new IslandResetEvent(island, uuid, false, location, null, island);
        ssl.onResetIsland(e);
        verify(sch).runTaskLater(eq(plugin), any(Runnable.class), anyLong());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#onPlayerMove(org.bukkit.event.player.PlayerMoveEvent)}.
     */
    @Test
    public void testOnPlayerMove() {
        testOnResetIsland();
        PlayerMoveEvent e = new PlayerMoveEvent(mockPlayer, location, location2);
        ssl.onPlayerMove(e);
        // No movement
        assertEquals(0D, e.getTo().getX(), 0D);
        assertEquals(0D, e.getTo().getZ(), 0D);
        verify(mockPlayer).isSneaking();

    }

    class WSettings implements WorldSettings {

        private final Map<String, Boolean> flags = new HashMap<>();

        @Override
        public GameMode getDefaultGameMode() {

            return null;
        }

        @Override
        public Map<Flag, Integer> getDefaultIslandFlags() {

            return null;
        }

        @Override
        public Map<Flag, Integer> getDefaultIslandSettings() {

            return null;
        }

        @Override
        public Difficulty getDifficulty() {

            return null;
        }

        @Override
        public void setDifficulty(Difficulty difficulty) {

        }

        @Override
        public String getFriendlyName() {

            return null;
        }

        @Override
        public int getIslandDistance() {

            return 0;
        }

        @Override
        public int getIslandHeight() {

            return 0;
        }

        @Override
        public int getIslandProtectionRange() {

            return 0;
        }

        @Override
        public int getIslandStartX() {

            return 0;
        }

        @Override
        public int getIslandStartZ() {

            return 0;
        }

        @Override
        public int getIslandXOffset() {

            return 0;
        }

        @Override
        public int getIslandZOffset() {

            return 0;
        }

        @Override
        public List<String> getIvSettings() {

            return null;
        }

        @Override
        public int getMaxHomes() {

            return 0;
        }

        @Override
        public int getMaxIslands() {

            return 0;
        }

        @Override
        public int getMaxTeamSize() {

            return 0;
        }

        @Override
        public int getNetherSpawnRadius() {

            return 0;
        }

        @Override
        public String getPermissionPrefix() {

            return null;
        }

        @Override
        public Set<EntityType> getRemoveMobsWhitelist() {

            return null;
        }

        @Override
        public int getSeaHeight() {

            return 0;
        }

        @Override
        public List<String> getHiddenFlags() {

            return null;
        }

        @Override
        public List<String> getVisitorBannedCommands() {

            return null;
        }

        @Override
        public Map<String, Boolean> getWorldFlags() {
            return flags;
        }

        @Override
        public String getWorldName() {

            return null;
        }

        @Override
        public boolean isDragonSpawn() {

            return false;
        }

        @Override
        public boolean isEndGenerate() {

            return false;
        }

        @Override
        public boolean isEndIslands() {

            return false;
        }

        @Override
        public boolean isNetherGenerate() {

            return false;
        }

        @Override
        public boolean isNetherIslands() {

            return false;
        }

        @Override
        public boolean isOnJoinResetEnderChest() {

            return false;
        }

        @Override
        public boolean isOnJoinResetInventory() {

            return false;
        }

        @Override
        public boolean isOnJoinResetMoney() {

            return false;
        }

        @Override
        public boolean isOnJoinResetHealth() {

            return false;
        }

        @Override
        public boolean isOnJoinResetHunger() {

            return false;
        }

        @Override
        public boolean isOnJoinResetXP() {

            return false;
        }

        @Override
        public @NonNull List<String> getOnJoinCommands() {

            return null;
        }

        @Override
        public boolean isOnLeaveResetEnderChest() {

            return false;
        }

        @Override
        public boolean isOnLeaveResetInventory() {

            return false;
        }

        @Override
        public boolean isOnLeaveResetMoney() {

            return false;
        }

        @Override
        public boolean isOnLeaveResetHealth() {

            return false;
        }

        @Override
        public boolean isOnLeaveResetHunger() {

            return false;
        }

        @Override
        public boolean isOnLeaveResetXP() {

            return false;
        }

        @Override
        public @NonNull List<String> getOnLeaveCommands() {

            return null;
        }

        @Override
        public boolean isUseOwnGenerator() {

            return false;
        }

        @Override
        public boolean isWaterUnsafe() {

            return false;
        }

        @Override
        public List<String> getGeoLimitSettings() {

            return null;
        }

        @Override
        public int getResetLimit() {

            return 0;
        }

        @Override
        public long getResetEpoch() {

            return 0;
        }

        @Override
        public void setResetEpoch(long timestamp) {

        }

        @Override
        public boolean isTeamJoinDeathReset() {

            return false;
        }

        @Override
        public int getDeathsMax() {

            return 0;
        }

        @Override
        public boolean isDeathsCounted() {

            return false;
        }

        @Override
        public boolean isDeathsResetOnNewIsland() {

            return false;
        }

        @Override
        public boolean isAllowSetHomeInNether() {

            return false;
        }

        @Override
        public boolean isAllowSetHomeInTheEnd() {

            return false;
        }

        @Override
        public boolean isRequireConfirmationToSetHomeInNether() {

            return false;
        }

        @Override
        public boolean isRequireConfirmationToSetHomeInTheEnd() {

            return false;
        }

        @Override
        public int getBanLimit() {

            return 0;
        }

        @Override
        public boolean isLeaversLoseReset() {

            return false;
        }

        @Override
        public boolean isKickedKeepInventory() {

            return false;
        }

        @Override
        public boolean isCreateIslandOnFirstLoginEnabled() {

            return false;
        }

        @Override
        public int getCreateIslandOnFirstLoginDelay() {

            return 0;
        }

        @Override
        public boolean isCreateIslandOnFirstLoginAbortOnLogout() {

            return false;
        }

    }

}
