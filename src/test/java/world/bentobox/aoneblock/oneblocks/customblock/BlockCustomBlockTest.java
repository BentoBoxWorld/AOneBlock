package world.bentobox.aoneblock.oneblocks.customblock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlock;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlockCreator;

/**
 * Routing tests for the {@code block} custom-blocks alias.
 * <p>
 * {@code type: block} is an alias for {@code type: block-data} — both feed
 * {@link BlockDataCustomBlock#fromMap(Map)}, whose {@code execute()} method
 * supports block ids, block states, NBT, and trailing setblock mode flags via
 * the fallback {@code /setblock} command dispatch. The alias exists so that
 * phase-file authors who are placing NBT-heavy blocks (e.g. preconfigured
 * spawners) aren't forced to use the misleading {@code block-data} key.
 */
class BlockCustomBlockTest {

    @Test
    void typeBlockRoutesToBlockDataCustomBlock() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "block");
        map.put("data", "spawner{SpawnData:{entity:{id:breeze}}} replace");

        Optional<OneBlockCustomBlock> result = OneBlockCustomBlockCreator.create(map);

        assertTrue(result.isPresent(), "`type: block` should resolve to a custom block");
        assertInstanceOf(BlockDataCustomBlock.class, result.get(),
                "`type: block` should produce a BlockDataCustomBlock instance");
    }

    @Test
    void typeBlockDataStillWorks() {
        // Regression: adding the `block` alias must not break the original
        // `block-data` registration.
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "block-data");
        map.put("data", "redstone_wire[power=15]");

        Optional<OneBlockCustomBlock> result = OneBlockCustomBlockCreator.create(map);

        assertTrue(result.isPresent());
        assertInstanceOf(BlockDataCustomBlock.class, result.get());
    }
}
