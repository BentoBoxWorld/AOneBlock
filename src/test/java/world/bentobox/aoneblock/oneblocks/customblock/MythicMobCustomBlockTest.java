package world.bentobox.aoneblock.oneblocks.customblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MythicMobCustomBlock#fromMap(Map)}.
 * <p>
 * Covers parsing of the {@code mythic-mob} YAML entry type introduced for
 * BentoBoxWorld/AOneBlock#303. Execute-path behavior (hook lookup, callback
 * dispatch) is exercised via manual integration testing against a live
 * BentoBox + MythicMobs install.
 */
class MythicMobCustomBlockTest {

    @Test
    void fromMapParsesAllFields() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mythic-mob");
        map.put("mob", "SkeletalKnight");
        map.put("level", 3);
        map.put("power", 1.5);
        map.put("display-name", "Boss");
        map.put("stance", "angry");
        map.put("underlying-block", "STONE");

        Optional<MythicMobCustomBlock> result = MythicMobCustomBlock.fromMap(map);

        assertTrue(result.isPresent());
        MythicMobCustomBlock block = result.get();
        assertEquals("SkeletalKnight", block.getMob());
        assertEquals(3D, block.getLevel());
        assertEquals(1.5F, block.getPower());
        assertEquals("Boss", block.getDisplayName());
        assertEquals("angry", block.getStance());
        assertEquals(Material.STONE, block.getUnderlyingBlock());
    }

    @Test
    void fromMapReturnsEmptyWhenMobMissing() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mythic-mob");
        // no "mob" key

        Optional<MythicMobCustomBlock> result = MythicMobCustomBlock.fromMap(map);

        assertTrue(result.isEmpty());
    }

    @Test
    void fromMapAppliesDefaultsForOptionalFields() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mob", "Goblin");

        Optional<MythicMobCustomBlock> result = MythicMobCustomBlock.fromMap(map);

        assertTrue(result.isPresent());
        MythicMobCustomBlock block = result.get();
        assertEquals("Goblin", block.getMob());
        assertEquals(1D, block.getLevel(), "level defaults to 1");
        assertEquals(0F, block.getPower(), "power defaults to 0");
    }

    @Test
    void fromMapAcceptsStringNumericFields() {
        // YAML can deliver numbers as strings depending on quoting; ensure we coerce.
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mob", "Goblin");
        map.put("level", "2");
        map.put("power", "0.75");

        Optional<MythicMobCustomBlock> result = MythicMobCustomBlock.fromMap(map);

        assertTrue(result.isPresent());
        assertEquals(2D, result.get().getLevel());
        assertEquals(0.75F, result.get().getPower());
    }

    @Test
    void registeredUnderMythicMobType() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mythic-mob");
        map.put("mob", "Goblin");

        var result = world.bentobox.aoneblock.oneblocks.OneBlockCustomBlockCreator.create(map);
        assertTrue(result.isPresent());
        assertNotNull(result.get());
        assertTrue(result.get() instanceof MythicMobCustomBlock);
    }
}
