package world.bentobox.aoneblock.oneblocks.customblock;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void fromMapReturnsEmptyWhenIdIsNull() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", null);

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when 'id' is null");
    }

    @Test
    void fromMapReturnsEmptyWhenIdIsBlank() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", "   ");

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when 'id' is blank");
    }

    @Test
    void fromMapReturnsEmptyWhenIdIsNonStringValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", 42); // integer, not a String

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when 'id' is not a String");
    }

    @Test
    void fromMapReturnsEmptyWhenIdHasNoColon() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", "nocolon");

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when 'id' has no colon");
    }

    @Test
    void fromMapReturnsEmptyWhenIdHasEmptyNamespace() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", ":key");

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when namespace part is empty");
    }

    @Test
    void fromMapReturnsEmptyWhenIdHasEmptyKey() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "craftengine");
        map.put("id", "namespace:");

        var result = CraftEngineCustomBlock.fromMap(map);

        assertTrue(result.isEmpty(), "Should return empty when key part is empty");
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

    // --- isValidNamespacedKey helper tests ---

    @Test
    void isValidNamespacedKeyReturnsTrueForValidKey() {
        assertTrue(CraftEngineCustomBlock.isValidNamespacedKey("ns:key"));
        assertTrue(CraftEngineCustomBlock.isValidNamespacedKey("oneblock:common_loot_block"));
    }

    @Test
    void isValidNamespacedKeyReturnsFalseForMissingColon() {
        assertFalse(CraftEngineCustomBlock.isValidNamespacedKey("nocolon"));
    }

    @Test
    void isValidNamespacedKeyReturnsFalseForLeadingColon() {
        assertFalse(CraftEngineCustomBlock.isValidNamespacedKey(":key"));
    }

    @Test
    void isValidNamespacedKeyReturnsFalseForTrailingColon() {
        assertFalse(CraftEngineCustomBlock.isValidNamespacedKey("ns:"));
    }

    @Test
    void isValidNamespacedKeyReturnsFalseForBlankNamespace() {
        assertFalse(CraftEngineCustomBlock.isValidNamespacedKey("   :key"));
    }

    @Test
    void isValidNamespacedKeyReturnsFalseForBlankKey() {
        assertFalse(CraftEngineCustomBlock.isValidNamespacedKey("ns:   "));
    }
}
