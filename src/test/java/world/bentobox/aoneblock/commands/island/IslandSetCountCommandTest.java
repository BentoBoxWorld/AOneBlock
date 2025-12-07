package world.bentobox.aoneblock.commands.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.WhiteBox;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * @author tastybento
 *
 */
public class IslandSetCountCommandTest extends CommonTestSetup {
    @Mock
    private CompositeCommand ac;

    @Mock
    private User user;
    @Mock
    private AOneBlock addon;
    private IslandSetCountCommand iscc;
    @Mock
    private BlockListener bl;
    @Mock
    private RanksManager rm;

    private final @NonNull OneBlockIslands oneBlockIsland = new OneBlockIslands(UUID.randomUUID().toString());


    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
    }

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        // Database
        MockedStatic<DatabaseSetup> mockedDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockedDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));


        // Set up RanksManager
        WhiteBox.setInternalState(RanksManager.class, "instance", rm);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
        when(user.getWorld()).thenReturn(world);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(mockPlayer);
        when(user.getName()).thenReturn("tastybento");
        when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        User.setPlugin(plugin);

        // Parent command has no aliases
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(world);
        when(ac.getAddon()).thenReturn(addon);

        // Islands
        //when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.hasIsland(world, user)).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);

        // IWM
        //when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // BlockListener
        when(addon.getBlockListener()).thenReturn(bl);
        when(bl.getIsland(island)).thenReturn(oneBlockIsland);

        // DUT
        iscc = new IslandSetCountCommand(this.ac, settings.getSetCountCommand().split(" ")[0],
                settings.getSetCountCommand().split(" "));
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#IslandSetCountCommand(world.bentobox.bentobox.api.commands.CompositeCommand, java.lang.String, java.lang.String[])}.
     */
    @Test
    public void testIslandSetCountCommand() {
        assertNotNull(iscc);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("island.setcount", iscc.getPermission());
        assertEquals("aoneblock.commands.island.setcount.parameters", iscc.getParameters());
        assertEquals("aoneblock.commands.island.setcount.description", iscc.getDescription());
        assertTrue(iscc.isConfigurableRankCommand());
        assertTrue(iscc.isOnlyPlayer());

    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    public void testExecuteUserStringListOfStringShowHelp() {
        assertFalse(iscc.execute(user, "", Collections.emptyList()));
        verify(user).sendMessage("commands.help.header", "[label]", null);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringNoIsland() {
        when(im.hasIsland(world, user)).thenReturn(false);
        when(im.inTeam(world, uuid)).thenReturn(false);
        assertFalse(iscc.execute(user, "", List.of("2000")));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringLowRank() {
        when(rm.getRank(anyInt())).thenReturn(RanksManager.MEMBER_RANK_REF);
        assertFalse(iscc.execute(user, "", List.of("2000")));
        verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, RanksManager.MEMBER_RANK_REF);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringRankOKNegativeCount() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        assertFalse(iscc.execute(user, "", List.of("-2000")));
        verify(user).sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, "-2000");
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringRankOKTooHighCount() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        oneBlockIsland.setLifetime(0);
        assertFalse(iscc.execute(user, "", List.of("2000")));
        verify(user).sendMessage("aoneblock.commands.island.setcount.too-high", TextVariables.NUMBER, "0");
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandSetCountCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        oneBlockIsland.setLifetime(4000);
        oneBlockIsland.add(new OneBlockObject(EntityType.ALLAY, 20));
        assertTrue(iscc.execute(user, "", List.of("2000")));
        verify(user).sendMessage("aoneblock.commands.island.setcount.set", TextVariables.NUMBER, "2000");
        assertEquals(2000, oneBlockIsland.getBlockNumber());
        assertTrue(oneBlockIsland.getQueue().isEmpty());

    }

}
