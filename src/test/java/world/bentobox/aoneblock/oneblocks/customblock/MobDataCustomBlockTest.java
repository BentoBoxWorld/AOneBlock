package world.bentobox.aoneblock.oneblocks.customblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.BentoBox;

/**
 * Unit tests for {@link MobDataCustomBlock#fromMap(Map)}.
 * <p>
 * Covers parsing of the {@code mob-data} YAML entry type introduced for
 * BentoBoxWorld/AOneBlock#488. Execute-path behavior (dispatchCommand,
 * scheduler, MakeSpace) is exercised via manual integration testing on a
 * live server; these tests focus on deterministic input parsing.
 */
class MobDataCustomBlockTest {

    @Test
    void fromMapParsesDataAndUnderlyingBlock() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mob-data");
        map.put("data", "minecraft:breeze{Glowing:1b}");
        map.put("underlying-block", "STONE");

        Optional<MobDataCustomBlock> result = MobDataCustomBlock.fromMap(map);

        assertTrue(result.isPresent());
        assertEquals("minecraft:breeze{Glowing:1b}", result.get().getData());
        assertEquals(Material.STONE, result.get().getUnderlyingBlock());
    }

    @Test
    void fromMapReturnsEmptyWhenDataMissing() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mob-data");
        // no "data" key

        Optional<MobDataCustomBlock> result = MobDataCustomBlock.fromMap(map);

        assertTrue(result.isEmpty());
    }

    @Test
    void fromMapAllowsMissingUnderlyingBlock() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("data", "minecraft:zombie");

        Optional<MobDataCustomBlock> result = MobDataCustomBlock.fromMap(map);

        assertTrue(result.isPresent());
        assertNull(result.get().getUnderlyingBlock(),
                "Unspecified underlying block should parse as null (execute falls back to STONE)");
    }

    @Test
    void fromMapFallsBackToNullWhenUnderlyingBlockInvalid() {
        // BentoBox.getInstance() is called to log a warning; stub it statically so the
        // test doesn't require a full CommonTestSetup.
        BentoBox mockBentoBox = Mockito.mock(BentoBox.class);
        try (MockedStatic<BentoBox> mocked = Mockito.mockStatic(BentoBox.class)) {
            mocked.when(BentoBox::getInstance).thenReturn(mockBentoBox);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("data", "minecraft:zombie");
            map.put("underlying-block", "NOT_A_REAL_MATERIAL");

            Optional<MobDataCustomBlock> result = MobDataCustomBlock.fromMap(map);

            assertTrue(result.isPresent());
            assertNull(result.get().getUnderlyingBlock());
            // Ensure a warning was emitted
            Mockito.verify(mockBentoBox).logWarning(Mockito.contains("NOT_A_REAL_MATERIAL"));
        }
    }

    @Test
    void buildSummonCommandPlacesNbtAfterCoordinates() {
        // Regression: the vanilla /summon grammar is `summon <entity> <x> <y> <z> [nbt]`.
        // Previously we glued NBT to the entity id, producing an "Unhandled exception"
        // in VanillaCommandWrapper. This test locks in the corrected ordering.
        String data = "breeze{CustomName:[{text:Breezy}],Glowing:1b,attributes:[{id:scale,base:2f}]}";
        String command = MobDataCustomBlock.buildSummonCommand(data, "minecraft:oneblock_world",
                "1600.5", "81.0", "1600.5");

        assertEquals(
                "execute in minecraft:oneblock_world run summon breeze 1600.5 81.0 1600.5 "
                        + "{CustomName:[{text:Breezy}],Glowing:1b,attributes:[{id:scale,base:2f}]}",
                command);
    }

    @Test
    void buildSummonCommandWithNoNbt() {
        // A bare entity id (no NBT or components) should still produce a valid command.
        String command = MobDataCustomBlock.buildSummonCommand("minecraft:zombie",
                "minecraft:world", "0.5", "65.0", "0.5");

        assertEquals("execute in minecraft:world run summon minecraft:zombie 0.5 65.0 0.5", command);
    }

    @Test
    void buildSummonCommandWithComponentBrackets() {
        // Modern component syntax uses `[` instead of `{` (e.g. `pig[minecraft:rotation=...]`).
        // The split must handle whichever comes first.
        String command = MobDataCustomBlock.buildSummonCommand("pig[minecraft:rotation={yaw:90f}]",
                "minecraft:world", "1.5", "64.0", "1.5");

        assertEquals(
                "execute in minecraft:world run summon pig 1.5 64.0 1.5 [minecraft:rotation={yaw:90f}]",
                command);
    }

    @Test
    void registeredUnderMobDataType() {
        // Sanity check: ensure the OneBlockCustomBlockCreator routes "mob-data"
        // to MobDataCustomBlock::fromMap.
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "mob-data");
        map.put("data", "minecraft:pig");

        var result = world.bentobox.aoneblock.oneblocks.OneBlockCustomBlockCreator.create(map);
        assertTrue(result.isPresent());
        assertNotNull(result.get());
        assertTrue(result.get() instanceof MobDataCustomBlock);
    }
}
