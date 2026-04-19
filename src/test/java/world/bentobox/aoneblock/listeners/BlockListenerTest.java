package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockFromToEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.level.Level;

/**
 * @author tastybento
 *
 */
public class BlockListenerTest extends CommonTestSetup {

    // Class under test
    private BlockListener bl;

    @Mock
    AOneBlock addon;
    @Mock
    private Settings pluginSettings;

    @Mock
    private OneBlocksManager obm;

    private Island island;

    @Mock
    private PlayersManager pm;
    @Mock
    private Bank bank;
    @Mock
    private Level level;

    private @NonNull OneBlockIslands is;

    private @NonNull OneBlockPhase phase;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // This has to be done beforeClass otherwise the tests will interfere with each other
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        // Database
        MockedStatic<DatabaseSetup> mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // Addon
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getPlayers()).thenReturn(pm);
        when(addon.getOverWorld()).thenReturn(world);
        when(addon.getIslands()).thenReturn(im);
        when(addon.inWorld(world)).thenReturn(true);

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

        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);

        bl = new BlockListener(addon);
    }

    /**
     * @throws java.lang.Exception - exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        deleteAll(new File("database"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    void testOnBlockFromToNotInWorld() {
        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    void testOnBlockFromToNotIslandCenter() {
        when(addon.inWorld(any(World.class))).thenReturn(true);
        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        when(from.getWorld()).thenReturn(world);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockFromTo(org.bukkit.event.block.BlockFromToEvent)}
     */
    @Test
    void testOnBlockFromToCenterBlock() {
        when(addon.inWorld(any(World.class))).thenReturn(true);
        island.setCenter(location);
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));

        Block from = mock(Block.class);
        BlockFace to = BlockFace.NORTH;
        when(from.getLocation()).thenReturn(location);
        when(from.getRelative(any())).thenReturn(from);
        when(from.getWorld()).thenReturn(world);
        BlockFromToEvent e = new BlockFromToEvent(from, to);
        bl.onBlockFromTo(e);
        assertTrue(e.isCancelled());
    }

}
