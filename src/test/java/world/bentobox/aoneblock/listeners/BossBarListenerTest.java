package world.bentobox.aoneblock.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.kyori.adventure.text.Component;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.MagicBlockEvent;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;

/**
 * Tests the display of the boss bar and action bar, and in particular that the
 * config.yml settings turn them off (https://github.com/BentoBoxWorld/AOneBlock/issues/537).
 * @author tastybento
 */
public class BossBarListenerTest extends CommonTestSetup {

    private AOneBlock addon;
    private Settings settings;
    private BossBarListener bbl;
    @Mock
    private OneBlocksManager obm;
    @Mock
    private OneBlockIslands obi;
    @Mock
    private BossBar bossBar;
    @Mock
    private Block block;

    /**
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        addon = spy(new AOneBlock());
        settings = new Settings();
        addon.setSettings(settings);
        doNothing().when(addon).logError(anyString());
        doReturn(obm).when(addon).getOneBlockManager();
        doReturn(obi).when(addon).getOneBlocksIsland(any());

        // Phase progress
        when(obi.getPhaseName()).thenReturn("Plains");
        when(obm.getNextPhaseBlocks(obi)).thenReturn(100);
        when(obm.getPhaseBlocks(obi)).thenReturn(500);
        when(obm.getPercentageDone(obi)).thenReturn(80D);

        // Bukkit
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);
        mockedBukkit.when(() -> Bukkit.createBossBar(anyString(), any(), any())).thenReturn(bossBar);
        when(bossBar.getPlayers()).thenReturn(Collections.emptyList());

        bbl = new BossBarListener(addon);
    }

    /**
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void fireMagicBlockEvent() {
        bbl.onBreakBlockEvent(new MagicBlockEvent(island, uuid, null, block, Material.STONE));
    }

    /**
     * Test that the action bar is shown when enabled in the config and allowed on the island.
     */
    @Test
    void testActionBarShownWhenEnabled() {
        when(island.isAllowed(addon.ONEBLOCK_ACTIONBAR)).thenReturn(true);
        fireMagicBlockEvent();
        verify(mockPlayer).sendActionBar(any(Component.class));
    }

    /**
     * Test for https://github.com/BentoBoxWorld/AOneBlock/issues/537 - the action bar
     * must not be shown when disabled in config.yml, even though the boss bar flag has
     * registered the listener.
     */
    @Test
    void testActionBarNotShownWhenDisabledInConfig() {
        settings.setActionBar(false);
        when(island.isAllowed(addon.ONEBLOCK_ACTIONBAR)).thenReturn(true);
        fireMagicBlockEvent();
        verify(mockPlayer, never()).sendActionBar(any(Component.class));
    }

    /**
     * Test that the action bar is not shown when the island flag denies it.
     */
    @Test
    void testActionBarNotShownWhenFlagDenied() {
        // island.isAllowed is false by default in CommonTestSetup
        fireMagicBlockEvent();
        verify(mockPlayer, never()).sendActionBar(any(Component.class));
    }

    /**
     * Test that the boss bar is shown when enabled in the config and allowed on the island.
     */
    @Test
    void testBossBarShownWhenEnabled() {
        when(island.isAllowed(addon.ONEBLOCK_BOSSBAR)).thenReturn(true);
        fireMagicBlockEvent();
        verify(bossBar).addPlayer(mockPlayer);
    }

    /**
     * Test that the boss bar is not shown when disabled in config.yml.
     */
    @Test
    void testBossBarNotShownWhenDisabledInConfig() {
        settings.setBossBar(false);
        when(island.isAllowed(addon.ONEBLOCK_BOSSBAR)).thenReturn(true);
        fireMagicBlockEvent();
        mockedBukkit.verify(() -> Bukkit.createBossBar(anyString(), any(), any()), never());
        verify(bossBar, never()).addPlayer(any());
    }
}
