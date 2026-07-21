package world.bentobox.aoneblock.oneblocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
@SuppressWarnings("java:S3577")
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
     */
	@Override
	@AfterEach
	public void tearDown() throws Exception {
	    super.tearDown();
		deleteAll(new File("database"));
		cleanPhaseFiles();
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
	void testOneBlocksManager() {
		File f = new File("phases", "0_plains.yml");
		assertTrue(f.exists());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#loadPhases()}.
	 *
     */
	// @Ignore("Cannot deserialize objects right now")
	@Test
	void testLoadPhases() throws NumberFormatException, IOException {
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
     */
	@Test
	void testGetPhaseList() throws NumberFormatException, IOException {
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
     */
	@Test
	void testGetPhaseString() throws NumberFormatException, IOException {
		testLoadPhases();
		assertFalse(obm.getPhase("sdf").isPresent());
		assertTrue(obm.getPhase("Plains").isPresent());
		assertTrue(obm.getPhase("Underground").isPresent());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#saveOneBlockConfig()}.
	 *
     */
	@Disabled("Not saving")
	@Test
	void testSaveOneBlockConfig() throws NumberFormatException, IOException {
		 testLoadPhases();
		 assertTrue(obm.saveOneBlockConfig());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(OneBlockPhase)}.
	 *
     */
	@Test
	void testGetNextPhase() throws NumberFormatException, IOException {
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
     */
	@Test
	void testCopyPhasesFromAddonJar() throws IOException {
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
     */
	@Test
	@Disabled("TODO: fix initBlock test setup to work with new config structure")
	void testInitBlock() throws IOException {
		System.out.println(oneBlocks);
		obm.initBlock("0", obPhase, oneBlocks);
		assertEquals("", obPhase.getPhaseName());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	void testAddFirstBlockBadMateril() {
		obm.addFirstBlock(obPhase, "shshhs");
		verify(plugin).logError("[AOneBlock] Bad firstBlock material: shshhs");
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	void testAddFirstBlockNullMaterial() {
		obm.addFirstBlock(obPhase, null);
		assertNull(obPhase.getFirstBlock());
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
	 */
	@Test
	void testAddFirstBlock() {
		obm.addFirstBlock(obPhase, "SPONGE");
		assertNotNull(obPhase.getFirstBlock());
		assertEquals(Material.SPONGE, obPhase.getFirstBlock().getMaterial());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addCommands(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	void testAddCommands() {
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
	void testAddRequirements() {
		obm.addRequirements(obPhase, oneBlocks);
		assertTrue(obPhase.getRequirements().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addChests(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	void testAddChests() throws IOException {
		obm.addChests(obPhase, oneBlocks);
		assertTrue(obPhase.getChests().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addMobs(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	void testAddMobs() throws IOException {
		obm.addMobs(obPhase, oneBlocks);
		assertTrue(obPhase.getMobs().isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addBlocks(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
	 */
	@Test
	void testAddBlocks() {
		obm.addBlocks(obPhase, oneBlocks);
		assertTrue(obPhase.getBlocks().isEmpty());
	}

	/**
	 * Verifies that a phase can keep its existing map-form {@code blocks:} section
	 * untouched while adding custom entries via a sibling {@code custom-blocks:}
	 * list. This is the backwards-compatible entry point for mob-data / mythic-mob.
	 */
	@Test
	void testAddBlocksWithCustomBlocksSibling() throws InvalidConfigurationException {
		String yaml = """
                name: Plains
                blocks:
                  PODZOL: 40
                  DIRT: 1000
                custom-blocks:
                  - type: mob-data
                    data: minecraft:breeze{Glowing:1b}
                    underlying-block: STONE
                    probability: 50
                """;
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(yaml);

		obm.addBlocks(obPhase, cfg);

		// Two vanilla materials registered via the map form...
		assertEquals(2, obPhase.getBlocks().size());
		assertTrue(obPhase.getBlocks().containsKey(Material.PODZOL));
		assertTrue(obPhase.getBlocks().containsKey(Material.DIRT));
		// ...plus one custom block from the sibling list. Vanilla totals (40 + 1000)
		// plus the custom block probability (50) should appear in blockTotal.
		assertEquals(40 + 1000 + 50, obPhase.getBlockTotal());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhase(int)}.
	 */
	@Test
	void testGetPhaseInt() {
		@Nullable
		OneBlockPhase phase = obm.getPhase(1);
		assertNull(phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getBlockProbs()}.
	 */
	@Test
	void testGetBlockProbs() {
		NavigableMap<Integer, OneBlockPhase> probs = obm.getBlockProbs();
		assertNotNull(probs);
		assertTrue(probs.isEmpty());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#savePhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	void testSavePhase() {
		new File("addons/AOneBlock/phases").mkdirs();
		boolean result = obm.savePhase(obPhase);
		assertTrue(result);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	void testGetNextPhaseOneBlockPhase() {
		@Nullable
		OneBlockPhase phase = obm.getNextPhase(obPhase);
		assertNull(phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhaseBlocks(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	void testGetNextPhaseBlocks() {
		int phase = obm.getNextPhaseBlocks(obi);
		assertEquals(-1, phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPercentageDone(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	void testGetPercentageDone() {
		double percent = obm.getPercentageDone(obi);
		assertEquals(0.0, percent, 0.0);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getProbs(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
	 */
	@Test
	void testGetProbs() {
		obm.getProbs(obPhase);
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getAllProbs()}.
	 */
	@Test
	void testGetAllProbs() {
		obm.getAllProbs();
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	void testGetNextPhaseOneBlockIslands() {
		String phase = obm.getNextPhase(obi);
		assertEquals("", phase);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhaseBlocks(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
	 */
	@Test
	void testGetPhaseBlocks() {
		assertEquals(-1, obm.getPhaseBlocks(obi));
	}

	/**
	 * Test that a valid {@code CHEST_WITH_X} entry in fixedBlocks produces a chest
	 * OneBlockObject whose inventory contains the specified item at slot 0.
	 */
	@Test
	void testLoadPhases_fixedBlockChestWithItem() throws Exception {
		String yaml = """
                name: Plains
                biome: PLAINS
                fixedBlocks:
                  0: GRASS_BLOCK
                  5: CHEST_WITH_WATER_BUCKET
                blocks:
                  GRASS_BLOCK: 1000
                """;
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(yaml);

		// initBlock parses fixedBlocks and delegates to parseStringBlock -> parseChestWithItem
		obm.initBlock("0", obPhase, cfg);

		assertNotNull(obPhase.getFixedBlocks(), "fixedBlocks should not be null");

		// Slot 5 should be a chest containing a WATER_BUCKET
		OneBlockObject chest = obPhase.getFixedBlocks().get(5);
		assertNotNull(chest, "fixedBlocks should contain an entry at position 5");
		assertEquals(Material.CHEST, chest.getMaterial());
		assertNotNull(chest.getChest(), "Chest contents should not be null");
		assertFalse(chest.getChest().isEmpty(), "Chest should have contents");
		assertEquals(Material.WATER_BUCKET, chest.getChest().get(0).getType());

		// Slot 0 should be a regular GRASS_BLOCK (set as firstBlock via addFixedBlocks)
		assertEquals(Material.GRASS_BLOCK, obPhase.getFirstBlock().getMaterial());

		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#isVersionAtLeast(String, String)}.
	 */
	@Test
	void testIsVersionAtLeast() {
		assertTrue(OneBlocksManager.isVersionAtLeast("26.2", "26.2"));
		assertTrue(OneBlocksManager.isVersionAtLeast("26.2.1", "26.2"));
		assertTrue(OneBlocksManager.isVersionAtLeast("26.3", "26.2"));
		assertTrue(OneBlocksManager.isVersionAtLeast("1.21.11", "1.21.2"));
		assertTrue(OneBlocksManager.isVersionAtLeast("1.21.11-R0.1-SNAPSHOT", "1.21.11"));
		assertFalse(OneBlocksManager.isVersionAtLeast("1.21.11", "26.2"));
		assertFalse(OneBlocksManager.isVersionAtLeast("1.21", "1.21.11"));
		assertFalse(OneBlocksManager.isVersionAtLeast("", "26.2"));
		assertFalse(OneBlocksManager.isVersionAtLeast(null, "26.2"));
		assertFalse(OneBlocksManager.isVersionAtLeast("1.21.11", "not-a-version"));
	}

	private static final File PHASES_DIR = new File("addons/AOneBlock/phases");
	private static final File INDEX_FILE = new File("addons/AOneBlock/phases_index.yml");

	/**
	 * Removes the phases folder and index so each index test starts clean and
	 * leaves nothing behind for the other tests.
	 */
	private void cleanPhaseFiles() throws IOException {
		deleteAll(PHASES_DIR);
		java.nio.file.Files.deleteIfExists(INDEX_FILE.toPath());
	}

	/**
	 * A phase tagged with a newer Minecraft version than the server runs must be
	 * skipped with a plain log entry and no errors - and its files must never be
	 * parsed. The chest file here is deliberately invalid YAML: if the loader
	 * touched it, an error would be logged. The test server is mocked as 1.21.10
	 * in {@link CommonTestSetup}.
	 */
	@Test
	void testLoadPhasesSkipsPhaseRequiringNewerVersion() throws IOException {
		PHASES_DIR.mkdirs();
		String yaml = """
                '15000':
                  name: Sulfur Caves
                  requiredMinecraftVersion: '26.2'
                  firstBlock: SULFUR
                  biome: SULFUR_CAVES
                  blocks:
                    SULFUR: 100
                  mobs:
                    SULFUR_CUBE: 100
                """;
		java.nio.file.Files.writeString(new File(PHASES_DIR, "15000_sulfur_caves.yml").toPath(), yaml);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "15000_sulfur_caves_chests.yml").toPath(),
				"{{{{ this is not YAML :::");
		obm.loadPhases();
		assertTrue(obm.getBlockProbs().isEmpty());
		assertTrue(INDEX_FILE.exists(), "Index should have been generated from the phase files");
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("Skipping phase Sulfur Caves"));
		verify(plugin, never()).logError(anyString());
		verify(plugin, never()).logWarning(anyString());
	}

	/**
	 * A phase whose required version is satisfied by the server must load normally
	 * and keep the version tag. With the index, start blocks are the running sum
	 * of lengths, so a single phase starts at 0 regardless of its legacy key.
	 */
	@Test
	void testLoadPhasesLoadsPhaseWithSatisfiedVersion() throws IOException {
		PHASES_DIR.mkdirs();
		String yaml = """
                '15000':
                  name: Sulfur Caves
                  requiredMinecraftVersion: '1.21'
                  firstBlock: STONE
                  biome: PLAINS
                  blocks:
                    STONE: 100
                  mobs:
                    CAVE_SPIDER: 20
                """;
		java.nio.file.Files.writeString(new File(PHASES_DIR, "15000_sulfur_caves.yml").toPath(), yaml);
		obm.loadPhases();
		assertTrue(obm.getBlockProbs().containsKey(0));
		OneBlockPhase phase = obm.getPhase(0);
		assertEquals("Sulfur Caves", phase.getPhaseName());
		assertEquals(Material.STONE, phase.getFirstBlock().getMaterial());
		assertEquals("1.21", phase.getRequiredMinecraftVersion());
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * A disabled phase takes up no blocks: the next phase shifts down to fill the
	 * gap and the goto lands right after the last loaded phase.
	 */
	@Test
	void testLoadPhasesDisabledPhaseCollapses() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "beta.yml").toPath(), """
                '100':
                  name: Beta
                  biome: PLAINS
                  blocks:
                    STONE: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 100
                    enabled: false
                  - file: beta
                    section: '100'
                    name: Beta
                    length: 200
                gotoAtEnd: 0
                """);
		obm.loadPhases();
		assertEquals("Beta", obm.getPhase(0).getPhaseName());
		assertEquals(0, (int) obm.getPhase(200).getGotoBlock());
		assertEquals(2, obm.getBlockProbs().size());
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("Skipping phase Alpha"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Chest files are read without eager deserialization: an item that does not
	 * exist on this server version is skipped with a log line, the rest of the
	 * chest loads, and no errors are thrown.
	 */
	@Test
	void testLoadPhasesChestWithUnknownItemSkipsItem() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha_chests.yml").toPath(), """
                '0':
                  chests:
                    '1':
                      contents:
                        0:
                          ==: org.bukkit.inventory.ItemStack
                          DataVersion: 4438
                          id: minecraft:sulfur
                          count: 1
                          schema_version: 1
                        1:
                          ==: org.bukkit.inventory.ItemStack
                          DataVersion: 4438
                          id: minecraft:iron_ingot
                          count: 4
                          schema_version: 1
                      rarity: COMMON
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 100
                """);
		obm.loadPhases();
		OneBlockPhase phase = obm.getPhase(0);
		assertNotNull(phase);
		assertEquals(1, phase.getChests().size());
		assertEquals(1, phase.getChests().iterator().next().getChest().size());
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("Skipping item minecraft:sulfur"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Object-form block entries can carry a required Minecraft version; gated
	 * entries are skipped with a log line, satisfied ones load with their weight.
	 * The test server is mocked as 1.21.10.
	 */
	@Test
	void testAddBlocksVersionedEntries() throws InvalidConfigurationException {
		String yaml = """
                blocks:
                  STONE: 100
                  GOLD_BLOCK:
                    weight: 50
                    requiredMinecraftVersion: '26.2'
                  IRON_BLOCK:
                    weight: 25
                    requiredMinecraftVersion: '1.20'
                """;
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(yaml);
		obm.addBlocks(obPhase, cfg);
		assertTrue(obPhase.getBlocks().containsKey(Material.STONE));
		assertFalse(obPhase.getBlocks().containsKey(Material.GOLD_BLOCK));
		assertEquals(25, obPhase.getBlocks().get(Material.IRON_BLOCK));
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("Skipping block GOLD_BLOCK"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Object-form mob entries can carry a required Minecraft version, matching the
	 * block behaviour.
	 */
	@Test
	void testAddMobsVersionedEntries() throws InvalidConfigurationException, IOException {
		String yaml = """
                mobs:
                  ZOMBIE: 20
                  WITHER_SKELETON:
                    weight: 10
                    requiredMinecraftVersion: '26.2'
                  CAVE_SPIDER:
                    weight: 5
                    requiredMinecraftVersion: '1.19'
                """;
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(yaml);
		obm.addMobs(obPhase, cfg);
		assertEquals(20, obPhase.getMobs().get(org.bukkit.entity.EntityType.ZOMBIE));
		assertFalse(obPhase.getMobs().containsKey(org.bukkit.entity.EntityType.WITHER_SKELETON));
		assertEquals(5, obPhase.getMobs().get(org.bukkit.entity.EntityType.CAVE_SPIDER));
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("Skipping mob WITHER_SKELETON"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * After loading, the manager holds the live index model: every entry in phase
	 * order with its length, plus the goto target.
	 */
	@Test
	void testGetPhaseIndex() throws IOException {
		obm.loadPhases();
		List<PhaseIndexEntry> index = obm.getPhaseIndex();
		assertEquals(2, index.size());
		assertEquals("Plains", index.get(0).getName());
		assertEquals(700, index.get(0).getLength());
		assertEquals("Underground", index.get(1).getName());
		assertEquals(10300, index.get(1).getLength());
		assertTrue(index.get(0).isEnabled());
		assertEquals(0, (int) obm.getGotoAtEnd());
		// Loaded phases know their index entry
		assertEquals(index.get(0), obm.getPhase(0).getIndexEntry());
	}

	/**
	 * Reordering the live index, saving it, and reloading moves the phases: start
	 * blocks are recomputed from the new order's lengths.
	 */
	@Test
	void testReorderPhaseIndex() throws IOException {
		cleanPhaseFiles();
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "beta.yml").toPath(), """
                '100':
                  name: Beta
                  biome: PLAINS
                  blocks:
                    STONE: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 100
                  - file: beta
                    section: '100'
                    name: Beta
                    length: 200
                gotoAtEnd: 0
                """);
		obm.loadPhases();
		assertEquals("Alpha", obm.getPhase(0).getPhaseName());
		assertEquals("Beta", obm.getPhase(100).getPhaseName());
		// Swap the two phases and apply
		java.util.Collections.swap(obm.getPhaseIndex(), 0, 1);
		assertTrue(obm.saveIndex());
		obm.loadPhases();
		assertEquals("Beta", obm.getPhase(0).getPhaseName());
		assertEquals("Alpha", obm.getPhase(200).getPhaseName());
		assertEquals(0, (int) obm.getPhase(300).getGotoBlock());
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Saving an indexed phase writes back to the file it came from, under its
	 * stable section key - not to a file named after the computed start block.
	 */
	@Test
	void testSavePhaseKeepsIndexedFileName() throws IOException, InvalidConfigurationException {
		cleanPhaseFiles();
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '5000':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '5000'
                    name: Alpha
                    length: 100
                """);
		obm.loadPhases();
		OneBlockPhase phase = obm.getPhase(0);
		assertNotNull(phase);
		assertTrue(obm.savePhase(phase));
		// Same file, same section key, no new file named after the start block
		assertTrue(new File(PHASES_DIR, "alpha.yml").exists());
		assertFalse(new File(PHASES_DIR, "0_alpha.yml").exists());
		YamlConfiguration saved = new YamlConfiguration();
		saved.load(new File(PHASES_DIR, "alpha.yml"));
		assertTrue(saved.isConfigurationSection("5000"));
		assertEquals("Alpha", saved.getString("5000.name"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * A malformed index must not stop the addon: it logs an error and falls back
	 * to loading the phase files directly, as before the index existed.
	 */
	@Test
	void testLoadPhasesMalformedIndexFallsBack() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), "{{{{ this is not YAML :::");
		obm.loadPhases();
		assertEquals("Alpha", obm.getPhase(0).getPhaseName());
		verify(plugin).logError(org.mockito.ArgumentMatchers.contains("Could not load phases_index.yml"));
		verify(plugin).logWarning(org.mockito.ArgumentMatchers.contains("Phase index could not be used"));
	}

	/**
	 * An index entry whose file is missing is re-pointed at the folder file that
	 * holds a phase with the same name, and its length is refreshed from the gaps
	 * between the folder files' legacy start-block keys. This follows shipped
	 * file renames across addon versions without losing the server's layout.
	 */
	@Test
	void testReconcileRepointsRenamedFile() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "old_alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "beta.yml").toPath(), """
                '500':
                  name: Beta
                  biome: PLAINS
                  blocks:
                    STONE: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "1200_goto_0.yml").toPath(), """
                '1200':
                  gotoBlock: 0
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: new_alpha
                    section: '0'
                    name: Alpha
                    length: 100
                  - file: beta
                    section: '500'
                    name: Beta
                    length: 250
                """);
		obm.loadPhases();
		List<PhaseIndexEntry> index = obm.getPhaseIndex();
		assertEquals(2, index.size());
		assertEquals("old_alpha", index.get(0).getFile());
		assertEquals(500, index.get(0).getLength(), "Length should come from the folder's legacy keys");
		assertEquals(700, index.get(1).getLength(),
				"A matching entry's length should also be refreshed from the folder once repair was needed");
		assertEquals("Alpha", obm.getPhase(0).getPhaseName());
		assertEquals("Beta", obm.getPhase(500).getPhaseName());
		assertEquals(0, (int) obm.getGotoAtEnd(), "The folder's goto should be adopted");
		assertEquals(0, (int) obm.getPhase(1200).getGotoBlock());
		// The repaired index was saved
		String saved = java.nio.file.Files.readString(INDEX_FILE.toPath());
		assertTrue(saved.contains("old_alpha"));
		assertFalse(saved.contains("new_alpha"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * A phase file in the folder that the index does not know about is added to
	 * the index, positioned among the other entries by its legacy start-block
	 * key. Custom phases always show up.
	 */
	@Test
	void testReconcileAddsUnindexedFile() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "gamma.yml").toPath(), """
                '250':
                  name: Gamma
                  biome: PLAINS
                  blocks:
                    DIRT: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "beta.yml").toPath(), """
                '500':
                  name: Beta
                  biome: PLAINS
                  blocks:
                    STONE: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 250
                  - file: beta
                    section: '500'
                    name: Beta
                    length: 300
                """);
		obm.loadPhases();
		List<String> names = obm.getPhaseIndex().stream().map(PhaseIndexEntry::getName).toList();
		assertEquals(List.of("Alpha", "Gamma", "Beta"), names);
		assertEquals("Alpha", obm.getPhase(0).getPhaseName());
		assertEquals("Gamma", obm.getPhase(250).getPhaseName());
		assertEquals("Beta", obm.getPhase(500).getPhaseName());
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("added Gamma"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * An index entry whose file is neither in the folder nor matched by name is
	 * restored from the addon jar. This recovers shipped phases added by an
	 * upgrade, whose files are never copied into an existing phases folder.
	 */
	@Test
	void testReconcileRestoresShippedFileFromJar() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: 0_plains
                    section: '0'
                    name: Plains
                    length: 100
                """);
		obm.loadPhases();
		assertTrue(new File(PHASES_DIR, "0_plains.yml").exists(), "Phase file should be restored from the jar");
		assertEquals(1, obm.getPhaseIndex().size());
		assertEquals("Plains", obm.getPhase(0).getPhaseName());
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("restored missing 0_plains.yml"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * An index entry whose file is gone and cannot be recovered is removed with a
	 * warning, so the index - and the admin panel built from it - never shows
	 * phases that do not exist.
	 */
	@Test
	void testReconcileRemovesDeadEntry() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 100
                  - file: ghost
                    section: '900'
                    name: Ghost
                    length: 100
                """);
		obm.loadPhases();
		assertEquals(1, obm.getPhaseIndex().size());
		assertEquals("Alpha", obm.getPhaseIndex().get(0).getName());
		verify(plugin).logWarning(org.mockito.ArgumentMatchers.contains("removed Ghost"));
	}

	/**
	 * Section keys do not have to be numbers: a custom phase file with a plain
	 * key is discovered, added at the end of the index with the default length,
	 * its chest file loads (chests pair by file name, not by number), and a
	 * second load leaves the index unchanged.
	 */
	@Test
	void testReconcileAddsUnkeyedCustomFileWithChests() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "custom.yml").toPath(), """
                my_phase:
                  name: Custom
                  biome: PLAINS
                  blocks:
                    STONE: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "custom_chests.yml").toPath(), """
                my_phase:
                  chests:
                    '1':
                      contents:
                        0:
                          ==: org.bukkit.inventory.ItemStack
                          DataVersion: 4438
                          id: minecraft:iron_ingot
                          count: 4
                          schema_version: 1
                      rarity: COMMON
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 100
                """);
		obm.loadPhases();
		List<PhaseIndexEntry> index = obm.getPhaseIndex();
		assertEquals(2, index.size());
		assertEquals("Custom", index.get(1).getName());
		assertEquals("my_phase", index.get(1).getSection());
		assertEquals(OneBlocksManager.DEFAULT_PHASE_LENGTH, index.get(1).getLength());
		// Loads right after Alpha, with its chest
		OneBlockPhase custom = obm.getPhase(100);
		assertEquals("Custom", custom.getPhaseName());
		assertEquals(1, custom.getChests().size());
		verify(plugin).log(org.mockito.ArgumentMatchers.contains("added Custom"));
		verify(plugin, never()).logError(anyString());
		// Second load claims the unkeyed section - nothing changes
		obm.loadPhases();
		assertEquals(2, obm.getPhaseIndex().size());
		verify(plugin, times(1)).log(org.mockito.ArgumentMatchers.contains("added Custom"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * Once an admin has set lengths (adminLengths flag in the index), repair work
	 * still happens but lengths are never refreshed from the files' legacy keys,
	 * and the flag survives a save/reload round trip.
	 */
	@Test
	void testReconcileKeepsAdminLengths() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(new File(PHASES_DIR, "gamma.yml").toPath(), """
                '500':
                  name: Gamma
                  biome: PLAINS
                  blocks:
                    DIRT: 100
                """);
		// Admin-set length 123 differs from the 500 implied by the legacy keys
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 123
                adminLengths: true
                """);
		obm.loadPhases();
		// Gamma was added (a repair) but Alpha's admin-set length stuck
		assertEquals(2, obm.getPhaseIndex().size());
		assertEquals(123, obm.getPhaseIndex().get(0).getLength());
		// The flag survived the reconcile-triggered save
		String saved = java.nio.file.Files.readString(INDEX_FILE.toPath());
		assertTrue(saved.contains("adminLengths: true"));
		verify(plugin, never()).logError(anyString());
	}

	/**
	 * When index and folder already agree, reconciliation changes nothing and the
	 * index is not rewritten - admin-set order, lengths, and enabled flags stick.
	 */
	@Test
	void testReconcileNoChangeDoesNotRewriteIndex() throws IOException {
		PHASES_DIR.mkdirs();
		java.nio.file.Files.writeString(new File(PHASES_DIR, "alpha.yml").toPath(), """
                '0':
                  name: Alpha
                  biome: PLAINS
                  blocks:
                    GRASS_BLOCK: 100
                """);
		java.nio.file.Files.writeString(INDEX_FILE.toPath(), """
                phases:
                  - file: alpha
                    section: '0'
                    name: Alpha
                    length: 9999
                """);
		obm.loadPhases();
		assertEquals(9999, obm.getPhaseIndex().get(0).getLength());
		verify(plugin, never()).log(org.mockito.ArgumentMatchers.contains("Updated"));
		verify(plugin, never()).logError(anyString());
		verify(plugin, never()).logWarning(anyString());
	}

	/**
	 * Test that an invalid {@code CHEST_WITH_X} entry (unknown item) logs an error
	 * and is ignored — it must not appear in fixedBlocks.
	 */
	@Test
	void testLoadPhases_fixedBlockChestWithInvalidItem() throws Exception {
		String yaml = """
                name: Plains
                biome: PLAINS
                fixedBlocks:
                  6: CHEST_WITH_INVALID_ITEM_XYZ
                blocks:
                  GRASS_BLOCK: 1000
                """;
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.loadFromString(yaml);

		// initBlock parses fixedBlocks and delegates to parseStringBlock -> parseChestWithItem
		obm.initBlock("0", obPhase, cfg);

		// The invalid CHEST_WITH entry should be silently skipped (logged but not added)
		assertTrue(obPhase.getFixedBlocks().isEmpty(),
				"fixedBlocks should be empty because the item name is invalid");
		verify(plugin).logError(org.mockito.ArgumentMatchers.contains("CHEST_WITH item is invalid"));
	}

}
