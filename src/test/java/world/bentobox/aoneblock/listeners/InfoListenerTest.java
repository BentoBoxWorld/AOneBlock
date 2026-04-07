package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.island.IslandInfoEvent;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class InfoListenerTest extends CommonTestSetup {
    @Mock
    private AOneBlock addon;
    @Mock
    private BlockListener bl;
    @Mock
    private Player player;
    private InfoListener il;
    @Mock
    private @NonNull OneBlockIslands is;
    
    private static final UUID ID = UUID.randomUUID();

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getPlugin()).thenReturn(plugin);

        User.setPlugin(plugin);
        
        // Player
        when(player.getUniqueId()).thenReturn(ID);
        when(player.spigot()).thenReturn(spigot);
        // Island
        when(addon.getOneBlocksIsland(island)).thenReturn(is);
        when(is.getBlockNumber()).thenReturn(400);
        when(is.getLifetime()).thenReturn(900L);
        when(is.getPhaseName()).thenReturn("Googy");
            
        when(addon.getOverWorld()).thenReturn(world);
        // Player
        when(mockPlayer.getUniqueId()).thenReturn(ID);
        when(player.getWorld()).thenReturn(world);
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
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#InfoListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    void testInfoListener() {
        assertNotNull(il);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#onInfo(world.bentobox.bentobox.api.events.island.IslandInfoEvent)}.
     */
    @Test
    void testOnInfo() {
        IslandInfoEvent e = new IslandInfoEvent(island, ID, false, location, addon);
        il.onInfo(e);
        verify(lm).get(any(User.class), eq("aoneblock.commands.info.count"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#onInfo(world.bentobox.bentobox.api.events.island.IslandInfoEvent)}.
     */
    @Test
    void testOnInfoOtherAddon() {
        IslandInfoEvent e = new IslandInfoEvent(island, ID, false, location, mock(Addon.class));
        il.onInfo(e);
        verify(lm, never()).get(any(User.class), eq("aoneblock.commands.info.count"));
    }

}
