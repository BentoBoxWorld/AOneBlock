package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.PhaseSound;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.level.Level;

/**
 * @author tastybento
 *
 */
public class CheckPhaseTest extends CommonTestSetup {

    @Mock
    AOneBlock addon;
    @Mock
    private PlayersManager pm;

    private @NonNull OneBlockIslands is;

    private @NonNull OneBlockPhase phase;
    @Mock
    private OneBlocksManager obm;
    private Island island;
    private CheckPhase bl;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private BlockListener blis;
    @Mock
    private Bank bank;
    @Mock
    private Level level;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Addon
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getOverWorld()).thenReturn(world);
        when(addon.getIslands()).thenReturn(im);
        when(addon.inWorld(world)).thenReturn(true);
        when(addon.getBlockListener()).thenReturn(blis);

        // Player
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("tastybento");
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.getWorld()).thenReturn(world);
        User.setPlugin(plugin);
        User user = User.getInstance(mockPlayer);

        // Island
        island = new Island();
        island.setOwner(UUID.randomUUID());
        island.setName("island_name");
        // Players Manager
        when(pm.getName(any())).thenReturn("tastybento2");
        when(pm.getUser(any(UUID.class))).thenReturn(user);

        // Bank
        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        // Phat balance to start
        when(bm.getBalance(island)).thenReturn(new Money(100000D));
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));
        // Level
        when(level.getIslandLevel(eq(world), any())).thenReturn(1000L);
        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));

        // Placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String) a.getArgument(1, String.class));

        bl = new CheckPhase(addon, blis);
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
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testSetNewPhase() {
        // Set up that a phase has been completed
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        // The phase the user has just moved to
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartCommands(List.of("start1", "start2"));

        // The previous phase
        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");

        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(mockPlayer, island, is, phase);
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify phase name change
        assertEquals("Next Phase", is.getPhaseName());
        // Verify title shown with correct phase name
        ArgumentCaptor<Title> titleCaptor = ArgumentCaptor.forClass(Title.class);
        verify(mockPlayer).showTitle(titleCaptor.capture());
        assertEquals(Component.text("Next Phase"), titleCaptor.getValue().title());

    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testSetNewPhaseSecondTime() {
        // Set up that a phase has been completed
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(10500L);
        // The phase the user has just moved to
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartCommands(List.of("start1", "start2"));

        // The previous phase
        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");

        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(mockPlayer, island, is, phase);
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous, never()).getFirstTimeEndCommands();
        // Verify phase name change
        assertEquals("Next Phase", is.getPhaseName());
        // Verify title shown with correct phase name
        ArgumentCaptor<Title> titleCaptor = ArgumentCaptor.forClass(Title.class);
        verify(mockPlayer).showTitle(titleCaptor.capture());
        assertEquals(Component.text("Next Phase"), titleCaptor.getValue().title());

    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testSetNewPhaseNullPlayer() {
        // Set up that a phase has been completed
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        // The phase the user has just moved to
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartCommands(List.of("start1", "start2"));

        // The previous phase
        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");

        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(null, island, is, phase);
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify phase name change
        assertEquals("Next Phase", is.getPhaseName());
        // Verify title shown with correct phase name
        ArgumentCaptor<Title> titleCaptor = ArgumentCaptor.forClass(Title.class);
        verify(mockPlayer).showTitle(titleCaptor.capture());
        assertEquals(Component.text("Next Phase"), titleCaptor.getValue().title());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testCheckPhaseNPCPlayer() {
        when(mockPlayer.hasMetadata("NPC")).thenReturn(true);
        // Set up that a phase has been completed
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        // The phase the user has just moved to
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartCommands(List.of("start1", "start2"));

        // The previous phase
        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");

        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(mockPlayer, island, is, phase);
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify phase name change
        assertEquals("Next Phase", is.getPhaseName());
        // Verify title shown with correct phase name
        ArgumentCaptor<Title> titleCaptor = ArgumentCaptor.forClass(Title.class);
        verify(mockPlayer).showTitle(titleCaptor.capture());
        assertEquals(Component.text("Next Phase"), titleCaptor.getValue().title());

    }

    /**
     * Test that the previous phase's end-sound and the new phase's start-sound are
     * both played to the player, with the configured volume and pitch.
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testSetNewPhasePlaysSounds() {
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartSound(new PhaseSound("minecraft:ui.toast.challenge_complete", 1F, 1F));

        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");
        when(previous.getEndSound()).thenReturn(new PhaseSound("custompack:phase.complete", 0.5F, 1.2F));
        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(mockPlayer, island, is, phase);

        // End sound of the phase being completed, with its volume/pitch
        verify(mockPlayer).playSound(any(Location.class), eq("custompack:phase.complete"), eq(0.5F), eq(1.2F));
        // Start sound of the phase being entered
        verify(mockPlayer).playSound(any(Location.class), eq("minecraft:ui.toast.challenge_complete"), eq(1F),
                eq(1F));
    }

    /**
     * Test that phase sounds are suppressed (like the title) when the resolved
     * user - e.g. the island owner on the NPC/minion path - is online but in a
     * different world. Test method for
     * {@link world.bentobox.aoneblock.listeners.CheckPhase#setNewPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    void testSetNewPhaseSoundSuppressedInOtherWorld() {
        // Player is online but in another world (addon.inWorld(otherWorld) is false by default)
        World otherWorld = mock(World.class);
        when(mockPlayer.getWorld()).thenReturn(otherWorld);

        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Next Phase");
        phase.setStartSound(new PhaseSound("minecraft:ui.toast.challenge_complete", 1F, 1F));

        OneBlockPhase previous = mock(OneBlockPhase.class);
        when(previous.getPhaseName()).thenReturn("Previous");
        when(previous.getEndSound()).thenReturn(new PhaseSound("custompack:phase.complete", 1F, 1F));
        when(obm.getPhase("Previous")).thenReturn(Optional.of(previous));

        bl.setNewPhase(mockPlayer, island, is, phase);

        // Neither the title nor any sound should be sent to an out-of-world player
        verify(mockPlayer, never()).showTitle(any(Title.class));
        verify(mockPlayer, never()).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
        // ...but end-commands still run: command execution must NOT be coupled to the
        // online/in-world notify gate that sounds and the title use.
        verify(previous).getEndCommands();
        // Phase change still happens
        assertEquals("Next Phase", is.getPhaseName());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#replacePlaceholders(org.bukkit.entity.Player, java.lang.String, java.lang.String, world.bentobox.bentobox.database.objects.Island, java.util.List)}.
     */
    @Test
    void testReplacePlaceholders() {
        // Commands
        /*
         * [island] - Island name [owner] - Island owner's name [player] - The name of
         * the player who broke the block triggering the commands [phase] - the name of
         * this phase [blocks] - the number of blocks broken [level] - island level
         * (Requires Levels Addon) [bank-balance] - island bank balance (Requires Bank
         * Addon) [eco-balance] - player's economy balance (Requires Vault and an
         * economy plugin)
         * 
         */
        List<String> commandList = new ArrayList<>();

        commandList.add("no replacement");
        commandList.add("[island] [owner] [phase] [blocks] [level] [bank-balance] [eco-balance]");
        List<String> r = bl.replacePlaceholders(mockPlayer, "phaseName", "1000", island, commandList);
        assertEquals(2, r.size());
        assertEquals("no replacement", r.get(0));
        assertEquals("island_name tastybento2 phaseName 1000 1000 100000.0 0.0", r.get(1));
        verify(phm, times(2)).replacePlaceholders(eq(mockPlayer), any());
    }
}
