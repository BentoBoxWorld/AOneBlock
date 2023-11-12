package world.bentobox.aoneblock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, Config.class, DatabaseSetup.class })
public class AOneBlockTest {

	@Mock
	private User user;
	@Mock
	private IslandsManager im;
	@Mock
	private Island island;

	private AOneBlock addon;
	@Mock
	private BentoBox plugin;
	@Mock
	private FlagsManager fm;
	@Mock
	private Settings settings;
	@Mock
	private PlaceholdersManager phm;

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
		new File("config.yml").delete();
		deleteAll(new File("addons"));
		deleteAll(new File("panels"));
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
		Whitebox.setInternalState(BentoBox.class, "instance", plugin);
		when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

		// The database type has to be created one line before the thenReturn() to work!
		DatabaseType value = DatabaseType.JSON;
		when(plugin.getSettings()).thenReturn(settings);
		when(settings.getDatabaseType()).thenReturn(value);
		when(plugin.getPlaceholdersManager()).thenReturn(phm);
		// Placeholders
		when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String) a.getArgument(1, String.class));

		// Command manager
		CommandsManager cm = mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);

		// Player
		Player p = mock(Player.class);
		// Sometimes use Mockito.withSettings().verboseLogging()
		when(user.isOp()).thenReturn(false);
		UUID uuid = UUID.randomUUID();
		when(user.getUniqueId()).thenReturn(uuid);
		when(user.getPlayer()).thenReturn(p);
		when(user.getName()).thenReturn("tastybento");
		User.setPlugin(plugin);

		// Island World Manager
		IslandWorldManager iwm = mock(IslandWorldManager.class);
		when(plugin.getIWM()).thenReturn(iwm);

		// Player has island to begin with
		island = mock(Island.class);
		when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(island);
		when(plugin.getIslands()).thenReturn(im);

		// Locales
		// Return the reference (USE THIS IN THE FUTURE)
		when(user.getTranslation(Mockito.anyString()))
				.thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

		// Server
		PowerMockito.mockStatic(Bukkit.class);
		Server server = mock(Server.class);
		when(Bukkit.getServer()).thenReturn(server);
		when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
		when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

		// Create an Addon
		addon = new AOneBlock();
		File jFile = new File("addon.jar");
		try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {

			// Copy over config file from src folder
			Path fromPath = Paths.get("src/main/resources/config.yml");
			Path path = Paths.get("config.yml");
			Files.copy(fromPath, path);

			// Add the new files to the jar.
			add(path, tempJarOutputStream);

			// Copy over panels file from src folder
			fromPath = Paths.get("src/main/resources/panels/phases_panel.yml");
			path = Paths.get("panels");
			Files.createDirectory(path);
			path = Paths.get("panels/phases_panel.yml");
			Files.copy(fromPath, path);

			// Add the new files to the jar.
			add(path, tempJarOutputStream);
		}

		File dataFolder = new File("addons/AOneBlock");
		addon.setDataFolder(dataFolder);
		addon.setFile(jFile);
		AddonDescription desc = new AddonDescription.Builder("bentobox", "aoneblock", "1.3").description("test")
				.authors("tasty").build();
		addon.setDescription(desc);
		// Addons manager
		AddonsManager am = mock(AddonsManager.class);
		when(plugin.getAddonsManager()).thenReturn(am);

		// Flags manager
		when(plugin.getFlagsManager()).thenReturn(fm);
		when(fm.getFlags()).thenReturn(Collections.emptyList());

		// RanksManager
		RanksManager rm = new RanksManager();
		when(plugin.getRanksManager()).thenReturn(rm);

	}

	private void add(Path path, JarOutputStream tempJarOutputStream) throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(path.toFile())) {
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			JarEntry entry = new JarEntry(path.toString());
			tempJarOutputStream.putNextEntry(entry);
			while ((bytesRead = fis.read(buffer)) != -1) {
				tempJarOutputStream.write(buffer, 0, bytesRead);
			}
		}

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onEnable()}.
	 */
	@Test
	public void testOnEnable() {
		testOnLoad();
		addon.setState(State.ENABLED);
		addon.onEnable();
		verify(plugin, never()).logError(anyString());
		assertNotEquals(State.DISABLED, addon.getState());
		assertNotNull(addon.getBlockListener());
		assertNotNull(addon.getOneBlockManager());

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onLoad()}.
	 */
	@Test
	public void testOnLoad() {
		addon.onLoad();
		// Check that config.yml file has been saved
		File check = new File("addons/AOneBlock", "config.yml");
		assertTrue(check.exists());
		assertTrue(addon.getPlayerCommand().isPresent());
		assertTrue(addon.getAdminCommand().isPresent());

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onReload()}.
	 */
	@Test
	public void testOnReload() {
		addon.onEnable();
		addon.onReload();
		// Check that config.yml file has been saved
		File check = new File("addons/AOneBlock", "config.yml");
		assertTrue(check.exists());
	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#createWorlds()}.
	 */
	@Test
	public void testCreateWorlds() {
		addon.onLoad();
		addon.createWorlds();
		verify(plugin).log("[aoneblock] Creating AOneBlock world ...");
		verify(plugin).log("[aoneblock] Creating AOneBlock's Nether...");
		verify(plugin).log("[aoneblock] Creating AOneBlock's End World...");

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#getSettings()}.
	 */
	@Test
	public void testGetSettings() {
		addon.onLoad();
		assertNotNull(addon.getSettings());

	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getWorldSettings()}.
	 */
	@Test
	public void testGetWorldSettings() {
		addon.onLoad();
		assertEquals(addon.getSettings(), addon.getWorldSettings());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getOneBlocksIsland(world.bentobox.bentobox.database.objects.Island)}.
	 */
	@Test
	public void testGetOneBlocksIsland() {
		addon.onEnable();
		@NonNull
		OneBlockIslands i = addon.getOneBlocksIsland(island);
		assertEquals(island.getUniqueId(), i.getUniqueId());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getOneBlockManager()}.
	 */
	@Test
	public void testGetOneBlockManager() {
		assertNull(addon.getOneBlockManager());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getBlockListener()}.
	 */
	@Test
	public void testGetBlockListener() {
		assertNull(addon.getBlockListener());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getPlaceholdersManager()}.
	 */
	@Test
	public void testGetPlaceholdersManager() {
		assertNull(addon.getPlaceholdersManager());
	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#getHoloListener()}.
	 */
	@Test
	public void testGetHoloListener() {
		assertNull(addon.getHoloListener());
	}
}
