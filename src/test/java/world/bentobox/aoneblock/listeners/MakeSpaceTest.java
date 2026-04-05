package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.events.BlockClearEvent;

/**
 * Tests for {@link MakeSpace}.
 */
public class MakeSpaceTest extends CommonTestSetup {

    @Mock
    private AOneBlock addon;
    @Mock
    private Settings settings;
    @Mock
    private Entity entity;
    @Mock
    private Block centerBlock;
    @Mock
    private Block blockAbove;

    /** Bounding box of a 1×2×1 entity standing at (0.5, 0, 0.5). */
    private static final BoundingBox NARROW_BB = new BoundingBox(0, 0, 0, 1, 2, 1);

    /** Bounding box of a 3×2×3 entity (e.g. elder guardian) centred at (0.5, 0, 0.5). */
    private static final BoundingBox WIDE_BB = new BoundingBox(-1, 0, -1, 2, 2, 2);

    /** Bounding box of a standard solid block at block-coordinate (0, 0, 0). */
    private static final BoundingBox SOLID_BLOCK_BB = new BoundingBox(0, 0, 0, 1, 1, 1);

    /** Bounding box that is far away and will not overlap any entity BB above. */
    private static final BoundingBox DISTANT_BLOCK_BB = new BoundingBox(10, 10, 10, 11, 11, 11);

    private MakeSpace makeSpace;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        when(addon.getSettings()).thenReturn(settings);
        when(settings.isWaterMobProtection()).thenReturn(true);

        // World basics
        when(world.getMaxHeight()).thenReturn(256);
        when(world.getBlockAt(any(Location.class))).thenReturn(centerBlock);

        // Default entity setup (non-water, narrow)
        when(entity.getWorld()).thenReturn(world);
        when(entity.getBoundingBox()).thenReturn(NARROW_BB);
        when(entity.getType()).thenReturn(EntityType.COW);

        // Default center block setup: solid block that overlaps NARROW_BB
        when(centerBlock.getBoundingBox()).thenReturn(SOLID_BLOCK_BB);
        when(centerBlock.getRelative(BlockFace.UP)).thenReturn(blockAbove);
        when(centerBlock.getBlockData()).thenReturn(mock(org.bukkit.block.data.BlockData.class));
        when(centerBlock.getType()).thenReturn(Material.STONE);

        // Spawn location
        when(location.getBlockX()).thenReturn(0);
        when(location.getBlockZ()).thenReturn(0);
        when(location.getX()).thenReturn(0.5);
        when(location.getZ()).thenReturn(0.5);

