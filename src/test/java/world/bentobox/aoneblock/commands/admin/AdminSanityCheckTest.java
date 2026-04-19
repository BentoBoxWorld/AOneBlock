package world.bentobox.aoneblock.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * Tests for {@link AdminSanityCheck}
 */
class AdminSanityCheckTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;

    @Mock
    private User user;

    @Mock
    private AOneBlock addon;

    @Mock
    private OneBlocksManager oneBlocksManager;

    @Mock
    private OneBlockPhase phase;

    private AdminSanityCheck cmd;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getOneBlockManager()).thenReturn(oneBlocksManager);
        when(oneBlocksManager.getPhaseList()).thenReturn(List.of("PLAINS", "UNDERGROUND"));
        cmd = new AdminSanityCheck(ac);
        // Inject the addon mock so canExecute/execute can call addon.getOneBlockManager()
        java.lang.reflect.Field addonField = AdminSanityCheck.class.getDeclaredField("addon");
        addonField.setAccessible(true);
        addonField.set(cmd, addon);
        when(user.isPlayer()).thenReturn(true);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that the command is created successfully.
     */
    @Test
    void testAdminSanityCheck() {
        assertNotNull(cmd);
    }

    /**
     * Test setup() uses the correct "sanity" locale keys (not "setchest" keys).
     */
    @Test
    void testSetup() {
        assertEquals("admin.sanity", cmd.getPermission());
        assertEquals("aoneblock.commands.admin.sanity.description", cmd.getDescription());
        assertEquals("aoneblock.commands.admin.sanity.parameters", cmd.getParameters());
    }

    /**
     * Test canExecute with no args returns true.
     */
    @Test
    void testCanExecuteNoArgs() {
        assertTrue(cmd.canExecute(user, "sanity", List.of()));
    }

    /**
     * Test canExecute with too many args returns false and shows help.
     */
    @Test
    void testCanExecuteTooManyArgs() {
        assertFalse(cmd.canExecute(user, "sanity", List.of("PLAINS", "extra")));
    }

    /**
     * Test canExecute with an unknown phase sends the sanity locale key (not setchest).
     */
    @Test
    void testCanExecuteUnknownPhase() {
        when(oneBlocksManager.getPhase("UNKNOWN")).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "sanity", List.of("unknown")));
        verify(user).sendMessage("aoneblock.commands.admin.sanity.unknown-phase");
    }

    /**
     * Test canExecute with a valid phase returns true.
     */
    @Test
    void testCanExecuteKnownPhase() {
        when(oneBlocksManager.getPhase("PLAINS")).thenReturn(Optional.of(phase));
        assertTrue(cmd.canExecute(user, "sanity", List.of("plains")));
    }

    /**
     * Test execute with no args runs getAllProbs and sends the sanity see-console message.
     */
    @Test
    void testExecuteNoArgs() {
        assertTrue(cmd.execute(user, "sanity", List.of()));
        verify(oneBlocksManager).getAllProbs();
        verify(user).sendMessage("aoneblock.commands.admin.sanity.see-console");
    }

    /**
     * Test execute with a phase arg runs getProbs(phase) and sends the sanity see-console message.
     */
    @Test
    void testExecuteWithPhase() {
        // Set the phase field via canExecute first
        when(oneBlocksManager.getPhase("PLAINS")).thenReturn(Optional.of(phase));
        cmd.canExecute(user, "sanity", List.of("plains"));
        assertTrue(cmd.execute(user, "sanity", List.of("plains")));
        verify(oneBlocksManager).getProbs(phase);
        verify(user).sendMessage("aoneblock.commands.admin.sanity.see-console");
    }

}
