package world.bentobox.aoneblock.oneblocks;

import org.bukkit.Color;
import org.bukkit.Sound;

/**
 * Defines a mob
 * @author tastybento
 *
 */
public class MobAspects {

    private final Sound sound;
    private final Color color;
    /**
     * @param sound - sound to play
     * @param color - color of particles to show
     */
    public MobAspects(Sound sound, Color color) {
        this.sound = sound;
        this.color = color;
    }
    /**
     * @return the sound
     */
    public Sound getSound() {
        return sound;
    }
    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }
}
