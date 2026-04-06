package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.Brushable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockObject;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * Tests for BlockListener focusing on onPlayerInteract and spawnBlock.
 *
 * @author tastybento
 */
class BlockListenerTest2 extends CommonTestSetup {

    // Class under test
    private BlockListener bl;

    @Mock
    AOneBlock addon;
    @Mock
    private Settings addonSettings;
    @Mock
    private OneBlocksManager obm;
    @Mock
    private HoloListener holoListener;
    @Mock
    private PlayersManager pm;
    @Mock
    private Bank bank;
    @Mock
    private Level level;

    private Island island;

    private @NonNull OneBlockIslands is;
    private @NonNull OneBlockPhase phase;

    @Mock
    private Block magicBlock;

    private MockedStatic<DatabaseSetup> mockDb;

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // Database setup (same pattern as BlockListenerTest)
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
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
        when(addon.getHoloListener()).thenReturn(holoListener);
        when(addon.getSettings()).thenReturn(addonSettings);
        when(addonSettings.getMobWarning()).thenReturn(0);
        when(addonSettings.isDropOnTop()).thenReturn(true);
        when(addonSettings.isClearBlocks()).thenReturn(false);

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
        island.setCenter(location);

        // OneBlockIslands data
        is = new OneBlockIslands(island.getUniqueId());
        is.setPhaseName("Plains");
        is.setBlockNumber(0);

        // Phase
        phase = new OneBlockPhase("0");
        phase.setPhaseName("Plains");
        phase.addBlock(Material.STONE, 100);

        // Players Manager
        when(pm.getName(any())).thenReturn("tastybento2");
        when(pm.getUser(any(UUID.class))).thenReturn(user);

