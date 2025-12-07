package world.bentobox.aoneblock.commands.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * Test class
 */
class IslandRespawnBlockCommandTest extends CommonTestSetup {
    
    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private AOneBlock addon;
    private IslandRespawnBlockCommand rbc;
    @Mock
    private @NotNull Block block;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(ac.getAddon()).thenReturn(addon);
        when(ac.getWorld()).thenReturn(world);
        when(user.getWorld()).thenReturn(world);
        mockedUtil.when(() -> Util.sameWorld(any(World.class), any(World.class))).thenReturn(true);
        // User has island
        when(im.getIsland(world, user)).thenReturn(island);
        // Island center block
        when(island.getWorld()).thenReturn(world);
        when(island.getCenter()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);
        when(block.getType()).thenReturn(Material.BEDROCK);
        when(location.add(any(Vector.class))).thenReturn(location);

        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        rbc = new IslandRespawnBlockCommand(ac, settings.getRespawnBlockCommand().split(" ")[0],
                settings.getRespawnBlockCommand().split(" "));
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#IslandRespawnBlockCommand(world.bentobox.bentobox.api.commands.CompositeCommand, java.lang.String, java.lang.String[])}.
     */
    @Test
    void testIslandRespawnBlockCommand() {
        assertNotNull(rbc);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("respawn-block", rbc.getPermission());
        assertEquals("aoneblock.commands.respawn-block.description", rbc.getDescription());
        assertTrue(rbc.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecute() {
        assertTrue(rbc.canExecute(user, "", List.of()));
        verify(user, never()).sendMessage(anyString());
    }
    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteWrongWorld() {
        mockedUtil.when(() -> Util.sameWorld(any(World.class), any(World.class))).thenReturn(false);
        assertFalse(rbc.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.wrong-world");
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        assertFalse(rbc.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
       // when(block.getType()).thenReturn(Material.STONE);
       rbc.execute(user, "", List.of());
       verify(user).sendMessage("aoneblock.commands.respawn-block.block-respawned");
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringAir() {
       when(block.getType()).thenReturn(Material.AIR);
       rbc.execute(user, "", List.of());
       verify(user).sendMessage("aoneblock.commands.respawn-block.block-respawned");
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.commands.island.IslandRespawnBlockCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringSomething() {
       when(block.getType()).thenReturn(Material.STONE);
       rbc.execute(user, "", List.of());
       verify(user).sendMessage("aoneblock.commands.respawn-block.block-exist");
       verify(world, times(18)).spawnParticle(eq(Particle.DUST), eq(location), eq(5), eq(0.1), eq(0.0d), eq(0.1), eq(1.0d), any());
    }

}
