package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
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
public class NoBlockHandlerTest {
    
    private static final UUID ID = UUID.randomUUID();
    
    @Mock
    private AOneBlock aob;
    @Mock
    private Player p;
    
    private NoBlockHandler nbh;
    
    @Mock
    private Block block;
    @Mock
    private BlockListener bl;
    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private World world;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
     // Player
        when(p.getUniqueId()).thenReturn(ID);
        
        // In world
        when(aob.inWorld(world)).thenReturn(true);
        
        // Location
        when(location.getWorld()).thenReturn(world);
        
        // Block
        when(location.getBlock()).thenReturn(block);
        when(block.isEmpty()).thenReturn(false);
        
        // Island
        when(island.getCenter()).thenReturn(location);
        
        // Island Manager
        when(aob.getIslands()).thenReturn(im);
        when(island.getCenter()).thenReturn(location);
        when(im.getIsland(world, ID)).thenReturn(island);

        nbh = new NoBlockHandler(aob);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#NoBlockHandler(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testNoBlockHandler() {
        assertFalse(nbh == null);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnSolidBlock() {
        PlayerRespawnEvent event = new PlayerRespawnEvent(p, location, false, false);
        nbh.onRespawn(event);
        verify(block, never()).setType(any(Material.class));
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnAirBlock() {
        when(block.isEmpty()).thenReturn(true);
        PlayerRespawnEvent event = new PlayerRespawnEvent(p, location, false, false);
        nbh.onRespawn(event);
        verify(block).setType(any(Material.class));
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnAirBlockWrongWorld() {
        when(aob.inWorld(world)).thenReturn(false);
        when(block.isEmpty()).thenReturn(true);
        PlayerRespawnEvent event = new PlayerRespawnEvent(p, location, false, false);
        nbh.onRespawn(event);
        verify(block, never()).setType(any(Material.class));
        
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnAirBlockNoIsland() {
        when(im.getIsland(world, ID)).thenReturn(null);
        when(block.isEmpty()).thenReturn(true);
        PlayerRespawnEvent event = new PlayerRespawnEvent(p, location, false, false);
        nbh.onRespawn(event);
        verify(block, never()).setType(any(Material.class));
        
    }


}
