package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
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
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, Util.class })
public class HoloListenerTest {
    @Mock
    private BentoBox plugin;
    @Mock
    AOneBlock addon;
    private HoloListener hl;
    @Mock
    private Island island;
    private UUID uuid = UUID.randomUUID();
    @Mock
    private Location location;
    @Mock
    private World world;
    @Mock
    private TextDisplay hologram;
    private Settings settings;
    @Mock
    private @NonNull OneBlockIslands is;
    @Mock
    private Player player;
    @Mock
    private LocalesManager lm;
    @Mock
    private PlaceholdersManager pm;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private @NonNull OneBlockPhase phase;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        // Settings
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Island
        when(island.getWorld()).thenReturn(world);
        when(island.getCenter()).thenReturn(location);
        when(island.getOwner()).thenReturn(uuid);
        
        // World
        when(world.spawn(any(Location.class), eq(TextDisplay.class))).thenReturn(hologram);

        // OneBlock Island
        when(is.getHologram()).thenReturn("Hologram");
        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        when(location.add(any(Vector.class))).thenReturn(location);
        // Chunks
        PowerMockito.mockStatic(Util.class, Mockito.RETURNS_MOCKS);
        CompletableFuture<Chunk> future = CompletableFuture.completedFuture(null);
        when(Util.getChunkAtAsync(location)).thenReturn(future); // Load the chunk immediately
        
        Player nonHolo = mock(Player.class);
        
        Collection<Entity> entities = List.of(nonHolo , hologram);
        // Get entities
        when(hologram.getType()).thenReturn(EntityType.TEXT_DISPLAY);
        when(world.getNearbyEntities(any(Location.class), anyDouble(), anyDouble(), anyDouble())).thenReturn(entities);
        
        // Player and translations
        when(player.getUniqueId()).thenReturn(uuid);
        User.getInstance(player);
        User.setPlugin(plugin);
        
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(plugin.getPlaceholdersManager()).thenReturn(pm);
        
        // Bukkit
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        when(Bukkit.getScheduler()).thenReturn(scheduler);
        
        // DUT
        hl = new HoloListener(addon);
    }

    @After
    public void tearDown()  {
        User.clearUsers();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#HoloListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testHoloListener() {
        assertNotNull(hl);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#onDeletedIsland(world.bentobox.bentobox.api.events.island.IslandDeleteEvent)}.
     */
    @Test
    public void testOnDeletedIsland() {
        IslandDeleteEvent event = new IslandDeleteEvent(island, uuid, false, location);
        this.hl.onDeletedIsland(event);
        verify(hologram).remove();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#onDisable()}.
     */
    @Test
    public void testOnDisable() {
        hl.setUp(island, is, true);
        hl.onDisable();
        verify(hologram, times(2)).remove();
    }

    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    public void testSetUpNoHolograms() {
        settings.setUseHolograms(false);
        hl.setUp(island, is, true);
        verify(scheduler, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    public void testSetUpNoScheduling() {
        settings.setHologramDuration(0);
        hl.setUp(island, is, true);
        verify(scheduler, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    public void testSetUpNewIsland() {
        hl.setUp(island, is, true);
        verify(is).setHologram("");
        verify(scheduler).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    public void testSetUpNotNewIsland() {
        hl.setUp(island, is, false);
        verify(is, never()).setHologram(anyString());
        verify(scheduler).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#process(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testProcessNull() {
        when(phase.getHologramLine(anyInt())).thenReturn(null);
        when(is.getHologram()).thenReturn(""); // Return blank
        hl.process(island, is, phase);
        verify(scheduler, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#process(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    public void testProcess() {
        when(phase.getHologramLine(anyInt())).thenReturn("my Holo");
        hl.process(island, is, phase);
        verify(scheduler).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }

}
