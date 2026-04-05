package world.bentobox.aoneblock.oneblocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.multilib.MultiLib;

/**
 * @author tastybento
 *
 */
public class OneBlocksManagerTest3 extends CommonTestSetup {

	private static File jFile;
	private static YamlConfiguration oneBlocks;
    @Mock
	private AddonsManager am;
	private OneBlocksManager obm;
	private OneBlockPhase obPhase;
	@Mock
	private @NonNull OneBlockIslands obi;

	@BeforeAll
	public static void beforeClass() throws IOException, InvalidConfigurationException {
		// Make the addon jar
		jFile = new File("addon.jar");
		// Copy over config file from src folder
		/*
		 * Path fromPath = Paths.get("src/main/resources/config.yml"); Path path =
		 * Paths.get("config.yml"); Files.copy(fromPath, path);
		 */
		// Dummy oneblocks.yml
		String oneblocks = """
                '0':
                  name: Plains
                  firstBlock: GRASS_BLOCK
                  biome: PLAINS
                  fixedBlocks:
                    0: GRASS_BLOCK
                    1: GRASS_BLOCK
                  holograms:
                    0: &aGood Luck!
                  blocks:
                    GRASS_BLOCK: 2000
                    BIRCH_LOG: 500
                  mobs:
                    SHEEP: 150
                    VILLAGER: 30
                '700':
                  name: Underground
                  firstBlock: STONE
                  biome: TAIGA
                  blocks:
                    EMERALD_ORE: 5
                    COBWEB: 250
                    DIRT: 500
                '11000':
                  gotoBlock: 0""";
		oneBlocks = new YamlConfiguration();
		oneBlocks.loadFromString(oneblocks);
		// Save
		File obFileDir = new File("phases");

		File obFile = new File(obFileDir, "0_plains.yml");
		obFileDir.mkdirs();
		oneBlocks.save(obFile);
		/*
		 * // Copy over block config file from src folder fromPath =
		 * Paths.get("src/main/resources/oneblocks.yml"); path =
		 * Paths.get("oneblocks.yml"); Files.copy(fromPath, path);
		 */
		try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
			// Added the new files to the jar.
			try (FileInputStream fis = new FileInputStream(obFile)) {
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				JarEntry entry = new JarEntry(obFile.toPath().toString());
				tempJarOutputStream.putNextEntry(entry);
				while ((bytesRead = fis.read(buffer)) != -1) {
					tempJarOutputStream.write(buffer, 0, bytesRead);
				}
			}
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	@BeforeEach
	public void setUp() throws Exception {
	    super.setUp();
		// Database
		AbstractDatabaseHandler<Object> h = Mockito.mock(AbstractDatabaseHandler.class);
		MockedStatic<DatabaseSetup> mockDb = Mockito.mockStatic(DatabaseSetup.class);
		DatabaseSetup dbSetup = Mockito.mock(DatabaseSetup.class);
		mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
		when(dbSetup.getHandler(Mockito.any())).thenReturn(h);
		when(h.saveObject(Mockito.any())).thenReturn(CompletableFuture.completedFuture(true));
		// Commands manager
		CommandsManager cm = Mockito.mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);
		// MultiLib - prevent BukkitImpl from checking Paper classloader
		Mockito.mockStatic(MultiLib.class);
		// Addon
        AOneBlock addon = new AOneBlock();
		File dataFolder = new File("addons/AOneBlock");
		addon.setDataFolder(dataFolder);
		addon.setFile(jFile);
		AddonDescription desc = new AddonDescription.Builder("bentobox", "AOneBlock", "1.3").description("test")
				.authors("tastybento").build();
		addon.setDescription(desc);
		// Addons manager
		when(plugin.getAddonsManager()).thenReturn(am);

		// Phase
		obPhase = new OneBlockPhase("0");

		// Class under test
		obm = new OneBlocksManager(addon);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@AfterEach
	public void tearDown() throws Exception {
	    super.tearDown();
		deleteAll(new File("database"));
	}

	@AfterAll
	public static void cleanUp() throws Exception {

		new File("addon.jar").delete();
		new File("config.yml").delete();

		deleteAll(new File("addons"));
		deleteAll(new File("phases"));
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#OneBlocksManager(world.bentobox.aoneblock.AOneBlock)}.
	 *
     */
	@Test
	public void testOneBlocksManager() {
		File f = new File("phases", "0_plains.yml");
		assertTrue(f.exists());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#loadPhases()}.
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	// @Ignore("Cannot deserialize objects right now")
	@Test
	public void testLoadPhases() throws NumberFormatException, IOException {
		obm.loadPhases();
		verify(plugin, never()).logError(anyString());
		assertEquals(Material.GRASS_BLOCK, obm.getPhase(0).getFirstBlock().getMaterial());
		assertEquals(Biome.PLAINS, obm.getPhase(0).getPhaseBiome());
		assertEquals("Plains", obm.getPhase(0).getPhaseName());
		assertNull(obm.getPhase(0).getGotoBlock());

		assertEquals(Material.STONE, obm.getPhase(700).getFirstBlock().getMaterial());
		assertEquals(Biome.TAIGA, obm.getPhase(700).getPhaseBiome());
		assertEquals("Underground", obm.getPhase(700).getPhaseName());
		assertNull(obm.getPhase(700).getGotoBlock());

		assertEquals(0, (int) obm.getPhase(11000).getGotoBlock());

	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhaseList()}.
	 * 
	 * @throws InvalidConfigurationException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	@Test
	public void testGetPhaseList() throws NumberFormatException, IOException, InvalidConfigurationException {
		testLoadPhases();
		List<String> l = obm.getPhaseList();
		assertEquals(2, l.size());
		assertEquals("Plains", l.get(0));
		assertEquals("Underground", l.get(1));

	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhase(java.lang.String)}.
	 * 
	 * @throws InvalidConfigurationException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	@Test
	public void testGetPhaseString() throws NumberFormatException, IOException, InvalidConfigurationException {
		testLoadPhases();
		assertFalse(obm.getPhase("sdf").isPresent());
		assertTrue(obm.getPhase("Plains").isPresent());
		assertTrue(obm.getPhase("Underground").isPresent());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#saveOneBlockConfig()}.
	 * 
	 * @throws NumberFormatException
	 * @throws IOException 
	 */
	@Disabled("Not saving")
	@Test
	public void testSaveOneBlockConfig() throws NumberFormatException, IOException {
		 testLoadPhases();
		 assertTrue(obm.saveOneBlockConfig());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(OneBlockPhase)}.
	 * 
	 * @throws InvalidConfigurationException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	@Test
	public void testGetNextPhase() throws NumberFormatException, IOException, InvalidConfigurationException {
		testLoadPhases();
		OneBlockPhase plains = obm.getPhase("Plains").get();
		OneBlockPhase underground = obm.getPhase("Underground").get();
		OneBlockPhase gotoPhase = obm.getPhase(11000);
		assertEquals(underground, obm.getNextPhase(plains));
		assertEquals(gotoPhase, obm.getNextPhase(underground));
		assertNull(obm.getNextPhase(gotoPhase));
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#copyPhasesFromAddonJar(java.io.File)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCopyPhasesFromAddonJar() throws IOException {
		File dest = new File("dest");
		dest.mkdir();
		obm.copyPhasesFromAddonJar(dest);
		File check = new File(dest, "0_plains.yml");
		assertTrue(check.exists());
		// Clean up
		deleteAll(dest);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#initBlock(java.lang.String, world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 * 
	 * @throws IOException
	 */
	@Test
	@Disabled
	public void testInitBlock() throws IOException {
		System.out.println(oneBlocks);
		obm.initBlock("0", obPhase, oneBlocks);
		assertEquals("", obPhase.getPhaseName());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	public void testAddFirstBlockBadMateril() {
		obm.addFirstBlock(obPhase, "shshhs");
		verify(plugin).logError("[AOneBlock] Bad firstBlock material: shshhs");
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	public void testAddFirstBlockNullMaterial() {
		obm.addFirstBlock(obPhase, null);
		assertNull(obPhase.getFirstBlock());
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	public void testAddFirstBlock() {
		obm.addFirstBlock(obPhase, "SPONGE");
		assertNotNull(obPhase.getFirstBlock());
		assertEquals(Material.SPONGE, obPhase.getFirstBlock().getMaterial());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addCommands(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	public void testAddCommands() {
		obm.addCommands(obPhase, oneBlocks);
		assertTrue(obPhase.getStartCommands().isEmpty());
		assertTrue(obPhase.getEndCommands().isEmpty());
		assertTrue(obPhase.getFirstTimeEndCommands().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addRequirements(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	public void testAddRequirements() {
		obm.addRequirements(obPhase, oneBlocks);
		assertTrue(obPhase.getRequirements().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addChests(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	public void testAddChests() throws IOException {
		obm.addChests(obPhase, oneBlocks);
		assertTrue(obPhase.getChests().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addMobs(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	public void testAddMobs() throws IOException {
		obm.addMobs(obPhase, oneBlocks);
		assertTrue(obPhase.getMobs().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addBlocks(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	public void testAddBlocks() {
		obm.addBlocks(obPhase, oneBlocks);
		assertTrue(obPhase.getBlocks().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhase(int)}.
	 */
	@Test
	public void testGetPhaseInt() {
		@Nullable
		OneBlockPhase phase = obm.getPhase(1);
		assertNull(phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getBlockProbs()}.
	 */
	@Test
	public void testGetBlockProbs() {
		NavigableMap<Integer, OneBlockPhase> probs = obm.getBlockProbs();
		assertNotNull(probs);
		assertTrue(probs.isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#savePhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	public void testSavePhase() {
		new File("addons/AOneBlock/phases").mkdirs();
		boolean result = obm.savePhase(obPhase);
		assertTrue(result);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	public void testGetNextPhaseOneBlockPhase() {
		@Nullable
		OneBlockPhase phase = obm.getNextPhase(obPhase);
		assertNull(phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhaseBlocks(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	public void testGetNextPhaseBlocks() {
		int phase = obm.getNextPhaseBlocks(obi);
		assertEquals(-1, phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPercentageDone(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	public void testGetPercentageDone() {
		double percent = obm.getPercentageDone(obi);
		assertEquals(0.0, percent, 0.0);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getProbs(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	public void testGetProbs() {
		obm.getProbs(obPhase);
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getAllProbs()}.
	 */
	@Test
	public void testGetAllProbs() {
		obm.getAllProbs();
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	public void testGetNextPhaseOneBlockIslands() {
		String phase = obm.getNextPhase(obi);
		assertEquals("", phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhaseBlocks(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	public void testGetPhaseBlocks() {
		assertEquals(-1, obm.getPhaseBlocks(obi));
	}

}
