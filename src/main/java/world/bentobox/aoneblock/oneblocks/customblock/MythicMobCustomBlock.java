package world.bentobox.aoneblock.oneblocks.customblock;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.listeners.MakeSpace;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintEntity.MythicMobRecord;
import world.bentobox.bentobox.hooks.MythicMobsHook;

/**
 * A custom block that spawns a MythicMob via the BentoBox {@link MythicMobsHook}.
 * <p>
 * Example YAML entry:
 *
 * <pre>
 * - type: mythic-mob
 *   mob: SkeletalKnight      # MythicMob type id (required)
 *   level: 3                 # optional, default 1
 *   power: 1.0               # optional, default 0
 *   display-name: "Boss"     # optional
 *   stance: ""               # optional
 *   underlying-block: STONE  # optional
 *   probability: 5
 * </pre>
 *
 * <p>
 * This class has no compile-time dependency on the MythicMobs plugin — it
 * talks only to BentoBox's {@link MythicMobsHook}. When the hook is not
 * present (e.g. MythicMobs is not installed) the spawn is logged and
 * skipped. See BentoBoxWorld/AOneBlock#303.
 */
public class MythicMobCustomBlock implements OneBlockCustomBlock {

    private final String mob;
    private final double level;
    private final float power;
    private final String displayName;
    private final String stance;
    private final Material underlyingBlock;

    public MythicMobCustomBlock(@NonNull String mob, double level, float power,
            @Nullable String displayName, @Nullable String stance, @Nullable Material underlyingBlock) {
        this.mob = mob;
        this.level = level;
        this.power = power;
        this.displayName = displayName;
        this.stance = stance;
        this.underlyingBlock = underlyingBlock;
    }

    public static Optional<MythicMobCustomBlock> fromMap(Map<?, ?> map) {
        String mob = Objects.toString(map.get("mob"), null);
        if (mob == null) {
            return Optional.empty();
        }

        double level = parseDouble(map.get("level"), 1D);
        float power = (float) parseDouble(map.get("power"), 0D);
        String displayName = Objects.toString(map.get("display-name"), null);
        String stance = Objects.toString(map.get("stance"), null);

        String underlyingBlockValue = Objects.toString(map.get("underlying-block"), null);
        Material underlyingBlock = underlyingBlockValue == null ? null : Material.getMaterial(underlyingBlockValue);
        if (underlyingBlockValue != null && underlyingBlock == null) {
            BentoBox.getInstance().logWarning("Underlying block " + underlyingBlockValue
                    + " does not exist and will be replaced with STONE.");
        }

        return Optional.of(new MythicMobCustomBlock(mob, level, power, displayName, stance, underlyingBlock));
    }

    private static double parseDouble(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void execute(AOneBlock addon, Block block) {
        try {
            block.setType(Objects.requireNonNullElse(underlyingBlock, Material.STONE));

            Location spawnLoc = block.getLocation().add(new Vector(0.5D, 1D, 0.5D));

            Optional<MythicMobsHook> hookOpt = BentoBox.getInstance().getHooks().getHook("MythicMobs")
                    .filter(MythicMobsHook.class::isInstance)
                    .map(MythicMobsHook.class::cast);

            if (hookOpt.isEmpty()) {
                BentoBox.getInstance().logWarning(
                        "mythic-mob '" + mob + "' requested but MythicMobs hook is not available.");
                return;
            }

            MythicMobsHook hook = hookOpt.get();
            MythicMobRecord record = new MythicMobRecord(
                    mob,
                    displayName != null ? displayName : mob,
                    level,
                    power,
                    stance != null ? stance : "");

            Consumer<Entity> onSpawn = entity -> {
                if (addon.getSettings().isClearBlocks()) {
                    new MakeSpace(addon).makeSpace(entity, spawnLoc);
                }
            };

            // Prefer the 3-arg overload (BentoBox >= 3.14.0) so MakeSpace can run after
            // the 40-tick spawn delay. Fall back to the 2-arg method on older BentoBox.
            if (!invokeWithCallback(hook, record, spawnLoc, onSpawn)) {
                hook.spawnMythicMob(record, spawnLoc);
            }

            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
        } catch (Exception e) {
            BentoBox.getInstance().logError("Could not spawn mythic-mob '" + mob + "': " + e.getMessage());
        }
    }

    /**
     * Attempts to call the 3-arg {@code spawnMythicMob(record, location, Consumer)}
     * via reflection so this class still compiles and runs against BentoBox versions
     * that don't yet ship the callback overload.
     *
     * @return true if the callback overload was invoked successfully
     */
    private boolean invokeWithCallback(MythicMobsHook hook, MythicMobRecord record, Location spawnLoc,
            Consumer<Entity> onSpawn) {
        try {
            Method m = MythicMobsHook.class.getMethod("spawnMythicMob",
                    MythicMobRecord.class, Location.class, Consumer.class);
            m.invoke(hook, record, spawnLoc, onSpawn);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            BentoBox.getInstance().logError("Failed to invoke MythicMobsHook callback overload: " + e.getMessage());
            return false;
        }
    }

    public String getMob() {
        return mob;
    }

    public double getLevel() {
        return level;
    }

    public float getPower() {
        return power;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStance() {
        return stance;
    }

    public Material getUnderlyingBlock() {
        return underlyingBlock;
    }
}
