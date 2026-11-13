package world.bentobox.aoneblock.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.events.PhaseFinaleEndEvent;
import world.bentobox.aoneblock.events.PhaseFinaleStartEvent;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.PhaseFinale;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * Runs phase finales: the boss that must be defeated at the end of a phase
 * before the magic block moves on to the next one.
 * <p>
 * A finale is <i>armed</i> when the last block of a phase is mined, and
 * <i>gates</i> the next break of the magic block until the boss is dead. The
 * armed state is persisted in {@link OneBlockIslands}; the boss itself is not,
 * so a restart, an empty island or a mob-clearing plugin only ever sends the
 * fight back to "hit the block to summon the boss", never to a free pass.
 *
 * @author tastybento
 */
public class FinaleListener implements Listener {

    /** Ticks between checks of each running finale. */
    static final long TICK_PERIOD = 10L;
    /** How many checks an island may be empty of fighters before the boss leaves. */
    static final int EMPTY_CHECKS = 20;
    /** Boss health scales for up to this many players. */
    static final int MAX_SCALED_PLAYERS = 4;
    /** Extra health per additional player, as a fraction of base health. */
    static final double HEALTH_PER_PLAYER = 0.5D;
    /** Health reduction per consecutive loss, as a fraction of base health. */
    static final double MERCY_PER_FAIL = 0.1D;
    /** The boss never drops below this fraction of its health through mercy. */
    static final double MERCY_FLOOR = 0.5D;

    private static final String NAME = "[name]";

    private final AOneBlock addon;
    /** Running finales by island unique id. */
    private final Map<String, ActiveFinale> active = new HashMap<>();
    /** Running finales by boss entity id. */
    private final Map<UUID, ActiveFinale> byBoss = new HashMap<>();
    /** Running finales by minion entity id. */
    private final Map<UUID, ActiveFinale> byMinion = new HashMap<>();
    private BukkitTask task;

    /**
     * A finale in progress.
     */
    static class ActiveFinale {
        final Island island;
        final OneBlockIslands is;
        final String phaseName;
        final PhaseFinale finale;
        final Block block;
        final LivingEntity boss;
        final Set<UUID> minions = new HashSet<>();
        final Set<UUID> viewers = new HashSet<>();
        final BossBar bar;
        final String plainName;
        boolean playerDied;
        int emptyChecks;

        ActiveFinale(Island island, OneBlockIslands is, String phaseName, PhaseFinale finale, Block block,
                LivingEntity boss, Component name) {
            this.island = island;
            this.is = is;
            this.phaseName = phaseName;
            this.finale = finale;
            this.block = block;
            this.boss = boss;
            this.bar = BossBar.bossBar(name, 1F, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
            this.plainName = PlainTextComponentSerializer.plainText().serialize(name);
        }
    }

    /**
     * @param addon - addon
     */
    public FinaleListener(@NonNull AOneBlock addon) {
        this.addon = addon;
    }

    // ---------------------------------------------------------------------
    // Section: Arming and gating, called by the BlockListener
    // ---------------------------------------------------------------------

    /**
     * Called after the block count goes up. Arms the finale if that was the last
     * block of a phase that has one.
     *
     * @param is - island data, block number already incremented
     * @param phase - the phase the mined block belonged to
     */
    public void arm(@NonNull OneBlockIslands is, @NonNull OneBlockPhase phase) {
        if (!addon.getSettings().isPhaseFinales() || phase.getFinale() == null || phase.getPhaseName() == null) {
            return;
        }
        if (addon.getOneBlockManager().getPhase(is.getBlockNumber()) != phase) {
            is.setFinalePending(phase.getPhaseName());
        }
    }

