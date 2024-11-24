package world.bentobox.aoneblock;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class PlaceholdersManagerTest {
    @Mock
    private AOneBlock addon;
    @Mock
    private User user;

    private UUID uuid = UUID.randomUUID();

    private AOneBlockPlaceholders pm;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private @Nullable Location location;
    private @NonNull OneBlockIslands obi;
    @Mock
    private World world;
    @Mock
    private OneBlocksManager obm;
    private Settings settings;
    @Mock
    private PlaceholdersManager phm;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // User
        when(user.getLocation()).thenReturn(location);
        when(user.getTranslation("aoneblock.placeholders.infinite")).thenReturn("Infinite");
        when(user.getWorld()).thenReturn(world);
        // Addon
        when(addon.getIslands()).thenReturn(im);
        when(addon.getOverWorld()).thenReturn(world);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.inWorld(world)).thenReturn(true);
        when(im.getProtectedIslandAt(any())).thenReturn(Optional.of(island));
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.getIslands(world, user)).thenReturn(List.of(island));
        obi = new OneBlockIslands("uniqueId");
        obi.setPhaseName("first");
        obi.setBlockNumber(1000);
        when(addon.getOneBlocksIsland(any())).thenReturn(obi);
        // OneBlockManager
        when(obm.getNextPhase(any(OneBlockIslands.class))).thenReturn("next_phase");
        when(obm.getPercentageDone(any(OneBlockIslands.class))).thenReturn(70D);
        when(obm.getNextPhaseBlocks(any(OneBlockIslands.class))).thenReturn(123);
        // Settings
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        // Island
        when(island.getOwner()).thenReturn(uuid);

        pm = new AOneBlockPlaceholders(addon, phm);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getPhaseByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetPhaseByLocation() {
        assertEquals("", pm.getPhaseByLocation(user));
        assertEquals("", pm.getPhaseByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("first", pm.getPhaseByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getPhaseByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getCountByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetCountByLocation() {
        assertEquals("", pm.getCountByLocation(user));
        assertEquals("", pm.getCountByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("1000", pm.getCountByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getCountByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getPhase(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetPhase() {
        assertEquals("", pm.getPhase(user));
        assertEquals("", pm.getPhase(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("first", pm.getPhase(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getPhase(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getCount(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetCount() {
        assertEquals("", pm.getCount(user));
        assertEquals("", pm.getCount(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("1000", pm.getCount(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getCount(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getNextPhaseByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetNextPhaseByLocation() {
        assertEquals("", pm.getNextPhaseByLocation(user));
        assertEquals("", pm.getNextPhaseByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("next_phase", pm.getNextPhaseByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getNextPhaseByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getNextPhase(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetNextPhase() {
        assertEquals("", pm.getNextPhase(user));
        assertEquals("", pm.getNextPhase(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("next_phase", pm.getNextPhase(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getNextPhase(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getNextPhaseBlocksByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetNextPhaseBlocksByLocation() {
        assertEquals("", pm.getNextPhaseBlocksByLocation(user));
        assertEquals("", pm.getNextPhaseBlocksByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("123", pm.getNextPhaseBlocksByLocation(user));
        when(obm.getNextPhaseBlocks(any())).thenReturn(-1);
        assertEquals("Infinite", pm.getNextPhaseBlocksByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getNextPhaseBlocksByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getNextPhaseBlocks(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetNextPhaseBlocks() {
        assertEquals("", pm.getNextPhaseBlocks(user));
        assertEquals("", pm.getNextPhaseBlocks(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("123", pm.getNextPhaseBlocks(user));
        when(obm.getNextPhaseBlocks(any())).thenReturn(-1);
        assertEquals("Infinite", pm.getNextPhaseBlocks(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getNextPhaseBlocks(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getPercentDoneByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetPercentDoneByLocation() {
        assertEquals("", pm.getPercentDoneByLocation(user));
        assertEquals("", pm.getPercentDoneByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("70%", pm.getPercentDoneByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getPercentDoneByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getPercentDone(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetPercentDone() {
        assertEquals("", pm.getPercentDone(user));
        assertEquals("", pm.getPercentDone(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("70%", pm.getPercentDone(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getPercentDone(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getDoneScaleByLocation(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetDoneScaleByLocation() {
        assertEquals("", pm.getDoneScaleByLocation(user));
        assertEquals("", pm.getDoneScaleByLocation(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("&a■■■■■&c■■■", pm.getDoneScaleByLocation(user));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        assertEquals("", pm.getDoneScaleByLocation(user));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlockPlaceholders#getDoneScale(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetDoneScale() {
        assertEquals("", pm.getDoneScale(user));
        assertEquals("", pm.getDoneScale(null));
        when(user.getUniqueId()).thenReturn(uuid);
        assertEquals("&a■■■■■&c■■■", pm.getDoneScale(user));
        when(im.getIsland(world, user)).thenReturn(null);
        when(im.getIslands(world, user)).thenReturn(List.of());
        assertEquals("", pm.getDoneScale(user));
    }

}
