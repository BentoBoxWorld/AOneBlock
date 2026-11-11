package world.bentobox.aoneblock.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.kyori.adventure.text.Component;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.PhaseFinaleEndEvent;
import world.bentobox.aoneblock.events.PhaseFinaleStartEvent;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.PhaseFinale;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;

/**
 * Tests the phase finale boss fights.
 */
class FinaleListenerTest extends CommonTestSetup {

    private static final String PLAINS = "Plains";

    @Mock
    private AOneBlock addon;
    @Mock
    private OneBlocksManager obm;
    @Mock
    private BlockListener blis;
    @Mock
    private CheckPhase check;
    @Mock
    private Block block;
    @Mock
    private Zombie boss;
    @Mock
    private AttributeInstance attribute;
    @Mock
    private PlayersManager pm;

    private Settings settings;
    private OneBlockPhase plains;
    private OneBlockPhase next;
    private OneBlockIslands is;
    private FinaleListener fl;
    private UUID bossId;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        settings = new Settings();
        settings.setClearBlocks(false);
        when(addon.getSettings()).thenReturn(settings);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(obm);
        when(addon.getBlockListener()).thenReturn(blis);
        when(addon.getPlayers()).thenReturn(pm);
        when(blis.getCheckPhase()).thenReturn(check);
        mockedUtil.when(() -> Util.parseMiniMessageOrLegacy(anyString()))
                .thenAnswer(a -> Component.text(a.getArgument(0, String.class)));

        // Phases
        plains = new OneBlockPhase("0");
        plains.setPhaseName(PLAINS);
        plains.setFinale(finale(true));
        next = new OneBlockPhase("700");
        next.setPhaseName("Underground");
        when(obm.getPhase(PLAINS)).thenReturn(Optional.of(plains));

        // Island
        when(island.getUniqueId()).thenReturn("island-id");
        when(island.onIsland(any())).thenReturn(true);
        when(island.getOwner()).thenReturn(uuid);
        is = new OneBlockIslands("island-id");
        is.setPhaseName(PLAINS);

        // World
        when(world.getDifficulty()).thenReturn(Difficulty.NORMAL);
        when(world.getPlayers()).thenReturn(List.of(mockPlayer));
        when(world.getMinHeight()).thenReturn(-64);
        when(world.dropItem(any(), any())).thenReturn(mock(Item.class));
        when(block.getWorld()).thenReturn(world);
        when(block.getLocation()).thenAnswer(a -> new Location(world, 0, 64, 0));

        // Player
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(mockPlayer.getLocation()).thenReturn(new Location(world, 1, 65, 1));
        when(mockPlayer.getWorld()).thenReturn(world);
        mockedBukkit.when(() -> Bukkit.getPlayer(any(UUID.class))).thenReturn(mockPlayer);
        User.setPlugin(plugin);
        User user = User.getInstance(mockPlayer);
        when(pm.getUser(any(UUID.class))).thenReturn(user);

        // Boss
        bossId = UUID.randomUUID();
        when(boss.getUniqueId()).thenReturn(bossId);
        when(boss.isValid()).thenReturn(true);
        when(boss.getType()).thenReturn(EntityType.ZOMBIE);
        when(boss.getAttribute(any())).thenReturn(attribute);
        when(boss.getLocation()).thenReturn(new Location(world, 0, 65, 0));
        when(boss.getWorld()).thenReturn(world);
        when(boss.getBoundingBox()).thenReturn(new org.bukkit.util.BoundingBox(0, 65, 0, 1, 67, 1));
        when(boss.getKiller()).thenReturn(mockPlayer);
        when(attribute.getValue()).thenReturn(100D);
        when(world.spawnEntity(any(), eq(EntityType.ZOMBIE))).thenReturn(boss);

        fl = new FinaleListener(addon);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private PhaseFinale finale(boolean voidRespawn) {
        return new PhaseFinale(EntityType.ZOMBIE, "The Colossus", 100, 2, 4, 0, voidRespawn, 20, Map.of(),
                Map.of(Material.IRON_INGOT, 8), List.of(), List.of());
    }

    private void summon() {
        is.setFinalePending(PLAINS);
        assertTrue(fl.gate(island, is, mockPlayer, block));
    }

    @Test
    void testScaledHealth() {
        assertEquals(100D, FinaleListener.scaledHealth(100, 1, 0));
        assertEquals(100D, FinaleListener.scaledHealth(100, 0, 0));
        assertEquals(150D, FinaleListener.scaledHealth(100, 2, 0));
        // Capped at four players
        assertEquals(250D, FinaleListener.scaledHealth(100, 9, 0));
        // Mercy
        assertEquals(90D, FinaleListener.scaledHealth(100, 1, 1), 0.001);
        assertEquals(50D, FinaleListener.scaledHealth(100, 1, 20), 0.001);
    }

