package world.bentobox.aoneblock.listeners;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
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

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.island.IslandInfoEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, Util.class })
public class InfoListenerTest {
    @Mock
    private BentoBox plugin;
    @Mock
    private AOneBlock addon;
    @Mock
    private Player p;
    
    @Mock
    private BlockListener bl;
    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private Player player;
    @Mock
    private World world;
    private InfoListener il;
    @Mock
    private @NonNull OneBlockIslands is;
    @Mock
    private LocalesManager lm;
    @Mock
    private PlaceholdersManager phm;
    
    private static final UUID ID = UUID.randomUUID();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(toString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        when(phm.replacePlaceholders(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));

        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        User.setPlugin(plugin);
        
        // Player
        when(player.getUniqueId()).thenReturn(ID);
        
        // Island
        when(addon.getOneBlocksIsland(island)).thenReturn(is);
        when(is.getBlockNumber()).thenReturn(400);
        when(is.getLifetime()).thenReturn(900L);
        when(is.getPhaseName()).thenReturn("Googy");
            
        when(addon.getOverWorld()).thenReturn(world);
        // Player
        when(p.getUniqueId()).thenReturn(ID);
        User.getInstance(player);
        
        // Island
        when(island.getCenter()).thenReturn(location);
        
        // Island Manager
        when(addon.getIslands()).thenReturn(im);
        when(island.getCenter()).thenReturn(location);
        when(im.getIsland(world, ID)).thenReturn(island);

        when(addon.getBlockListener()).thenReturn(bl);

        il = new InfoListener(addon);
    }
    
    /**
     * @throws java.lang.Exception - exception
     */
    @After
    public void tearDown() throws Exception {
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#InfoListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    public void testInfoListener() {
        assertNotNull(il);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#onInfo(world.bentobox.bentobox.api.events.island.IslandInfoEvent)}.
     */
    @Test
    public void testOnInfo() {
        IslandInfoEvent e = new IslandInfoEvent(island, ID, false, location, addon);
        il.onInfo(e);
        verify(player).sendMessage("aoneblock.commands.info.count");
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#onInfo(world.bentobox.bentobox.api.events.island.IslandInfoEvent)}.
     */
    @Test
    public void testOnInfoOtherAddon() {
        IslandInfoEvent e = new IslandInfoEvent(island, ID, false, location, mock(Addon.class));
        il.onInfo(e);
        verify(player, never()).sendMessage("aoneblock.commands.info.count");
    }

}
