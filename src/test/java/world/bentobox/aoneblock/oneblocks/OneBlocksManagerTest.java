package world.bentobox.aoneblock.oneblocks;

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
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
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.AddonsManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class})
public class OneBlocksManagerTest {
    private static File jFile;
    private static YamlConfiguration oneBlocks;
    @Mock
    private BentoBox plugin;
    private AOneBlock addon;
    @Mock
    private AddonsManager am;
    private OneBlocksManager obm;

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
                        "  firstBlock: GRASS_BLOCK\n" +
                        "  biome: PLAINS\n" +
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
        File obFile = new File("oneblocks.yml");
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
    @SuppressWarnings("deprecation")
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
        new File("oneblocks.yml").delete();
        deleteAll(new File("addons"));
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
     */
    @Test
    public void testOneBlocksManager() {
        File f = new File(addon.getDataFolder(), "oneblocks.yml");
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
        assertTrue(obm.getPhaseList().size() == 3);
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

    @Ignore
    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getProbs(world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testGetProbs() {

    }

    @Ignore
    /**
     * Test method for {@link world.bentobox.aoneblock.oneblocks.OneBlocksManager#getAllProbs()}.
     */
    @Test
    public void testGetAllProbs() {
        fail("Not yet implemented"); // TODO
    }

}
