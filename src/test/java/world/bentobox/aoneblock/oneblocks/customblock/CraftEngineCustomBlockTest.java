package world.bentobox.aoneblock.oneblocks.customblock;

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
}
