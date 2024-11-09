package world.bentobox.aoneblock.listeners;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.MobAspects;

/**
 * Plays a warning sound
 * @author tastybento
 *
 */
public class WarningSounder {

    private final AOneBlock addon;

    /**
     * @param addon
     */
    public WarningSounder(AOneBlock addon) {
        this.addon = addon;
    }
    /**
     * Mob aspects.
     */
    private Map<EntityType, MobAspects> MOB_ASPECTS;

    void play(@NonNull OneBlockIslands is, @NonNull Block block) {
        if (MOB_ASPECTS == null) {
            initialize(); // Done to avoid static definition with Sound due to test issues
        }
        List<EntityType> opMob = is.getNearestMob(addon.getSettings().getMobWarning());
        opMob.stream().filter(MOB_ASPECTS::containsKey).map(MOB_ASPECTS::get).forEach(s -> {
            block.getWorld().playSound(block.getLocation(), s.sound(), 1F, 1F);
            block.getWorld().spawnParticle(Particle.DUST, block.getLocation().add(new Vector(0.5, 1.0, 0.5)), 10, 0.5,
                    0, 0.5, 1, new Particle.DustOptions(s.color(), 1));
        });

    }

    private void initialize() {
        Map<EntityType, MobAspects> m = new EnumMap<>(EntityType.class);
        m.put(EntityType.BLAZE, new MobAspects(Sound.ENTITY_BLAZE_AMBIENT, Color.fromRGB(238, 211, 91)));
        m.put(EntityType.CAVE_SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(63, 37, 31)));
        m.put(EntityType.CREEPER, new MobAspects(Sound.ENTITY_CREEPER_PRIMED, Color.fromRGB(125, 255, 106)));
        m.put(EntityType.DROWNED, new MobAspects(Sound.ENTITY_DROWNED_AMBIENT, Color.fromRGB(109, 152, 144)));
        m.put(EntityType.ELDER_GUARDIAN, new MobAspects(Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        m.put(EntityType.ENDERMAN, new MobAspects(Sound.ENTITY_ENDERMAN_AMBIENT, Color.fromRGB(0, 0, 0)));
        m.put(EntityType.ENDERMITE, new MobAspects(Sound.ENTITY_ENDERMITE_AMBIENT, Color.fromRGB(30, 30, 30)));
        m.put(EntityType.EVOKER, new MobAspects(Sound.ENTITY_EVOKER_AMBIENT, Color.fromRGB(144, 148, 148)));
        m.put(EntityType.GHAST, new MobAspects(Sound.ENTITY_GHAST_AMBIENT, Color.fromRGB(242, 242, 242)));
        m.put(EntityType.GUARDIAN, new MobAspects(Sound.ENTITY_GUARDIAN_AMBIENT, Color.fromRGB(201, 143, 113)));
        m.put(EntityType.HUSK, new MobAspects(Sound.ENTITY_HUSK_AMBIENT, Color.fromRGB(111, 104, 90)));
        m.put(EntityType.ILLUSIONER, new MobAspects(Sound.ENTITY_ILLUSIONER_AMBIENT, Color.fromRGB(144, 149, 149)));
        m.put(EntityType.PIGLIN, new MobAspects(Sound.ENTITY_PIGLIN_AMBIENT, Color.fromRGB(255, 215, 0)));
        m.put(EntityType.PIGLIN_BRUTE, new MobAspects(Sound.ENTITY_PIGLIN_BRUTE_AMBIENT, Color.fromRGB(255, 215, 0)));
        m.put(EntityType.ZOMBIFIED_PIGLIN, new MobAspects(Sound.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT, Color.fromRGB(125, 100, 0)));
        m.put(EntityType.PILLAGER, new MobAspects(Sound.ENTITY_PILLAGER_AMBIENT, Color.fromRGB(74, 74, 53)));
        m.put(EntityType.RAVAGER, new MobAspects(Sound.ENTITY_RAVAGER_AMBIENT, Color.fromRGB(85, 78, 73)));
        m.put(EntityType.SHULKER, new MobAspects(Sound.ENTITY_SHULKER_AMBIENT, Color.fromRGB(142, 106, 146)));
        m.put(EntityType.SILVERFISH, new MobAspects(Sound.ENTITY_SILVERFISH_AMBIENT, Color.fromRGB(211, 211, 211)));
        m.put(EntityType.SKELETON, new MobAspects(Sound.ENTITY_SKELETON_AMBIENT, Color.fromRGB(211, 211, 211)));
        m.put(EntityType.SPIDER, new MobAspects(Sound.ENTITY_SPIDER_AMBIENT, Color.fromRGB(94, 84, 73)));
        m.put(EntityType.STRAY, new MobAspects(Sound.ENTITY_STRAY_AMBIENT, Color.fromRGB(118, 132, 135)));
        m.put(EntityType.VEX, new MobAspects(Sound.ENTITY_VEX_AMBIENT, Color.fromRGB(137, 156, 176)));
        m.put(EntityType.VINDICATOR, new MobAspects(Sound.ENTITY_VINDICATOR_AMBIENT, Color.fromRGB(137, 156, 166)));
        m.put(EntityType.WITCH, new MobAspects(Sound.ENTITY_WITCH_AMBIENT, Color.fromRGB(56, 39, 67)));
        m.put(EntityType.WITHER, new MobAspects(Sound.ENTITY_WITHER_AMBIENT, Color.fromRGB(80, 80, 80)));
        m.put(EntityType.WARDEN, new MobAspects(Sound.ENTITY_WARDEN_AMBIENT, Color.fromRGB(6, 72, 86))); //ADDED WARDEN SOUND
        m.put(EntityType.WITHER_SKELETON, new MobAspects(Sound.ENTITY_WITHER_SKELETON_AMBIENT, Color.fromRGB(100, 100, 100)));
        m.put(EntityType.ZOGLIN, new MobAspects(Sound.ENTITY_ZOGLIN_AMBIENT, Color.fromRGB(255, 192, 203)));
        m.put(EntityType.ZOMBIE, new MobAspects(Sound.ENTITY_ZOMBIE_AMBIENT, Color.fromRGB(74, 99, 53)));
        m.put(EntityType.ZOMBIE_VILLAGER, new MobAspects(Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT, Color.fromRGB(111, 104, 90)));

        MOB_ASPECTS = Collections.unmodifiableMap(m);

    }

}
