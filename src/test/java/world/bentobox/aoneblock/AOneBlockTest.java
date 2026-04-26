package world.bentobox.aoneblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;

/**
 * @author tastybento
 *
 */
public class AOneBlockTest extends CommonTestSetup {

    @Mock
    private User user;

    private AOneBlock addon;
    @Mock
    private FlagsManager fm;
    @Mock
    private Settings settings;

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
        new File("config.yml").delete();
        deleteAll(new File("addons"));
        deleteAll(new File("panels"));
    }

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // This has to be done beforeClass otherwise the tests will interfere with each
        // other
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        // Database
        MockedStatic<DatabaseSetup> mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));


        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(settings);
        when(settings.getDatabaseType()).thenReturn(value);
        
        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(mockPlayer);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Player has island to begin with
        when(im.getIsland(any(), any(UUID.class))).thenReturn(island);

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

    }

    private void add(Path path, JarOutputStream tempJarOutputStream) throws IOException {
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
    void testOnEnable() {
        testOnLoad();
        addon.setState(State.ENABLED);
        addon.onEnable();
        assertNotEquals(State.DISABLED, addon.getState());
        assertNotNull(addon.getBlockListener());
        assertNotNull(addon.getOneBlockManager());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#onLoad()}.
     */
    @Test
    void testOnLoad() {
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
    void testOnReload() {
        addon.onLoad();
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
    void testCreateWorlds() {
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
    void testGetSettings() {
        addon.onLoad();
        assertNotNull(addon.getSettings());

    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.AOneBlock#getWorldSettings()}.
     */
    @Test
    void testGetWorldSettings() {
        addon.onLoad();
        assertEquals(addon.getSettings(), addon.getWorldSettings());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.AOneBlock#getOneBlocksIsland(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetOneBlocksIsland() {
        addon.onLoad();
        addon.onEnable();
        @NonNull
        OneBlockIslands i = addon.getOneBlocksIsland(island);
        assertEquals(island.getUniqueId(), i.getUniqueId());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#loadData()} when
     * oneBlockManager is null (addon not yet enabled). Should return false without
     * throwing NullPointerException.
     */
    @Test
    void testLoadDataWhenManagerIsNull() {
        // oneBlockManager is null before onEnable() is called
        assertNull(addon.getOneBlockManager());
        // Should not throw NPE, should return false (no error)
        assertFalse(addon.loadData());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.AOneBlock#getOneBlockManager()}.
     */
    @Test
    void testGetOneBlockManager() {
        assertNull(addon.getOneBlockManager());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.AOneBlock#getBlockListener()}.
     */
    @Test
    void testGetBlockListener() {
        assertNull(addon.getBlockListener());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.AOneBlock#getPlaceholdersManager()}.
     */
    @Test
    void testGetPlaceholdersManager() {
        assertNull(addon.getPlaceholdersManager());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#getHoloListener()}.
     */
    @Test
    void testGetHoloListener() {
        assertNull(addon.getHoloListener());
    }
}
