package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.mockito.ArgumentCaptor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public class HoloListenerTest extends CommonTestSetup {
    @Mock
    AOneBlock addon;
    private HoloListener hl;
    @Mock
    private TextDisplay hologram;
    private Settings settings;
    @Mock
    private @NonNull OneBlockIslands is;
    @Mock
    private @NonNull OneBlockPhase phase;


    /**
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
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
        CompletableFuture<Chunk> future = CompletableFuture.completedFuture(null);
        mockedUtil.when(() -> Util.getChunkAtAsync(location)).thenReturn(future); // Load the chunk immediately
        
        Player nonHolo = mock(Player.class);
        
        Collection<Entity> entities = List.of(nonHolo , hologram);
        // Get entities
        when(hologram.getType()).thenReturn(EntityType.TEXT_DISPLAY);
        when(world.getNearbyEntities(any(Location.class), anyDouble(), anyDouble(), anyDouble())).thenReturn(entities);
        
        // Player and translations
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        User.getInstance(mockPlayer);
        User.setPlugin(plugin);
        
        // DUT
        hl = new HoloListener(addon);
    }

    @Override
    @AfterEach
    public void tearDown()throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#HoloListener(world.bentobox.aoneblock.AOneBlock)}.
     */
    @Test
    void testHoloListener() {
        assertNotNull(hl);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#onDeletedIsland(world.bentobox.bentobox.api.events.island.IslandDeleteEvent)}.
     */
    @Test
    void testOnDeletedIsland() {
        IslandDeleteEvent event = new IslandDeleteEvent(island, uuid, false, location);
        this.hl.onDeletedIsland(event);
        verify(hologram).remove();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#onDisable()}.
     */
    @Test
    void testOnDisable() {
        hl.setUp(island, is, true);
        hl.onDisable();
        verify(hologram, times(2)).remove();
    }

    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    void testSetUpNoHolograms() {
        settings.setUseHolograms(false);
        hl.setUp(island, is, true);
        verify(sch, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    void testSetUpNoScheduling() {
        settings.setHologramDuration(0);
        hl.setUp(island, is, true);
        verify(sch, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    void testSetUpNewIsland() {
        hl.setUp(island, is, true);
        verify(is).setHologram("aoneblock.island.starting-hologram");
        verify(sch).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#setUp(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, boolean)}.
     */
    @Test
    void testSetUpNotNewIsland() {
        hl.setUp(island, is, false);
        verify(is, never()).setHologram(anyString());
        verify(sch).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }


    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#process(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    void testProcessNull() {
        when(phase.getHologramLine(anyInt())).thenReturn(null);
        when(is.getHologram()).thenReturn(""); // Return blank
        hl.process(island, is, phase);
        verify(sch, never()).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.listeners.HoloListener#process(world.bentobox.bentobox.database.objects.Island, world.bentobox.aoneblock.dataobjects.OneBlockIslands, world.bentobox.aoneblock.oneblocks.OneBlockPhase)}.
     */
    @Test
    void testProcess() {
        when(phase.getHologramLine(anyInt())).thenReturn("my Holo");
        hl.process(island, is, phase);
        verify(sch).runTaskLater(isNull(), any(Runnable.class), anyLong());
    }

    /**
     * Captures the component the hologram was actually given.
     */
    private Component displayed(String hologramLine) {
        when(phase.getHologramLine(anyInt())).thenReturn(hologramLine);
        // process() writes the line to the data object then reads it straight back, and that
        // object is a mock, so the read has to be stubbed too or it returns the setUp default.
        when(is.getHologram()).thenReturn(hologramLine);
        hl.process(island, is, phase);
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(hologram).text(captor.capture());
        return captor.getValue();
    }

    /**
     * Phase file hologram lines are read straight from YAML, so this is the only place their
     * formatting is resolved. MiniMessage tags used to appear as literal text.
     */
    @Test
    void testHologramParsesMiniMessage() {
        Component c = displayed("<green><bold>Plains</bold></green>");
        assertEquals("Plains", PlainTextComponentSerializer.plainText().serialize(c));
        String legacy = LegacyComponentSerializer.legacySection().serialize(c);
        assertTrue(legacy.contains("\u00a7a"), "expected green in " + legacy);
        assertTrue(legacy.contains("\u00a7l"), "expected bold in " + legacy);
    }

    /**
     * Legacy '&' codes must keep working - every existing phase file uses them.
     */
    @Test
    void testHologramParsesLegacyAmpersand() {
        Component c = displayed("&aGood Luck!");
        assertEquals("Good Luck!", PlainTextComponentSerializer.plainText().serialize(c));
        assertTrue(LegacyComponentSerializer.legacySection().serialize(c).contains("\u00a7a"));
    }

    /**
     * Hex was not supported here before - the serializer was built without hex enabled.
     */
    @Test
    void testHologramParsesHex() {
        Component c = displayed("&#55FF55Good Luck!");
        assertEquals("Good Luck!", PlainTextComponentSerializer.plainText().serialize(c));
        assertEquals(TextColor.fromHexString("#55FF55"), c.color());
    }

    /**
     * The starting hologram comes from the locale file via User.getTranslation, which hands back
     * section codes. A serializer bound to '&' left those in as literal text.
     */
    @Test
    void testHologramParsesSectionCodes() {
        Component c = displayed("\u00a7aWelcome");
        assertEquals("Welcome", PlainTextComponentSerializer.plainText().serialize(c));
        assertTrue(LegacyComponentSerializer.legacySection().serialize(c).contains("\u00a7a"));
    }

}
