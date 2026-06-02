package world.bentobox.aoneblock.oneblocks;

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a sound that is played to a player when a phase starts or ends.
 * <p>
 * The sound key is a free-form string so that resource-pack (custom) sounds
 * declared in a pack's {@code sounds.json} can be used in addition to the
 * vanilla Minecraft sound keys, e.g. {@code minecraft:ui.toast.challenge_complete}
 * or {@code myresourcepack:phase.complete}.
 *
 * Values are passed straight through to Bukkit; this record does not clamp
 * them. Minecraft itself treats a volume above 1.0 as an increased hearing
 * radius (not louder) and clamps pitch client-side to the 0.5 - 2.0 range.
 *
 * @param sound  the (namespaced) sound key to play
 * @param volume the volume, where 1.0 is normal; values above 1.0 increase the
 *               distance at which the sound can be heard
 * @param pitch  the pitch, where 1.0 is normal (Minecraft clamps to 0.5 - 2.0)
 *
 * @author tastybento
 */
public record PhaseSound(String sound, float volume, float pitch) {

    /**
     * Plays this sound to the given player at their current location. Because
     * the sound key is passed through as a raw string, custom resource-pack
     * sounds are supported.
     *
     * @param player the player to play the sound to. If {@code null} nothing happens.
     */
    public void play(@Nullable Player player) {
        if (player == null || sound == null || sound.isEmpty()) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
