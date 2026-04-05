package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;

/**
 * @author tastybento
 *
 */
public class BlockProtectTest extends CommonTestSetup {
    
    private BlockProtect bp;
    @Mock
    AOneBlock addon;
    @Mock
    private Block block;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
 
        when(mockPlayer.getWorld()).thenReturn(world);
        // In World
        when(addon.inWorld(world)).thenReturn(true);
        
        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        
        // Block
        when(block.getWorld()).thenReturn(world);
        when(block.getLocation()).thenReturn(location);
        
        // Island Manager
        when(addon.getIslands()).thenReturn(im);
        when(island.getCenter()).thenReturn(location);
        when(im.getIslandAt(any())).thenReturn(Optional.of(island));
            
        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        // Class under test
        bp = new BlockProtect(addon);
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
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#BlockProtect(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    void testBlockProtect() {
        assertNotNull(bp);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(PlayerInteractEvent)}.
     */
    @Test
    void testOnBlockDamage() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        PlayerInteractEvent blockDamageEvent = new PlayerInteractEvent(mockPlayer, Action.LEFT_CLICK_BLOCK, item, block,
                BlockFace.UP);
        bp.onBlockDamage(blockDamageEvent);
        verify(addon).inWorld(world);
        verify(im).getIslandAt(location);
        verify(world, times(48)).spawnParticle(eq(Particle.DUST), eq(null), eq(5),
                eq(0.1D), eq(0D), eq(0.1D), eq(1D), any(Particle.DustOptions.class));
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(PlayerInteractEvent)}.
     */
    @Test
    void testOnBlockDamageWrongWorld() {
        when(mockPlayer.getWorld()).thenReturn(mock(World.class));
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        PlayerInteractEvent blockDamageEvent = new PlayerInteractEvent(mockPlayer, Action.LEFT_CLICK_BLOCK, item, block,
                BlockFace.UP);
        bp.onBlockDamage(blockDamageEvent);
        verify(im, never()).getIslandAt(location);
        verify(world, never()).spawnParticle(any(Particle.class), any(Location.class),anyInt(), 
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(PlayerInteractEvent)}.
     */
    @Test
    void testOnBlockDamageNotCenterMagicBlock() {
        when(block.getLocation()).thenReturn(mock(Location.class));
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        PlayerInteractEvent blockDamageEvent = new PlayerInteractEvent(mockPlayer, Action.LEFT_CLICK_BLOCK, item, block,
                BlockFace.UP);
        bp.onBlockDamage(blockDamageEvent);
        verify(addon).inWorld(world);
        verify(im).getIslandAt(any(Location.class));
        verify(world, never()).spawnParticle(any(Particle.class), any(Location.class),anyInt(), 
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockChange(org.bukkit.event.entity.EntityChangeBlockEvent)}.
     */
    @Test
    void testOnBlockChange() {
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(mockPlayer, block, blockData);
        bp.onBlockChange(event);
        assertTrue(event.isCancelled());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockChange(org.bukkit.event.entity.EntityChangeBlockEvent)}.
     */
    @Test
    void testOnBlockChangeWrongWorld() {
        when(block.getWorld()).thenReturn(mock(World.class));
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(mockPlayer, block, blockData);
        bp.onBlockChange(event);
        assertFalse(event.isCancelled());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockChange(org.bukkit.event.entity.EntityChangeBlockEvent)}.
     */
    @Test
    void testOnBlockChangeNotMagicBlock() {
        when(block.getLocation()).thenReturn(mock(Location.class));
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(mockPlayer, block, blockData);
        bp.onBlockChange(event);
        assertFalse(event.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onExplosion(org.bukkit.event.entity.EntityExplodeEvent)}.
     */
    @Test
    void testOnExplosion() {
        List<Block> blocks = new ArrayList<>();
        EntityExplodeEvent event = new EntityExplodeEvent(mockPlayer, location, blocks, 0, null);
        bp.onExplosion(event);
        assertTrue(blocks.isEmpty());
        // Add the magic block
        blocks.add(block);
        // Magic block does not get blown up so it is removed
        bp.onExplosion(event);
        assertTrue(blocks.isEmpty());
        // Normal blocks remain
        Block b = mock(Block.class);
        when(b.getLocation()).thenReturn(mock(Location.class));
        blocks.add(b);
        bp.onExplosion(event);
        assertFalse(blocks.isEmpty());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onExplosion(org.bukkit.event.entity.EntityExplodeEvent)}.
     */
    @Test
    void testOnExplosionWrongWorld() {
        when(location.getWorld()).thenReturn(mock(World.class));
        List<Block> blocks = new ArrayList<>();
        EntityExplodeEvent event = new EntityExplodeEvent(mockPlayer, location, blocks, 0, null);
        blocks.add(block);
        // Block as correct location, but wrong world
        bp.onExplosion(event);
        assertFalse(blocks.isEmpty());
        // Normal blocks remain
        blocks.add(mock(Block.class));
        event = new EntityExplodeEvent(mockPlayer, location, blocks, 0, null);
        bp.onExplosion(event);
        assertFalse(blocks.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent)}.
     */
    @Test
    void testOnPistonExtendBlockPistonExtendEvent() {
        List<Block> blocks = new ArrayList<>();
        BlockPistonExtendEvent event = new BlockPistonExtendEvent(block, blocks, BlockFace.EAST);
        bp.onPistonExtend(event);
        assertFalse(event.isCancelled());
        blocks.add(block);
        bp.onPistonExtend(event);
        assertTrue(event.isCancelled());
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent)}.
     */
    @Test
    void testOnPistonExtendBlockPistonExtendEventWrongWorld() {
        when(block.getWorld()).thenReturn(mock(World.class));
        List<Block> blocks = new ArrayList<>();
        BlockPistonExtendEvent event = new BlockPistonExtendEvent(block, blocks, BlockFace.EAST);
        bp.onPistonExtend(event);
        assertFalse(event.isCancelled());
        blocks.add(block);
        bp.onPistonExtend(event);
        assertFalse(event.isCancelled());
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onPistonRetract(BlockPistonRetractEvent)}.
     */
    @Test
    void testOnPistonBlockPistonRetractEvent() {
        List<Block> blocks = new ArrayList<>();
        BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, blocks, BlockFace.EAST);
        bp.onPistonRetract(event);
        assertFalse(event.isCancelled());
        blocks.add(block);
        bp.onPistonRetract(event);
        assertTrue(event.isCancelled());
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onPistonRetract(BlockPistonRetractEvent)}.
     */
    @Test
    void testOnPistonBlockPistonRetractEventWrongWorld() {
        when(block.getWorld()).thenReturn(mock(World.class));
        List<Block> blocks = new ArrayList<>();
        BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, blocks, BlockFace.EAST);
        bp.onPistonRetract(event);
        assertFalse(event.isCancelled());
        blocks.add(block);
        bp.onPistonRetract(event);
        assertFalse(event.isCancelled());
        
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onFallingBlockSpawn(org.bukkit.event.entity.EntitySpawnEvent)}.
     */
    @Test
    void testOnFallingBlockSpawn() {
        Entity fallingBlock = mock(Entity.class);
        when(fallingBlock.getLocation()).thenReturn(location);
        when(fallingBlock.getType()).thenReturn(EntityType.FALLING_BLOCK);
        when(fallingBlock.getWorld()).thenReturn(world);
        EntitySpawnEvent event = new EntitySpawnEvent(fallingBlock);
        bp.onFallingBlockSpawn(event);
        assertTrue(event.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onFallingBlockSpawn(org.bukkit.event.entity.EntitySpawnEvent)}.
     */
    @Test
    void testOnFallingBlockSpawnWrongWorld() {
        Entity fallingBlock = mock(Entity.class);
        when(fallingBlock.getLocation()).thenReturn(location);
        when(fallingBlock.getType()).thenReturn(EntityType.FALLING_BLOCK);
        when(fallingBlock.getWorld()).thenReturn(mock(World.class));
        EntitySpawnEvent event = new EntitySpawnEvent(fallingBlock);
        bp.onFallingBlockSpawn(event);
        assertFalse(event.isCancelled());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onFallingBlockSpawn(org.bukkit.event.entity.EntitySpawnEvent)}.
     */
    @Test
    void testOnFallingBlockSpawnNotFallingBlock() {
        Entity fallingBlock = mock(Entity.class);
        when(fallingBlock.getLocation()).thenReturn(location);
        when(fallingBlock.getType()).thenReturn(EntityType.AREA_EFFECT_CLOUD);
        when(fallingBlock.getWorld()).thenReturn(world);
        EntitySpawnEvent event = new EntitySpawnEvent(fallingBlock);
        bp.onFallingBlockSpawn(event);
        assertFalse(event.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onFallingBlockSpawn(org.bukkit.event.entity.EntitySpawnEvent)}.
     */
    @Test
    void testOnFallingBlockSpawnWrongLocation() {
        Entity fallingBlock = mock(Entity.class);
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        when(l.getBlockX()).thenReturn(1234); // not center
        when(fallingBlock.getLocation()).thenReturn(l);
        when(fallingBlock.getType()).thenReturn(EntityType.FALLING_BLOCK);
        when(fallingBlock.getWorld()).thenReturn(world);
        EntitySpawnEvent event = new EntitySpawnEvent(fallingBlock);
        bp.onFallingBlockSpawn(event);
        assertFalse(event.isCancelled());
    }

}
