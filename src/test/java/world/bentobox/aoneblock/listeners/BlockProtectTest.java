package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class BlockProtectTest {
    
    private BlockProtect bp;
    @Mock
    AOneBlock addon;
    @Mock
    private Player p;
    @Mock
    private Block block;
    @Mock
    private World world;
    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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
            
        // Class under test
        bp = new BlockProtect(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#BlockProtect(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testBlockProtect() {
        assertNotNull(bp);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(org.bukkit.event.block.BlockDamageEvent)}.
     */
    @Test
    public void testOnBlockDamage() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockDamageEvent blockDamageEvent = new BlockDamageEvent(p, block, item, false);
        bp.onBlockDamage(blockDamageEvent);
        verify(addon).inWorld(world);
        verify(im).getIslandAt(location);
        verify(world, times(48)).spawnParticle(eq(Particle.REDSTONE), eq(null), eq(5),
                eq(0.1D), eq(0D), eq(0.1D), eq(1D), any(Particle.DustOptions.class));
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(org.bukkit.event.block.BlockDamageEvent)}.
     */
    @Test
    public void testOnBlockDamageWrongWorld() {
        when(block.getWorld()).thenReturn(mock(World.class));
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockDamageEvent blockDamageEvent = new BlockDamageEvent(p, block, item, false);
        bp.onBlockDamage(blockDamageEvent);
        verify(im, never()).getIslandAt(location);
        verify(world, never()).spawnParticle(any(Particle.class), any(Location.class),anyInt(), 
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockDamage(org.bukkit.event.block.BlockDamageEvent)}.
     */
    @Test
    public void testOnBlockDamageNotCenterMagicBlock() {
        when(block.getLocation()).thenReturn(mock(Location.class));
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockDamageEvent blockDamageEvent = new BlockDamageEvent(p, block, item, false);
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
    public void testOnBlockChange() {
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(p, block, blockData);
        bp.onBlockChange(event);
        assertTrue(event.isCancelled());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockChange(org.bukkit.event.entity.EntityChangeBlockEvent)}.
     */
    @Test
    public void testOnBlockChangeWrongWorld() {
        when(block.getWorld()).thenReturn(mock(World.class));
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(p, block, blockData);
        bp.onBlockChange(event);
        assertFalse(event.isCancelled());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onBlockChange(org.bukkit.event.entity.EntityChangeBlockEvent)}.
     */
    @Test
    public void testOnBlockChangeNotMagicBlock() {
        when(block.getLocation()).thenReturn(mock(Location.class));
        BlockData blockData = mock(BlockData.class);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(p, block, blockData);
        bp.onBlockChange(event);
        assertFalse(event.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onExplosion(org.bukkit.event.entity.EntityExplodeEvent)}.
     */
    @Test
    public void testOnExplosion() {
        List<Block> blocks = new ArrayList<>();
        EntityExplodeEvent event = new EntityExplodeEvent(p, location, blocks, 0);
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
    public void testOnExplosionWrongWorld() {
        when(location.getWorld()).thenReturn(mock(World.class));
        List<Block> blocks = new ArrayList<>();
        EntityExplodeEvent event = new EntityExplodeEvent(p, location, blocks, 0);
        blocks.add(block);
        // Block as correct location, but wrong world
        bp.onExplosion(event);
        assertFalse(blocks.isEmpty());
        // Normal blocks remain
        blocks.add(mock(Block.class));
        event = new EntityExplodeEvent(p, location, blocks, 0);
        bp.onExplosion(event);
        assertFalse(blocks.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.BlockProtect#onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent)}.
     */
    @Test
    public void testOnPistonExtendBlockPistonExtendEvent() {
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
    public void testOnPistonExtendBlockPistonExtendEventWrongWorld() {
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
    public void testOnPistonBlockPistonRetractEvent() {
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
    public void testOnPistonBlockPistonRetractEventWrongWorld() {
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
    public void testOnFallingBlockSpawn() {
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
    public void testOnFallingBlockSpawnWrongWorld() {
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
    public void testOnFallingBlockSpawnNotFallingBlock() {
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
    public void testOnFallingBlockSpawnWrongLocation() {
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