    @Test
    void testArmOnLastBlock() {
        when(obm.getPhase(700)).thenReturn(next);
        is.setBlockNumber(700);
        fl.arm(is, plains);
        assertEquals(PLAINS, is.getFinalePending());
    }

    @Test
    void testArmMidPhase() {
        when(obm.getPhase(300)).thenReturn(plains);
        is.setBlockNumber(300);
        fl.arm(is, plains);
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testArmDisabled() {
        settings.setPhaseFinales(false);
        when(obm.getPhase(700)).thenReturn(next);
        is.setBlockNumber(700);
        fl.arm(is, plains);
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testArmNoFinale() {
        plains.setFinale(null);
        when(obm.getPhase(700)).thenReturn(next);
        is.setBlockNumber(700);
        fl.arm(is, plains);
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testGateNothingPending() {
        assertFalse(fl.gate(island, is, mockPlayer, block));
        verify(world, never()).spawnEntity(any(), any(EntityType.class));
    }

    @Test
    void testGateSummonsBoss() {
        summon();
        verify(world).spawnEntity(any(), eq(EntityType.ZOMBIE));
        verify(pim).callEvent(any(PhaseFinaleStartEvent.class));
        verify(boss).setPersistent(false);
        verify(boss).setTarget(mockPlayer);
        verify(boss).getAttribute(Attribute.SCALE);
        verify(attribute).setBaseValue(2D);
        verify(world).strikeLightningEffect(any());
        verify(mockPlayer).showBossBar(any());
        assertTrue(fl.isActive(island));
        // Still pending until it is dead
        assertEquals(PLAINS, is.getFinalePending());
    }

    @Test
    void testGateOnlyOneBoss() {
        summon();
        assertTrue(fl.gate(island, is, mockPlayer, block));
        verify(world, times(1)).spawnEntity(any(), eq(EntityType.ZOMBIE));
    }

    @Test
    void testGateMinionCannotSummon() {
        is.setFinalePending(PLAINS);
        assertTrue(fl.gate(island, is, null, block));
        verify(world, never()).spawnEntity(any(), any(EntityType.class));
        assertEquals(PLAINS, is.getFinalePending());
    }

    @Test
    void testGatePeacefulWaives() {
        when(world.getDifficulty()).thenReturn(Difficulty.PEACEFUL);
        is.setFinalePending(PLAINS);
        assertFalse(fl.gate(island, is, mockPlayer, block));
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testGateDisabledWaives() {
        settings.setPhaseFinales(false);
        is.setFinalePending(PLAINS);
        assertFalse(fl.gate(island, is, mockPlayer, block));
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testGateOtherPhaseWaives() {
        // The count was moved somewhere else while a finale was pending
        is.setFinalePending(PLAINS);
        is.setPhaseName("Winter");
        assertFalse(fl.gate(island, is, mockPlayer, block));
        assertEquals("", is.getFinalePending());
    }

    @Test
    void testGateSpawnFailureWaives() {
        when(boss.isValid()).thenReturn(false);
        is.setFinalePending(PLAINS);
        assertFalse(fl.gate(island, is, mockPlayer, block));
        assertEquals("", is.getFinalePending());
        assertFalse(fl.isActive(island));
    }

    @Test
    void testEnvironmentalDamageCancelled() {
        summon();
        EntityDamageEvent e = mock(EntityDamageEvent.class);
        when(e.getEntity()).thenReturn(boss);
        when(e.getCause()).thenReturn(DamageCause.SUFFOCATION);
        fl.onBossDamage(e);
        verify(e).setCancelled(true);
    }

    @Test
    void testPlayerDamageAllowed() {
        summon();
        EntityDamageByEntityEvent e = mock(EntityDamageByEntityEvent.class);
        when(e.getEntity()).thenReturn(boss);
        when(e.getCause()).thenReturn(DamageCause.ENTITY_ATTACK);
        when(e.getDamager()).thenReturn(mockPlayer);
        fl.onBossDamage(e);
        verify(e, never()).setCancelled(true);
    }

    @Test
    void testPlayerArrowAllowedMobArrowNot() {
        summon();
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(mockPlayer);
        EntityDamageByEntityEvent e = mock(EntityDamageByEntityEvent.class);
        when(e.getEntity()).thenReturn(boss);
        when(e.getCause()).thenReturn(DamageCause.PROJECTILE);
        when(e.getDamager()).thenReturn(arrow);
        fl.onBossDamage(e);
        verify(e, never()).setCancelled(true);

        Arrow arrow2 = mock(Arrow.class);
        EntityDamageByEntityEvent e2 = mock(EntityDamageByEntityEvent.class);
        when(e2.getEntity()).thenReturn(boss);
        when(e2.getCause()).thenReturn(DamageCause.PROJECTILE);
        when(e2.getDamager()).thenReturn(arrow2);
        fl.onBossDamage(e2);
        verify(e2).setCancelled(true);
    }

    @Test
    void testVoidRespawn() {
        summon();
        EntityDamageEvent e = mock(EntityDamageEvent.class);
        when(e.getEntity()).thenReturn(boss);
        when(e.getCause()).thenReturn(DamageCause.VOID);
        fl.onBossDamage(e);
        verify(e).setCancelled(true);
        verify(boss).teleport(any(Location.class));
    }

    @Test
    void testVoidKillsWhenRespawnOff() {
        plains.setFinale(finale(false));
        summon();
        EntityDamageEvent e = mock(EntityDamageEvent.class);
        when(e.getEntity()).thenReturn(boss);
        when(e.getCause()).thenReturn(DamageCause.VOID);
        fl.onBossDamage(e);
        verify(e, never()).setCancelled(true);
        verify(boss, never()).teleport(any(Location.class));
    }

    @Test
    void testOtherEntityDamageIgnored() {
        summon();
        EntityDamageEvent e = mock(EntityDamageEvent.class);
        when(e.getEntity()).thenReturn(mock(Zombie.class, a -> a.getMethod().getName().equals("getUniqueId")
                ? UUID.randomUUID() : null));
        fl.onBossDamage(e);
        verify(e, never()).setCancelled(true);
    }

    @Test
    void testBossDeathWins() {
        summon();
        EntityDeathEvent e = mock(EntityDeathEvent.class);
        when(e.getEntity()).thenReturn(boss);
        fl.onDeath(e);
        assertEquals("", is.getFinalePending());
        assertTrue(is.getDefeatedFinales().contains(PLAINS));
        assertFalse(fl.isActive(island));
        verify(world).dropItem(any(), eq(new ItemStack(Material.IRON_INGOT, 8)));
        verify(pim).callEvent(any(PhaseFinaleEndEvent.class));
        verify(blis).saveIsland(island);
        verify(mockPlayer).hideBossBar(any());
        // The way is open
        assertFalse(fl.gate(island, is, mockPlayer, block));
    }

    @Test
    void testBossRemovedIsNotAWin() {
        summon();
        when(boss.isValid()).thenReturn(false);
        fl.tickAll();
        assertFalse(fl.isActive(island));
        assertEquals(PLAINS, is.getFinalePending());
        assertEquals(0, is.getFinaleFails());
        assertTrue(is.getDefeatedFinales().isEmpty());
    }

    @Test
    void testEmptyIslandBossRetreatsAndMercy() {
        summon();
        PlayerDeathEvent death = mock(PlayerDeathEvent.class);
        when(death.getEntity()).thenReturn(mockPlayer);
        fl.onPlayerDeath(death);
        when(world.getPlayers()).thenReturn(List.of());
        for (int i = 0; i < FinaleListener.EMPTY_CHECKS; i++) {
            fl.tickAll();
        }
        assertFalse(fl.isActive(island));
        verify(boss).remove();
        assertEquals(PLAINS, is.getFinalePending());
        assertEquals(1, is.getFinaleFails());
    }

    @Test
    void testLeash() {
        summon();
        when(island.onIsland(any())).thenReturn(false);
        // The player is still a fighter
        when(island.onIsland(mockPlayer.getLocation())).thenReturn(true);
        fl.tickAll();
        verify(boss).teleport(any(Location.class));
    }

    @Test
    void testBossCannotChangeBlocks() {
        summon();
        EntityChangeBlockEvent e = mock(EntityChangeBlockEvent.class);
        when(e.getEntity()).thenReturn(boss);
        fl.onChangeBlock(e);
        verify(e).setCancelled(true);
    }

    @Test
    void testClear() {
        summon();
        fl.clear(island, is);
        assertFalse(fl.isActive(island));
        assertEquals("", is.getFinalePending());
        verify(boss).remove();
    }

    @Test
    void testOnDisable() {
        summon();
        fl.onDisable();
        assertFalse(fl.isActive(island));
        verify(boss).remove();
        // Still pending for after the restart
        assertEquals(PLAINS, is.getFinalePending());
    }
}
