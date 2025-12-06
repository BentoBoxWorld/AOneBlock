package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;

/**
 * @author tastybento
 */
public class JoinLeaveListenerTest extends CommonTestSetup {
    
    @Mock
    private AOneBlock aob;
    private JoinLeaveListener jll;
    @Mock
    private BlockListener bl;
    
    private static final UUID ID = UUID.randomUUID();
    private static final Vector VECTOR = new Vector(123,120,456);
    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        
        when(aob.getOverWorld()).thenReturn(world);
        // Player
        when(mockPlayer.getUniqueId()).thenReturn(ID);
        
        // Island
        when(island.getCenter()).thenReturn(location);
        when(location.toVector()).thenReturn(VECTOR);
        
        // Island Manager
        when(aob.getIslands()).thenReturn(im);
        when(island.getCenter()).thenReturn(location);
        when(im.getIsland(world, ID)).thenReturn(island);

        // Save is successful
        when(bl.saveIsland(any())).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));
        when(aob.getBlockListener()).thenReturn(bl);
        jll = new JoinLeaveListener(aob);
        
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
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#JoinLeaveListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testJoinLeaveListener() {
        assertNotNull(jll);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuit() {
        PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "nothing");
        jll.onPlayerQuit(event);
        verify(aob,never()).logError(anyString());
        verify(bl).saveIsland(island);
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuitNoIsland() {
        when(im.getIsland(world, ID)).thenReturn(null);
        PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "nothing");
        jll.onPlayerQuit(event);
        verify(aob,never()).logError(anyString());
        verify(bl, never()).saveIsland(island);
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuitSaveError() {
        when(bl.saveIsland(any())).thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        PlayerQuitEvent event = new PlayerQuitEvent(mockPlayer, "nothing");
        jll.onPlayerQuit(event);
        verify(aob).logError(anyString());
        verify(bl).saveIsland(island);
        verify(aob).logError("Could not save AOneBlock island at 123,120,456 to database null");
    }

}
