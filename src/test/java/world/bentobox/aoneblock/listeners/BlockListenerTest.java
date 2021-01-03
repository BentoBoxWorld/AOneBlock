package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import world.bentobox.bentobox.managers.PlaceholdersManager;
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
    @Mock
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
    private PlaceholdersManager phm;

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
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        // Placeholders
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String)a.getArgument(1, String.class));

        // Addon
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getOverWorld()).thenReturn(world);

        // Player
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);
        User.getInstance(player);

        // Island
        island = new Island();
        island.setOwner(UUID.randomUUID());
        island.setName("island_name");

        // Players Manager
        when(pm.getName(any())).thenReturn("tastybento2");

        // Bank
        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        // Phat balance to start
        when(bm.getBalance(eq(island))).thenReturn(new Money(100000D));
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));
        // Level
        when(level.getIslandLevel(eq(world), any())).thenReturn(1000L);
        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));

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
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#replacePlaceholders(org.bukkit.entity.Player, java.lang.String, java.lang.String, world.bentobox.bentobox.database.objects.Island, java.util.List)}.
     */
    @Test
    public void testReplacePlaceholders() {
        // Commands
        /*
         * [island] - Island name
         * [owner] - Island owner's name
         * [player] - The name of the player who broke the block triggering the commands
         * [phase] - the name of this phase
         * [blocks] - the number of blocks broken
         * [level] - island level (Requires Levels Addon)
         * [bank-balance] - island bank balance (Requires Bank Addon)
         * [eco-balance] - player's economy balance (Requires Vault and an economy plugin)

         */
        List<String> commandList = new ArrayList<>();

        commandList.add("no replacement");
        commandList.add("[island] [owner] [phase] [blocks] [level] [bank-balance] [eco-balance]");
        List<String> r = bl.replacePlaceholders(player, "phaseName", "1000", island, commandList);
        assertEquals(2, r.size());
        assertEquals("no replacement", r.get(0));
        assertEquals("island_name tastybento2 phaseName 1000 1000 100000.0 0.0", r.get(1));
        verify(phm, times(2)).replacePlaceholders(eq(player), any());
    }

}
