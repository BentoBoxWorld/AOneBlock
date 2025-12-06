package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import net.md_5.bungee.api.chat.TextComponent;
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
    /*
    @Mock
    private LocalesManager lm;
    @Mock
    private PlaceholdersManager phm;
    */
    @Mock
    private Spigot spigot;
    
    private static final UUID ID = UUID.randomUUID();

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getPlugin()).thenReturn(plugin);
        //when(plugin.getLocalesManager()).thenReturn(lm);
        //when(lm.get(toString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        //when(phm.replacePlaceholders(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));

        //when(plugin.getPlaceholdersManager()).thenReturn(phm);
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
        checkSpigotMessage("aoneblock.commands.info.count");
    }

    /**
     * Check that spigot sent the message
     * @param message - message to check
     */
    public void checkSpigotMessage(String expectedMessage) {
        checkSpigotMessage(expectedMessage, 1);
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.InfoListener#onInfo(world.bentobox.bentobox.api.events.island.IslandInfoEvent)}.
     */
    @Test
    public void testOnInfoOtherAddon() {
        IslandInfoEvent e = new IslandInfoEvent(island, ID, false, location, mock(Addon.class));
        il.onInfo(e);
        checkSpigotMessage("aoneblock.commands.info.count", 0);
    }

    public void checkSpigotMessage(String expectedMessage, int expectedOccurrences) {
        // Capture the argument passed to spigot().sendMessage(...) if messages are sent
        ArgumentCaptor<TextComponent> captor = ArgumentCaptor.forClass(TextComponent.class);

        // Verify that sendMessage() was called at least 0 times (capture any sent messages)
        verify(spigot, atLeast(0)).sendMessage(captor.capture());

        // Get all captured TextComponents
        List<TextComponent> capturedMessages = captor.getAllValues();

        // Count the number of occurrences of the expectedMessage in the captured messages
        long actualOccurrences = capturedMessages.stream().map(component -> component.toLegacyText()) // Convert each TextComponent to plain text
                .filter(messageText -> messageText.contains(expectedMessage)) // Check if the message contains the expected text
                .count(); // Count how many times the expected message appears

        // Assert that the number of occurrences matches the expectedOccurrences
        assertEquals(expectedOccurrences,
                actualOccurrences, "Expected message occurrence mismatch: " + expectedMessage);
    }

}
