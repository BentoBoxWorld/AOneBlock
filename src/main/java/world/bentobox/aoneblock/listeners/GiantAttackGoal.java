package world.bentobox.aoneblock.listeners;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.NonNull;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

/**
 * Giants have no AI in vanilla. This Paper mob goal makes one chase and hit
 * the nearest survival player. Kept in its own class so servers without the
 * Paper goal API only fail to load this, not the finale listener.
 * @author tastybento
 */
public class GiantAttackGoal implements Goal<Giant> {

    private static final double RANGE = 32;
    private static final int ATTACK_COOLDOWN = 20;

    private final GoalKey<Giant> key;
    private final Giant giant;
    private int cooldown;

    GiantAttackGoal(Plugin plugin, Giant giant) {
        this.key = GoalKey.of(Giant.class, new NamespacedKey(plugin, "giant_attack"));
        this.giant = giant;
    }

    /**
     * Give this giant the attack goal.
     * @param plugin - owning plugin
     * @param giant - giant
     */
    static void apply(Plugin plugin, Giant giant) {
        Bukkit.getMobGoals().addGoal(giant, 1, new GiantAttackGoal(plugin, giant));
    }

    @Override
    public boolean shouldActivate() {
        LivingEntity target = giant.getTarget();
        if (target == null || !target.isValid() || !isFair(target)) {
            giant.setTarget(giant.getNearbyEntities(RANGE, RANGE, RANGE).stream()
                    .filter(GiantAttackGoal::isFair).map(LivingEntity.class::cast).findFirst().orElse(null));
        }
        return giant.getTarget() != null;
    }

    private static boolean isFair(Object e) {
        return e instanceof Player p && !p.isDead()
                && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE);
    }

    @Override
    public void tick() {
        LivingEntity target = giant.getTarget();
        if (target == null) {
            return;
        }
        giant.lookAt(target);
        giant.getPathfinder().moveTo(target, 1.0);
        // Vanilla reach scales awkwardly with a giant's hitbox, so use a manual check
        double scale = Math.max(1D, giant.getWidth() / 3.6D);
        double reach = 6D * scale;
        if (--cooldown <= 0 && giant.getLocation().distanceSquared(target.getLocation()) < reach * reach) {
            giant.swingMainHand();
            giant.attack(target);
            cooldown = ATTACK_COOLDOWN;
        }
    }

    @Override
    public @NonNull GoalKey<Giant> getKey() {
        return key;
    }

    @Override
    public @NonNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK, GoalType.TARGET);
    }
}
