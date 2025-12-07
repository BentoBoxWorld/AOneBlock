package world.bentobox.aoneblock.commands.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.listeners.BossBarListener;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Test class
 */
class IslandBossBarCommandTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;

    @Mock
    private User user;
    @Mock
    private AOneBlock addon;

    private IslandBossBarCommand bbc;

    @Mock
    private BossBarListener bb;


    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(ac.getAddon()).thenReturn(addon);

        when(addon.getBossBar()).thenReturn(bb);

        when(user.getLocation()).thenReturn(location);

        when(im.getIslandAt(location)).thenReturn(Optional.of(island));

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        bbc = new IslandBossBarCommand(ac, settings.getBossBarCommand().split(" ")[0],
                settings.getBossBarCommand().split(" "));
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
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandBossBarCommand#IslandBossBarCommand(world.bentobox.bentobox.api.commands.CompositeCommand, java.lang.String, java.lang.String[])}.
     */
    @Test
    void testIslandBossBarCommand() {
        assertNotNull(bbc);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandBossBarCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("island.bossbar", bbc.getPermission());
        assertEquals("aoneblock.commands.island.bossbar.description", bbc.getDescription());
        assertTrue(bbc.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandBossBarCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        bbc.execute(user, "", List.of());
        verify(bb).toggleUser(user);
        verify(user).sendMessage("aoneblock.bossbar.not-active");
    }

}
