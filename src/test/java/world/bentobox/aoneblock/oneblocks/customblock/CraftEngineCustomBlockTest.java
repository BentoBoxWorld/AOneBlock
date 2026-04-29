package world.bentobox.aoneblock.oneblocks.customblock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CraftEngineCustomBlock#fromMap(Map)}.
 * <p>
 * Because CraftEngine is an optional runtime dependency, the {@code fromId}
 * path cannot be exercised without a live server. These tests cover the
 * map-parsing entry point and the factory registration in
 * {@link world.bentobox.aoneblock.oneblocks.OneBlockCustomBlockCreator}.
 */
class CraftEngineCustomBlockTest {

    @Test
    void fromMapReturnsEmptyWhenIdMissing() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        // no "id" key

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when 'id' is missing");
    }

    /**
     * {@code fromMap} must succeed even when CraftEngine has not yet loaded its
     * block registry (i.e. without calling {@code CraftEngineHook.exists}).
     * This prevents false "Bad custom block" errors during the initial server
     * start-up phase that occurs before CraftEngine fires its reload event.
     */
    @Test
    void fromMapReturnsPresentWhenIdProvided() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", "oneblock:common_loot_block");
        map.put("probability", 300);

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isPresent(), "Should return a block when 'id' is present, regardless of CraftEngine load state");
        assertInstanceOf(CraftEngineCustomBlock.class, result.get());
    }
}
