package world.bentobox.aoneblock.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.PhaseIndexEntry;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Tests for {@link AdminPhasesCommand}
 */
class AdminPhasesCommandTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private AOneBlock addon;
    @Mock
    private OneBlocksManager oneBlocksManager;

    private AdminPhasesCommand cmd;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getOneBlockManager()).thenReturn(oneBlocksManager);
        cmd = new AdminPhasesCommand(ac);
        // Inject the addon mock so canExecute/execute can call addon.getOneBlockManager()
        java.lang.reflect.Field addonField = AdminPhasesCommand.class.getDeclaredField("addon");
        addonField.setAccessible(true);
        addonField.set(cmd, addon);
        when(user.isPlayer()).thenReturn(true);
        when(user.getPlayer()).thenReturn(mockPlayer);
        when(user.getTranslation(anyString())).thenAnswer(i -> i.getArgument(0, String.class));
        when(user.getTranslation(anyString(), any(String[].class))).thenAnswer(i -> i.getArgument(0, String.class));
    }

    /**
     * Test that the command is created successfully with the right settings.
     */
    @Test
    void testSetup() {
        assertNotNull(cmd);
        assertEquals("admin.phases", cmd.getPermission());
        assertEquals("aoneblock.commands.admin.phases.description", cmd.getDescription());
        assertTrue(cmd.isOnlyPlayer());
    }

    /**
     * Test canExecute with args shows help and returns false.
     */
    @Test
    void testCanExecuteWithArgs() {
        assertFalse(cmd.canExecute(user, "phases", List.of("something")));
    }

    /**
     * Test canExecute when no index is loaded sends a message and returns false.
     */
    @Test
    void testCanExecuteNoIndex() {
        when(oneBlocksManager.getPhaseIndex()).thenReturn(List.of());
        assertFalse(cmd.canExecute(user, "phases", List.of()));
        verify(user).sendMessage("aoneblock.commands.admin.phases.no-index");
    }

    /**
     * Test canExecute with an index returns true.
     */
    @Test
    void testCanExecuteWithIndex() {
        when(oneBlocksManager.getPhaseIndex()).thenReturn(List.of(new PhaseIndexEntry()));
        assertTrue(cmd.canExecute(user, "phases", List.of()));
    }

    /**
     * Test execute opens the panel.
     */
    @Test
    void testExecute() {
        PhaseIndexEntry entry = new PhaseIndexEntry();
        entry.setFile("alpha");
        entry.setName("Alpha");
        entry.setLength(100);
        when(oneBlocksManager.getPhaseIndex()).thenReturn(new java.util.ArrayList<>(List.of(entry)));
        when(oneBlocksManager.isPhaseAvailable(any())).thenReturn(true);
        when(oneBlocksManager.getBlockProbs()).thenReturn(new TreeMap<>());
        assertTrue(cmd.execute(user, "phases", List.of()));
    }
}