    /**
     * Called when the magic block is about to move to a new phase. Starts the
     * pending finale if there is one.
     *
     * @param island - island
     * @param is - island data
     * @param player - player breaking the block, or null if it is not a player
     * @param block - the magic block
     * @return true if the break must be cancelled because a boss stands in the way
     */
    public boolean gate(@NonNull Island island, @NonNull OneBlockIslands is, @Nullable Player player,
            @NonNull Block block) {
        String pending = is.getFinalePending();
        if (pending.isEmpty()) {
            return false;
        }
        PhaseFinale finale = addon.getOneBlockManager().getPhase(pending).map(OneBlockPhase::getFinale).orElse(null);
        if (!addon.getSettings().isPhaseFinales() || finale == null || !pending.equalsIgnoreCase(is.getPhaseName())
                || block.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            // Nothing to fight, e.g., finales were turned off or the phase was edited
            waive(is);
            return false;
        }
        ActiveFinale running = active.get(island.getUniqueId());
        if (running != null) {
            if (player != null) {
                User.getInstance(player).notify("aoneblock.finale.defeat-boss", NAME, running.plainName);
            }
            return true;
        }
        if (player == null || player.hasMetadata("NPC")) {
            // Minions cannot summon the boss
            return true;
        }
        PhaseFinaleStartEvent event = new PhaseFinaleStartEvent(island, player.getUniqueId(), block, pending, finale);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            waive(is);
            return false;
        }
        if (!start(island, is, pending, finale, block, player)) {
            // The boss could not be spawned. Never lock an island behind a boss that is not there.
            waive(is);
            return false;
        }
        return true;
    }

    private void waive(OneBlockIslands is) {
        is.setFinalePending("");
        is.setFinaleFails(0);
    }

    /**
     * Drop any pending or running finale for this island without a result. Used
     * when an admin or player moves the block count.
     * @param island - island
     * @param is - island data
     */
    public void clear(@NonNull Island island, @NonNull OneBlockIslands is) {
        waive(is);
        ActiveFinale f = active.get(island.getUniqueId());
        if (f != null) {
            remove(f);
        }
    }

    // ---------------------------------------------------------------------
    // Section: The fight
    // ---------------------------------------------------------------------

    private boolean start(Island island, OneBlockIslands is, String phaseName, PhaseFinale finale, Block block,
            Player summoner) {
        World world = block.getWorld();
        Location home = home(block);
        Entity entity = world.spawnEntity(home, finale.boss());
        if (!(entity instanceof LivingEntity boss) || !entity.isValid()) {
            entity.remove();
            addon.logWarning("Could not spawn finale boss " + finale.boss() + " for phase " + phaseName);
            return false;
        }
        List<Player> fighters = fighters(island, world);
        Component name = Util.parseMiniMessageOrLegacy(finale.name());
        ActiveFinale f = new ActiveFinale(island, is, phaseName, finale, block, boss, name);

        // Dress the boss
        boss.customName(name);
        boss.setCustomNameVisible(true);
        boss.setPersistent(false);
        boss.setRemoveWhenFarAway(false);
        setAttribute(boss, Attribute.SCALE, finale.scale());
        double health = scaledHealth(finale.health(), fighters.size(), is.getFinaleFails());
        setAttribute(boss, Attribute.MAX_HEALTH, health);
        AttributeInstance max = boss.getAttribute(Attribute.MAX_HEALTH);
        if (max != null) {
            // The server may cap max health, so read it back
            boss.setHealth(max.getValue());
        }
        if (finale.damage() > 0) {
            setAttribute(boss, Attribute.ATTACK_DAMAGE, finale.damage());
        }
        if (finale.speed() > 0) {
            setAttribute(boss, Attribute.MOVEMENT_SPEED, finale.speed());
        }
        if (boss instanceof Giant giant) {
            try {
                GiantAttackGoal.apply(addon.getPlugin(), giant);
            } catch (LinkageError | RuntimeException e) {
                addon.logWarning("Giant bosses need Paper's mob goal API to fight back: " + e.getMessage());
            }
        }
        prepare(boss);
        provoke(boss, summoner);
        if (addon.getSettings().isClearBlocks()) {
            new MakeSpace(addon).makeSpace(boss, home);
        }

        // Minions
        finale.minions().forEach((type, count) -> spawnMinions(f, type, count, home));

        // Register
        active.put(island.getUniqueId(), f);
        byBoss.put(boss.getUniqueId(), f);
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(addon.getPlugin(), this::tickAll, TICK_PERIOD, TICK_PERIOD);
        }

