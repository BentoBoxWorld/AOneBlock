package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFromToEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, DatabaseSetup.class, Util.class})
public class BlockListenerTest {

    // Class under test
    private BlockListener bl;

    @Mock
    BentoBox plugin;
    @Mock
    AOneBlock addon;
    private static AbstractDatabaseHandler<Object> h;
    @Mock
    private Settings pluginSettings;

    private User user;
    @Mock
    private World world;
    @Mock
    private OneBlocksManager obm;

    private Island island;

    @Mock
    private @Nullable Player player;

    @Mock
    private PlayersManager pm;
    @Mock
    private Bank bank;
    @Mock
    private Level level;
    @Mock
    private Location location;
    @Mock
    private IslandsManager im;

    private @NonNull OneBlockIslands is;

    private @NonNull OneBlockPhase phase;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        // This has to be done beforeClass otherwise the tests will interfere with each other
        h = mock(AbstractDatabaseHandler.class);
        // Database
        PowerMockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        tearDown();
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);

        // Addon
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getOverWorld()).thenReturn(world);
        when(addon.getIslands()).thenReturn(im);
        when(addon.inWorld(world)).thenReturn(true);

        // Player
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("tastybento");
        when(player.isOnline()).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        User.setPlugin(plugin);
        user = User.getInstance(player);

        // Island
        island = new Island();
        island.setOwner(UUID.randomUUID());
        island.setName("island_name");

        // Players Manager
        when(pm.getName(any())).thenReturn("tastybento2");
        when(pm.getUser(any(UUID.class))).thenReturn(user);

        // Bank
        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        // Phat balance to start
        when(bm.getBalance(island)).thenReturn(new Money(100000D));
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));
        // Level
        when(level.getIslandLevel(eq(world), any())).thenReturn(1000L);
        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));

        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);

        bl = new BlockListener(addon);
    }

    /**
     * @throws java.lang.Exception - exception
     */
    @After
    public void tearDown() throws Exception {
        deleteAll(new File("database"));
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    public void testOnBlockFromToNotInWorld() {
        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    public void testOnBlockFromToNotIslandCenter() {
        when(addon.inWorld(any(World.class))).thenReturn(true);
        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        when(from.getWorld()).thenReturn(world);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    public void testOnBlockFromToCenterBlock() {
        when(addon.inWorld(any(World.class))).thenReturn(true);
        island.setCenter(location);
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));

        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        when(from.getWorld()).thenReturn(world);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertTrue(e.isCancelled());
    }

}
