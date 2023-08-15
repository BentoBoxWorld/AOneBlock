package world.bentobox.aoneblock.oneblocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.AddonsManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class})
public class OneBlocksManagerTest2 {

    private static File jFile;
    private static YamlConfiguration oneBlocks;
    @Mock
    private BentoBox plugin;
    private AOneBlock addon;
    @Mock
    private AddonsManager am;
    private OneBlocksManager obm;
    private OneBlockPhase obPhase;
    @Mock
    private @NonNull OneBlockIslands obi;

    @BeforeClass
    public static void beforeClass() throws IOException, InvalidConfigurationException {
        // Make the addon jar
        jFile = new File("addon.jar");
        // Copy over config file from src folder
        /*
        Path fromPath = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(fromPath, path);*/
        // Dummy oneblocks.yml
        String oneblocks =
                "'0':\n" +
                        "  name: Plains\n" +
                        "  icon: GRASS_BLOCK\n" +
                        "  firstBlock: GRASS_BLOCK\n" +
                        "  biome: PLAINS\n" +
                        "  fixedBlocks:\n" +
                        "    0: GRASS_BLOCK\n" +
                        "    1: GRASS_BLOCK\n" +
                        "  holograms:\n" +
                        "    0: &aGood Luck!\n" +
                        "  blocks:\n" +
                        "    GRASS_BLOCK: 2000\n" +
                        "    BIRCH_LOG: 500\n" +
                        "  mobs:\n" +
                        "    SHEEP: 150\n" +
                        "    VILLAGER: 30\n" +
                        "'700':\n" +
                        "  name: Underground\n" +
                        "  firstBlock: STONE\n" +
                        "  biome: TAIGA\n" +
                        "  blocks:\n" +
                        "    EMERALD_ORE: 5\n" +
                        "    COBWEB: 250\n" +
                        "    DIRT: 500\n" +
                        "'11000':\n" +
                        "  gotoBlock: 0";
        oneBlocks = new YamlConfiguration();
        oneBlocks.loadFromString(oneblocks);
        // Save
        File obFileDir = new File("phases");

        File obFile = new File(obFileDir, "0_plains.yml");
        obFileDir.mkdirs();
        oneBlocks.save(obFile);
        /*
        // Copy over block config file from src folder
        fromPath = Paths.get("src/main/resources/oneblocks.yml");
        path = Paths.get("oneblocks.yml");
        Files.copy(fromPath, path);
         */
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(obFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry entry = new JarEntry(obFile.toPath().toString());
                tempJarOutputStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        // Addon
        addon = new AOneBlock();
        File dataFolder = new File("addons/AOneBlock");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "AOneBlock", "1.3").description("test").authors("tastybento").build();
        addon.setDescription(desc);
        //addon.setSettings(new Settings());
        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class);
        ItemMeta meta = mock(ItemMeta.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(itemFactory.getItemMeta(any())).thenReturn(meta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        when(Bukkit.getUnsafe()).thenReturn(unsafe);

        // Phase
        obPhase = new OneBlockPhase("0");

        // Class under test
        obm = new OneBlocksManager(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        deleteAll(new File("database"));
    }

    @AfterClass
    public static void cleanUp() throws Exception {

        new File("addon.jar").delete();
        new File("config.yml").delete();

        deleteAll(new File("addons"));
        deleteAll(new File("phases"));
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
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#OneBlocksManager(world.bentobox.aoneblock.AOneBlock)}.
     * @throws IOException
     */
    @Test
    public void testOneBlocksManager() throws IOException {
        File f = new File("phases", "0_plains.yml");
        assertTrue(f.exists());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#loadPhases()}.
     * @throws InvalidConfigurationException
     * @throws IOException
     * @throws NumberFormatException
     */
    //@Ignore("Cannot deserialize objects right now")
    @Test
    public void testLoadPhases() throws NumberFormatException, IOException, InvalidConfigurationException {
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

        assertEquals(0, (int)obm.getPhase(11000).getGotoBlock());


    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhaseList()}.
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
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhase(java.lang.String)}.
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
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#saveOneBlockConfig()}.
     * @throws InvalidConfigurationException
     * @throws IOException
     * @throws NumberFormatException
     */
    @Ignore("Not saving")
    @Test
    public void testSaveOneBlockConfig() throws NumberFormatException, IOException, InvalidConfigurationException {
        //testLoadPhases();
        //assertTrue(obm.saveOneBlockConfig());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(OneBlockPhase)}.
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
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#copyPhasesFromAddonJar(java.io.File)}.
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
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#initBlock(java.lang.String, world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     * @throws IOException
     */
    @Test
    public void testInitBlock() throws IOException {
        System.out.println(oneBlocks);
        obm.initBlock("0", obPhase, oneBlocks);
        assertEquals("", obPhase.getPhaseName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addFirstBlock(world.bentobox.aoneblock.oneblocks.OneBlockPhase, java.lang.String)}.
     */
    @Test
    public void testAddFirstBlock() {
        obm.addFirstBlock(obPhase, "SPONGE");
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addCommands(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     */
    @Test
    public void testAddCommands() {
        obm.addCommands(obPhase, oneBlocks);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addRequirements(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     */
    @Test
    public void testAddRequirements() {
        obm.addRequirements(obPhase, oneBlocks);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addChests(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     */
    @Test
    public void testAddChests() throws IOException {
        obm.addChests(obPhase, oneBlocks);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addMobs(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     */
    @Test
    public void testAddMobs() throws IOException {
        obm.addMobs(obPhase, oneBlocks);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#addBlocks(world.bentobox.aoneblock.oneblocks.OneBlockPhase, org.bukkit.configuration.ConfigurationSection)}.
     */
    @Test
    public void testAddBlocks() throws IOException {
        obm.addBlocks(obPhase, oneBlocks);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPhase(int)}.
     */
    @Test
    public void testGetPhaseInt() {
        @Nullable
        OneBlockPhase phase = obm.getPhase(1);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getBlockProbs()}.
     */
    @Test
    public void testGetBlockProbs() {
        NavigableMap<Integer, OneBlockPhase> probs = obm.getBlockProbs();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#savePhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testSavePhase() {
        boolean result = obm.savePhase(obPhase);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testGetNextPhaseOneBlockPhase() {
        @Nullable
        OneBlockPhase phase = obm.getNextPhase(obPhase);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhaseBlocks(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
     */
    @Test
    public void testGetNextPhaseBlocks() {
        int phase = obm.getNextPhaseBlocks(obi);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getPercentageDone(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
     */
    @Test
    public void testGetPercentageDone() {
        double percent = obm.getPercentageDone(obi);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getProbs(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testGetProbs() {
        obm.getProbs(obPhase);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getAllProbs()}.
     */
    @Test
    public void testGetAllProbs() {
        obm.getAllProbs();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getNextPhase(world.bentobox.aoneblock.dataobjects.OneBlockIslands)}.
     */
    @Test
    public void testGetNextPhaseOneBlockIslands() {
        String phase = obm.getNextPhase(obi);
    }

}
