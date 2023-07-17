package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, DatabaseSetup.class, Util.class})
public class CheckPhaseTest {

    @Mock
    BentoBox plugin;
    @Mock
    AOneBlock addon;
    @Mock
    private World world;
    @Mock
    private PlayersManager pm;
    @Mock
    private IslandsManager im;

    private @NonNull OneBlockIslands is;

    private @NonNull OneBlockPhase phase;
    @Mock
    private OneBlocksManager obm;
    @Mock
    private @Nullable Player player;
    private Island island;
    private CheckPhase bl;
    private User user;
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
    @Before
    public void setUp() throws Exception {
        // Bukkit
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // Addon
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getOverWorld()).thenReturn(world);
        when(addon.getIslands()).thenReturn(im);
        when(addon.inWorld(world)).thenReturn(true);
        when(addon.getBlockListener()).thenReturn(blis);

        // Player
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("tastybento");
        when(player.isOnline()).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        User.setPlugin(plugin);
        user = User.getInstance(player);

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
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String)a.getArgument(1, String.class));


        bl = new CheckPhase(addon, blis);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#checkPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    public void testCheckPhase() {
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

        assertTrue(bl.checkPhase(player, island, is, phase));
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify title shown
        verify(player).sendTitle("Next Phase", null, -1, -1, -1);

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#checkPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    public void testCheckPhaseSecondTime() {
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

        assertTrue(bl.checkPhase(player, island, is, phase));
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous, never()).getFirstTimeEndCommands();
        // Verify title shown
        verify(player).sendTitle("Next Phase", null, -1, -1, -1);

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#checkPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    public void testCheckPhaseNullPlayer() {
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

        assertTrue(bl.checkPhase(null, island, is, phase));
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify title shown
        verify(player).sendTitle("Next Phase", null, -1, -1, -1);

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#checkPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    public void testCheckPhaseNPCPlayer() {
        when(player.hasMetadata("NPC")).thenReturn(true);
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

        assertTrue(bl.checkPhase(player, island, is, phase));
        // Verify commands run
        verify(previous).getEndCommands();
        verify(previous).getFirstTimeEndCommands();
        // Verify title shown
        verify(player).sendTitle("Next Phase", null, -1, -1, -1);

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.CheckPhase#checkPhase(Player, Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}
     */
    @Test
    public void testCheckSamePhase() {
        is = new OneBlockIslands(UUID.randomUUID().toString());
        is.setPhaseName("Previous");
        is.setBlockNumber(500);
        is.setLifetime(500L);
        // The phase the user has just moved to
        phase = new OneBlockPhase("500");
        phase.setPhaseName("Previous");

        assertFalse(bl.checkPhase(player, island, is, phase));

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#replacePlaceholders(org.bukkit.entity.Player, java.lang.String, java.lang.String, world.bentobox.bentobox.database.objects.Island, java.util.List)}.
     */
    @Test
    public void testReplacePlaceholders() {
        // Commands
        /*
         * [island] - Island name
         * [owner] - Island owner's name
         * [player] - The name of the player who broke the block triggering the commands
         * [phase] - the name of this phase
         * [blocks] - the number of blocks broken
         * [level] - island level (Requires Levels Addon)
         * [bank-balance] - island bank balance (Requires Bank Addon)
         * [eco-balance] - player's economy balance (Requires Vault and an economy plugin)

         */
        List<String> commandList = new ArrayList<>();

        commandList.add("no replacement");
        commandList.add("[island] [owner] [phase] [blocks] [level] [bank-balance] [eco-balance]");
        List<String> r = bl.replacePlaceholders(player, "phaseName", "1000", island, commandList);
        assertEquals(2, r.size());
        assertEquals("no replacement", r.get(0));
        assertEquals("island_name tastybento2 phaseName 1000 1000 100000.0 0.0", r.get(1));
        verify(phm, times(2)).replacePlaceholders(eq(player), any());
    }
}
