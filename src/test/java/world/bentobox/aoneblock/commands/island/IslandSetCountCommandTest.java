package world.bentobox.aoneblock.commands.island;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, DatabaseSetup.class })
public class IslandSetCountCommandTest {
	@Mock
	private BentoBox plugin;
	@Mock
	private CompositeCommand ac;
	@Mock
	private User user;
	@Mock
	private LocalesManager lm;
	@Mock
	private AOneBlock addon;
	private UUID uuid;
	@Mock
	private World world;
	@Mock
	private IslandsManager im;
	@Mock
	private @Nullable Island island;
	@Mock
	private IslandWorldManager iwm;
	private IslandSetCountCommand iscc;
	@Mock
	private BlockListener bl;
	private @NonNull OneBlockIslands oneBlockIsland = new OneBlockIslands(UUID.randomUUID().toString());

	private static AbstractDatabaseHandler<Object> h;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
		// This has to be done beforeClass otherwise the tests will interfere with each
		// other
		h = mock(AbstractDatabaseHandler.class);
		// Database
		PowerMockito.mockStatic(DatabaseSetup.class);
		DatabaseSetup dbSetup = mock(DatabaseSetup.class);
		when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
		when(dbSetup.getHandler(any())).thenReturn(h);
		when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
	}

	@After
	public void tearDown() throws IOException {
		User.clearUsers();
		Mockito.framework().clearInlineMocks();
		deleteAll(new File("database"));
		deleteAll(new File("database_backup"));
	}

	private void deleteAll(File file) throws IOException {
		if (file.exists()) {
			Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Set up plugin
		BentoBox plugin = mock(BentoBox.class);
		Whitebox.setInternalState(BentoBox.class, "instance", plugin);

		// Command manager
		CommandsManager cm = mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);

		// Player
		Player p = mock(Player.class);
		// Sometimes use Mockito.withSettings().verboseLogging()
		when(user.isOp()).thenReturn(false);
		when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
		when(user.getWorld()).thenReturn(world);
		uuid = UUID.randomUUID();
		when(user.getUniqueId()).thenReturn(uuid);
		when(user.getPlayer()).thenReturn(p);
		when(user.getName()).thenReturn("tastybento");
		when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
		User.setPlugin(plugin);

		// Parent command has no aliases
		when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
		when(ac.getWorld()).thenReturn(world);
		when(ac.getAddon()).thenReturn(addon);

		// Islands
		when(plugin.getIslands()).thenReturn(im);
		when(im.getIsland(world, user)).thenReturn(island);
		when(im.hasIsland(world, user)).thenReturn(true);
		when(im.inTeam(world, uuid)).thenReturn(true);
		when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
		when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);

		// IWM
		when(plugin.getIWM()).thenReturn(iwm);
		when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");

		// Settings
		Settings settings = new Settings();
		when(addon.getSettings()).thenReturn(settings);

		// RanksManager
		RanksManager rm = new RanksManager();
		when(plugin.getRanksManager()).thenReturn(rm);

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
