package world.bentobox.aoneblock.oneblocks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PhaseSound}.
 *
 * @author tastybento
 */
class PhaseSoundTest {

    @Test
    void testPlayInvokesPlayerPlaySoundWithKeyVolumeAndPitch() {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        when(player.getLocation()).thenReturn(location);

        new PhaseSound("custompack:fanfare", 0.5F, 1.5F).play(player);

        verify(player).playSound(location, "custompack:fanfare", 0.5F, 1.5F);
    }

    @Test
    void testPlayWithNullPlayerIsNoOp() {
        assertDoesNotThrow(() -> new PhaseSound("custompack:fanfare", 1F, 1F).play(null));
    }

    @Test
    void testPlayWithEmptySoundIsNoOp() {
        Player player = mock(Player.class);

        new PhaseSound("", 1F, 1F).play(player);

        verify(player, never()).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }
}