        // Announce
        world.strikeLightningEffect(home);
        world.playSound(home, Sound.EVENT_RAID_HORN, 64F, 1F);
        fighters.forEach(p -> {
            show(f, p);
            User.getInstance(p).sendMessage("aoneblock.finale.start", NAME, f.plainName);
        });
        return true;
    }

    private void spawnMinions(ActiveFinale f, EntityType type, int count, Location home) {
        for (int i = 0; i < count; i++) {
            Entity e = home.getWorld().spawnEntity(home, type);
            if (!e.isValid()) {
                continue;
            }
            e.setPersistent(false);
            prepare(e);
            f.minions.add(e.getUniqueId());
            byMinion.put(e.getUniqueId(), f);
        }
    }

    /**
     * Boss health for the number of players fighting and the number of
     * consecutive losses.
     *
     * @param base - base health from the phase file
     * @param players - number of players on the island
     * @param fails - consecutive losses
     * @return health
     */
    static double scaledHealth(double base, int players, int fails) {
        int extra = Math.clamp(players, 1, MAX_SCALED_PLAYERS) - 1;
        double mercy = Math.max(MERCY_FLOOR, 1D - MERCY_PER_FAIL * Math.max(0, fails));
        return base * (1D + HEALTH_PER_PLAYER * extra) * mercy;
    }

    /**
     * Stops nether mobs from zombifying in the overworld part-way through a fight.
     */
    private static void prepare(Entity entity) {
        if (entity instanceof PiglinAbstract piglin) {
            piglin.setImmuneToZombification(true);
        } else if (entity instanceof Hoglin hoglin) {
            hoglin.setImmuneToZombification(true);
        }
    }

    /**
     * Point the boss at a player.
     */
    private static void provoke(LivingEntity boss, Player player) {
        if (boss instanceof Warden warden) {
            // Wardens ignore targets they are not angry at, and dig away when calm
            warden.setAnger(player, 150);
        }
        if (boss instanceof Mob mob) {
            mob.setTarget(player);
        }
    }

    private static void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private static Location home(Block block) {
        return block.getLocation().add(0.5D, 1D, 0.5D);
    }

    /**
     * @return living, non-spectating players inside the island's protected area
     */
    private List<Player> fighters(Island island, World world) {
        return world.getPlayers().stream().filter(p -> !p.isDead() && p.getGameMode() != GameMode.SPECTATOR)
                .filter(p -> island.onIsland(p.getLocation())).toList();
    }

    private void show(ActiveFinale f, Player p) {
        if (f.viewers.add(p.getUniqueId())) {
            p.showBossBar(f.bar);
        }
    }

    private void hideAll(ActiveFinale f) {
        f.viewers.stream().map(Bukkit::getPlayer).filter(java.util.Objects::nonNull)
                .forEach(p -> p.hideBossBar(f.bar));
        f.viewers.clear();
    }

    void tickAll() {
        new ArrayList<>(active.values()).forEach(this::tick);
        if (active.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick(ActiveFinale f) {
        LivingEntity boss = f.boss;
        if (!boss.isValid()) {
            // Removed by something other than a kill. Never a win.
            abandon(f);
            return;
        }
        List<Player> fighters = fighters(f.island, f.block.getWorld());
        if (fighters.isEmpty()) {
            if (++f.emptyChecks >= EMPTY_CHECKS) {
                abandon(f);
            }
            return;
        }
        f.emptyChecks = 0;

        // Leash
        Location loc = boss.getLocation();
        if (!f.island.onIsland(loc) || (f.finale.voidRespawn() && loc.getY() < loc.getWorld().getMinHeight())) {
            recall(f);
        }

        // Boss bar
        AttributeInstance max = boss.getAttribute(Attribute.MAX_HEALTH);
        if (max != null && max.getValue() > 0) {
            f.bar.progress((float) Math.clamp(boss.getHealth() / max.getValue(), 0D, 1D));
        }
        Set<UUID> present = new HashSet<>();
        fighters.forEach(p -> {
            present.add(p.getUniqueId());
            show(f, p);
        });
        f.viewers.removeIf(uuid -> {
            if (present.contains(uuid)) {
                return false;
            }
            Optional.ofNullable(Bukkit.getPlayer(uuid)).ifPresent(p -> p.hideBossBar(f.bar));
            return true;
        });

        // Keep the boss interested
        if (boss instanceof Mob mob && mob.getTarget() == null) {
            Location bl = boss.getLocation();
            fighters.stream().filter(p -> p.getGameMode() != GameMode.CREATIVE)
                    .min((a, b) -> Double.compare(a.getLocation().distanceSquared(bl),
                            b.getLocation().distanceSquared(bl)))
                    .ifPresent(p -> provoke(mob, p));
        }
    }

    /**
     * Bring the boss back to the magic block.
     */
    private void recall(ActiveFinale f) {
        Location home = home(f.block);
        f.boss.setFallDistance(0F);
        f.boss.teleport(home);
        home.getWorld().playSound(home, Sound.ENTITY_ENDERMAN_TELEPORT, 2F, 0.5F);
        home.getWorld().spawnParticle(Particle.PORTAL, home, 60, 0.5D, 1D, 0.5D);
    }

    /**
     * The boss leaves without being beaten.
     */
    private void abandon(ActiveFinale f) {
        if (f.playerDied) {
            f.is.setFinaleFails(f.is.getFinaleFails() + 1);
        }
        remove(f);
        fighters(f.island, f.block.getWorld())
                .forEach(p -> User.getInstance(p).sendMessage("aoneblock.finale.retreat", NAME, f.plainName));
        Bukkit.getPluginManager()
                .callEvent(new PhaseFinaleEndEvent(f.island, null, f.block, f.phaseName, false, false));
    }

    private void win(ActiveFinale f, @Nullable Player killer) {
        boolean firstTime = f.is.getDefeatedFinales().add(f.phaseName);
        f.is.setFinalePending("");
        f.is.setFinaleFails(0);
        remove(f);

        World world = f.block.getWorld();
        Location home = home(f.block);
        f.finale.drops().forEach((material, amount) -> {
            int stack = Math.max(1, material.getMaxStackSize());
            int left = amount;
            while (left > 0) {
                int size = Math.min(left, stack);
                world.dropItem(home, new ItemStack(material, size));
                left -= size;
            }
        });
        world.playSound(home, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2F, 1F);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, home, 80, 0.5D, 1D, 0.5D, 0.4D);

        // Commands run as the killer, or the owner if there is no killer
        User user = killer != null ? User.getInstance(killer) : addon.getPlayers().getUser(f.island.getOwner());
        if (user != null) {
            CheckPhase check = addon.getBlockListener().getCheckPhase();
            String number = addon.getOneBlockManager().getPhase(f.phaseName).map(OneBlockPhase::getBlockNumber)
                    .orElse("");
            Util.runCommands(user, check.replacePlaceholders(killer, f.phaseName, number, f.island, f.finale.commands()),
                    "Commands run for defeating the boss of " + f.phaseName);
            if (firstTime) {
                Util.runCommands(user,
                        check.replacePlaceholders(killer, f.phaseName, number, f.island, f.finale.firstTimeCommands()),
                        "Commands run for first time defeating the boss of " + f.phaseName);
            }
        }
        fighters(f.island, world)
                .forEach(p -> User.getInstance(p).sendMessage("aoneblock.finale.defeated", NAME, f.plainName));
        Bukkit.getPluginManager().callEvent(new PhaseFinaleEndEvent(f.island,
                killer == null ? null : killer.getUniqueId(), f.block, f.phaseName, true, firstTime));
        addon.getBlockListener().saveIsland(f.island);
    }

    /**
     * Take the finale's entities and boss bar out of the world and forget it.
     */
    private void remove(ActiveFinale f) {
        active.remove(f.island.getUniqueId());
        byBoss.remove(f.boss.getUniqueId());
        if (f.boss.isValid()) {
            f.boss.remove();
        }
        f.minions.forEach(uuid -> {
            byMinion.remove(uuid);
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) {
                e.getWorld().spawnParticle(Particle.POOF, e.getLocation(), 10, 0.3D, 0.5D, 0.3D, 0.02D);
                e.remove();
            }
        });
        f.minions.clear();
        hideAll(f);
    }

    /**
     * Remove every running finale. Called when the addon is disabled.
     */
    public void onDisable() {
        new ArrayList<>(active.values()).forEach(this::remove);
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @param island - island
     * @return true if a boss is currently alive on this island
     */
    public boolean isActive(@NonNull Island island) {
        return active.containsKey(island.getUniqueId());
    }

    // ---------------------------------------------------------------------
    // Section: Listeners
    // ---------------------------------------------------------------------

    /**
     * Only players can hurt a boss. This shuts out suffocation boxes, lava, drop
     * traps and grinders.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBossDamage(EntityDamageEvent e) {
        ActiveFinale f = byBoss.get(e.getEntity().getUniqueId());
        if (f == null) {
            return;
        }
        if (e.getCause() == DamageCause.VOID) {
            if (f.finale.voidRespawn()) {
                e.setCancelled(true);
                recall(f);
            }
            return;
        }
        if (e.getCause() == DamageCause.KILL
                || (e instanceof EntityDamageByEntityEvent ee && isPlayerDamage(ee.getDamager()))) {
            return;
        }
        e.setCancelled(true);
    }

    private static boolean isPlayerDamage(Entity damager) {
        return damager instanceof Player || (damager instanceof Projectile p && p.getShooter() instanceof Player)
                || (damager instanceof Tameable t && t.getOwner() instanceof Player);
    }

    /**
     * Boss death is the win.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        ActiveFinale f = byBoss.get(uuid);
        if (f != null) {
            win(f, e.getEntity().getKiller());
            return;
        }
        f = byMinion.remove(uuid);
        if (f != null) {
            f.minions.remove(uuid);
        }
    }

    /**
     * Track deaths so that a lost fight makes the next attempt a little easier.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (active.isEmpty()) {
            return;
        }
        Location loc = e.getEntity().getLocation();
        active.values().stream().filter(f -> f.island.onIsland(loc)).forEach(f -> f.playerDied = true);
    }

    /**
     * Finale mobs never change blocks.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if (isFinaleEntity(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    /**
     * Finale mobs' explosions hurt players, not islands.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (isFinaleEntity(e.getEntity())) {
            e.blockList().clear();
        }
    }

    /**
     * Finale mobs' fireballs do not set the island alight.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getIgnitingEntity() != null && isFinaleEntity(e.getIgnitingEntity())) {
            e.setCancelled(true);
        }
    }

    /**
     * Finale mobs do not burn in daylight.
     * @param e - event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent e) {
        if (!(e instanceof EntityCombustByEntityEvent) && !(e instanceof EntityCombustByBlockEvent)
                && isFinaleEntity(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    private boolean isFinaleEntity(Entity entity) {
        if (active.isEmpty()) {
            return false;
        }
        UUID uuid = entity.getUniqueId();
        if (byBoss.containsKey(uuid) || byMinion.containsKey(uuid)) {
            return true;
        }
        return entity instanceof Projectile p && p.getShooter() instanceof Entity shooter
                && (byBoss.containsKey(shooter.getUniqueId()) || byMinion.containsKey(shooter.getUniqueId()));
    }

    /**
     * @param e - event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(IslandDeleteEvent e) {
        Optional.ofNullable(active.get(e.getIsland().getUniqueId())).ifPresent(this::remove);
    }

    /**
     * @param e - event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandReset(IslandResettedEvent e) {
        Optional.ofNullable(active.get(e.getOldIsland().getUniqueId())).ifPresent(this::remove);
        Optional.ofNullable(active.get(e.getIsland().getUniqueId())).ifPresent(this::remove);
    }
}
