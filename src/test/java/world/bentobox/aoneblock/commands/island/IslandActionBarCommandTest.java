package world.bentobox.aoneblock.commands.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Test class
 */
class IslandActionBarCommandTest extends CommonTestSetup {
    
    @Mock
    private CompositeCommand ac;

    @Mock
    private User user;
    @Mock
    private AOneBlock addon;
    
    private IslandActionBarCommand abc;


    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        
     // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        
        abc = new IslandActionBarCommand(ac, settings.getActionBarCommand().split(" ")[0],
                settings.getActionBarCommand().split(" "));
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
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandActionBarCommand#IslandActionBarCommand(world.bentobox.bentobox.api.commands.CompositeCommand, java.lang.String, java.lang.String[])}.
     */
    @Test
    void testIslandActionBarCommand() {
        assertNotNull(abc);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandActionBarCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("island.actionbar", abc.getPermission());
        assertEquals("aoneblock.commands.island.actionbar.description", abc.getDescription());
        assertTrue(abc.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandActionBarCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        abc.execute(user, "", List.of());
        verify(user).getMetaData("aoneblock.actionbar");
        verify(user).putMetaData(eq("aoneblock.actionbar"), any());
        verify(user).sendMessage("aoneblock.commands.island.actionbar.status_off");
    }

}
