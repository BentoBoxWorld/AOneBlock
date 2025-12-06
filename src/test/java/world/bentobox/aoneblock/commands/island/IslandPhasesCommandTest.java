package world.bentobox.aoneblock.commands.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * Test class
 */
class IslandPhasesCommandTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private AOneBlock addon;
    @Mock
    private OneBlocksManager obm;

    private IslandPhasesCommand ipc;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(ac.getAddon()).thenReturn(addon);
        when(obm.getBlockProbs()).thenReturn(new TreeMap<>());
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getIslandsManager()).thenReturn(im);
        AddonDescription desc = new AddonDescription.Builder("name", "", "").build();
        when(addon.getDescription()).thenReturn(desc);

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        ipc = new IslandPhasesCommand(ac, settings.getPhasesCommand().split(" ")[0],
                settings.getPhasesCommand().split(" "));
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandPhasesCommand#IslandPhasesCommand(world.bentobox.bentobox.api.commands.CompositeCommand, java.lang.String, java.lang.String[])}.
     */
    @Test
    void testIslandPhasesCommand() {
        assertNotNull(ipc);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandPhasesCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("phases", ipc.getPermission());
        assertEquals("aoneblock.commands.phases.description", ipc.getDescription());
        assertTrue(ipc.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandPhasesCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        ipc.execute(user, "", List.of());
        verify(addon).logError("There are no available phases for selection!");
        verify(user).sendMessage("no-phases",
                TextVariables.GAMEMODE, this.addon.getDescription().getName());
    }

}
