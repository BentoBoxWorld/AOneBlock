package world.bentobox.aoneblock.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.PhaseIndexEntry;
import world.bentobox.bentobox.api.user.User;

/**
 * Tests for {@link AdminPhasesPanel} - the pick-up / drop / toggle logic.
 */
class AdminPhasesPanelTest extends CommonTestSetup {

    @Mock
    private AOneBlock addon;
    @Mock
    private OneBlocksManager obm;
    @Mock
    private User user;

    private List<PhaseIndexEntry> index;
    private AdminPhasesPanel panel;

    private PhaseIndexEntry entry(String name) {
        PhaseIndexEntry e = new PhaseIndexEntry();
        e.setFile(name.toLowerCase());
        e.setSection("0");
        e.setName(name);
        e.setLength(100);
        return e;
    }

    private List<String> names() {
        return index.stream().map(PhaseIndexEntry::getName).toList();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getOneBlockManager()).thenReturn(obm);
        index = new ArrayList<>(List.of(entry("Alpha"), entry("Beta"), entry("Gamma")));
        when(obm.getPhaseIndex()).thenReturn(index);
        when(obm.isPhaseAvailable(any())).thenReturn(true);
        when(obm.getBlockProbs()).thenReturn(new TreeMap<>());
        when(obm.getGotoAtEnd()).thenReturn(0);
        when(obm.saveIndex()).thenReturn(true);
        when(user.getTranslation(anyString())).thenAnswer(i -> i.getArgument(0, String.class));
        when(user.getTranslation(anyString(), any(String[].class))).thenAnswer(i -> i.getArgument(0, String.class));
        when(user.getPlayer()).thenReturn(mockPlayer);
        panel = new AdminPhasesPanel(addon, user);
    }

    /**
     * The panel builds without errors in both idle and holding states.
     */
    @Test
    void testBuild() {
        panel.build();
        panel.pickUp(1);
        panel.build();
        verify(addon, never()).logError(anyString());
    }

    /**
     * Picking up the first phase and dropping it on the second display position
     * shoves the others right: Alpha moves after Beta.
     */
    @Test
    void testPickUpAndDrop() throws IOException {
        panel.pickUp(0);
        panel.dropAt(1);
        assertEquals(List.of("Beta", "Alpha", "Gamma"), names());
        verify(obm).saveIndex();
        verify(obm).loadPhases();
        verify(user).sendMessage("aoneblock.commands.admin.phases.saved");
    }

    /**
     * Dropping at the end position appends the held phase.
     */
    @Test
    void testDropAtEnd() throws IOException {
        panel.pickUp(0);
        panel.dropAt(2);
        assertEquals(List.of("Beta", "Gamma", "Alpha"), names());
        verify(obm).saveIndex();
    }

    /**
     * Dropping a phase back where it came from is a no-op order-wise but still
     * persists cleanly.
     */
    @Test
    void testDropAtSamePlace() {
        panel.pickUp(1);
        panel.dropAt(1);
        assertEquals(List.of("Alpha", "Beta", "Gamma"), names());
    }

    /**
     * Putting a held phase back changes nothing and saves nothing.
     */
    @Test
    void testPutBack() throws IOException {
        panel.pickUp(2);
        panel.putBack();
        assertEquals(List.of("Alpha", "Beta", "Gamma"), names());
        verify(obm, never()).saveIndex();
        verify(obm, never()).loadPhases();
    }

    /**
     * Right-click toggling flips the enabled state and persists.
     */
    @Test
    void testToggle() throws IOException {
        PhaseIndexEntry beta = index.get(1);
        assertTrue(beta.isEnabled());
        panel.toggle(beta);
        assertFalse(beta.isEnabled());
        verify(obm).saveIndex();
        verify(obm).loadPhases();
        panel.toggle(beta);
        assertTrue(beta.isEnabled());
    }

    /**
     * A failed save tells the admin instead of failing silently.
     */
    @Test
    void testPersistFailure() {
        when(obm.saveIndex()).thenReturn(false);
        panel.pickUp(0);
        panel.dropAt(2);
        verify(user).sendMessage("aoneblock.commands.admin.phases.save-failed");
    }

    /**
     * A reload failure after saving is reported too.
     */
    @Test
    void testReloadFailure() throws IOException {
        org.mockito.Mockito.doThrow(new IOException("boom")).when(obm).loadPhases();
        panel.pickUp(0);
        panel.dropAt(2);
        verify(addon).logError("Could not reload phases: boom");
        verify(user).sendMessage("aoneblock.commands.admin.phases.save-failed");
    }
}
