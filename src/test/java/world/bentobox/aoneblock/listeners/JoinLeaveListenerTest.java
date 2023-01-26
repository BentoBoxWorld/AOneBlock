package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
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
 * @author bengibbs
 * @PrepareForTest( {Util.class} )
 */
@RunWith(PowerMockRunner.class)
public class JoinLeaveListenerTest {
    
    @Mock
    private AOneBlock aob;
    @Mock
    private Player p;
    
    private JoinLeaveListener jll;
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
    
    private static final UUID ID = UUID.randomUUID();
    private static final Vector VECTOR = new Vector(123,120,456);
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        
        when(aob.getOverWorld()).thenReturn(world);
        // Player
        when(p.getUniqueId()).thenReturn(ID);
        
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
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#JoinLeaveListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testJoinLeaveListener() {
        assertFalse(jll == null);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.JoinLeaveListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuit() {
        PlayerQuitEvent event = new PlayerQuitEvent(p, "nothing");
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
        PlayerQuitEvent event = new PlayerQuitEvent(p, "nothing");
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
        PlayerQuitEvent event = new PlayerQuitEvent(p, "nothing");
        jll.onPlayerQuit(event);
        verify(aob).logError(anyString());
        verify(bl).saveIsland(island);
        verify(aob).logError(eq("Could not save AOneBlock island at 123,120,456 to database null"));
    }

}