        // Bank
        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        when(bm.getBalance(island)).thenReturn(new Money(100000D));
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));

        // Level
        when(level.getIslandLevel(eq(world), any())).thenReturn(1000L);
        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));

        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        when(location.getBlock()).thenReturn(magicBlock);

        // Magic block
        when(magicBlock.getLocation()).thenReturn(location);
        when(magicBlock.getWorld()).thenReturn(world);

        // IslandsManager - default: island at location is our island
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));

        // Util.getChunkAtAsync must return a CompletableFuture (used in onNewIsland)
        mockedUtil.when(() -> Util.getChunkAtAsync(any(Location.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        bl = new BlockListener(addon);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        mockDb.closeOnDemand();
        super.tearDown();
        deleteAll(new File("database"));
    }

    // =========================================================================
    // onPlayerInteract tests
    // =========================================================================

    /**
     * Helper to create a mock PlayerInteractEvent pointing at a suspicious block.
     */
    private PlayerInteractEvent makeInteractEvent(Action action, Block block, EquipmentSlot hand) {
        PlayerInteractEvent e = mock(PlayerInteractEvent.class);
        when(e.getPlayer()).thenReturn(mockPlayer);
        when(e.getAction()).thenReturn(action);
        when(e.getHand()).thenReturn(hand);
        when(e.getClickedBlock()).thenReturn(block);
        return e;
    }

    /**
     * Set up mockPlayer to be holding a brush.
     * Uses a mock ItemStack to avoid MockBukkit material-validation issues.
     */
    private void playerHoldsBrush() {
        ItemStack brush = mock(ItemStack.class);
        when(brush.getType()).thenReturn(Material.BRUSH);
        when(mockPlayer.getInventory()).thenReturn(inv);
        when(inv.getItemInMainHand()).thenReturn(brush);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Not in addon world → early return, block is unchanged.
     */
    @Test
    void testOnPlayerInteractNotInWorld() {
        when(addon.inWorld(world)).thenReturn(false);
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Action is not RIGHT_CLICK_BLOCK → early return.
     */
    @Test
    void testOnPlayerInteractNotRightClick() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        PlayerInteractEvent e = makeInteractEvent(Action.LEFT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Hand is OFF_HAND → early return.
     */
    @Test
    void testOnPlayerInteractOffHand() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.OFF_HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Clicked block is null → early return (no NPE).
     */
    @Test
    void testOnPlayerInteractNullBlock() {
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, null, EquipmentSlot.HAND);

        // Should not throw NPE
        assertDoesNotThrow(() -> bl.onPlayerInteract(e));
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Block is STONE (not suspicious) → early return.
     */
    @Test
    void testOnPlayerInteractWrongBlockType() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.STONE);
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Player is holding a STICK, not a BRUSH → early return.
     */
    @Test
    void testOnPlayerInteractWrongTool() {
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        ItemStack stick = mock(ItemStack.class);
        when(stick.getType()).thenReturn(Material.STICK);
        when(mockPlayer.getInventory()).thenReturn(inv);
        when(inv.getItemInMainHand()).thenReturn(stick);
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * No island at block location → early return.
     */
    @Test
    void testOnPlayerInteractNoIslandAtLocation() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        Location blockLoc = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(blockLoc);
        when(im.getIslandAt(blockLoc)).thenReturn(Optional.empty());
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Island exists at location but block is not the island center → early return.
     */
    @Test
    void testOnPlayerInteractBlockNotIslandCenter() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        // Block is at a different location than the island center
        Location blockLoc = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(blockLoc);
        // getIslandAt returns the island, but island.getCenter() != blockLoc
        when(im.getIslandAt(blockLoc)).thenReturn(Optional.of(island));
        // island.getCenter() is `location` (set in setUp), not `blockLoc`
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Block data is not Brushable → no action taken.
     */
    @Test
    void testOnPlayerInteractNotBrushableBlockData() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        when(magicBlock.getLocation()).thenReturn(location);
        // Non-Brushable block data
        when(magicBlock.getBlockData()).thenReturn(mock(org.bukkit.block.data.BlockData.class));
        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);

        bl.onPlayerInteract(e);

        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Brushing is in progress (dusted + 1 <= maxDusted) → increment dusted and update block data.
     */
    @Test
    void testOnPlayerInteractBrushingInProgress() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        when(magicBlock.getLocation()).thenReturn(location);

        Brushable brushable = mock(Brushable.class);
        when(magicBlock.getBlockData()).thenReturn(brushable);
        when(brushable.getDusted()).thenReturn(1);
        when(brushable.getMaximumDusted()).thenReturn(3);

        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);
        bl.onPlayerInteract(e);

        // dusted should be incremented to 2 and block data updated
        verify(brushable).setDusted(2);
        verify(magicBlock).setBlockData(brushable);
        // Block should NOT be set to AIR
        verify(magicBlock, never()).setType(Material.AIR);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Brushing is finished on SUSPICIOUS_GRAVEL → set block to AIR and play gravel break sound.
     */
    @Test
    void testOnPlayerInteractBrushingFinishedGravel() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        Location blockCenter = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(location);
        when(location.add(0.5, 0.5, 0.5)).thenReturn(blockCenter);
        when(blockCenter.getWorld()).thenReturn(world); // used by e.getWorld() if needed

        Brushable brushable = mock(Brushable.class);
        when(magicBlock.getBlockData()).thenReturn(brushable);
        when(brushable.getDusted()).thenReturn(3);
        when(brushable.getMaximumDusted()).thenReturn(3);

        // No BrushableBlock state (state is not a BrushableBlock)
        BlockState plainState = mock(BlockState.class);
        when(magicBlock.getState()).thenReturn(plainState);

        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);
        bl.onPlayerInteract(e);

        verify(magicBlock).setType(Material.AIR);
        verify(world).playSound(eq(blockCenter), eq(Sound.BLOCK_SUSPICIOUS_GRAVEL_BREAK), anyFloat(), anyFloat());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Brushing finished on SUSPICIOUS_SAND → play sand break sound.
     */
    @Test
    void testOnPlayerInteractBrushingFinishedSand() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_SAND);
        Location blockCenter = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(location);
        when(location.add(0.5, 0.5, 0.5)).thenReturn(blockCenter);

        Brushable brushable = mock(Brushable.class);
        when(magicBlock.getBlockData()).thenReturn(brushable);
        when(brushable.getDusted()).thenReturn(3);
        when(brushable.getMaximumDusted()).thenReturn(3);

        BlockState plainState = mock(BlockState.class);
        when(magicBlock.getState()).thenReturn(plainState);

        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);
        bl.onPlayerInteract(e);

        verify(magicBlock).setType(Material.AIR);
        verify(world).playSound(eq(blockCenter), eq(Sound.BLOCK_SUSPICIOUS_SAND_BREAK), anyFloat(), anyFloat());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Brushing finished and block state IS a BrushableBlock with a loot table → items are dropped.
     */
    @Test
    void testOnPlayerInteractBrushingFinishedWithLootTable() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        Location blockCenter = mock(Location.class);
        when(blockCenter.getWorld()).thenReturn(world);
        when(blockCenter.clone()).thenReturn(blockCenter); // LootContext.Builder calls location.clone()
        when(magicBlock.getLocation()).thenReturn(location);
        when(location.add(0.5, 0.5, 0.5)).thenReturn(blockCenter);

        Brushable brushable = mock(Brushable.class);
        when(magicBlock.getBlockData()).thenReturn(brushable);
        when(brushable.getDusted()).thenReturn(3);
        when(brushable.getMaximumDusted()).thenReturn(3);

        // Block state is a BrushableBlock with a loot table
        BrushableBlock bbState = mock(BrushableBlock.class);
        when(magicBlock.getState()).thenReturn(bbState);
        LootTable lootTable = mock(LootTable.class);
        when(bbState.getLootTable()).thenReturn(lootTable);
        ItemStack lootItem = mock(ItemStack.class);
        when(lootItem.getType()).thenReturn(Material.DIAMOND);
        when(lootTable.populateLoot(any(Random.class), any(LootContext.class))).thenReturn(List.of(lootItem));

        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);
        bl.onPlayerInteract(e);

        verify(magicBlock).setType(Material.AIR);
        verify(world).dropItemNaturally(eq(blockCenter), eq(lootItem));
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onPlayerInteract(PlayerInteractEvent)}
     * Brushing finished, BrushableBlock state but loot table is null → no items dropped.
     */
    @Test
    void testOnPlayerInteractBrushingFinishedNoLootTable() {
        playerHoldsBrush();
        when(magicBlock.getType()).thenReturn(Material.SUSPICIOUS_GRAVEL);
        Location blockCenter = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(location);
        when(location.add(0.5, 0.5, 0.5)).thenReturn(blockCenter);

        Brushable brushable = mock(Brushable.class);
        when(magicBlock.getBlockData()).thenReturn(brushable);
        when(brushable.getDusted()).thenReturn(3);
        when(brushable.getMaximumDusted()).thenReturn(3);

        BrushableBlock bbState = mock(BrushableBlock.class);
        when(magicBlock.getState()).thenReturn(bbState);
        when(bbState.getLootTable()).thenReturn(null);

        PlayerInteractEvent e = makeInteractEvent(Action.RIGHT_CLICK_BLOCK, magicBlock, EquipmentSlot.HAND);
        bl.onPlayerInteract(e);

        verify(magicBlock).setType(Material.AIR);
        verify(world, never()).dropItemNaturally(any(), any());
    }

    // =========================================================================
    // spawnBlock tests (via reflection — method is private)
    // =========================================================================

    /**
     * Calls the private spawnBlock method via reflection.
     */
    private void callSpawnBlock(OneBlockObject nextBlock, Block block) throws Exception {
        Method m = BlockListener.class.getDeclaredMethod("spawnBlock", OneBlockObject.class, Block.class);
        m.setAccessible(true);
        try {
            m.invoke(bl, nextBlock, block);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw ex;
        }
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — custom block.
     * When nextBlock is a custom block, execute() is called on it.
     */
    @Test
    void testSpawnBlockCustomBlock() throws Exception {
        OneBlockCustomBlock customBlock = mock(OneBlockCustomBlock.class);
        OneBlockObject nextBlock = new OneBlockObject(customBlock, 1);

        callSpawnBlock(nextBlock, magicBlock);

        verify(customBlock).execute(addon, magicBlock);
        verify(magicBlock, never()).setType(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — regular material (STONE).
     * The block is set to STONE without physics; no other side effects.
     */
    @Test
    void testSpawnBlockRegularMaterial() throws Exception {
        OneBlockObject nextBlock = new OneBlockObject(Material.STONE, 1);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.STONE, false);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — CHEST with items.
     * The block is set to CHEST and its inventory is populated from the OneBlockObject chest data.
     */
    @Test
    void testSpawnBlockChestWithItems() throws Exception {
        ItemStack diamond = mock(ItemStack.class);
        when(diamond.getType()).thenReturn(Material.DIAMOND);
        Map<Integer, ItemStack> chestContents = Map.of(0, diamond);
        OneBlockObject nextBlock = new OneBlockObject(chestContents, OneBlockObject.Rarity.COMMON);

        // Mock block state as Chest
        Chest chestState = mock(Chest.class);
        when(magicBlock.getState()).thenReturn(chestState);
        Inventory chestInv = mock(Inventory.class);
        when(chestState.getBlockInventory()).thenReturn(chestInv);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.CHEST, false);
        verify(chestInv).setItem(0, diamond);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — CHEST (UNCOMMON rarity).
     * The UNCOMMON rarity spawns particles; COMMON does not.
     */
    @Test
    void testSpawnBlockChestUncommonRarity() throws Exception {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.EMERALD);
        Map<Integer, ItemStack> contents = Map.of(0, item);
        OneBlockObject nextBlock = new OneBlockObject(contents, OneBlockObject.Rarity.UNCOMMON);

        Chest chestState = mock(Chest.class);
        when(magicBlock.getState()).thenReturn(chestState);
        Inventory chestInv = mock(Inventory.class);
        when(chestState.getBlockInventory()).thenReturn(chestInv);
        Location chestLoc = mock(Location.class);
        when(magicBlock.getLocation()).thenReturn(chestLoc);
        when(chestLoc.add(any(org.bukkit.util.Vector.class))).thenReturn(chestLoc);
        when(magicBlock.getWorld()).thenReturn(world);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.CHEST, false);
        // UNCOMMON rarity → particles spawned
        verify(world).spawnParticle(any(), any(Location.class), any(Integer.class),
                any(Double.class), any(Double.class), any(Double.class),
                any(Double.class), any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — OAK_LEAVES.
     * Leaves are set persistent so they don't decay.
     */
    @Test
    void testSpawnBlockLeaves() throws Exception {
        OneBlockObject nextBlock = new OneBlockObject(Material.OAK_LEAVES, 1);

        // Mock block state and data as Leaves
        BlockState state = mock(BlockState.class);
        Leaves leavesData = mock(Leaves.class);
        when(magicBlock.getState()).thenReturn(state);
        when(state.getBlockData()).thenReturn(leavesData);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.OAK_LEAVES, false);
        verify(leavesData).setPersistent(true);
        verify(magicBlock).setBlockData(leavesData);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — SUSPICIOUS_GRAVEL with loot table.
     * Block data is set, state is updated with a loot table.
     */
    @Test
    void testSpawnBlockSuspiciousGravelWithLootTable() throws Exception {
        OneBlockObject nextBlock = new OneBlockObject(Material.SUSPICIOUS_GRAVEL, 1);

        BrushableBlock bbState = mock(BrushableBlock.class);
        when(magicBlock.getState()).thenReturn(bbState);

        LootTable lootTable = mock(LootTable.class);
        mockedBukkit.when(() -> Bukkit.getLootTable(any(NamespacedKey.class))).thenReturn(lootTable);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.SUSPICIOUS_GRAVEL, false);
        verify(bbState).setLootTable(lootTable);
        verify(bbState).update(true, false);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — SUSPICIOUS_GRAVEL, no loot table found.
     * When Bukkit.getLootTable returns null, a warning is logged.
     */
    @Test
    void testSpawnBlockSuspiciousGravelNoLootTable() throws Exception {
        OneBlockObject nextBlock = new OneBlockObject(Material.SUSPICIOUS_GRAVEL, 1);

        BrushableBlock bbState = mock(BrushableBlock.class);
        when(magicBlock.getState()).thenReturn(bbState);

        mockedBukkit.when(() -> Bukkit.getLootTable(any(NamespacedKey.class))).thenReturn(null);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.SUSPICIOUS_GRAVEL, false);
        verify(bbState, never()).setLootTable(any());
        verify(bbState, never()).update(anyBoolean(), anyBoolean());
        // Warning should be logged
        verify(plugin).logWarning(anyString());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener} spawnBlock — SUSPICIOUS_SAND with loot table.
     */
    @Test
    void testSpawnBlockSuspiciousSandWithLootTable() throws Exception {
        OneBlockObject nextBlock = new OneBlockObject(Material.SUSPICIOUS_SAND, 1);

        BrushableBlock bbState = mock(BrushableBlock.class);
        when(magicBlock.getState()).thenReturn(bbState);

        LootTable lootTable = mock(LootTable.class);
        mockedBukkit.when(() -> Bukkit.getLootTable(any(NamespacedKey.class))).thenReturn(lootTable);

        callSpawnBlock(nextBlock, magicBlock);

        verify(magicBlock).setType(Material.SUSPICIOUS_SAND, false);
        verify(bbState).setLootTable(lootTable);
        verify(bbState).update(true, false);
    }

    // =========================================================================
    // onNewIsland / onDeletedIsland tests
    // =========================================================================

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onNewIsland(IslandCreatedEvent)}
     * Island is in addon world → setUp is called, hologram is initialized.
     */
    @Test
    void testOnNewIslandCreatedInWorld() {
        Island newIsland = mock(Island.class);
        when(newIsland.getWorld()).thenReturn(world);
        when(newIsland.getCenter()).thenReturn(location);
        String uid = UUID.randomUUID().toString();
        when(newIsland.getUniqueId()).thenReturn(uid);

        world.bentobox.bentobox.api.events.island.IslandCreatedEvent e =
                mock(world.bentobox.bentobox.api.events.island.IslandCreatedEvent.class);
        when(e.getIsland()).thenReturn(newIsland);

        bl.onNewIsland(e);

        // HoloListener.setUp should be called once
        verify(holoListener).setUp(eq(newIsland), any(OneBlockIslands.class), eq(true));
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onNewIsland(IslandCreatedEvent)}
     * Island is NOT in addon world → setUp is NOT called.
     */
    @Test
    void testOnNewIslandCreatedNotInWorld() {
        when(addon.inWorld(world)).thenReturn(false);

        Island newIsland = mock(Island.class);
        when(newIsland.getWorld()).thenReturn(world);

        world.bentobox.bentobox.api.events.island.IslandCreatedEvent e =
                mock(world.bentobox.bentobox.api.events.island.IslandCreatedEvent.class);
        when(e.getIsland()).thenReturn(newIsland);

        bl.onNewIsland(e);

        verify(holoListener, never()).setUp(any(), any(), anyBoolean());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onNewIsland(IslandResettedEvent)}
     * Island is in addon world → setUp is called, hologram is initialized.
     */
    @Test
    void testOnNewIslandResettedInWorld() {
        Island resetIsland = mock(Island.class);
        when(resetIsland.getWorld()).thenReturn(world);
        when(resetIsland.getCenter()).thenReturn(location);
        String uid = UUID.randomUUID().toString();
        when(resetIsland.getUniqueId()).thenReturn(uid);

        world.bentobox.bentobox.api.events.island.IslandResettedEvent e =
                mock(world.bentobox.bentobox.api.events.island.IslandResettedEvent.class);
        when(e.getIsland()).thenReturn(resetIsland);

        bl.onNewIsland(e);

        verify(holoListener).setUp(eq(resetIsland), any(OneBlockIslands.class), eq(true));
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onDeletedIsland(IslandDeleteEvent)}
     * Island is in addon world → island data is removed from cache and database.
     */
    @Test
    void testOnDeletedIslandInWorld() {
        Island toDelete = mock(Island.class);
        when(toDelete.getWorld()).thenReturn(world);
        String uid = UUID.randomUUID().toString();
        when(toDelete.getUniqueId()).thenReturn(uid);

        // Put an entry in the cache by pre-loading the island
        bl.getIsland(island); // primes the cache with island's UUID

        world.bentobox.bentobox.api.events.island.IslandDeleteEvent e =
                mock(world.bentobox.bentobox.api.events.island.IslandDeleteEvent.class);
        when(e.getIsland()).thenReturn(toDelete);

        assertDoesNotThrow(() -> bl.onDeletedIsland(e));

        // Confirm the method completes without exception (cache and DB deletion happened)
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onDeletedIsland(IslandDeleteEvent)}
     * Island is NOT in addon world → no deletion occurs.
     */
    @Test
    void testOnDeletedIslandNotInWorld() {
        when(addon.inWorld(world)).thenReturn(false);

        Island toDelete = mock(Island.class);
        when(toDelete.getWorld()).thenReturn(world);

        world.bentobox.bentobox.api.events.island.IslandDeleteEvent e =
                mock(world.bentobox.bentobox.api.events.island.IslandDeleteEvent.class);
        when(e.getIsland()).thenReturn(toDelete);

        assertDoesNotThrow(() -> bl.onDeletedIsland(e));
        // No exception; nothing to verify beyond the early return
    }

    // =========================================================================
    // onItemSpawn tests
    // =========================================================================

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onItemSpawn(ItemSpawnEvent)}
     * Drop-on-top feature is disabled → event is not cancelled.
     */
    @Test
    void testOnItemSpawnDropOnTopDisabled() {
        when(addonSettings.isDropOnTop()).thenReturn(false);

        Item item = mock(Item.class);
        ItemSpawnEvent e = mock(ItemSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);
        when(e.getEntity()).thenReturn(item);

        bl.onItemSpawn(e);

        verify(e, never()).setCancelled(true);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onItemSpawn(ItemSpawnEvent)}
     * Spawn is not in addon world → event is not cancelled.
     */
    @Test
    void testOnItemSpawnNotInWorld() {
        when(addon.inWorld(world)).thenReturn(false);

        Item item = mock(Item.class);
        ItemSpawnEvent e = mock(ItemSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);
        when(e.getEntity()).thenReturn(item);

        bl.onItemSpawn(e);

        verify(e, never()).setCancelled(true);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onItemSpawn(ItemSpawnEvent)}
     * Item spawns in world but NOT at island center → event is not cancelled.
     */
    @Test
    void testOnItemSpawnNotAtIslandCenter() {
        // Use a different location for the spawn (not the island center)
        Location spawnLoc = mock(Location.class);
        Block spawnBlock = mock(Block.class);
        Location spawnBlockLoc = mock(Location.class);
        when(spawnLoc.getWorld()).thenReturn(world);
        when(spawnLoc.getBlock()).thenReturn(spawnBlock);
        when(spawnBlock.getLocation()).thenReturn(spawnBlockLoc);
        // spawnBlockLoc != island.getCenter() (which is `location`)
        when(im.getIslandAt(spawnLoc)).thenReturn(Optional.of(island));

        Item item = mock(Item.class);
        ItemSpawnEvent e = mock(ItemSpawnEvent.class);
        when(e.getLocation()).thenReturn(spawnLoc);
        when(e.getEntity()).thenReturn(item);

        bl.onItemSpawn(e);

        verify(e, never()).setCancelled(true);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onItemSpawn(ItemSpawnEvent)}
     * Item spawns exactly at island center → event is cancelled and item is dropped one block higher.
     */
    @Test
    void testOnItemSpawnAtIslandCenter() {
        // island.getCenter() == location; location.getBlock() == magicBlock; magicBlock.getLocation() == location
        Location dropLoc = mock(Location.class);
        when(location.add(0.5, 1, 0.5)).thenReturn(dropLoc);

        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.getType()).thenReturn(Material.DIAMOND);
        Item item = mock(Item.class);
        when(item.getItemStack()).thenReturn(itemStack);

        ItemSpawnEvent e = mock(ItemSpawnEvent.class);
        when(e.getLocation()).thenReturn(location);
        when(e.getEntity()).thenReturn(item);

        bl.onItemSpawn(e);

        verify(e).setCancelled(true);
        verify(world).dropItem(dropLoc, itemStack);
    }

    // =========================================================================
    // onBlockBreak / onBlockBreakByMinion guard-clause tests
    // =========================================================================

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockBreak(BlockBreakEvent)}
     * Block is not in addon world → event is not processed.
     */
    @Test
    void testOnBlockBreakNotInWorld() {
        when(addon.inWorld(any(World.class))).thenReturn(false);
        Block b = mock(Block.class);
        when(b.getWorld()).thenReturn(world);
        when(b.getLocation()).thenReturn(location);
        BlockBreakEvent e = new BlockBreakEvent(b, mockPlayer);

        bl.onBlockBreak(e);

        assertFalse(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockBreak(BlockBreakEvent)}
     * Block is in world but there is no island at that location → event is not processed.
     */
    @Test
    void testOnBlockBreakNoIslandAtLocation() {
        Block b = mock(Block.class);
        when(b.getWorld()).thenReturn(world);
        Location otherLoc = mock(Location.class);
        when(b.getLocation()).thenReturn(otherLoc);
        when(im.getIslandAt(otherLoc)).thenReturn(Optional.empty());
        BlockBreakEvent e = new BlockBreakEvent(b, mockPlayer);

        bl.onBlockBreak(e);

        assertFalse(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockBreakByMinion(EntityInteractEvent)}
     * Entity is NOT an ARMOR_STAND → event is not processed.
     */
    @Test
    void testOnBlockBreakByMinionNotArmorStand() {
        Entity zombie = mock(Entity.class);
        EntityInteractEvent e = mock(EntityInteractEvent.class);
        when(e.getBlock()).thenReturn(magicBlock);
        when(e.getEntityType()).thenReturn(EntityType.ZOMBIE);
        when(magicBlock.getWorld()).thenReturn(world);

        bl.onBlockBreakByMinion(e);

        // Not an armor stand → early return, no processing
        verify(im, never()).getIslandAt(any());
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockBreakByMinion(EntityInteractEvent)}
     * Block is not in addon world → event is not processed.
     */
    @Test
    void testOnBlockBreakByMinionNotInWorld() {
        when(addon.inWorld(world)).thenReturn(false);

        Entity armorStand = mock(Entity.class);
        EntityInteractEvent e = mock(EntityInteractEvent.class);
        when(e.getBlock()).thenReturn(magicBlock);
        when(e.getEntityType()).thenReturn(EntityType.ARMOR_STAND);
        when(magicBlock.getWorld()).thenReturn(world);

        bl.onBlockBreakByMinion(e);

        verify(im, never()).getIslandAt(any());
    }

    // =========================================================================
    // onBlockBreak(PlayerBucketFillEvent) guard tests
    // =========================================================================

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#onBlockBreak(PlayerBucketFillEvent)}
     * Block is not in addon world → event is not processed.
     */
    @Test
    void testOnBucketFillNotInWorld() {
        when(addon.inWorld(any(World.class))).thenReturn(false);
        Block b = mock(Block.class);
        when(b.getWorld()).thenReturn(world);
        when(b.getLocation()).thenReturn(location);
        // PlayerBucketFillEvent needs a few more mocks
        PlayerBucketFillEvent e = mock(PlayerBucketFillEvent.class);
        when(e.getBlock()).thenReturn(b);

        bl.onBlockBreak(e);

        // Verify island lookup was not made
        verify(im, never()).getIslandAt(any());
    }

    // =========================================================================
    // getIsland / saveIsland tests
    // =========================================================================

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#getIsland(Island)}
     * If island is not in cache, a new OneBlockIslands is created and cached.
     */
    @Test
    void testGetIslandNotInCache() {
        OneBlockIslands result = bl.getIsland(island);

        // Should return a valid, non-null result
        assertTrue(result != null);
        assertTrue(result.getUniqueId().equals(island.getUniqueId()));

        // Second call should return the same cached object
        OneBlockIslands cached = bl.getIsland(island);
        assertTrue(result == cached);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#saveIsland(Island)}
     * Island in cache → save is attempted and returns a CompletableFuture.
     */
    @Test
    void testSaveIslandInCache() {
        // Put it in the cache
        bl.getIsland(island);

        var future = bl.saveIsland(island);
        assertTrue(future != null);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.BlockListener#saveIsland(Island)}
     * Island not in cache → returns a completed true future immediately.
     */
    @Test
    void testSaveIslandNotInCache() throws Exception {
        Island notCached = new Island();
        notCached.setUniqueId(UUID.randomUUID().toString());

        var future = bl.saveIsland(notCached);
        assertTrue(future.get());
    }

}
