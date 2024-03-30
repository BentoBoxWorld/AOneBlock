package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.Settings;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.user.Notifier;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class })
public class StartSafetyListenerTest {

    private AOneBlock addon;
    private StartSafetyListener ssl;
    @Mock
    private Island island;
    private UUID uuid = UUID.randomUUID();
    @Mock
    private Location location;
    @Mock
    private Location location2;
    @Mock
    private World world;
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private Flag flag;

    private @NonNull WSettings ws = new WSettings();
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private Player player;
    @Mock
    private LocalesManager lm;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private Notifier notifier;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        when(Bukkit.getScheduler()).thenReturn(scheduler);

        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        addon = new AOneBlock();
        addon.setIslandWorld(world);
        addon.setSettings(new Settings());

        // Player
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getWorld()).thenReturn(world);
        User.getInstance(player);

        when(world.getName()).thenReturn("world");

        when(flag.isSetForWorld(world)).thenReturn(true);

        when(iwm.inWorld(world)).thenReturn(true);
        when(iwm.getWorldSettings(world)).thenReturn(ws);
        when(plugin.getIWM()).thenReturn(iwm);

        when(location.getWorld()).thenReturn(world);
        when(location2.getWorld()).thenReturn(world);
        when(island.getWorld()).thenReturn(world);
        when(island.getCenter()).thenReturn(location);

        when(plugin.getNotifier()).thenReturn(notifier);

        // Placeholders
        when(phm.replacePlaceholders(any(), anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // BentoBox
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        when(location2.getX()).thenReturn(0.5D);

        addon.START_SAFETY.setSetting(world, true);

        ssl = new StartSafetyListener(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        User.clearUsers();
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
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), anyLong());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#onResetIsland(world.bentobox.bentobox.api.events.island.IslandResetEvent)}.
     */
    @Test
    public void testOnResetIsland() {
        IslandResetEvent e = new IslandResetEvent(island, uuid, false, location, null, island);
        ssl.onResetIsland(e);
        verify(scheduler).runTaskLater(eq(plugin), any(Runnable.class), anyLong());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.StartSafetyListener#onPlayerMove(org.bukkit.event.player.PlayerMoveEvent)}.
     */
    @Test
    public void testOnPlayerMove() {
        testOnResetIsland();
        PlayerMoveEvent e = new PlayerMoveEvent(player, location, location2);
        ssl.onPlayerMove(e);
        // No movement
        assertEquals(0D, e.getTo().getX(), 0D);
        assertEquals(0D, e.getTo().getZ(), 0D);
        verify(player).isSneaking();

    }

    class WSettings implements WorldSettings {

        private Map<String, Boolean> flags = new HashMap<>();

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
