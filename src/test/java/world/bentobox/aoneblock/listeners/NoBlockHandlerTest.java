package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;

/**
 * @author tastybento
 *
 */
public class NoBlockHandlerTest extends CommonTestSetup {

    private static final UUID ID = UUID.randomUUID();

    @Mock
    private AOneBlock aob;
    
    private NoBlockHandler nbh;

    @Mock
    private Block block;
    @Mock
    private BlockListener bl;
 
    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
     // Player
        when(mockPlayer.getUniqueId()).thenReturn(ID);
        
        // In world
        when(aob.inWorld(world)).thenReturn(true);
        
        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        
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
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.NoBlockHandler#NoBlockHandler(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testNoBlockHandler() {
	assertNotNull(nbh);
    }

    /**
     * Test method for
     * {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnSolidBlock() {
	PlayerRespawnEvent event = new PlayerRespawnEvent(mockPlayer, location, false, false, false, RespawnReason.DEATH);
	nbh.onRespawn(event);
	verify(block, never()).setType(any(Material.class));

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.NoBlockHandler#onRespawn(org.bukkit.event.player.PlayerRespawnEvent)}.
     */
    @Test
    public void testOnRespawnAirBlock() {
        when(block.isEmpty()).thenReturn(true);
        PlayerRespawnEvent event = new PlayerRespawnEvent(mockPlayer, location, false, false, false, RespawnReason.DEATH);
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
        PlayerRespawnEvent event = new PlayerRespawnEvent(mockPlayer, location, false, false, true, RespawnReason.DEATH);
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
        PlayerRespawnEvent event = new PlayerRespawnEvent(mockPlayer, location, false, false, false, RespawnReason.DEATH);
        nbh.onRespawn(event);
        verify(block, never()).setType(any(Material.class));
        
    }

}