        makeSpace = new MakeSpace(addon);
    }

    // -------------------------------------------------------------------------
    // WATER_ENTITIES constant
    // -------------------------------------------------------------------------

    @Test
    public void testWaterEntitiesContainsGuardian() {
        assertTrue(MakeSpace.WATER_ENTITIES.contains(EntityType.GUARDIAN));
    }

    @Test
    public void testWaterEntitiesContainsElderGuardian() {
        assertTrue(MakeSpace.WATER_ENTITIES.contains(EntityType.ELDER_GUARDIAN));
    }

    @Test
    public void testWaterEntitiesDoesNotContainCow() {
        assertFalse(MakeSpace.WATER_ENTITIES.contains(EntityType.COW));
    }

    // -------------------------------------------------------------------------
    // makeSpace — narrow, non-water entity, overlapping block
    // -------------------------------------------------------------------------

    /**
     * A narrow non-water entity whose bounding box overlaps the center block should
     * cause that block to be broken and set to AIR.
     */
    @Test
    public void testMakeSpaceBreaksOverlappingBlock() {
        makeSpace.makeSpace(entity, location);

        verify(centerBlock, atLeastOnce()).breakNaturally();
        verify(centerBlock, atLeastOnce()).setType(Material.AIR);
    }

    /**
     * When the event is not cancelled, breakNaturally() must be called.
     */
    @Test
    public void testMakeSpaceEventNotCancelledProceedsNormally() {
        // pim.callEvent leaves the event uncancelled by default
        makeSpace.makeSpace(entity, location);

        verify(centerBlock, atLeastOnce()).breakNaturally();
    }

    /**
     * BlockClearEvent must be fired once per makeSpace call.
     */
    @Test
    public void testMakeSpaceFiresBlockClearEvent() {
        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(any(BlockClearEvent.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — cancellation
    // -------------------------------------------------------------------------

    /**
     * When the BlockClearEvent is cancelled, no blocks should be broken or set.
     */
    @Test
    public void testMakeSpaceCancelledEventPreventsBlockChanges() {
        doAnswer(invocation -> {
            BlockClearEvent e = invocation.getArgument(0);
            e.setCancelled(true);
            return null;
        }).when(pim).callEvent(any(BlockClearEvent.class));

        makeSpace.makeSpace(entity, location);

        verify(centerBlock, never()).breakNaturally();
        verify(centerBlock, never()).setType(any(Material.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — block does not overlap entity bounding box
    // -------------------------------------------------------------------------

    /**
     * A block whose bounding box does not overlap the entity's bounding box should
     * not be added to the air-block list and therefore not be broken.
     */
    @Test
    public void testMakeSpaceNoOverlapBlockNotBroken() {
        when(centerBlock.getBoundingBox()).thenReturn(DISTANT_BLOCK_BB);

        makeSpace.makeSpace(entity, location);

        verify(centerBlock, never()).breakNaturally();
        verify(centerBlock, never()).setType(Material.AIR);
    }

    // -------------------------------------------------------------------------
    // makeSpace — water entity
    // -------------------------------------------------------------------------

    /**
     * For a water entity with protection enabled, waterBlocks should contain the
     * centre block when it is not water.
     */
    @Test
    public void testMakeSpaceWaterEntitySetsWaterOnNonWaterBlock() {
        when(entity.getType()).thenReturn(EntityType.GUARDIAN);
        when(centerBlock.getBoundingBox()).thenReturn(SOLID_BLOCK_BB);
        // block is not Waterlogged and not WATER
        when(centerBlock.getType()).thenReturn(Material.STONE);

        makeSpace.makeSpace(entity, location);

        verify(centerBlock, atLeastOnce()).setType(Material.WATER);
    }

    /**
     * For a water entity with protection enabled, a Waterlogged block that is not
     * yet waterlogged should be set to waterlogged rather than replaced with water.
     */
    @Test
    public void testMakeSpaceWaterEntityWaterloggedBlock() {
        when(entity.getType()).thenReturn(EntityType.GUARDIAN);
        when(centerBlock.getBoundingBox()).thenReturn(SOLID_BLOCK_BB);

        Waterlogged waterlogged = mock(Waterlogged.class);
        when(waterlogged.isWaterlogged()).thenReturn(false);
        when(centerBlock.getBlockData()).thenReturn(waterlogged);

        makeSpace.makeSpace(entity, location);

        verify(waterlogged, atLeastOnce()).setWaterlogged(true);
        verify(centerBlock, never()).setType(Material.WATER);
    }

    /**
     * A block that already IS water should not be added to waterBlocks, so
     * setType(WATER) must not be called again.
     */
    @Test
    public void testMakeSpaceWaterEntitySkipsExistingWaterBlock() {
        when(entity.getType()).thenReturn(EntityType.GUARDIAN);
        when(centerBlock.getBoundingBox()).thenReturn(DISTANT_BLOCK_BB); // no overlap → not in airBlocks
        when(centerBlock.getType()).thenReturn(Material.WATER);
        // Not Waterlogged
        when(centerBlock.getBlockData()).thenReturn(mock(org.bukkit.block.data.BlockData.class));

        makeSpace.makeSpace(entity, location);

        verify(centerBlock, never()).setType(Material.WATER);
    }

    /**
     * When water mob protection is disabled, a water-entity type should not trigger
     * any water-block placement.
     */
    @Test
    public void testMakeSpaceWaterMobProtectionDisabledSkipsWater() {
        when(settings.isWaterMobProtection()).thenReturn(false);
        when(entity.getType()).thenReturn(EntityType.GUARDIAN);
        when(centerBlock.getBoundingBox()).thenReturn(SOLID_BLOCK_BB);
        when(centerBlock.getType()).thenReturn(Material.STONE);

        makeSpace.makeSpace(entity, location);

        verify(centerBlock, never()).setType(Material.WATER);
    }

    /**
     * For a water entity, an air block above the center block must be included so
     * that dolphins have breathing space (blockAbove must be broken).
     */
    @Test
    public void testMakeSpaceWaterEntityAddsAirBlockAbove() {
        when(entity.getType()).thenReturn(EntityType.GUARDIAN);
        // Make bounding box end below maxHeight so the "+1" check passes
        when(entity.getBoundingBox()).thenReturn(new BoundingBox(0, 0, 0, 1, 1, 1));
        when(world.getMaxHeight()).thenReturn(256);

        when(blockAbove.getBoundingBox()).thenReturn(DISTANT_BLOCK_BB); // above block won't overlap
        when(blockAbove.getBlockData()).thenReturn(mock(org.bukkit.block.data.BlockData.class));
        when(blockAbove.getType()).thenReturn(Material.STONE);

        makeSpace.makeSpace(entity, location);

        // blockAbove was registered as an air-block, so breakNaturally + setType(AIR) apply
        verify(blockAbove, atLeastOnce()).breakNaturally();
        verify(blockAbove, atLeastOnce()).setType(Material.AIR);
    }

    // -------------------------------------------------------------------------
    // makeSpace — wide entity (widthX > 1 and widthZ > 1)
    // -------------------------------------------------------------------------

    /**
     * For an entity wider than 1 block in both X and Z, getBlockAt is called for
     * each X/Z position within the bounding box in addition to the centre column.
     */
    @Test
    public void testMakeSpaceWideEntityProcessesExtraBlocks() {
        when(entity.getBoundingBox()).thenReturn(WIDE_BB);

        // All extra blocks also return centerBlock mock for simplicity
        when(world.getBlockAt(any(Location.class))).thenReturn(centerBlock);

        makeSpace.makeSpace(entity, location);

        // With a 3×2×3 BB and 2 Y-levels, getBlockAt is called many times.
        // The key assertion: event is still fired and blocks are processed.
        verify(pim).callEvent(any(BlockClearEvent.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — entity wide in X only
    // -------------------------------------------------------------------------

    /**
     * For an entity that is wider in X but not in Z (widthX > 1, widthZ <= 1),
     * extra blocks along the X axis are processed.
     */
    @Test
    public void testMakeSpaceWideXOnlyProcessesXBlocks() {
        // widthX = 2, widthZ = 1
        BoundingBox wideX = new BoundingBox(0, 0, 0, 2, 2, 1);
        when(entity.getBoundingBox()).thenReturn(wideX);
        when(world.getBlockAt(any(Location.class))).thenReturn(centerBlock);

        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(any(BlockClearEvent.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — entity wide in Z only
    // -------------------------------------------------------------------------

    /**
     * For an entity that is wider in Z but not in X (widthZ > 1, widthX <= 1),
     * extra blocks along the Z axis are processed.
     */
    @Test
    public void testMakeSpaceWideZOnlyProcessesZBlocks() {
        // widthX = 1, widthZ = 2
        BoundingBox wideZ = new BoundingBox(0, 0, 0, 1, 2, 2);
        when(entity.getBoundingBox()).thenReturn(wideZ);
        when(world.getBlockAt(any(Location.class))).thenReturn(centerBlock);

        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(any(BlockClearEvent.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — world max-height boundary
    // -------------------------------------------------------------------------

    /**
     * The Y loop must be clamped to world.getMaxHeight(), so blocks above that
     * limit are never queried.
     */
    @Test
    public void testMakeSpaceRespectsWorldMaxHeight() {
        // Entity BB from y=0 to y=500, but world only has 256 height
        when(entity.getBoundingBox()).thenReturn(new BoundingBox(0, 0, 0, 1, 500, 1));
        when(world.getMaxHeight()).thenReturn(256);

        // Should complete without error (no IndexOutOfBounds, etc.)
        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(any(BlockClearEvent.class));
    }

    // -------------------------------------------------------------------------
    // makeSpace — BlockClearEvent lists are populated correctly
    // -------------------------------------------------------------------------

    /**
     * The BlockClearEvent must carry the air-block list that contains the
     * overlapping centre block.
     */
    @Test
    public void testMakeSpaceBlockClearEventContainsAirBlock() {
        // centerBlock overlaps (SOLID_BLOCK_BB overlaps NARROW_BB)
        org.mockito.ArgumentCaptor<BlockClearEvent> captor =
                org.mockito.ArgumentCaptor.forClass(BlockClearEvent.class);

        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(captor.capture());
        List<Block> airBlocks = captor.getValue().getAirBlocks();
        assertTrue(airBlocks.contains(centerBlock));
    }

    /**
     * For a non-water entity, the waterBlocks list in the event must be empty.
     */
    @Test
    public void testMakeSpaceNonWaterEntityHasEmptyWaterBlocksList() {
        org.mockito.ArgumentCaptor<BlockClearEvent> captor =
                org.mockito.ArgumentCaptor.forClass(BlockClearEvent.class);

        makeSpace.makeSpace(entity, location);

        verify(pim).callEvent(captor.capture());
        assertTrue(captor.getValue().getWaterBlocks().isEmpty());
    }
}
